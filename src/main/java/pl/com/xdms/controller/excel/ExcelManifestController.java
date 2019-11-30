package pl.com.xdms.controller.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.excel.ExcelManifestService;

import java.io.IOException;
import java.util.List;

/**
 * Created on 30.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("coordinator/excel")
public class ExcelManifestController implements ExcelController<Manifest>{

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
        return getInputStreamSourceResponseEntity(excelManifestService,"manifest_upload_template");
    }

    @Override
    public List<Manifest> uploadFile(MultipartFile file) {
        return null;
    }

    @Override
    public Manifest entityValidation(Long key, Manifest entity) {
        return null;
    }

    @Override
    public ResponseEntity<List<Manifest>> saveAllEntities(List<Manifest> objList) {
        return null;
    }
}
