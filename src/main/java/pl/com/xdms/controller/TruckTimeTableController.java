package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
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
@RequestMapping("warehouse/{urlCode:^[a-z_]{5,8}$}")
@PropertySource("classpath:messages.properties")
public class TruckTimeTableController {

    @Value("${error.http.message}")
    String errorMessage;

    @Value("${message.http.message}")
    String messageMessage;

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
     * @param urlCode             - url code of the warehouse
     * @param tttArrivalDatePlan - date when the TTT should arrive to this warehouse
     * @return List of TruckTimeTable entities.
     */
    @GetMapping("/ttt/{tttArrivalDatePlan:^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]?$}")
    public List<TruckTimeTable> getTttForCertainWarehouseAccordingDate(@PathVariable String urlCode, @PathVariable String tttArrivalDatePlan) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        return truckService.getTttService().getTttByWarehouseAndDay(warehouse, tttArrivalDatePlan);
    }

    /**
     * Endpoint of get request for the certain TTT entity which is being searched by its ID.
     *
     * @param id of the TTT in Database
     * @return TruckTimeTable and status 200 if found. 404 if will not.
     */
    @GetMapping("ttt/{id:^\\d+$}")
    public ResponseEntity<TruckTimeTable> getTruckTimeTableById(@PathVariable String urlCode, @PathVariable Long id) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TruckTimeTable truckTimeTable = truckService.getTttService().getTTTByWarehouseAndId(id, warehouse);
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
    public ResponseEntity<TruckTimeTable> createTruckTimeTable(@PathVariable String urlCode, @RequestBody @Valid TruckTimeTable truckTimeTable, BindingResult bindingResult) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        if (warehouse == null) {
            return ResponseEntity.unprocessableEntity().body(truckTimeTable);
        }
        truckTimeTable.setWarehouse(warehouse);
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
        log.info("Try to create TruckTimeTable with TruckName: {}, for Warehouse: {}", truckTimeTable.getTruckName(), warehouse.getName());
        return ResponseEntity.status(201).body(truckService.getTttService().save(truckTimeTable));
    }

    /**
     * Endpoint for Delete request of the TTT. ID parameter will be used to find the TTT in DB.
     *
     * @param id - Long ID of the TTT in the Database.
     * @return ResponseEntity with http status
     * - 200 - if the TTT was removed successfully;
     * - 404 - if TTT wasn't found by ID;
     * - 422 - If the TTT has status ARRIVED;
     * - 200 - if TTT was successfully removed
     * - 400 - if there were other reasons the TTT wasn't removed.
     */
    @DeleteMapping("ttt/delete/{id:^\\d+$}")
    public ResponseEntity<String> deleteTruckTimeTable(@PathVariable String urlCode, @PathVariable Long id) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TruckTimeTable truckTimeTable = truckService.getTttService().getTTTByWarehouseAndId(id, warehouse);

        HttpHeaders headers = new HttpHeaders();
        if (truckTimeTable == null) {
            log.info("TTT with id: {} not found, returning error", id);
            headers.set(errorMessage, "TTT Not Found");
            return ResponseEntity.notFound().headers(headers).build();//404
        } else if (truckTimeTable.getTttStatus().getTttStatusName().equals(TTTEnum.ARRIVED)) {
            log.info("The TTT {} has already arrived and cannot be removed", truckTimeTable.getTruckName());
            headers.set(messageMessage, String.format("TTT with id=%d has status Arrived", id));
            return ResponseEntity.status(422).headers(headers).build();
        } else if (truckService.deleteTtt(truckTimeTable)) {
            log.info("TTT with id: {} was removed in Database in Warehouse {} (code {})", id,
                    truckTimeTable.getWarehouse().getName(), truckTimeTable.getWarehouse().getUrlCode());
            headers.set(messageMessage, String.format("TTT with id=%d was successfully removed.", id));
            return ResponseEntity.ok().headers(headers).build(); //200
        }
        headers.set(errorMessage, "TTT could not be deleted. Check Manifests from this TTT");
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
    public ResponseEntity<TruckTimeTable> updateTruckTimeTable(@PathVariable String urlCode, @RequestBody @Valid TruckTimeTable truckTimeTable, BindingResult bindingResult) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        HttpHeaders headers = new HttpHeaders();
        Long id = truckTimeTable.getTttID();
        if (id != null) {
            TruckTimeTable tttFromDataBase = truckService.getTttService().getTTTByWarehouseAndId(id, warehouse);
            if (tttFromDataBase == null) {
                log.warn("TTT with id: {} not found, returning error", id);
                headers.set(errorMessage, String.format("TTT with id=%d not found, returning error", id));
                return ResponseEntity.notFound().headers(headers).build(); // 404
            } else if (bindingResult.hasErrors()){
                //if given entity doesn't correspond conditions of parameters annotation in TTT class
                headers = requestErrorService.getErrorHeaders(bindingResult);
                return ResponseEntity.status(412).headers(headers).body(truckTimeTable); //PRECONDITION_FAILED
            } else if (tttFromDataBase.getTttStatus().getTttStatusName().equals(TTTEnum.ARRIVED) ||
                    LocalDateTime.parse(truckTimeTable.getTttArrivalDatePlan()).isBefore(LocalDateTime.now())) {
                log.info("TTT with id: {} has status ARRIVED and couldn't be changed: {}", id, tttFromDataBase.getTttStatus().getTttStatusName());
                log.info("Also check the ETA in given TTT, the Date couldn't be in the past. Is in the Past? : {}", LocalDateTime.parse(truckTimeTable.getTttArrivalDatePlan()).isBefore(LocalDateTime.now()));
                return ResponseEntity.status(422).header(errorMessage, String.format("TTT id=%d has status ARRIVED or ETA date is in the Past", id)).build(); //UNPROCESSABLE_ENTITY
            }
            tttFromDataBase.setTttArrivalDatePlan(truckTimeTable.getTttArrivalDatePlan());
            tttFromDataBase.setTruckName(truckTimeTable.getTruckName());
            truckService.getTttService().save(tttFromDataBase);
            return ResponseEntity.status(200).header(messageMessage, String.format("TTT with id=%d was successfully updated", id)).build(); //OK
        }
        return ResponseEntity.notFound().header(errorMessage, "Not Existing").build();
    }

}
