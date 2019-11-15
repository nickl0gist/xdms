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
import pl.com.xdms.service.ExcelReferenceService;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.ReferenceService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class ExcelReferenceController implements ExcelController<Reference>{

    private final ExcelReferenceService excelReferenceService;
    private final ReferenceService referenceService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ExcelReferenceController(ExcelReferenceService excelReferenceService,
                                    ReferenceService referenceService,
                                    FileStorageService fileStorageService) {
        this.excelReferenceService = excelReferenceService;
        this.referenceService = referenceService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @GetMapping("/download/references.xlsx")
    public ResponseEntity<InputStreamSource> downloadBase() throws IOException {

        List<Reference> references = referenceService.getAllReferences();
        ByteArrayInputStream in = excelReferenceService.instanceToExcelFromTemplate(references);
        in.close();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=references.xlsx");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        InputStreamResource isr = new InputStreamResource(in);

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(isr);
    }

    @SuppressWarnings("Duplicates")
    @Override
    @PostMapping("/references/uploadFile")
    public List<Reference> uploadFile(@RequestParam("file") MultipartFile file) {
        Path filePath = fileStorageService.storeFile(file);
        Map<Long, Reference> referenceMap = excelReferenceService.readExcel(filePath.toFile());

        return referenceMap.entrySet()
                .stream()
                .map(x -> entityValidation(x.getKey(), x.getValue()))
                .collect(Collectors.toList());
    }


    @Override
    public Reference entityValidation(Long key, Reference reference) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Reference>> constraintValidator = validator.validate(reference);
        if (!constraintValidator.isEmpty()) {
            log.warn("Row would not be persisted {} : {}", key, constraintValidator);
            reference.setIsActive(false);
        }
        return reference;
    }

    @Override
    @PostMapping("/references/save_all")
    public ResponseEntity<List<Reference>> saveAllEntities(@RequestBody List<Reference> referenceList) {
        referenceList.forEach(x -> log.info("Reference to be save: {}", x.toString()));
        referenceService.save(referenceList.stream()
                .filter(Reference::getIsActive)
                .collect(Collectors.toList())
        );
        return ResponseEntity.status(201).header("Message", "Only Active References were saved").body(referenceList);
    }

    @GetMapping("/download/file")
    public ResponseEntity<InputStreamSource> downloadFile() throws IOException {
        log.warn("Start");

        ByteArrayInputStream in = new ByteArrayInputStream(Files.readAllBytes(Paths.get("E:/UBU/_XDMS/src/main/resources/exceltemps/references.xlsx")));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=references.xlsx");
        log.warn("End");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
    }
}
