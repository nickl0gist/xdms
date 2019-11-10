package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.service.ExcelService;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.ReferenceService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created on 28.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RestController
@RequestMapping("coordinator/excel")
@Slf4j
public class ExcelController {

    private final ExcelService excelService;
    private final ReferenceService referenceService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ExcelController(ExcelService excelService,
                           ReferenceService referenceService,
                           FileStorageService fileStorageService) {
        this.excelService = excelService;
        this.referenceService = referenceService;
        this.fileStorageService = fileStorageService;
    }

    @GetMapping("/download/references.xlsx")
    public ResponseEntity<InputStreamSource> downloadReferencesBase() {

        List<Reference> references = referenceService.getAllReferences();
        ByteArrayInputStream in = excelService.referencesToExcelFromTemplate(references);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=references.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
    }

    /**
     * @param file - excel file from user with references.
     * @return - list of validated references. Not valid references will have status isAstive = false.
     */
    @PostMapping("/references/uploadFile")
    public List<Reference> uploadFile(@RequestParam("file") MultipartFile file) {
        Path filePath = fileStorageService.storeFile(file);
        Map<Long, Reference> referenceMap = excelService.readExcel(filePath.toFile());

        return referenceMap.entrySet()
                .stream()
                .map(x -> referenceValidation(x.getKey(), x.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Check if reference is Valid. If it isn`t isActive field will be set to false.
     * @param key - number of row in Excel sheet staring from 1
     * @param reference - Reference mapped from Excel row
     * @return - Reference instance
     */
    private Reference referenceValidation(Long key, Reference reference) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Reference>> constraintValidator = validator.validate(reference);
        if (!constraintValidator.isEmpty()) {
            log.warn("Row {} : {}", key, constraintValidator);
            reference.setIsActive(false);
        }
        return reference;
    }

    /**
     * Controller saves on Active references
     * @param referenceList to be persisted in Database
     * @return status "Created" and reference list from request with both statuses.
     */
    @PostMapping("/references/saveall")
    public ResponseEntity<List<Reference>> saveAllReferences(@RequestBody List<Reference> referenceList) {
        referenceList.forEach(x -> log.info(x.toString()));
        referenceService.save(referenceList.stream()
                .filter(Reference::getIsActive)
                .collect(Collectors.toList())
        );
        return ResponseEntity.status(201).header("Message", "Only Active References were saved").body(referenceList);
    }
}
