package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.truck.TruckService;

import java.util.List;

/**
 * Created on 18.04.2020
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping
public class TpaController {

    private final TruckService truckService;
    private final WarehouseService warehouseService;

    @Autowired
    public TpaController(TruckService truckService, WarehouseService warehouseService) {
        this.truckService = truckService;
        this.warehouseService = warehouseService;
    }

    @GetMapping("{wh_url}/tpa/{tpaDepartureDatePlan:^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]?$}")
    public List<TPA> getTpaForCertainWarehouseAccordingDate(@PathVariable String wh_url, @PathVariable String tpaDepartureDatePlan){
        Warehouse warehouse = warehouseService.getWarehouseByUrl(wh_url);
        return truckService.getTpaService().getTpaByWarehouseAndDay(warehouse, tpaDepartureDatePlan);
    }

    @GetMapping("tpa/{id}")
    public ResponseEntity<TPA> getTruckTimeTableById(@PathVariable Long id){
        TPA tpa = truckService.getTpaService().getTpaById(id);
        if(tpa != null){
            log.info("TPA was found {}", tpa);
            return ResponseEntity.ok(tpa);
        } else {
            log.warn("TPA with id: {} not found, returning error", id);
            return ResponseEntity.notFound().build();
        }
    }
}
