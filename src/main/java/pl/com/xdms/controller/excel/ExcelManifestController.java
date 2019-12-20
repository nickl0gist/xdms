package pl.com.xdms.controller.excel;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.manifest.Manifest;
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
public class ExcelManifestController implements ExcelController<Manifest> {

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
    public List<Manifest> uploadFile(MultipartFile file) {
        Path filePath = fileStorageService.storeFile(file);
        Map<Long, Manifest> resultList = excelManifestService.readExcel(filePath.toFile());

        return resultList.entrySet()
                .stream()
                .map(x -> entityValidation(x.getKey(), x.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean validation(Long key, Manifest manifest, Logger log) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        if (manifest != null) {
            log.info(manifest.toString());
            Set<ConstraintViolation<Manifest>> manifestValidator = validator.validate(manifest);
            if(manifest.getManifestsReferenceSet() != null) {
                Set<ConstraintViolation<ManifestReference>> referenceValidator = manifest.getManifestsReferenceSet()
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
    public Manifest entityValidation(Long key, Manifest manifest) {
        manifest.setIsActive(validation(key, manifest, log));
        return manifest;
    }

    @Override
    public ResponseEntity<List<Manifest>> saveAllEntities(List<Manifest> objList) {
        return null;
    }


}
