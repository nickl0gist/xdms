package pl.com.xdms.controller.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.ReferenceService;
import pl.com.xdms.service.excel.ExcelReferenceService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created on 28.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RestController
@RequestMapping("coordinator/excel")
@Slf4j
public class ExcelReferenceController implements ExcelController<Reference> {

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
        return getInputStreamSourceResponseEntity(references, excelReferenceService);
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
        if (!validation(key, reference, log)){
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
}
