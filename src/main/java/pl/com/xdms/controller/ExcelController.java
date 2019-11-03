package pl.com.xdms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.service.ExcelService;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.ReferenceService;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Created on 28.10.2019
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
    public ResponseEntity<InputStreamSource> downloadReferencesBase(){

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

        List<Reference> referenceList = excelService.readExcel(filePath);
        return referenceList;
        //referenceService.save(referenceList);
    }

    @PostMapping("/references/saveall")
    @ResponseStatus(HttpStatus.CREATED)
    public void saveAllReferences(@RequestBody List<Reference> referenceList){
        referenceList.forEach(x -> LOG.info(x.toString()));
        referenceService.save(referenceList);
    }
}
