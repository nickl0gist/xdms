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
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.service.CustomerService;
import pl.com.xdms.service.FileStorageService;
import pl.com.xdms.service.excel.ExcelCustomerService;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created on 20.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("coordinator/excel")
public class ExcelCustomerController implements ExcelController<Customer> {

    private final FileStorageService fileStorageService;
    private final CustomerService customerService;
    private final ExcelCustomerService excelCustomerService;

    @Autowired
    public ExcelCustomerController(FileStorageService fileStorageService,
                                   CustomerService customerService,
                                   ExcelCustomerService excelCustomerService) {

        this.fileStorageService = fileStorageService;
        this.customerService = customerService;
        this.excelCustomerService = excelCustomerService;
    }

    @Override
    @GetMapping("/download/customers.xlsx")
    public ResponseEntity<InputStreamSource> downloadBase() throws IOException {
        List<Customer> suppliers = customerService.getAllCustomers();
        return getInputStreamSourceResponseEntity(suppliers, excelCustomerService, "customers");
    }

    @SuppressWarnings("Duplicates")
    @Override
    @PostMapping("/customers/uploadFile")
    public List<Customer> uploadFile(MultipartFile file) {
        Path filePath = fileStorageService.storeFile(file);
        Map<Long, Customer> referenceMap = excelCustomerService.readExcel(filePath.toFile());

        return referenceMap.entrySet()
                .stream()
                .map(x -> entityValidation(x.getKey(), x.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public Customer entityValidation(Long key, Customer customer) {
        if (!validation(key, customer, log)){
            customer.setIsActive(false);
        }
        return customer;
    }

    @Override
    @PostMapping("/customers/save_all")
    public ResponseEntity<List<Customer>> saveAllEntities(List<Customer> customerList) {
        customerList.forEach(x -> log.info("Customer to be save: {}", x.toString()));
        customerService.save(customerList.stream()
                .filter(Customer::getIsActive)
                .collect(Collectors.toList())
        );
        return ResponseEntity.status(201).header("Message", "Only Active Customer were saved").body(customerList);
    }
}
