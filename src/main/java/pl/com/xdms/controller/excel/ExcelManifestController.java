package pl.com.xdms.controller.excel;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.dto.ManifestTpaTttDTO;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.excel.ExcelManifestService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    public ExcelManifestController(FileStorageService fileStorageService, ExcelManifestService excelManifestService) {
        this.fileStorageService = fileStorageService;
        this.excelManifestService = excelManifestService;
    }

    @Override
    @GetMapping("/manifest_upload_template.xlsx")
    public ResponseEntity<InputStreamSource> downloadBase() throws IOException {
        return getInputStreamSourceResponseEntity(excelManifestService, "manifest_upload_template");
    }

    @SuppressWarnings("Duplicates")
    @Override
    @PostMapping("/manifests/uploadFile")
    public List<ManifestTpaTttDTO> uploadFile(MultipartFile file) {
        Path filePath = fileStorageService.storeFile(file);
        Map<Long, ManifestTpaTttDTO> resultList = excelManifestService.readExcel(filePath.toFile());

        return resultList.entrySet()
                .stream()
                //.map(x -> entityValidation(x.getKey(), x.getValue()))
                .map(x -> x.getValue())
                .collect(Collectors.toList());
    }

    @Override
    public ManifestTpaTttDTO entityValidation(Long key, ManifestTpaTttDTO manifestTpaTttDTO) {
        validation(key, manifestTpaTttDTO, log);
        return null;
    }

    public boolean validation(Long key, ManifestTpaTttDTO manifestTpaTttDTO, Logger log) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

        if (manifestTpaTttDTO != null) {
            log.info(manifestTpaTttDTO.toString());
            Set<ConstraintViolation<ManifestTpaTttDTO>> manifestValidator = validator.validate(manifestTpaTttDTO);
            if(manifestTpaTttDTO.getManifestReferenceSetDTO() != null) {
                Set<ConstraintViolation<ManifestReference>> referenceValidator = manifestTpaTttDTO.getManifestReferenceSetDTO()
                        .stream()
                        .map(x -> validator.validate(x))
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet());
                if (!manifestValidator.isEmpty() || !referenceValidator.isEmpty()) {
                    log.info("Row {} would not be persisted: {}", key, manifestValidator);
                    referenceValidator.forEach(x -> log.info("Reference has errors {}", x));
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public ResponseEntity<List<ManifestTpaTttDTO>> saveAllEntities(List<ManifestTpaTttDTO> objList) {
        return null;
    }


}
