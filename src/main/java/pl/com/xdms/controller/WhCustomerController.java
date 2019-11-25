package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.WhCustomerService;

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

    @Autowired
    public WhCustomerController(WhCustomerService whCustomerService, WarehouseService warehouseService) {
        this.whCustomerService = whCustomerService;
        this.warehouseService = warehouseService;
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
    public ResponseEntity<List<WhCustomer>> getOnlyActiveCustomersForWarehouse(@PathVariable String wh_url){
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
}

