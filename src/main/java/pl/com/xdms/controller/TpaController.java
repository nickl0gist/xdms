package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.ManifestReferenceService;
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
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("warehouse/{urlCode:^[a-z_]{5,8}$}")
@PropertySource("classpath:messages.properties")
public class TpaController {

    @Value("${error.http.message}")
    String errorMessage;

    @Value("${message.http.message}")
    String messageMessage;

    private final TruckService truckService;
    private final WarehouseService warehouseService;
    private final RequestErrorService requestErrorService;
    private final ManifestReferenceService manifestReferenceService;

    @Autowired
    public TpaController(TruckService truckService, WarehouseService warehouseService,
                         RequestErrorService requestErrorService, ManifestReferenceService manifestReferenceService) {
        this.truckService = truckService;
        this.warehouseService = warehouseService;
        this.requestErrorService = requestErrorService;
        this.manifestReferenceService = manifestReferenceService;
    }

    /**
     * Used to get all the TPA for certain warehouse from particular date.
     *
     * @param urlCode              - Url code of the Warehouse
     * @param tpaDepartureDatePlan - Date given in format YYYY-MM-DD
     * @return List of TPAs
     */
    @GetMapping("tpa/{tpaDepartureDatePlan:^20[0-9]{2}-[0-1][0-9]-[0-3][0-9]?$}")
    public ResponseEntity<List<TPA>> getTpaForCertainWarehouseAccordingDate(@PathVariable String urlCode, @PathVariable String tpaDepartureDatePlan) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        if (warehouse == null) {
            return ResponseEntity.notFound().header(errorMessage, String.format("The Warehouse with url-code:\"%s\" wasn't found", urlCode)).build();
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(tpaDepartureDatePlan);
        } catch (ParseException pe) {
            return ResponseEntity.badRequest().header(errorMessage, String.format("Given Date is not correct %s", tpaDepartureDatePlan)).build();
        }
        return ResponseEntity.ok(truckService.getTpaService().getTpaByWarehouseAndDay(warehouse, tpaDepartureDatePlan));
    }

    /**
     * Used for getting one particular TPA by its ID in Database
     *
     * @param id - Path variable for searching the the TPA
     * @return - ResponseEntity with TPA inside and status 200 if it was found ot Empty and status 404 if wasn't.
     */
    @GetMapping("tpa/{id:^\\d+$}")
    public ResponseEntity<TPA> getTpaById(@PathVariable Long id, @PathVariable String urlCode) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TPA tpa = truckService.getTpaService().getTpaByWarehouseAndId(id, warehouse);
        if (tpa != null) {
            log.info("TPA was found {}", tpa);
            return ResponseEntity.ok(tpa);
        } else {
            log.warn("TPA with id: {} was not found, in scope of Warehouse {}", id, urlCode);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get All Tpa with status DELAYED for Particular Warehouse
     *
     * @param urlCode - urlCode of the Warehouse
     * @return - List of TPA (List<TPA>)
     */
    @GetMapping("tpa/delayed")
    public List<TPA> getListOfDelayedTpaForWarehouse(@PathVariable String urlCode) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        return truckService.getTpaService().getAllDelayedForWarehouse(warehouse);
    }

    /**
     * Get All Tpa with status BUFFER for Particular Warehouse
     *
     * @param urlCode - urlCode of the Warehouse
     * @return - List of TPA (List<TPA>)
     */
    @GetMapping("tpa/buffer")
    public List<TPA> getListOfTpaInBufferForWarehouse(@PathVariable String urlCode) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        return truckService.getTpaService().getAllBufferedForWarehouse(warehouse);
    }

    /**
     * Get All Tpa with status CLOSED for Particular Warehouse
     *
     * @param urlCode - urlCode of the Warehouse
     * @return - List of TPA (List<TPA>)
     */
    @GetMapping("tpa/closed")
    public List<TPA> getListOfClosedTpaForWarehouse(@PathVariable String urlCode) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        return truckService.getTpaService().getAllClosedForWarehouse(warehouse);
    }

    /**
     * Get All not CLOSED Tpa for Particular Warehouse
     *
     * @param urlCode - urlCode of the Warehouse
     * @return - List of TPA (List<TPA>)
     */
    @GetMapping("tpa/notClosed")
    public List<TPA> getListOfNotClosedTpaForWarehouse(@PathVariable String urlCode) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        return truckService.getTpaService().getAllNotClosedForWarehouse(warehouse);
    }

    /**
     * Get All Tpa with status IN_PROGRESS for Particular Warehouse
     *
     * @param urlCode - urlCode of the Warehouse
     * @return - List of TPA (List<TPA>)
     */
    @GetMapping("tpa/in_progress")
    public List<TPA> getListOfTpaInProgressForWarehouse(@PathVariable String urlCode) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        return truckService.getTpaService().getAllInProgressForWarehouse(warehouse);
    }

    /**
     * Endpoint dedicated for updating of the TPA only if the TPA has't status CLOSED.
     * It is possible to update only next parameters: Name, DeparturePlan.
     *
     * @param tpaUpdated    - The TPA from Request given by the User
     * @param bindingResult - BindingResult entity to catch if there any Errors in conditions of TPA parameters.
     * @return ResponseEntity - response codes with messages:
     * - 200 - when the given TPA entity passed all the required conditions;
     * - 404 - if the ID of the given Entity wasn't find in DB;
     * - 412 - if given entity doesn't correspond conditions of parameters annotation in TPA class;
     * - 422 - if Dates of The DeparturePlan or Real are in the Past.
     */
    @PutMapping("tpa/update")
    public ResponseEntity<TPA> updateTpa(@PathVariable String urlCode, @RequestBody @Valid TPA tpaUpdated, BindingResult bindingResult) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        HttpHeaders headers = new HttpHeaders();
        Long id = tpaUpdated.getTpaID();
        if (id != null) {
            TPA tpaToUpdate = truckService.getTpaService().getTpaByWarehouseAndId(id, warehouse);
            //if TPA doesn't exist
            if (tpaToUpdate == null) {
                log.info("Given TPA does not exist in DB and could not be updated");
                headers.set(messageMessage, "Given TPA does not exist in DB and could not be updated");
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
                headers.set(messageMessage, String.format("Given Dates are in the Past or The TPA id=%d is CLOSED", id));
                return ResponseEntity.status(422).headers(headers).build();
            }
            tpaToUpdate.setDeparturePlan(tpaUpdated.getDeparturePlan());
            tpaToUpdate.setName(tpaUpdated.getName());
            log.info("TPA with ID={} was successfully updated", id);
            return ResponseEntity.ok().header(messageMessage, String.format("TPA with ID=%d was successfully updated", id)).body(truckService.getTpaService().save(tpaToUpdate));
        }
        return ResponseEntity.notFound().header("ERROR", "Not Existing").build();
    }

    /**
     * The endpoint dedicated for ManifestReference split.
     *
     * @param manRefId          - Long Id of the ManifestReference which should be split.
     * @param tpaToId           - Long Id of the TPA where will be placed parts from divided manifest.
     * @param manifestReference - ManifestReference entity which should be placed into TPA with id tpaToId.
     * @return TPA were the changes were made, if they were. Possible response statuses:
     * - 404 - when any Entities of ManifestReference (which should be split) or TPA (where the split parts should be placed)
     * - 403 - if TPA where split part should be taken from or placed to is CLOSED
     * - 400 - if Qty of pcs, pallets or boxes of split manifestReference cannot be greater than origin one.
     * - 200 - when ManifestReference was split successfully.
     */
    @PutMapping("tpa/{tpaFromId:^\\d+$}/split/man_ref/{manRefId:^\\d+$}/tpa_to/{tpaToId:^\\d+$}")
    public ResponseEntity<TPA> splitManifestReferenceToAnotherTpa(@PathVariable Long manRefId, @PathVariable Long tpaToId,
                                                                  @RequestBody ManifestReference manifestReference,
                                                                  @PathVariable String urlCode, @PathVariable Long tpaFromId) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        ManifestReference manifestReferenceToSplit = manifestReferenceService.findById(manRefId);
        TPA tpaTo = truckService.getTpaService().getTpaByWarehouseAndId(tpaToId, warehouse);
        TPA tpaFrom = truckService.getTpaService().getTpaByWarehouseAndId(tpaFromId, warehouse);
        if (manifestReferenceToSplit == null) {
            log.info("The manifest which has to be split with id={} wasn't found", manRefId);
            return ResponseEntity.notFound().header(errorMessage, String.format("The manifest which has to be split with id=%d wasn't found", manRefId)).build();
        }
        if (tpaTo == null || tpaFrom == null) {
            log.info("The TPA with id={} where split part should be assigned does not exist. Or the TPA with id={} where part has to be taken from doesn't exist in scope of Warehouse {}", tpaToId, tpaFromId, urlCode);
            return ResponseEntity.notFound().header(errorMessage, String.format("The TPA with id=%d where split part should be assigned does not exist. Or the TPA with id=%d where part has to be taken from doesn't exist in scope of Warehouse %s", tpaToId, tpaFromId, urlCode)).build();
        }
        if (tpaFrom.getStatus().getStatusName().equals(TPAEnum.CLOSED)) {
            log.info("The TPA with id={} where split part should be taken from is CLOSED", tpaFrom.getTpaID());
            return ResponseEntity.status(403).header(errorMessage, String.format("The TPA with id=%d where split part should be taken from is CLOSED", tpaFrom.getTpaID())).build();
        }
        if (tpaTo.getStatus().getStatusName().equals(TPAEnum.CLOSED)) {
            log.info("The TPA with id={} where split part should be placed to is CLOSED", tpaTo.getTpaID());
            return ResponseEntity.status(403).header(errorMessage, String.format("The TPA with id=%d where split part should be placed to is CLOSED", tpaTo.getTpaID())).build();
        }
        if (!checkIfSplitIsPossible(manifestReferenceToSplit, manifestReference)) {
            log.info("Qty of pcs, pallets or boxes of split manifestReference cannot be greater than origin one!");
            return ResponseEntity.badRequest().header(errorMessage, "Qty of pcs, pallets or boxes of split manifestReference cannot be greater than origin one!").build();
        }

        ManifestReference manifestReferenceToSave = manifestReferenceService.split(manifestReferenceToSplit, manifestReference);
        manifestReferenceToSplit = manifestReferenceService.update(manifestReferenceToSplit, manifestReferenceToSave);

        tpaTo.getManifestReferenceSet().add(manifestReferenceToSave);
        truckService.getTpaService().save(tpaTo);

        log.info("ManifestReference with id={} was split to ManifestReference with id={}", manifestReferenceToSplit.getManifestReferenceId(), manifestReferenceToSave.getManifestReferenceId());
        return ResponseEntity.ok().header(messageMessage, String.format("ManifestReference with id=%d was successfully placed in TPA id=%d", manifestReferenceToSave.getManifestReferenceId(), tpaToId)).body(manifestReferenceToSplit.getTpa());
    }

    private boolean checkIfSplitIsPossible(ManifestReference manifestReferenceToSplit, ManifestReference manifestReference) {
        return manifestReferenceToSplit.getQtyReal() > manifestReference.getQtyReal() &&
                manifestReferenceToSplit.getPalletQtyReal() > manifestReference.getPalletQtyReal() &&
                manifestReferenceToSplit.getBoxQtyReal() > manifestReference.getBoxQtyReal();
    }

    /**
     * Endpoint dedicated to manual creation of The TPA by user using web form. The ETD date shouldn't in the Past.
     * The Entity will get status IN_PROGRESS if the ETD has the same day as the current date. If the ETD in future, the
     * status will be BUFFER.
     *
     * @param tpaToCreate   - TPA entity received from the user
     * @param bindingResult - BindingResult entity to catch if there any Errors in conditions of TPA parameters.
     * @return ResponseEntity with http header "Message" or "Error".
     * Response status:
     * - 422 - If provided ETD date is in the Past;
     * - 412 - if BindingResult has errors;
     * - 200 - if provided Entity meets the conditions.
     */
    @PostMapping("tpa/create")
    public ResponseEntity<TPA> createTpa(@PathVariable String urlCode, @RequestBody @Valid TPA tpaToCreate, BindingResult bindingResult) {
        HttpHeaders headers = new HttpHeaders();
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        if(!tpaToCreate.getTpaDaysSetting().getWhCustomer().getWarehouse().equals(warehouse)){
            return ResponseEntity.status(404).header(errorMessage, String.format("Given TPA is could not be saved out of scope of Warehouse %s", urlCode)).body(tpaToCreate);
        }
        if (bindingResult.hasErrors()) {
            //if given entity doesn't correspond conditions of parameters annotation in TPA class
            headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(412).headers(headers).body(tpaToCreate);
        }
        if (LocalDateTime.parse(tpaToCreate.getDeparturePlan()).isBefore(LocalDateTime.now())) {
            headers.set(errorMessage, "The ETD of the TPA is in the Past.");
            return ResponseEntity.status(422).headers(headers).body(tpaToCreate);
        }
        if (LocalDate.parse(tpaToCreate.getDeparturePlan().substring(0, 10)).equals(LocalDate.now())) {
            tpaToCreate.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.IN_PROGRESS));
        } else {
            tpaToCreate.setStatus(truckService.getTpaService().getTpaStatusByEnum(TPAEnum.BUFFER));
        }
        TPA tpaSaved = truckService.getTpaService().save(tpaToCreate);
        log.info("TPA with id={} was created and added to Database", tpaSaved.getTpaID());
        headers.set(messageMessage, String.format("The TPA name=%s was successfully saved in Warehouse %s", tpaSaved.getName(), tpaSaved.getTpaDaysSetting().getWhCustomer().getWarehouse().getName()));
        return ResponseEntity.status(200).headers(headers).body(tpaSaved);
    }

    /**
     * Endpoint for removing TPA from Database by given Id. Removing is only possible when the TPA doesn't have status
     * CLOSED and the ManifestSet doesn't contain any Manifests or ManifestReferences inside.
     *
     * @param id - Long id of TPA
     * @return Empty body with Status 204 if the removing was successful. Otherwise if the TPA will be found it will be
     * included to body response. Response status:
     * - 204 - if the removing was successful;
     * - 417 - if TPA Manifest set or ManifestReference are not empty;
     * - 403 - if TPA is CLOSED;
     * - 404 - if TPA wasn't found.
     */
    @DeleteMapping("tpa/{id:^\\d+$}")
    public ResponseEntity<TPA> deleteTpa(@PathVariable String urlCode, @PathVariable Long id) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TPA tpa = truckService.getTpaService().getTpaByWarehouseAndId(id, warehouse);
        HttpHeaders headers = new HttpHeaders();
        if (tpa == null) {
            log.info("TPA with id={} wasn't found in scope of Warehouse {}", id, urlCode);
            headers.add(errorMessage, String.format("TPA with id=%d wasn't found in scope of Warehouse %s", id, urlCode));
            return ResponseEntity.notFound().headers(headers).build();
        } else if (!tpa.getManifestSet().isEmpty() || !tpa.getManifestReferenceSet().isEmpty()) {
            log.info("TPA with id={} has not empty set of Manifests or References and couldn't be deleted", id);
            headers.add(errorMessage, String.format("TPA with id=%d has not empty set of Manifests or References and couldn't be deleted", id));
            return ResponseEntity.status(417).headers(headers).build();//Expectation Failed
        } else if (tpa.getStatus().getStatusName().equals(TPAEnum.CLOSED)) {
            log.info("TPA with id={} has status CLOSED couldn't be deleted", id);
            headers.add(errorMessage, String.format("TPA with id=%d has status CLOSED and couldn't be deleted", id));
            return ResponseEntity.status(403).headers(headers).build();
        } else {
            truckService.getTpaService().removeTpaById(id);
            log.info("TPA with id={} was successfully deleted", id);
            headers.add(messageMessage, String.format("TPA with id=%d was successfully deleted", id));
            return ResponseEntity.status(204).headers(headers).build();// No Content
        }
    }
}
