package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.truck.TruckService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Created on 18.04.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping
public class TpaController {

    private final TruckService truckService;
    private final WarehouseService warehouseService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public TpaController(TruckService truckService, WarehouseService warehouseService, RequestErrorService requestErrorService) {
        this.truckService = truckService;
        this.warehouseService = warehouseService;
        this.requestErrorService = requestErrorService;
    }

    @GetMapping("{wh_url}/tpa/{tpaDepartureDatePlan:^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]?$}")
    public List<TPA> getTpaForCertainWarehouseAccordingDate(@PathVariable String wh_url, @PathVariable String tpaDepartureDatePlan) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(wh_url);
        return truckService.getTpaService().getTpaByWarehouseAndDay(warehouse, tpaDepartureDatePlan);
    }

    @GetMapping("tpa/{id}")
    public ResponseEntity<TPA> getTruckTimeTableById(@PathVariable Long id) {
        TPA tpa = truckService.getTpaService().getTpaById(id);
        if (tpa != null) {
            log.info("TPA was found {}", tpa);
            return ResponseEntity.ok(tpa);
        } else {
            log.warn("TPA with id: {} not found, returning error", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint dedicated for updating of the TPA only if the TPA has't status CLOSED.
     * It is possible to update only next parameters: Name, DeparturePlan.
     * @param tpaUpdated    - The TPA from Request given by the User
     * @param bindingResult - BindingResult entity to catch if there any Errors in conditions of TPA parameters.
     * @return ResponseEntity - response codes with messages:
     * - 200 - when the given TPA entity passed all the required conditions;
     * - 404 - if the ID of the given Entity wasn't find in DB;
     * - 412 - if given entity doesn't correspond conditions of parameters annotation in TPA class;
     * - 422 - if Dates of The DeparturePlan or Real are in the Past.
     */
    @PutMapping("tpa/update")
    public ResponseEntity<TPA> updateTpa(@RequestBody @Valid TPA tpaUpdated, BindingResult bindingResult) {
        HttpHeaders headers = new HttpHeaders();
        Long id = tpaUpdated.getTpaID();
        if(id != null){
            TPA tpaToUpdate = truckService.getTpaService().getTpaById(id);
            String message = "Message:";
            //if TPA doesn't exist
            if (tpaToUpdate == null) {
                log.info("Given TPA does not exist in DB and could not be updated");
                headers.set(message, "Given TPA does not exist in DB and could not be updated");
                return ResponseEntity.notFound().headers(headers).build();
            }
            //if given entity doesn't correspond conditions of parameters annotation in TPA class
            if (bindingResult.hasErrors()) {
                headers = requestErrorService.getErrorHeaders(bindingResult);
                return ResponseEntity.status(412).headers(headers).body(tpaUpdated);
            }
            //if given dates are in the past response with badRequest
            if (tpaToUpdate.getStatus().getStatusName().equals(TPAEnum.CLOSED) ||
                    LocalDateTime.parse(tpaUpdated.getDeparturePlan()).isBefore(LocalDateTime.now())) {
                log.info("TPA with id: {} has status CLOSED and couldn't be changed: {}", id, tpaToUpdate.getStatus().getStatusName());
                log.info("Also check the ETD in given TPA, the Date couldn't be in the past. Is in the Past? : {}", LocalDateTime.parse(tpaUpdated.getDeparturePlan()).isBefore(LocalDateTime.now()));
                headers.set(message, String.format("Given Dates are in the Past or The TPA id=%d is CLOSED", id));
                return ResponseEntity.status(422).headers(headers).build();
            }
            tpaToUpdate.setDeparturePlan(tpaUpdated.getDeparturePlan());
            tpaToUpdate.setName(tpaUpdated.getName());
            truckService.getTpaService().save(tpaToUpdate);
            log.info("TPA with ID={} was successfully updated", id);
            headers.set(message, String.format("TPA with ID=%d was successfully updated", id));
            return ResponseEntity.ok().headers(headers).build();
        }
        return ResponseEntity.notFound().header("ERROR", "Not Existing").build();
    }



}
