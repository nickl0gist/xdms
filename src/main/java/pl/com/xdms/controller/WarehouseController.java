package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.WarehouseService;

import java.util.List;

/**
 * Controller for All users
 * Created on 23.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@CrossOrigin
@Slf4j
@RestController
@RequestMapping("warehouse")
public class WarehouseController {

    private final WarehouseService warehouseService;

    @Autowired
    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
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
     * Endpoint for receiving list of Warehouses which have status <b>isActive=true</b>
     * @return List of active Warehouses which are accessible for current user
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public List<Warehouse> getActiveWarehouses(){
        return warehouseService.getWarehousesWhereIsActive(true);
    }
}
