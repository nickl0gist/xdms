package pl.com.xdms.controller.excel;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.dto.ManifestTpaTttDTO;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.trucktimetable.TTTEnum;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.ManifestReferenceService;
import pl.com.xdms.service.ManifestService;
import pl.com.xdms.service.excel.ExcelManifestService;
import pl.com.xdms.service.truck.TruckService;

import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 30.11.2019
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("coordinator/excel")
public class ExcelManifestController implements ExcelController<ManifestTpaTttDTO> {

    private final FileStorageService fileStorageService;
    private final ExcelManifestService excelManifestService;
    private final ManifestService manifestService;
    private final TruckService truckService;
    private final ManifestReferenceService manifestReferenceService;

    @Autowired
    public ExcelManifestController(FileStorageService fileStorageService,
                                   ExcelManifestService excelManifestService,
                                   ManifestService manifestService,
                                   TruckService truckService,
                                   ManifestReferenceService manifestReferenceService) {
        this.fileStorageService = fileStorageService;
        this.excelManifestService = excelManifestService;
        this.manifestService = manifestService;
        this.truckService = truckService;
        this.manifestReferenceService = manifestReferenceService;
    }

    @Override
    @GetMapping("/download/manifest_upload_template.xlsx")
    public ResponseEntity<InputStreamSource> downloadBase() throws IOException {
        return getInputStreamSourceResponseEntity(excelManifestService, "manifest_upload_template");
    }

    @SuppressWarnings("Duplicates")
    @Override
    @PostMapping("/manifests/uploadFile")
    public List<ManifestTpaTttDTO> uploadFile(MultipartFile file) {
        Path filePath = fileStorageService.storeFile(file);
        Map.Entry<Long, ManifestTpaTttDTO> resultList = excelManifestService.readExcel(filePath.toFile()).entrySet().iterator().next();
        ManifestTpaTttDTO manifestTpaTttDTO = entityValidation(resultList.getValue());

        return Collections.singletonList(manifestTpaTttDTO);
    }

    @Override
    @Transactional
    @PostMapping("/forecast/save")
    public ResponseEntity<List<ManifestTpaTttDTO>> saveAllEntities(List<ManifestTpaTttDTO> objList) {

        //Check again given object to avoid any cheating from user
        ManifestTpaTttDTO object = entityValidation(objList.iterator().next());

        //Create deep copy of received object to send it back in response
        Gson gson = new Gson();
        ManifestTpaTttDTO deepCopy = gson.fromJson(gson.toJson(object), ManifestTpaTttDTO.class);

        //Gets sets of objects
        Map<Long, Manifest> manifestMapDTO = object.getManifestMapDTO();
        Set<TPA> tpaSetDTO = object.getTpaSetDTO();
        Set<TruckTimeTable> tttSetDTO = object.getTttSetDTO();
        Set<ManifestReference> manifestReferenceSetDTO = object.getManifestReferenceSetDTO();

        //Print received entities in logs
        loggerPrintStrings(manifestMapDTO, tpaSetDTO, tttSetDTO, manifestReferenceSetDTO);

        //Save elements to DB
        saveElements(manifestMapDTO, tpaSetDTO, tttSetDTO, manifestReferenceSetDTO);

        List<ManifestTpaTttDTO> resultList = new ArrayList<>();
        //Check again given object to
        resultList.add(entityValidation(deepCopy));
        log.info("ManifestReferenceDTO after saving \n {}", resultList.get(0));

        return ResponseEntity.status(201).header("Message", "dont know what tot say...").body(resultList);
    }

    /**
     * @param manifestTpaTttDTO - DTO entity with sets of TPA TTT ManifestReference and Map of Manifests
     * @return manifestTpaTttDTO with validated entities.
     */
    private ManifestTpaTttDTO entityValidation(ManifestTpaTttDTO manifestTpaTttDTO) {

        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        Set<TPA> tpaSetDTO = tpaSetValidation(manifestTpaTttDTO.getTpaSetDTO(), validator);
        Set<TruckTimeTable> tttSetDTO = tttSetValidation(manifestTpaTttDTO.getTttSetDTO(), validator);
        Map<Long, Manifest> manifestMapDTO = manifestValidation(manifestTpaTttDTO.getManifestMapDTO(), tttSetDTO, tpaSetDTO, validator);
        Set<ManifestReference> manifestReferenceSetDTO = manifestReferenceSetValidator(manifestTpaTttDTO.getManifestReferenceSetDTO(), validator);

        manifestTpaTttDTO.setManifestMapDTO(manifestMapDTO);
        manifestTpaTttDTO.setTpaSetDTO(tpaSetDTO);
        manifestTpaTttDTO.setTttSetDTO(tttSetDTO);
        manifestTpaTttDTO.setManifestReferenceSetDTO(manifestReferenceSetDTO);

        return manifestTpaTttDTO;

    }

