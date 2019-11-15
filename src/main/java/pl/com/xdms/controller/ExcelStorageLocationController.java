package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.storloc.StorageLocation;
import pl.com.xdms.service.ExcelStorageLocationService;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.StorageLocationService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created on 13.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("coordinator/excel")
public class ExcelStorageLocationController implements ExcelController<StorageLocation> {

    private StorageLocationService storageLocationService;
    private ExcelStorageLocationService excelStorageLocationService;
    private FileStorageService fileStorageService;

    @Autowired
    public ExcelStorageLocationController(StorageLocationService storageLocationService,
                                          ExcelStorageLocationService excelStorageLocationService,
                                          FileStorageService fileStorageService) {

        this.storageLocationService = storageLocationService;
        this.excelStorageLocationService = excelStorageLocationService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @GetMapping("/download/storage_locations.xlsx")
    public ResponseEntity<InputStreamSource> downloadBase() throws IOException {
        List<StorageLocation> storageLocations = storageLocationService.getAllStorLocs();
        ByteArrayInputStream in = excelStorageLocationService.instanceToExcelFromTemplate(storageLocations);
        in.close();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=storage_locations.xlsx");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
    }

    @SuppressWarnings("Duplicates")
    @Override
    @PostMapping("/storage_locations/uploadFile")
    public List<StorageLocation> uploadFile(MultipartFile file) {
        Path filePath = fileStorageService.storeFile(file);
        Map<Long, StorageLocation> storageLocationMap = excelStorageLocationService.readExcel(filePath.toFile());

        return storageLocationMap.entrySet()
                .stream()
                .map(y -> entityValidation(y.getKey(), y.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public StorageLocation entityValidation(Long key, StorageLocation entity) {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        log.warn(entity.toString());
        Set<ConstraintViolation<StorageLocation>> constraintValidator = validator.validate(entity);
        if (!constraintValidator.isEmpty()) {
            log.warn("Row {} would not be persisted: {}", key, constraintValidator);
            entity.setIsActive(false);
        }
        return entity;
    }

    @Override
    @PostMapping("/storage_locations/save_all")
    public ResponseEntity<List<StorageLocation>> saveAllEntities(List<StorageLocation> storageLocationList) {
        storageLocationList.forEach(x -> log.info("Storage Location to be save: {}", x.toString()));
        storageLocationService.save(storageLocationList.stream()
                .filter(StorageLocation::getIsActive)
                .collect(Collectors.toList())
        );
        return ResponseEntity.status(201).header("Message", "Only Active Storage Locations were saved").body(storageLocationList);

    }
}
