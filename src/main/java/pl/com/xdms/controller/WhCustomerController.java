package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;
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
@RequestMapping("coordinator/warehouse")
public class WhCustomerController {

    private final WhCustomerService whCustomerService;
    private final WarehouseService warehouseService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public WhCustomerController(WhCustomerService whCustomerService,
                                WarehouseService warehouseService,
                                RequestErrorService requestErrorService) {
        this.whCustomerService = whCustomerService;
        this.warehouseService = warehouseService;
        this.requestErrorService = requestErrorService;
    }

    @GetMapping("/{wh_url}/customers")
    public ResponseEntity<List<WhCustomer>> getAllCustomersForWarehouse(@PathVariable String wh_url){
        Warehouse warehouse = warehouseService.getWarehouseByUrl(wh_url);
        if (warehouse == null){
            return ResponseEntity.status(404).build();
        }
        List<WhCustomer> whCustomers = whCustomerService.getAllWhCustomerByWarehouse(warehouse);
        return ResponseEntity.status(200).body(whCustomers);
    }

    @GetMapping("/{wh_url}/customers/active")
    public ResponseEntity<List<WhCustomer>> getOnlyActiveWhCustomersForWarehouse(@PathVariable String wh_url){
        Warehouse warehouse = warehouseService.getWarehouseByUrl(wh_url);
        if (warehouse == null){
            return ResponseEntity.status(404).build();
        }
        List<WhCustomer> whCustomers = whCustomerService.getAllWhCustomersByWarehouseIsActive(warehouse);
        return ResponseEntity.status(200).body(whCustomers);
    }

    @GetMapping("/{wh_url}/customers/inactive")
    public ResponseEntity<List<WhCustomer>> getOnlyDeactivatedCustomersForWarehouse(@PathVariable String wh_url){
        Warehouse warehouse = warehouseService.getWarehouseByUrl(wh_url);
        if (warehouse == null){
            return ResponseEntity.status(404).build();
        }
        List<WhCustomer> whCustomers = whCustomerService.getAllWhCustomersByWarehouseNotActive(warehouse);
        return ResponseEntity.status(200).body(whCustomers);
    }

    @GetMapping("/{wh_url}/customer/{id}")
    public ResponseEntity<WhCustomer> getWhCustomerForWarehouse(@PathVariable String wh_url, @PathVariable Long id){
        Warehouse warehouse = warehouseService.getWarehouseByUrl(wh_url);
        if (warehouse == null){
            return ResponseEntity.status(404).build();
        }
        WhCustomer whCustomer = whCustomerService.findWhCustomerById(id);
        return (whCustomer != null)
                ? ResponseEntity.ok(whCustomer)
                : ResponseEntity.notFound().build();
    }

    /**
     * Could change only @code isActive parameter of existing WhCustomer connection.
     * @param whCustomer - existing connection warehouse with customer to be updated.
     * @param bindingResult - BindingResult
     * @return ResponseEntity<WhCustomer>
     */
    @SuppressWarnings("Duplicates")
    @PutMapping("")
    public ResponseEntity<WhCustomer> updateWarehouseCustomerIsActive(@RequestBody @Valid WhCustomer whCustomer, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(whCustomer);
        }
        WhCustomer repositoryWhCustomer = whCustomerService.update(whCustomer);
        return (repositoryWhCustomer != null)
                ? ResponseEntity.ok(repositoryWhCustomer)
                : ResponseEntity.notFound().build();
    }
}

