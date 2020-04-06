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
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.excel.ExcelManifestService;
import pl.com.xdms.service.truck.TruckService;
import pl.com.xdms.validator.ExcelManifestValidator;

import javax.transaction.Transactional;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created on 30.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("coordinator/excel")
public class ExcelManifestController implements ExcelController<ManifestTpaTttDTO> {

    private final FileStorageService fileStorageService;
    private final ExcelManifestService excelManifestService;
    private final TruckService truckService;
    private final ExcelManifestValidator excelManifestValidator;

    @Autowired
    public ExcelManifestController(FileStorageService fileStorageService,
                                   ExcelManifestService excelManifestService,
                                   TruckService truckService,
                                   ExcelManifestValidator excelManifestValidator) {
        this.fileStorageService = fileStorageService;
        this.excelManifestService = excelManifestService;
        this.truckService = truckService;
        this.excelManifestValidator = excelManifestValidator;
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
        ManifestTpaTttDTO manifestTpaTttDTO = excelManifestValidator.entityValidation(resultList.getValue());

        return Collections.singletonList(manifestTpaTttDTO);
    }

    @Override
    @Transactional
    @PostMapping("/forecast/save")
    public ResponseEntity<List<ManifestTpaTttDTO>> saveAllEntities(List<ManifestTpaTttDTO> objList) {

        //Check again given object to avoid any cheating from user
        ManifestTpaTttDTO object = excelManifestValidator.entityValidation(objList.iterator().next());

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
        truckService.saveElements(manifestMapDTO, tpaSetDTO, tttSetDTO, manifestReferenceSetDTO);

        List<ManifestTpaTttDTO> resultList = new ArrayList<>();
        //Check again given object to
        resultList.add(excelManifestValidator.entityValidation(deepCopy));
        log.info("ManifestReferenceDTO after saving \n {}", resultList.get(0));

        return ResponseEntity.status(201).header("Message", "dont know what to say...").body(resultList);
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
