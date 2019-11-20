package pl.com.xdms.controller.excel;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.SupplierService;
import pl.com.xdms.service.excel.ExcelSupplierService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created on 18.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("coordinator/excel")
public class ExcelSupplierController implements ExcelController<Supplier> {

    private final ExcelSupplierService excelSupplierService;
    private final SupplierService supplierService;
    private final FileStorageService fileStorageService;

    @Autowired
    public ExcelSupplierController(ExcelSupplierService excelSupplierService,
                                   SupplierService supplierService,
                                   FileStorageService fileStorageService) {
        this.excelSupplierService = excelSupplierService;
        this.supplierService = supplierService;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @GetMapping("/download/suppliers.xlsx")
    public ResponseEntity<InputStreamSource> downloadBase() throws IOException {
        List<Supplier> suppliers = supplierService.getAllSuppliers();
        return getInputStreamSourceResponseEntity(suppliers, excelSupplierService);
    }

    @SuppressWarnings("Duplicates")
    @Override
    @PostMapping("/suppliers/uploadFile")
    public List<Supplier> uploadFile(MultipartFile file) {
        Path filePath = fileStorageService.storeFile(file);
        Map<Long, Supplier> referenceMap = excelSupplierService.readExcel(filePath.toFile());

        return referenceMap.entrySet()
                .stream()
                .map(x -> entityValidation(x.getKey(), x.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public Supplier entityValidation(Long key, Supplier supplier) {
        if (!validation(key, supplier, log)){
            supplier.setIsActive(false);
        }
        return supplier;
    }

    @Override
    @PostMapping("/suppliers/save_all")
    public ResponseEntity<List<Supplier>> saveAllEntities(List<Supplier> supplierList) {
        supplierList.forEach(x -> log.info("Supplier to be save: {}", x.toString()));
        supplierService.save(supplierList.stream()
                .filter(Supplier::getIsActive)
                .collect(Collectors.toList())
        );
        return ResponseEntity.status(201).header("Message", "Only Active Suppliers were saved").body(supplierList);
    }
}
