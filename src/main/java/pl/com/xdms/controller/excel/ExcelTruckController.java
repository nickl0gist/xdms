package pl.com.xdms.controller.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.excel.ExcelManifestReferenceService;
import pl.com.xdms.service.truck.TruckService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.BASIC_ISO_DATE;

/**
 * Created on 21.07.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("warehouse/{urlCode:^[a-z_]{5,8}$}")
@PropertySource("classpath:messages.properties")
public class ExcelTruckController {
    @Value("${error.http.message}")
    String errorMessage;

    @Value("${message.http.message}")
    String messageMessage;

    private final FileStorageService fileStorageService;

    private final ExcelManifestReferenceService excelManifestReferenceService;

    private final TruckService truckService;

    private WarehouseService warehouseService;

    @Autowired
    public ExcelTruckController(FileStorageService fileStorageService, ExcelManifestReferenceService excelManifestReferenceService,
                                TruckService truckService, WarehouseService warehouseService) {
        this.fileStorageService = fileStorageService;
        this.excelManifestReferenceService = excelManifestReferenceService;
        this.truckService = truckService;
        this.warehouseService = warehouseService;
    }

    /**
     * Endpoint dedicated to receiving Excel file with ManifestReferences in particular TTT.
     *
     * @param id - Long TTT id
     * @return - ResponseEntity<InputStreamSource> with generated Excel file
     */
    @GetMapping("ttt/{id:^\\d+$}/reception.xlsx")
    public ResponseEntity<InputStreamSource> getInputStreamTttForReception(@PathVariable String urlCode, @PathVariable Long id) throws IOException {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TruckTimeTable ttt = truckService.getTttService().getTTTByWarehouseAndId(id, warehouse);
        if (ttt == null) {
            return ResponseEntity.notFound().build();
        }
        ByteArrayInputStream in = excelManifestReferenceService.instanceToExcelFromTemplate(ttt);
        LocalDateTime dateTime = LocalDateTime.now();
        String filename = "reception_" + dateTime.format(BASIC_ISO_DATE) + dateTime.getHour() + dateTime.getMinute();
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
     *
     * @param file - Excel File.
     */
    @PostMapping("ttt/{tttId:^\\d+$}/uploadFile")
    public ResponseEntity uploadFile(@PathVariable String urlCode,  @PathVariable Long tttId, MultipartFile file) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TruckTimeTable truckTimeTable = truckService.getTttService().getTttById(tttId);
        if(!truckTimeTable.getWarehouse().equals(warehouse)){
            return ResponseEntity.badRequest().header(errorMessage, "Given TTT is not in scope of given Warehouse").build();
        }
        Path filePath = fileStorageService.storeFile(file);
        String extension = file.getContentType();
        log.info("extension {}", extension);
        excelManifestReferenceService.saveReceptions(filePath.toFile(), warehouse);
        return ResponseEntity.ok().build();
    }

    @GetMapping("tpa/{id:^\\d+$}/tpaPackingList.xlsx")
    public ResponseEntity<InputStreamSource> getExcelPackingListOfTpa(@PathVariable String urlCode, @PathVariable Long id) throws IOException {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TPA tpa = truckService.getTpaService().getTpaByWarehouseAndId(id, warehouse);
        if (tpa == null || !tpa.getTpaDaysSetting().getWhCustomer().getWarehouse().equals(warehouse)) {
            return ResponseEntity.notFound().build();
        }
        LocalDateTime dateTime = LocalDateTime.now();
        ByteArrayInputStream in = excelManifestReferenceService.instanceToExcelFromTemplate(tpa);
        String filename = "packingList_"+ tpa.getName()+ "_" + dateTime.format(BASIC_ISO_DATE) + dateTime.getHour() + dateTime.getMinute();
        in.close();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + filename + ".xlsx");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(in));
    }
}
