package pl.com.xdms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.service.ExcelService;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.ReferenceService;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
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
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RestController
@RequestMapping("coordinator/excel")
public class ExcelController {

    private static final Logger LOG = LoggerFactory.getLogger(ExcelController.class);

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

    @PostMapping("/references/uploadFile")
    public List<Reference> uploadFile(@RequestParam("file") MultipartFile file) {
        Path filePath = fileStorageService.storeFile(file);
        Map<Integer, Reference> referenceMap = excelService.readExcel(filePath);

        return referenceMap.entrySet()
                .stream()
                .map(x -> referenceValidation(x.getKey(), x.getValue()))
                .collect(Collectors.toList());
        //referenceService.save(referenceList);
    }

    private Reference referenceValidation(Integer key, Reference reference) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<Reference>> constraintValidator = validator.validate(reference);
        if (!constraintValidator.isEmpty()) {
            LOG.warn("Row {} : {}", key, constraintValidator);
            reference.setIsActive(false);
        }
        return reference;
    }

    @PostMapping("/references/saveall")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<List<Reference>> saveAllReferences(@RequestBody List<@Valid Reference> referenceList, BindingResult bindingResult) {
/*        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(423)
                    .header("Message", "Some of references have unsupported properties")
                    .body(referenceList);
        }*/
        referenceList.forEach(x -> LOG.info(x.toString()));
        referenceService.save(referenceList.stream()
                .filter(Reference::getIsActive)
                .collect(Collectors.toList())
        );
        return ResponseEntity.status(201).header("Message", "Only Active References were saved").body(referenceList);
    }
}