    /**
     * Validates given entities against annotation conditions in Manifest class. Turns isActive to false if there is
     * not compliant conditions. Also checks if there is already existing manifest in DB with given Manifest Code.
     * In such case it will be turned to false also.
     *
     * @param manifestMapDTO - map with Manifests to be validated
     * @param tttSetDTO
     * @param tpaSetDTO
     * @param validator      - Validator
     * @return same Map type with checked entities.
     */
    private Map<Long, Manifest> manifestValidation(Map<Long, Manifest> manifestMapDTO, Set<TruckTimeTable> tttSetDTO, Set<TPA> tpaSetDTO, Validator validator) {
        if (manifestMapDTO != null) {
            for (Map.Entry<Long, Manifest> longManifestEntry : manifestMapDTO.entrySet()) {
                if (longManifestEntry.getValue() != null) {
                    Set<ConstraintViolation<Manifest>> constraintValidator = validator.validate(longManifestEntry.getValue());
                    Set<TruckTimeTable> checkingTTTset = tttSetDTO.stream()
                            .flatMap(n -> longManifestEntry.getValue().getTruckTimeTableSet()
                                    .stream()
                                    .filter(ttt -> ttt.getTruckName() != null)
                                    .filter(p -> p.getTttArrivalDatePlan().equals(n.getTttArrivalDatePlan()) && p.getTruckName().equals(n.getTruckName())))
                            .filter(ttt -> ttt.getIsActive() != null && !ttt.getIsActive())
                            .collect(Collectors.toSet());
                    Set<TPA> checkingTPAset = tpaSetDTO.stream()
                            .flatMap(n -> longManifestEntry.getValue().getTpaSet()
                                    .stream()
                                    .filter(tpa -> tpa.getName() != null)
                                    .filter(p -> p.getName().equals(n.getName()) && p.getDeparturePlan().equals(n.getDeparturePlan())))
                            .filter(tpa -> tpa.getIsActive() != null && !tpa.getIsActive())
                            .collect(Collectors.toSet());
                    if (!constraintValidator.isEmpty() || !checkingTTTset.isEmpty() || !checkingTPAset.isEmpty()) {
                        log.info("Manifest from Row {} would not be persisted: {} \n TPA set has wrong forecast - {}!, \n TTT set has wrong forecast - {}!",
                                longManifestEntry.getKey(), constraintValidator, !checkingTPAset.isEmpty(), !checkingTTTset.isEmpty());
                        longManifestEntry.getValue().setIsActive(false);
                    } else {
                        longManifestEntry.getValue().setIsActive(!manifestService.isManifestExisting(longManifestEntry.getValue()));
                    }
                }
            }
        }
        return manifestMapDTO;
    }

