package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.trucktimetable.TTTEnum;
import pl.com.xdms.domain.trucktimetable.TTTStatus;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.truck.TruckService;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * Created on 09.04.2020
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping
public class TruckTimeTableController {

    private final TruckService truckService;
    private final WarehouseService warehouseService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public TruckTimeTableController(TruckService truckService, WarehouseService warehouseService, RequestErrorService requestErrorService) {
        this.truckService = truckService;
        this.warehouseService = warehouseService;
        this.requestErrorService = requestErrorService;
    }

    @GetMapping("{wh_url}/ttt/{tttArrivalDatePlan:^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]?$}")
    public List<TruckTimeTable> getTttForCertainWarehouseAccordingDate(@PathVariable String wh_url, @PathVariable String tttArrivalDatePlan){
        Warehouse warehouse = warehouseService.getWarehouseByUrl(wh_url);
        return truckService.getTttService().getTttByWarehouseAndDay(warehouse, tttArrivalDatePlan);
    }

    @GetMapping("ttt/{id}")
    public ResponseEntity<TruckTimeTable> getTruckTimeTableById(@PathVariable Long id){
        TruckTimeTable truckTimeTable = truckService.getTttService().getTttById(id);
        if(truckTimeTable != null){
            log.info("TruckTimeTable was found {}", truckTimeTable);
            return ResponseEntity.ok(truckTimeTable);
        } else {
            log.warn("TruckTimeTable with id: {} not found, returning error", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint for creation new  TruckTimeTable using usual form in app. Depends on mandatory fields to be provided
     * before saving entity.
     * @param truckTimeTable -TruckTimeTable entity to be persisted in DataBase
     * @return TruckTimeTable saved in Database with id. Or 422 page with provided TruckTimeTable in request if there
     * error will be found.
     */
    @PostMapping("ttt/create")
    public ResponseEntity<TruckTimeTable> createTruckTimeTable(@RequestBody TruckTimeTable truckTimeTable) {
        Warehouse warehouse = null;
        if (truckTimeTable.getWarehouse() != null){
            warehouse = warehouseService.getWarehouseById(truckTimeTable.getWarehouse().getWarehouseID());
            truckTimeTable.setWarehouse(warehouse);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime dateTime = LocalDateTime.parse(truckTimeTable.getTttArrivalDatePlan(), formatter);
        TTTStatus tttStatus = truckService.getTttService().getTttStatusByEnum(TTTEnum.PENDING);

        if (dateTime.isBefore(LocalDateTime.now())) {
            tttStatus = truckService.getTttService().getTttStatusByEnum(TTTEnum.DELAYED);
        }
        truckTimeTable.setTttStatus(tttStatus);
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        Set<ConstraintViolation<TruckTimeTable>> constraintValidator = validator.validate(truckTimeTable);

        if(!constraintValidator.isEmpty() || warehouse == null){
            StringBuilder b = new StringBuilder();
            constraintValidator.forEach(b::append);
            return ResponseEntity.status(422).header("Error message", b.toString()).body(truckTimeTable);
        }
        log.info("Try to create TruckTimeTable with TruckName: {}, for Warehouse: {}", truckTimeTable.getTruckName(), warehouse.getName());
        return ResponseEntity.status(201).body(truckService.getTttService().save(truckTimeTable));
    }

    @DeleteMapping("ttt/delete/{id}")
    public ResponseEntity<String> deleteTruckTimeTable(@PathVariable Long id){
        TruckTimeTable truckTimeTable = truckService.getTttService().getTttById(id);
        if(truckTimeTable == null){
            log.warn("TTT with id: {} not found, returning error", id);
            return ResponseEntity.notFound().build();
        } else {
            if(truckService.deleteTtt(truckTimeTable)){
                log.warn("TTT with id: {} was removed in Database in Warehouse {} (code {})", id, truckTimeTable.getWarehouse().getName(), truckTimeTable.getWarehouse().getUrlCode());
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(422).build();
        }
    }

    @PutMapping("ttt/update")
    public ResponseEntity<TruckTimeTable> updateTruckTimeTable(@RequestBody TruckTimeTable truckTimeTable){
        TruckTimeTable tttFromDataBase = truckService.getTttService().getTttById(truckTimeTable.getTttID());
        if (tttFromDataBase == null) {
            log.warn("TTT with id: {} not found, returning error", truckTimeTable.getTttID());
            return ResponseEntity.notFound().build();
        }else if(tttFromDataBase.getTttStatus().getTttStatusName().equals(TTTEnum.ARRIVED) ||
                LocalDateTime.parse(tttFromDataBase.getTttArrivalDatePlan()).isBefore(LocalDateTime.now())) {
            log.info("TTT with id: {} has status ARRIVED or DELAYED and couldn't be changed: {}", tttFromDataBase.getTttID(), tttFromDataBase.getTttStatus().getTttStatusName());
            return ResponseEntity.status(412).build(); //PRECONDITION_FAILED
        }
            tttFromDataBase.setTttArrivalDatePlan(truckTimeTable.getTttArrivalDatePlan());
            tttFromDataBase.setTruckName(truckTimeTable.getTruckName());
            truckService.getTttService().save(tttFromDataBase);
            return ResponseEntity.status(200).build(); //OK status
    }
}
