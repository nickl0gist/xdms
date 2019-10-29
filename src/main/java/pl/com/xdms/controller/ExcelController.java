package pl.com.xdms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.service.ExcelService;
import pl.com.xdms.service.ReferenceService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    @Autowired
    public ExcelController(ExcelService excelService, ReferenceService referenceService) {
        this.excelService = excelService;
        this.referenceService = referenceService;
    }

    //TODO Change to give the file with actual database of references.
    @GetMapping("/download/references.xlsx")
    public ResponseEntity<InputStreamSource> downloadReferencesBase() throws IOException {

        List<Reference> references = referenceService.getAllReferences();
        ByteArrayInputStream in = excelService.referencesToExcel(references);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=references.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
    }
}