    /**
     * Validates given entities against annotation conditions in TruckTimeTable class. Turns isActive to false if there is
     * not compliant conditions. Also checks if there is already existing such TTT in DB with given truckName and planned
     * arrival DateTime. In such case it will be turned to false also.
     *
     * @param tttSet    - set of TTTs to check each element.
     * @param validator - Validator
     * @return - same Set with without null elements.
     */
    private Set<TruckTimeTable> tttSetValidation(Set<TruckTimeTable> tttSet, Validator validator) {
        if (tttSet != null) {
            for (TruckTimeTable ttt : tttSet) {
                if (ttt != null) {
                    Set<ConstraintViolation<TruckTimeTable>> constraintValidator = validator.validate(ttt);
                    boolean alreadyExisting = truckService.getTttService().isTttExisting(ttt);
                    if (!constraintValidator.isEmpty()) {
                        log.info("TTT {} would not be persisted: {}", ttt.getTruckName(), constraintValidator);
                        ttt.setIsActive(false);
                    } else if (alreadyExisting || ttt.getTttStatus().getTttStatusName().equals(TTTEnum.ERROR)) {
                        log.info("TTT {} would not be persisted: 1. Such Truck Name and ETA plan already existing: {} 2. The status is: {}",
                                ttt.getTruckName(), alreadyExisting, ttt.getTttStatus());
                        ttt.setIsActive(false);
                    } else {
                        ttt.setIsActive(true);
                    }
                }
            }
            return tttSet.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    /**
     * Validates given entities against annotation conditions in TPA class. Turns isActive to false if there is
     * not compliant conditions. Also checks if there is already existing such TPA in DB with given TPA Name and planned
     * departure DateTime. In such case it will be turned to false also.
     *
     * @param tpaSet    - set of TPAs to check each element.
     * @param validator - - Validator
     * @return - same Set with without null elements.
     */
    private Set<TPA> tpaSetValidation(Set<TPA> tpaSet, Validator validator) {
        if (tpaSet != null) {
            for (TPA tpa : tpaSet) {
                if (tpa != null) {
                    boolean alreadyExisting = truckService.getTpaService().isTpaExisting(tpa);
                    Set<ConstraintViolation<TPA>> constraintValidator = validator.validate(tpa);
                    if (!constraintValidator.isEmpty()) {
                        log.info("TPA {} would not be persisted: {}", tpa.getName(), constraintValidator);
                        tpa.setIsActive(false);
                    } else if (alreadyExisting || tpa.getStatus().getStatusName().equals(TPAEnum.ERROR)) {
                        log.info("TPA {} would not be persisted: 1. Such TPA name and ETD plan already existing: {} 2. The status is: {}",
                                tpa.getName(), alreadyExisting, tpa.getStatus());
                        tpa.setIsActive(false);
                    } else {
                        tpa.setIsActive(true);
                    }
                }
            }
            return tpaSet.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    /**
     * Validates given entities against annotation conditions in ManifestReference class. Turns isActive to false if there is
     * not compliant conditions.
     *
     * @param manifestReferenceSet - set with ManifestReferences elements to be checked.
     * @param validator            - Validator
     * @return - the same set with checked entities
     */
    private Set<ManifestReference> manifestReferenceSetValidator(Set<ManifestReference> manifestReferenceSet, Validator validator) {
        if (manifestReferenceSet != null) {
            for (ManifestReference manifestReference : manifestReferenceSet) {
                if (manifestReference != null) {
                    Set<ConstraintViolation<ManifestReference>> constraintValidator = validator.validate(manifestReference);
                    if (!constraintValidator.isEmpty()) {
                        log.info("Reference {} in Manifest {} would not be persisted: {}", manifestReference.getReference().getName(), manifestReference.getManifestCode(), constraintValidator);
                        manifestReference.setIsActive(false);
                    } else {
                        manifestReference.setIsActive(true);
                    }
                }
            }
            return manifestReferenceSet.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        }
        return new HashSet<>();
    }

    @Transactional
    void saveElements(Map<Long, Manifest> manifestMapDTO, Set<TPA> tpaSetDTO, Set<TruckTimeTable> tttSetDTO, Set<ManifestReference> manifestReferenceSetDTO) {
        //1. Save TTTs in DB
        List<TruckTimeTable> truckTimeTableListFromDB = truckService.getTttService().saveAll(new ArrayList<>(tttSetDTO)
                .stream()
                .filter(TruckTimeTable::getIsActive)
                .collect(Collectors.toList()));
        //2. Save all TPAs in DB
        List<TPA> tpaListFromDB = truckService.getTpaService().saveAll(new ArrayList<>(tpaSetDTO)
                .stream()
                .filter(TPA::getIsActive)
                .collect(Collectors.toList()));

        //3. Connect TPAs and TTTs with Manifests. Save all Manifests and receive these Manifests as DB entities
        List<Manifest> manifestsFromDB = connectManifestWithTpaAndTtt(new ArrayList<>(manifestMapDTO.values()), truckTimeTableListFromDB, tpaListFromDB)
                .stream()
                .filter(Manifest::getIsActive)
                .collect(Collectors.toList());
        manifestsFromDB = manifestService.saveAll(manifestsFromDB);

        //4. Add TPA to each ManifestReferences. Save all and receive them as entities from DB
        List<ManifestReference> manifestReferencesFromDB = connectManiRefToManifestAndTPA(new ArrayList<>(manifestReferenceSetDTO), manifestsFromDB, tpaListFromDB)
                .stream()
                .filter(manref -> manref.getIsActive() && manref.getManifest() != null)
                .collect(Collectors.toList());
        manifestReferencesFromDB = manifestReferenceService.saveAll(manifestReferencesFromDB);
    }

    private List<ManifestReference> connectManiRefToManifestAndTPA(List<ManifestReference> manifestReferences, List<Manifest> manifestsFromDB, List<TPA> tpaListFromDB) {
        for (ManifestReference manifestReference : manifestReferences) {
            TPA tpa = tpaListFromDB.stream()
                    .filter(x -> x.getName().equals(manifestReference.getTpaCode()) && x.getIsActive())
                    .reduce((a, b) -> {
                        throw new IllegalStateException("Multiple TPA entities " + a + " || " + b);
                    }).orElse(null);
            Manifest manifest = manifestsFromDB.stream()
                    .filter(x -> x.getManifestCode().equals(manifestReference.getManifestCode()) && x.getIsActive())
                    .reduce((a, b) -> {
                        throw new IllegalStateException("Multiple manifests " + a + " || " + b);
                    }).orElse(null);
            manifestReference.setTpa(tpa);
            manifestReference.setManifest(manifest);
        }
        return manifestReferences;
    }

    private List<Manifest> connectManifestWithTpaAndTtt(List<Manifest> manifests, List<TruckTimeTable> truckTimeTableListFromDB, List<TPA> tpaListFromDB) {
        for (Manifest manifest : manifests) {
            Set<TruckTimeTable> newTTTset = new HashSet<>();
            Set<TPA> newTPAset = new HashSet<>();
            for (TruckTimeTable oldTruckTimeTable : manifest.getTruckTimeTableSet()) {
                newTTTset.add(truckTimeTableListFromDB.stream()
                        .filter(x -> x.getTruckName().equals(oldTruckTimeTable.getTruckName()) && x.getTttArrivalDatePlan().equals(oldTruckTimeTable.getTttArrivalDatePlan()) && x.getIsActive())
                        .reduce((a, b) -> {
                            throw new IllegalStateException("Multiple TTT elements " + a + " || " + b);
                        }).orElse(null)
                );
            }
            manifest.setTruckTimeTableSet(newTTTset);
            for (TPA oldTpa : manifest.getTpaSet()) {
                newTPAset.add(tpaListFromDB.stream()
                        .filter(x -> x.getName().equals(oldTpa.getName()) && x.getDeparturePlan().equals(oldTpa.getDeparturePlan()) && x.getIsActive())
                        .reduce((a, b) -> {
                            throw new IllegalStateException("Multiple TPA elements " + a + " || " + b);
                        }).orElse(null)
                );
            }
            manifest.setTpaSet(newTPAset);
        }
        return manifests;
    }

    /**
     * Prints sets of entities which were given to be saved in DB
     *
     * @param manifestMapDTO          - set of Manifests to print
     * @param tpaSetDTO               - set of TPAs to print
     * @param tttSetDTO               - set of TTTs to print
     * @param manifestReferenceSetDTO - set of ManifestReferences to print
     */
    private void loggerPrintStrings(Map<Long, Manifest> manifestMapDTO,
                                    Set<TPA> tpaSetDTO,
                                    Set<TruckTimeTable> tttSetDTO,
                                    Set<ManifestReference> manifestReferenceSetDTO) {

        StringBuilder manifests = new StringBuilder();
        StringBuilder tpas = new StringBuilder();
        StringBuilder ttts = new StringBuilder();
        StringBuilder references = new StringBuilder();

        manifestMapDTO.forEach((key, value) -> manifests.append(key).append(" - ").append(value).append("\n"));
        tpaSetDTO.forEach(x -> tpas.append(x).append("\n"));
        tttSetDTO.forEach(x -> ttts.append(x).append("\n"));
        manifestReferenceSetDTO.forEach(x -> references.append(x).append("\n"));

        log.info("Map of manifests: \n{}", manifests);
        log.info("Set of TPA: \n{}", tpas);
        log.info("Set of TTT: \n{}", ttts);
        log.info("Set of Man. References: \n{}", references);
    }

    @Override
    public ManifestTpaTttDTO entityValidation(Long key, ManifestTpaTttDTO entity) {
        return null;
    }

}
