package pl.com.xdms.controller.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.excel.ExcelManifestReferenceService;
import pl.com.xdms.service.truck.TruckService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

/**
 * Created on 21.07.2020
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("coordinator/")
public class ExcelTttController{

    private final FileStorageService fileStorageService;

    private final ExcelManifestReferenceService excelManifestReferenceService;

    private final TruckService truckService;

    @Autowired
    public ExcelTttController(FileStorageService fileStorageService, ExcelManifestReferenceService excelManifestReferenceService, TruckService truckService) {
        this.fileStorageService = fileStorageService;
        this.excelManifestReferenceService = excelManifestReferenceService;
        this.truckService = truckService;
    }

    /**
     * Endpoint dedicated to receiving Excel file with ManifestReferences in particular TTT.
     * @param id - Long TTT id
     * @return - ResponseEntity<InputStreamSource> with generated Excel file
     */
    @GetMapping("ttt/{id:^\\d+$}/reception.xlsx")
    public ResponseEntity<InputStreamSource> getInputStreamSourceResponseEntity(@PathVariable Long id) throws IOException {
        TruckTimeTable ttt = truckService.getTttService().getTttById(id);
        if (ttt == null){
            return ResponseEntity.notFound().build();
        }
        ByteArrayInputStream in = excelManifestReferenceService.instanceToExcelFromTemplate(ttt);
        LocalDateTime dateTime = LocalDateTime.now();
        String filename = "reception_"+ dateTime.format(BASIC_ISO_DATE) + dateTime.getHour() + dateTime.getMinute();
        in.close();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename + ".xlsx");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
    }

    /**
     * Used when user tries to upload excel file to system with reception information.
     * @param file - Excel File.
     */
    @PostMapping("ttt/uploadFile")
    public void uploadFile(MultipartFile file) {
        Path filePath = fileStorageService.storeFile(file);
        String extension = file.getContentType();
        log.info("extension {}", extension);
        excelManifestReferenceService.saveReceptions(filePath.toFile());
    }
}
