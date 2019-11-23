package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.WarehouseService;

import javax.validation.Valid;
import java.util.List;

/**
 * Created on 23.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("admin/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public WarehouseController(WarehouseService warehouseService,
                               RequestErrorService requestErrorService) {
        this.warehouseService = warehouseService;
        this.requestErrorService = requestErrorService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Warehouse> getAllWarehouses(){
        return warehouseService.getAllWarehouses();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Warehouse> getWarehouseById(@PathVariable Long id){
        Warehouse warehouse = warehouseService.getWarehouseById(id);
        if (warehouse != null){
            log.info("Warehouse was found {}", warehouse);
            return ResponseEntity.ok(warehouse);
        } else {
            log.warn("Warehouse with id: {} not found, returning error", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/active")
    public List<Warehouse> getActiveSuppliers(){
        return warehouseService.getSuppliersWhereIsActive(true);
    }

    @GetMapping("/not_active")
    public List<Warehouse> getNotActiveSuppliers(){
        return warehouseService.getSuppliersWhereIsActive(false);
    }

    @GetMapping({"/ordered_by/{orderBy}/{direction}", "/ordered_by/{orderBy}"})
    @ResponseStatus(HttpStatus.OK)
    public List<Warehouse> getAllSuppliersOrderedBy(@PathVariable String orderBy, @PathVariable String direction){
        return warehouseService.getAllSuppliersOrderedBy(orderBy, direction);
    }

    @GetMapping("/search/{searchString}")
    @ResponseStatus(HttpStatus.OK)
    public List<Warehouse> searchSupplierByString(@PathVariable String searchString){
        return warehouseService.search(searchString);
    }

    @SuppressWarnings("Duplicates")
    @PutMapping
    public ResponseEntity<Warehouse> updateSupplier(@RequestBody @Valid Warehouse warehouse, BindingResult bindingResult){
        log.info("Try to update Warehouse with Id:{}", warehouse.getWarehouseID());
        if (bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(warehouse);
        }
        Warehouse repositoryWarehouse = warehouseService.updateWarehouse(warehouse);
        return (repositoryWarehouse != null)
                ? ResponseEntity.ok(repositoryWarehouse)
                : ResponseEntity.notFound().build();
    }

    @SuppressWarnings("Duplicates")
    @PostMapping
    public ResponseEntity<Warehouse> createSupplier(@RequestBody @Valid Warehouse warehouse, BindingResult bindingResult){
        log.info("Try to create warehouse with Name: {}, from: {}", warehouse.getName(), warehouse.getCountry());
        if(bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(warehouse);
        }
        warehouseService.save(warehouse);
        return ResponseEntity.status(201).build();
    }
}
