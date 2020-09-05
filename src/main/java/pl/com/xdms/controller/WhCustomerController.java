package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.service.CustomerService;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.WhCustomerService;

import javax.validation.Valid;
import java.util.List;

/**
 * Created on 25.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("warehouse/{urlCode:^[a-z_]{5,8}$}")
public class WhCustomerController {

    private final WhCustomerService whCustomerService;
    private final WarehouseService warehouseService;
    private final RequestErrorService requestErrorService;
    private final CustomerService customerService;

    @Autowired
    public WhCustomerController(WhCustomerService whCustomerService,
                                WarehouseService warehouseService,
                                RequestErrorService requestErrorService,
                                CustomerService customerService) {
        this.whCustomerService = whCustomerService;
        this.warehouseService = warehouseService;
        this.requestErrorService = requestErrorService;
        this.customerService = customerService;
    }

    /**
     * Endpoint dedicated to obtain List of all customers for current warehouse.
     * @param urlCode - unique url of warehouse where changes should be done.
     * @return List\<WhCustomer\> the request was successful.
     */
    @GetMapping("/customers")
    public ResponseEntity<List<WhCustomer>> getAllCustomersForWarehouse(@PathVariable String urlCode){
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        if (warehouse == null){
            return ResponseEntity.status(404).build();
        }
        List<WhCustomer> whCustomers = whCustomerService.getAllWhCustomerByWarehouse(warehouse);
        return ResponseEntity.status(200).body(whCustomers);
    }

    /**
     * Endpoint dedicated to obtain List of only active customers for current warehouse.
     * @param urlCode - unique url of warehouse where changes should be done.
     * @return List\<WhCustomer\> the request was successful.
     */
    @GetMapping("/customers/active")
    public ResponseEntity<List<WhCustomer>> getOnlyActiveWhCustomersForWarehouse(@PathVariable String urlCode){
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        if (warehouse == null){
            return ResponseEntity.status(404).build();
        }
        List<WhCustomer> whCustomers = whCustomerService.getAllWhCustomersByWarehouseIsActive(warehouse);
        return ResponseEntity.status(200).body(whCustomers);
    }

    /**
     * Endpoint dedicated to obtain List of not active customers for current warehouse.
     * @param urlCode - unique url of warehouse where changes should be done.
     * @return List\<WhCustomer\> the request was successful.
     */
    @GetMapping("/customers/inactive")
    public ResponseEntity<List<WhCustomer>> getOnlyDeactivatedCustomersForWarehouse(@PathVariable String urlCode){
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        if (warehouse == null){
            return ResponseEntity.status(404).build();
        }
        List<WhCustomer> whCustomers = whCustomerService.getAllWhCustomersByWarehouseNotActive(warehouse);
        return ResponseEntity.status(200).body(whCustomers);
    }

    /**
     * Endpoint dedicated to obtain certain WhCustomer for certain Warehouse and Customer
     * @param urlCode - unique url of warehouse where changes should be done.
     * @return List\<WhCustomer\> the request was successful.
     */
    @GetMapping("/customer/{id}")
    public ResponseEntity<WhCustomer> getWhCustomerForWarehouse(@PathVariable String urlCode, @PathVariable Long id){
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        Customer customer = customerService.getCustomerById(id);
        if (warehouse == null || customer == null){
            return ResponseEntity.status(404).build();
        }
        WhCustomer whCustomer = whCustomerService.findByWarehouseAndCustomer(warehouse, customer);
        return (whCustomer != null)
                ? ResponseEntity.ok(whCustomer)
                : ResponseEntity.notFound().build();
    }

    /**
     * Could change only <b>isActive</b> parameter of existing WhCustomer connection.
     * @param whCustomer - existing connection warehouse with customer to be updated.
     * @param bindingResult - BindingResult
     * @return ResponseEntity\<WhCustomer\>
     */
    @SuppressWarnings("Duplicates")
    @PutMapping("")
    public ResponseEntity<WhCustomer> updateWarehouseCustomerIsActive(@PathVariable String urlCode, @RequestBody @Valid WhCustomer whCustomer, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            log.info("1");
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(whCustomer);
        }
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        if (!whCustomer.getWarehouse().equals(warehouse)){
            return ResponseEntity.status(404).header("Error", "The Warehouse doesn't have such customer").build();
        }
        WhCustomer repositoryWhCustomer = whCustomerService.update(whCustomer);
        log.info("3: {}", repositoryWhCustomer);
        return (repositoryWhCustomer != null)
                ? ResponseEntity.ok(repositoryWhCustomer)
                : ResponseEntity.notFound().header("Error", "The WhCustomer wasn't found").build();
    }
}

