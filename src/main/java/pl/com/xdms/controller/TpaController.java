package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.tpa.TpaDaysSetting;
import pl.com.xdms.domain.tpa.WorkingDay;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.truck.TruckService;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final RequestErrorService requestErrorService;

    @Autowired
    public TpaController(TruckService truckService, WarehouseService warehouseService, RequestErrorService requestErrorService) {
        this.truckService = truckService;
        this.warehouseService = warehouseService;
        this.requestErrorService = requestErrorService;
    }

    /**
     * Used to get all the TPA for certain warehouse from particular date.
     * @param wh_url - Url code of the Warehouse
     * @param tpaDepartureDatePlan - Date given in format YYYY-MM-DD
     * @return List of TPAs
     */
    @GetMapping("{wh_url}/tpa/{tpaDepartureDatePlan:^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]?$}")
    public ResponseEntity<List<TPA>> getTpaForCertainWarehouseAccordingDate(@PathVariable String wh_url, @PathVariable String tpaDepartureDatePlan) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(wh_url);
        if (warehouse == null){
            return ResponseEntity.notFound().header("Error:", String.format("The Warehouse with url-code:\"%s\" wasn't found", wh_url)).build();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(tpaDepartureDatePlan);
        } catch (ParseException pe) {
            return ResponseEntity.badRequest().header("Error:", String.format("Given Date is not correct %s", tpaDepartureDatePlan)).build();
        }
        return ResponseEntity.ok(truckService.getTpaService().getTpaByWarehouseAndDay(warehouse, tpaDepartureDatePlan));
    }

    /**
     * Used for getting one particular TPA by its ID in Database
     * @param id - Path variable for searching the the TPA
     * @return - ResponseEntity with TPA inside and status 200 if it was found ot Empty and status 404 if wasn't.
     */
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
     *
     * @param tpaUpdated    - The TPA from Request given by the User
     * @param bindingResult - BindingResult entity to catch if there any Errors in conditions of TPA parameters.
     * @return ResponseEntity - response codes with messages:
     *          - 200 - when the given TPA entity passed all the required conditions;
     *          - 404 - if the ID of the given Entity wasn't find in DB;
     *          - 412 - if given entity doesn't correspond conditions of parameters annotation in TPA class;
     *          - 422 - if Dates of The DeparturePlan or Real are in the Past.
     */
    @PutMapping("tpa/update")
    public ResponseEntity<TPA> updateTpa(@RequestBody @Valid TPA tpaUpdated, BindingResult bindingResult) {
        HttpHeaders headers = new HttpHeaders();
        Long id = tpaUpdated.getTpaID();
        if (id != null) {
            TPA tpaToUpdate = truckService.getTpaService().getTpaById(id);
            String message = "Message:";
            //if TPA doesn't exist
            if (tpaToUpdate == null) {
                log.info("Given TPA does not exist in DB and could not be updated");
                headers.set(message, "Given TPA does not exist in DB and could not be updated");
                return ResponseEntity.notFound().headers(headers).build();
            } else if (bindingResult.hasErrors()) {
                //if given entity doesn't correspond conditions of parameters annotation in TPA class
                headers = requestErrorService.getErrorHeaders(bindingResult);
                return ResponseEntity.status(412).headers(headers).body(tpaUpdated);
            } else if (tpaToUpdate.getStatus().getStatusName().equals(TPAEnum.CLOSED) ||
                    LocalDateTime.parse(tpaUpdated.getDeparturePlan()).isBefore(LocalDateTime.now())) {
                //if given dates are in the past response with badRequest
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

    /**
     * Endpoint dedicated to manual creation of The TPA by user using web form. The ETD date shouldn't in the Past.
     * Response status:
     *          - 422 - If provided ETD date is in the Past;
     *          - 412 - if BindingResult has errors;
     *          - 200 - if provided Entity meets the conditions.
     * The Entity will get status IN_PROGRESS if the ETD has the same day as the current date. If the ETD in future, the
     * status will be BUFFER.
     * @param tpaToCreate - TPA entity received from the user
     * @param bindingResult - BindingResult entity to catch if there any Errors in conditions of TPA parameters.
     * @return ResponseEntity with http header "Message" or "Error".
     */
    @PostMapping("tpa/create")
    public ResponseEntity<TPA> createTpa(@RequestBody @Valid TPA tpaToCreate, BindingResult bindingResult){
        HttpHeaders headers = new HttpHeaders();
        if (bindingResult.hasErrors()) {
            //if given entity doesn't correspond conditions of parameters annotation in TPA class
            headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(412).headers(headers).body(tpaToCreate);
        }
        if(LocalDateTime.parse(tpaToCreate.getDeparturePlan()).isBefore(LocalDateTime.now())){
            headers.set("Error:", "The ETD of the TPA is in the Past.");
            return ResponseEntity.status(422).headers(headers).body(tpaToCreate);
        }
        if(LocalDate.parse(tpaToCreate.getDeparturePlan().substring(0,10)).equals(LocalDate.now())){
            tpaToCreate.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.IN_PROGRESS));
        } else {
            tpaToCreate.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.BUFFER));
        }
        TPA tpaSaved = truckService.getTpaService().save(tpaToCreate);
        log.info("TPA with id={} was created and added to Database", tpaSaved.getTpaID());
        headers.set("Message:", String.format("The TPA name=%s was successfully saved in Warehouse %s", tpaSaved.getName(), tpaSaved.getTpaDaysSetting().getWhCustomer().getWarehouse().getName()));
        return ResponseEntity.status(200).headers(headers).body(tpaSaved);
    }

    /**
     * Endpoint dedicated for receiving List of schedule of outbound trucks from certain warehouse to particular Customer
     * depending of WorkingDay.
     * @param whCustomerId - Long Id of the WhCustomer
     * @param workingDayId - Long Id of the Working Day
     * @return List Of TpaDaySetting for given parameters. Response status:
     *          - 200 - if given Parameters were given in accordance of conditions, and return if any TpaDaySettings
     *                  were found;
     *          - 404 - if WhCustomer id, or id of the WorkingDay were not found in DB;
     *          - 422 - if received WhCustomer entity has status isActive = false.
     */
    @GetMapping("tpa_settings/{whCustomerId:^[0-9]*$}/{workingDayId:^[0-9]*$}")
    public ResponseEntity<List<TpaDaysSetting>> getAllTpaDaysSettingsByWhCustomerIdAndWorkingDayId(@PathVariable Long whCustomerId, @PathVariable Long workingDayId){
        WhCustomer whCustomer = warehouseService.getWhCustomerById(whCustomerId);
        WorkingDay workingDay = warehouseService.getWorkingDayById(workingDayId);
        if(whCustomer == null || workingDay == null){
            log.info("The wrong parameters were passed to request whCustomerId={}, workingDayId={}; WhCustomer is null:{}", whCustomerId, workingDayId, whCustomer == null);
            return ResponseEntity.notFound().header("Error:", "Wrong parameters passed to request").build();
        }
        if(!whCustomer.getIsActive()){
            return ResponseEntity.unprocessableEntity().header("Warning:", String.format("The Connection of Warehouse=%s and Customer=%s is not active", whCustomer.getWarehouse().getName(), whCustomer.getCustomer().getName())).build();
        }
        List<TpaDaysSetting> tpaDaysSettings = truckService.getTpaDaysSettingsService().getTpaDaySettingsByWhCustomerAndWorkingDay(whCustomer, workingDay);
        return ResponseEntity.ok(tpaDaysSettings);
    }
}