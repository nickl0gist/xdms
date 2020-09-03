package pl.com.xdms.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.WarehouseService;

import javax.validation.Valid;
import java.util.List;

/**
 * Controller Created for Access only for Admin user
 * Created on 01.09.2020
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */

@Slf4j
@RestController
@RequestMapping("admin/warehouses")
public class AdminWarehouseController {

    private final WarehouseService warehouseService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public AdminWarehouseController(WarehouseService warehouseService, RequestErrorService requestErrorService) {
        this.warehouseService = warehouseService;
        this.requestErrorService = requestErrorService;
    }

    /**
     * Endpoint for getting a list of active warehouses
     * @return List\<Warehouse\> with only active elements
     */
    @GetMapping(value = "/active")
    public List<Warehouse> getActiveWarehouses(){
        return warehouseService.getWarehousesWhereIsActive(true);
    }

    /**
     * Endpoint for getting a list of Not active warehouses
     * @return List\<Warehouse\> with only Not active elements
     */
    @GetMapping("/not_active")
    public List<Warehouse> getNotActiveWarehouses(){
        return warehouseService.getWarehousesWhereIsActive(false);
    }

    /**
     * Endpoint for getting a list of all warehouses
     * @return List\<Warehouse\>
     */
    @GetMapping(produces = "application/json; charset=UTF-8")
    public List<Warehouse> getAllWarehouses(){
        return warehouseService.getAllWarehouses();
    }

    /**
     * Endpoint for ordering Warehouses by <b>country, city, street, post_code, name</b> in asc or desc direction.
     * @param orderBy - country, city, street, post_code, name
     * @param direction - asc or desc
     * @return - List\<Warehouse\>
     */
    @GetMapping({"/ordered_by/{orderBy}/{direction}", "/ordered_by/{orderBy}"})
    public List<Warehouse> getAllWarehousesOrderedBy(@PathVariable String orderBy, @PathVariable String direction){
        return warehouseService.getAllWarehousesOrderedBy(orderBy, direction);
    }

    /**
     * Searching of Warehouse by city, country, email, name, street, post_code
     * @param searchString - string given by user
     * @return List\<Warehouse\>
     */
    @GetMapping("/search/{searchString}")
    public List<Warehouse> searchWarehouseByString(@PathVariable String searchString){
        return warehouseService.search(searchString);
    }

    /**
     * Endpoint for getting particular Warehouse if it is connected with User
     * @param id  - id of Warehouse
     * @return chosen Warehouse if accessible.
     */
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

    /**
     *
     * @param warehouse posted by Admin to be updated
     * @param bindingResult to check conditions
     * @return Updated Warehouse whether it was successfully updated with status 200.
     * Warehouse entity sent by Admin with status 422 if it wasn't updated.
     */
    @SuppressWarnings("Duplicates")
    @PutMapping(headers="Accept=application/json")
    public ResponseEntity<Warehouse> updateWarehouse(@RequestBody @Valid Warehouse warehouse, BindingResult bindingResult){
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

    /**
     * Endpoint for creation new Warehouse in DB.
     * @param warehouse which was sent by Admin to be persisted in DB.
     * @param bindingResult to check conditions.
     * @return Status 201 if Warehouse was successfully saved. Status 422 if it wasn't.
     */
    @SuppressWarnings("Duplicates")
    @PostMapping(headers="Accept=application/json")
    public ResponseEntity<Warehouse> createWarehouse(@RequestBody @Valid Warehouse warehouse, BindingResult bindingResult){
        log.info("Try to create warehouse with Name: {}, from: {}", warehouse.getName(), warehouse.getCountry());
        if(bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(warehouse);
        }
        warehouseService.save(warehouse);
        return ResponseEntity.status(201).build();
    }

}

