package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.trucktimetable.TTTEnum;
import pl.com.xdms.domain.trucktimetable.TTTStatus;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.truck.TruckService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Created on 09.04.2020
 *
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

    /**
     * Endpoint for getting all TTT for certain warehouse and particular day
     *
     * @param wh_url             - wh-code of the warehouse
     * @param tttArrivalDatePlan - date when the TTT should arrive to this warehouse
     * @return List of TruckTimeTable entities.
     */
    @GetMapping("{wh_url}/ttt/{tttArrivalDatePlan:^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]?$}")
    public List<TruckTimeTable> getTttForCertainWarehouseAccordingDate(@PathVariable String wh_url, @PathVariable String tttArrivalDatePlan) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(wh_url);
        return truckService.getTttService().getTttByWarehouseAndDay(warehouse, tttArrivalDatePlan);
    }

    /**
     * Endpoint of get request for the certaing TTT entity which is being searched by its ID.
     *
     * @param id of the TTT in Database
     * @return TruckTimeTable and status 200 if found. 404 if will not.
     */
    @GetMapping("ttt/{id}")
    public ResponseEntity<TruckTimeTable> getTruckTimeTableById(@PathVariable Long id) {
        TruckTimeTable truckTimeTable = truckService.getTttService().getTttById(id);
        if (truckTimeTable != null) {
            log.info("TruckTimeTable was found {}", truckTimeTable);
            return ResponseEntity.ok(truckTimeTable);
        } else {
            log.warn("TruckTimeTable with id: {} not found, returning error", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint for creation new TruckTimeTable using usual form in app. Depends on mandatory fields to be provided
     * before saving entity.
     *
     * @param truckTimeTable -TruckTimeTable entity to be persisted in DataBase
     * @return TruckTimeTable saved in Database with id. Or 412 page with provided TruckTimeTable in request if there
     * error will be found.
     */
    @PostMapping("ttt/create")
    public ResponseEntity<TruckTimeTable> createTruckTimeTable(@RequestBody @Valid TruckTimeTable truckTimeTable, BindingResult bindingResult) {

        if (truckTimeTable.getWarehouse() == null) {
            return ResponseEntity.unprocessableEntity().body(truckTimeTable);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        LocalDateTime dateTime = LocalDateTime.parse(truckTimeTable.getTttArrivalDatePlan(), formatter);
        TTTStatus tttStatus = truckService.getTttService().getTttStatusByEnum(TTTEnum.PENDING);
        if (dateTime.isBefore(LocalDateTime.now())) {
            tttStatus = truckService.getTttService().getTttStatusByEnum(TTTEnum.DELAYED);
        }
        //if given entity doesn't correspond conditions of parameters annotation in TTT class
        if (bindingResult.hasErrors()) {
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(412).headers(headers).body(truckTimeTable);
        }
        truckTimeTable.setTttStatus(tttStatus);
        log.info("Try to create TruckTimeTable with TruckName: {}, for Warehouse: {}", truckTimeTable.getTruckName(), truckTimeTable.getWarehouse().getName());
        return ResponseEntity.status(201).body(truckService.getTttService().save(truckTimeTable));
    }

    /**
     * Endpoint for Delete request of the TTT. ID parameter will be used to find the TTT in DB.
     *
     * @param id - Long ID of the TTT in the Database.
     * @return 200 - if the TTT was removed successfully;
     * 404 - if TTT wasn't found by ID;
     * 422 - If the TTT has status arrived;
     * 200 - if TTT was successfully removed
     * 400 - if there were other reasons the TTT wasn't removed.
     */
    @DeleteMapping("ttt/delete/{id}")
    public ResponseEntity<String> deleteTruckTimeTable(@PathVariable Long id) {
        TruckTimeTable truckTimeTable = truckService.getTttService().getTttById(id);
        HttpHeaders headers = new HttpHeaders();
        if (truckTimeTable == null) {
            log.info("TTT with id: {} not found, returning error", id);
            headers.set("Message", "TTT Not Found");
            return ResponseEntity.notFound().headers(headers).build();//404
        } else if (truckTimeTable.getTttStatus().getTttStatusName().equals(TTTEnum.ARRIVED)) {
            log.info("The TTT {} has already arrived and cannot be removed", truckTimeTable.getTruckName());
            headers.set("Message", String.format("TTT with id=%d has status Arrived", id));
            return ResponseEntity.status(422).headers(headers).build();
        } else if (truckService.deleteTtt(truckTimeTable)) {
            log.info("TTT with id: {} was removed in Database in Warehouse {} (code {})", id,
                    truckTimeTable.getWarehouse().getName(), truckTimeTable.getWarehouse().getUrlCode());
            headers.set("Message", String.format("TTT with id=%d was successfully removed.", id));
            return ResponseEntity.ok().headers(headers).build(); //200
        }
        headers.set("Message", "TTT could not be deleted. Check Manifests from this TTT");
        return ResponseEntity.badRequest().headers(headers).build(); // 400
    }

    /**
     * Update Truck Time update EndPoint. ID parameter of TTT will be used to find it in DB
     *
     * @param truckTimeTable - The entity of TTT which should be updated.
     * @return Response Entity with codes:
     * - 404 - if the Entity wasn't found in DB by given ID;
     * - 412 - if TTT doesn't correspond the conditions defined in TruckTimeTable class;
     * - 422 - in Case when given TTT has status Arrived or Delayed, or given Date of arrival is in the Past;
     * - 200 - if TTT was updated successfully.
     */
    @PutMapping("ttt/update")
    public ResponseEntity<TruckTimeTable> updateTruckTimeTable(@RequestBody @Valid TruckTimeTable truckTimeTable, BindingResult bindingResult) {
        HttpHeaders headers = new HttpHeaders();
        Long id = truckTimeTable.getTttID();
        if (id != null) {
            TruckTimeTable tttFromDataBase = truckService.getTttService().getTttById(truckTimeTable.getTttID());
            if (tttFromDataBase == null) {
                log.warn("TTT with id: {} not found, returning error", truckTimeTable.getTttID());
                headers.set("Error:", String.format("TTT with id=%d not found, returning error", id));
                return ResponseEntity.notFound().headers(headers).build(); // 404
            } else if (bindingResult.hasErrors()){
                //if given entity doesn't correspond conditions of parameters annotation in TTT class
                headers = requestErrorService.getErrorHeaders(bindingResult);
                return ResponseEntity.status(412).headers(headers).body(truckTimeTable); //PRECONDITION_FAILED
            } else if (tttFromDataBase.getTttStatus().getTttStatusName().equals(TTTEnum.ARRIVED) ||
                    LocalDateTime.parse(truckTimeTable.getTttArrivalDatePlan()).isBefore(LocalDateTime.now())) {
                log.info("TTT with id: {} has status ARRIVED and couldn't be changed: {}", tttFromDataBase.getTttID(), tttFromDataBase.getTttStatus().getTttStatusName());
                log.info("Also check the ETA in given TTT, the Date couldn't be in the past. Is in the Past? : {}", LocalDateTime.parse(truckTimeTable.getTttArrivalDatePlan()).isBefore(LocalDateTime.now()));
                return ResponseEntity.status(422).header("Error:", String.format("TTT id=%d has status ARRIVED or ETA date is in the Past", id)).build(); //UNPROCESSABLE_ENTITY
            }
            tttFromDataBase.setTttArrivalDatePlan(truckTimeTable.getTttArrivalDatePlan());
            tttFromDataBase.setTruckName(truckTimeTable.getTruckName());
            truckService.getTttService().save(tttFromDataBase);
            return ResponseEntity.status(200).header("Message:", String.format("TTT with id=%d was successfully updated", id)).build(); //OK
        }
        return ResponseEntity.notFound().header("ERROR", "Not Existing").build();
    }
}