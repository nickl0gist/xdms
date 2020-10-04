package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.trucktimetable.TTTEnum;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WarehouseManifest;
import pl.com.xdms.service.ManifestService;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.truck.TruckService;

import javax.validation.Valid;
import java.util.List;

/**
 * Created on 03.06.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("warehouse/{urlCode:^[a-z_]{5,8}$}")
@PropertySource("classpath:messages.properties")
public class ManifestController {

    @Value("${error.http.message}")
    String errorMessage;

    @Value("${message.http.message}")
    String messageMessage;

    private final TruckService truckService;
    private final RequestErrorService requestErrorService;
    private final ManifestService manifestService;
    private final WarehouseService warehouseService;


    @Autowired
    public ManifestController(TruckService truckService, RequestErrorService requestErrorService, ManifestService manifestService, WarehouseService warehouseService) {
        this.truckService = truckService;
        this.requestErrorService = requestErrorService;
        this.manifestService = manifestService;
        this.warehouseService = warehouseService;
    }

    /**
     * Endpoint which returns a list with Manifests which currently were not assigned to any TTT to be delivered to any
     * of Warehouses.
     *
     * @return List Of Manifests.
     */
    @GetMapping("ttt/manifests/abandoned")
    public ResponseEntity<List<Manifest>> getManifestsWithoutTtt() {
        List<Manifest> manifests = manifestService.getAllTttAbandonedManifests();
        return ResponseEntity.ok(manifests);
    }

    /**
     * Searching of Manifest by it's id
     *
     * @param manifestId - id of the Manifest in DB
     * @return - Manifest if it was founded, status 404 if wasn't.
     */
    @GetMapping(value = "ttt/{tttId:^\\d+$}/manifest/{manifestId:^\\d+$}", headers = "truck=ttt")
    public ResponseEntity<WarehouseManifest> getManifestById(@PathVariable String urlCode, @PathVariable Long tttId, @PathVariable Long manifestId) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TruckTimeTable ttt = truckService.getTttService().getTTTByWarehouseAndId(tttId, warehouse);
        Manifest manifest = manifestService.findManifestById(manifestId);
        WarehouseManifest warehouseManifest = manifestService.getWarehouseManifestService().findByWarehouseAndManifest(warehouse, manifest);

        if (warehouse == null || ttt == null) {
            return ResponseEntity.badRequest().header(errorMessage, String.format("Warehouse %s doesn't contain TTT with id=%d", urlCode, tttId)).build();
        }
        if (!ttt.getManifestSet().contains(manifest) && warehouseManifest == null) {
            return ResponseEntity.notFound().header(errorMessage, String.format("The manifest with id=%d is not existing", manifestId)).build();
        }
        return ResponseEntity.ok(warehouseManifest);
    }

    /**
     * The endpoint dedicated to adding the the reference to particular manifest
     *
     * @param manifestId        - Long id of the manifest where the reference should be added
     * @param manifestReference - the entity of ManifestReference with all required information
     * @param bindingResult     - BindingResult entity to check annotation conditions in given manifestReference
     * @return Response with Manifest in the body. Possible statuses of response:
     * - 200 - if updating of the manifest was successful;
     * - 404 - if no manifests were found by the given id;
     * - 422 - if any of the annotation conditions in ManifestReference class were violated.
     */
    @PutMapping(value = "ttt/{tttId:^\\d+$}/manifest/{manifestId:^\\d+$}/addReference")//, headers = "truck=ttt")
    public ResponseEntity<Manifest> addReferenceToManifest(@PathVariable String urlCode, @PathVariable Long tttId,
                                                           @PathVariable Long manifestId, @RequestBody @Valid ManifestReference manifestReference,
                                                           BindingResult bindingResult) {

        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TruckTimeTable ttt = truckService.getTttService().getTTTByWarehouseAndId(tttId, warehouse);

        Manifest manifest = manifestService.findManifestById(manifestId);
        if (ttt == null || !ttt.getManifestSet().contains(manifest)) {
            log.info("Manifest with id={} wasn't found", manifestId);
            return ResponseEntity.notFound().header(errorMessage, String.format("Manifest with id=%d wasn't found in TTT=%d in Warehouse %s", manifestId, tttId, urlCode)).build();
        } else if (bindingResult.hasErrors()) {
            //if given entity doesn't correspond conditions of parameters annotation in ManifestReference class
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.unprocessableEntity().headers(headers).body(manifest); //422
        }
        manifest.getManifestsReferenceSet().add(manifestReference);
        manifest = manifestService.save(manifest);
        log.info("Reference {} was added to Manifest {}", manifestReference.getReference().getNumber(), manifest.getManifestCode());
        return ResponseEntity.ok().header(messageMessage, String.format("Reference %s was added to Manifest %s", manifestReference.getReference().getNumber(), manifest.getManifestCode())).body(manifest);
    }

    /**
     * The endpoint dedicated to update the Manifest from DB with information given with Manifest Entity from the request.
     * Only four parameters are allowed to be updated: boxQtyReal, palletQtyReal, TotalLdmReal, TotalWeightReal.
     *
     * @param warehouseManifestUpdated ManifestWarehouse entity given by user in request.
     * @param bindingResult            BindingResult to check if the given Manifest corresponds to annotation conditions.
     * @return Response entity with Manifest in body. Possible cases:
     * - 200 - if The given Entity is corresponding to all required conditions amd updating was successful;
     * - 400 - if id of the given manifest is null;
     * - 404 - if Manifest from Database wasn't found by Id of given manifest;
     * - 412 - if the given manifest does not correspond to annotation conditions of Manifest class
     */
    @PutMapping("ttt/{tttId:^\\d+$}/manifest/update")
    public ResponseEntity<WarehouseManifest> updateManifest(@PathVariable String urlCode, @PathVariable Long tttId,
                                                            @RequestBody @Valid WarehouseManifest warehouseManifestUpdated, BindingResult bindingResult) {

        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TruckTimeTable ttt = truckService.getTttService().getTTTByWarehouseAndId(tttId, warehouse);

        Long id = warehouseManifestUpdated.getManifest().getManifestID();
        Manifest manifestFromDb = id == null ? null : manifestService.findManifestById(id);

        WarehouseManifest warehouseManifestFromDB = manifestService.getWarehouseManifestService().findByWarehouseAndManifest(warehouse, manifestFromDb);

        if (ttt != null && ttt.getManifestSet().contains(manifestFromDb)) {
            //If manifest wasn't found by Id
            if (warehouseManifestFromDB == null) {
                log.info("Given Manifest with id={} does not exist in scope of Warehouse {} and could not be updated", id, urlCode);
                return ResponseEntity.notFound().header(errorMessage, String.format("The manifest with id=%d is not existing in Warehouse %s", id, urlCode)).build();
            } else if (bindingResult.hasErrors()) {
                //if given entity doesn't correspond conditions of parameters annotation in Manifest class
                HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
                return ResponseEntity.status(412).headers(headers).body(warehouseManifestUpdated);
            }
            //If all conditions are Ok Update manifest with given data
            warehouseManifestFromDB = manifestService.updateWarehouseManifest(warehouseManifestUpdated);

            if (!ttt.getTttStatus().getTttStatusName().equals(TTTEnum.ARRIVED))
                truckService.getTttService().setArrive(ttt);

            return ResponseEntity.ok().header(messageMessage, String.format("The Manifest with id=%d was successfully updated", id)).body(warehouseManifestFromDB);
        }
        return ResponseEntity.badRequest().header(errorMessage, "Does not Exist").build();
    }

    /**
     * The endpoint which should be invoke in order to delete Manifest from Database by given Id.
     *
     * @param manifestId - Long id of the Manifest to delete
     * @return ResponseEntity with headers of the result of request.
     * - 200 - if deletion was successful
     * - 422 - if Manifest has information about real quantities of pallets or boxes. The manifest would not be deleted
     * - 404 - if no manifest was found by given Id.
     */
    @DeleteMapping(value = "ttt/{tttId:^\\d+$}/remove_manifest/{manifestId:^\\d+$}")
    public ResponseEntity<TruckTimeTable> deleteManifest(@PathVariable String urlCode, @PathVariable Long tttId, @PathVariable Long manifestId) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TruckTimeTable ttt = truckService.getTttService().getTTTByWarehouseAndId(tttId, warehouse);

        Manifest manifest = manifestService.findManifestById(manifestId);
        WarehouseManifest warehouseManifest = manifestService.getWarehouseManifestByTttAndManifest(ttt, manifest);
        HttpHeaders headers = new HttpHeaders();
        if (ttt == null || !ttt.getManifestSet().contains(manifest) || manifest == null || warehouseManifest == null) {
            log.info("Manifest with id: {} not found in TTT {} for Warehouse {}, returning error", manifestId, tttId, urlCode);
            headers.set(errorMessage, String.format("Manifest with id=%d Not Found in DB", manifestId));
            return ResponseEntity.notFound().headers(headers).build();//404
        } else if (manifest.getPalletQtyReal() != null || manifest.getBoxQtyReal() != null) {
            log.info("Manifest with id: {} has arrived already and couldn't be deleted", manifestId);
            headers.set(messageMessage, String.format("Manifest with id=%d arrived already and couldn't be deleted", manifestId));
            return ResponseEntity.unprocessableEntity().headers(headers).build();//422
        }
        manifestService.removeManifest(manifest, ttt);
        log.info("Manifest with id: {} was deleted", manifestId);
        headers.set(messageMessage, String.format("Manifest %s was removed from TTT with id=%d", manifest.getManifestCode(), tttId));
        return ResponseEntity.ok().headers(headers).body(ttt);//200
    }

    /**
     * Endpoint dedicated to manual creation f Manifest in chosen TTT.
     * The given manifest should correspond to all Annotation conditions declared in Manifest.class.
     *
     * @param tttId         - Long Id of the TTT where tha manifest should be added.
     * @param manifest      - The Manifest Entity.
     * @param bindingResult - BindingResult entity for validation the Manifest.
     * @return Response Entity with http codes of response:
     * - 200 - is the given Manifest meets the conditions;
     * - 404 - if there no TTT was found by given Id;
     * - 409 - if Code of the given manifest is already existing in DB;
     * - 409 - if the Supplier or the Customer in given Manifest has status isActive = false;
     * - 412 - is the given Manifest do not correspond to annotation conditions in Manifest.class.;
     */
    @PostMapping("ttt/{tttId:^\\d+$}")
    public ResponseEntity<Manifest> addManifestInChosenTtt(@PathVariable String urlCode, @PathVariable Long tttId, @RequestBody @Valid Manifest manifest, BindingResult bindingResult) {
        HttpHeaders headers = new HttpHeaders();
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TruckTimeTable ttt = truckService.getTttService().getTTTByWarehouseAndId(tttId, warehouse);

        if (ttt == null) {
            log.info("The TTT with Id={} wasn't found in Warehouse {}", tttId, urlCode);
            headers.add(errorMessage, String.format("The TTT with Id=%d wasn't found in Warehouse %s", tttId, urlCode));
            return ResponseEntity.notFound().headers(headers).build(); // 404
        } else if (bindingResult.hasErrors()) {
            headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(412).headers(headers).body(manifest);
        } else if (manifestService.isManifestExisting(manifest)) {
            log.info("Manifest with code={} is existing in DB already", manifest.getManifestCode());
            headers.add(errorMessage, String.format("Manifest with code=%s is existing in DB already", manifest.getManifestCode()));
            return ResponseEntity.status(409).headers(headers).body(manifest); //409 - Conflict
        } else if (!manifest.getCustomer().getIsActive() || !manifest.getSupplier().getIsActive()) {
            log.info("The manifest=\"{}\" has Given Supplier isActive = {}, Customer isActive = {}", manifest.getManifestCode(), manifest.getSupplier().getIsActive(), manifest.getCustomer().getIsActive());
            headers.add(errorMessage, String.format("Manifest with code=%s has Given Supplier isActive = %b, Customer isActive = %b", manifest.getManifestCode(), manifest.getSupplier().getIsActive(), manifest.getCustomer().getIsActive()));
            return ResponseEntity.status(409).headers(headers).body(manifest);
        } else {
            Manifest manifestToSave = manifestService.addManifestToTruckTimeTableWithinWarehouse(warehouse, ttt, manifest);
            log.info("The manifest {} was successfully saved with id={}", manifestToSave.getManifestCode(), manifestToSave.getManifestID());
            headers.add(messageMessage, String.format("The manifest %s was successfully saved with id=%d", manifestToSave.getManifestCode(), manifestToSave.getManifestID()));
            return ResponseEntity.ok().headers(headers).body(manifestToSave);
        }
    }

    /**
     * Endpoint enables removing particular Manifest from set of chosen TPA by given id of TPA and id of Manifest.
     *
     * @param tpaId      - Long Id of TPA.
     * @param manifestId - Long Id of the Manifest.
     * @return the same TPA with updated Manifest Set. Response status:
     * - 200 - if the removing was successful;
     * - 404 - if no TPA was found by given TPA id;
     * - 405 - if no Manifest was found by given Manifest id;
     * - 400 - if TPA is already closed.
     */
    @DeleteMapping(value = "tpa/{tpaId:^\\d+$}/manifest/{manifestId:^\\d+$}", headers = "truck=tpa")
    public ResponseEntity<TPA> deleteManifestFromTpa(@PathVariable String urlCode, @PathVariable Long tpaId, @PathVariable Long manifestId) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TPA tpa = truckService.getTpaService().getTpaByWarehouseAndId(tpaId, warehouse);
        Manifest manifest = manifestService.findManifestById(manifestId);
        WarehouseManifest warehouseManifest = manifestService.getWarehouseManifestService().findByWarehouseAndManifest(warehouse, manifest);

        HttpHeaders headers = new HttpHeaders();
        if (tpa == null) {
            log.info("TPA with id={} wasn't found in Warehouse {}", tpaId, urlCode);
            headers.add(errorMessage, String.format("TPA with id=%d wasn't found in Warehouse %s", tpaId, urlCode));
            return ResponseEntity.notFound().headers(headers).build();
        } else if (tpa.getStatus().getStatusName().equals(TPAEnum.CLOSED)) {
            log.info("TPA with id={} has bean CLOSED already", tpaId);
            headers.add(errorMessage, String.format("TPA with id=%d has bean already CLOSED", tpaId));
            return ResponseEntity.badRequest().headers(headers).body(tpa);
        } else if (manifest != null && warehouseManifest != null) {
            truckService.removeManifestFromTpa(warehouseManifest, tpa);
            log.info("Manifest {} was removed from TPA with id={}", manifest.getManifestCode(), tpaId);
            headers.add(messageMessage, String.format("Manifest %s was removed from TPA with id=%d", manifest.getManifestCode(), tpaId));
            return ResponseEntity.ok().headers(headers).body(tpa);
        } else {
            log.info("The manifest with id={} wasn't found in TPA {}", manifestId, tpa.getName());
            headers.add(errorMessage, String.format("Manifest with id=%d wasn't found in TPA %s", manifestId, tpa.getName()));
            return ResponseEntity.status(405).headers(headers).body(tpa);//Method Not Allowed
        }
    }

    /**
     * Endpoint enables removing particular Manifest from set of chosen TTT by given id of TTT and id of Manifest.
     *
     * @param tttId      - Long Id of TTT.
     * @param manifestId - Long Id of the Manifest.
     * @return the same TTT with updated Manifest Set. Response status:
     * - 200 - if the removing was successful;
     * - 404 - if no TTT was found by given TTT id;
     * - 405 - if no Manifest was found by given Manifest id.
     */
    @DeleteMapping(value = "ttt/{tttId:^\\d+$}/manifest/{manifestId:^\\d+$}", headers = "truck=ttt")
    public ResponseEntity<TruckTimeTable> deleteManifestFromTtt(@PathVariable String urlCode, @PathVariable Long tttId, @PathVariable Long manifestId) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TruckTimeTable ttt = truckService.getTttService().getTTTByWarehouseAndId(tttId, warehouse);
        Manifest manifest = manifestService.findManifestById(manifestId);
        WarehouseManifest warehouseManifest = manifestService.getWarehouseManifestService().findByTttAndManifest(ttt, manifest);

        HttpHeaders headers = new HttpHeaders();
        if (ttt == null || warehouse == null) {
            log.info("TTT with id={} wasn't found in Warehouse {}", tttId, urlCode);
            headers.add(errorMessage, String.format("TTT with id=%d wasn't found in Warehouse %s", tttId, urlCode));
            return ResponseEntity.notFound().headers(headers).build();

        } else if (manifest != null && (warehouseManifest.getPalletQty() == null && warehouseManifest.getBoxQtyReal() == null)) {
            truckService.removeManifestFromWarehouse(manifest, ttt);
            log.info("Manifest {} was removed from TTT with id={}", manifest.getManifestCode(), tttId);
            headers.add(messageMessage, String.format("Manifest %s was removed from TTT with id=%d", manifest.getManifestCode(), tttId));
            return ResponseEntity.ok().headers(headers).body(ttt);
        } else {
            log.info("The manifest with id={} wasn't found in TTT {}", manifestId, ttt.getTruckName());
            headers.add(errorMessage, String.format("Manifest with id=%d wasn't found in TTT %s", manifestId, ttt.getTruckName()));
            return ResponseEntity.status(405).headers(headers).body(ttt);//Method Not Allowed
        }
    }

    @PutMapping("tpa/{tpaIdFrom:^\\d+$}/manifest/{manifestId:^\\d+$}/move_to/tpa/{tpaIdTo:^\\d+$}")
    public ResponseEntity<TPA> moveManifestFromCurrentTpaToAnother(@PathVariable String urlCode, @PathVariable Long tpaIdFrom,
                                                                   @PathVariable Long manifestId, @PathVariable Long tpaIdTo) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        TPA tpaFrom = truckService.getTpaService().getTpaById(tpaIdFrom);
        TPA tpaTo = truckService.getTpaService().getTpaById(tpaIdTo);
        Manifest manifest = manifestService.findManifestById(manifestId);

        if (manifest == null || tpaFrom == null || tpaTo == null || !tpaFrom.getTpaDaysSetting().getWhCustomer().getWarehouse().equals(warehouse) ||
                !tpaTo.getTpaDaysSetting().getWhCustomer().getWarehouse().equals(warehouse) ||
                !manifest.getTpaSet().contains(tpaFrom)) {
            return ResponseEntity
                    .badRequest()
                    .header(errorMessage,
                            String.format("There are bad parameters in request, some entities could not be found: tpaFromId=%d, tpaToId=%d, Warehouse urlCode=%s, manifestId=%d",
                                    tpaIdFrom,
                                    tpaIdTo,
                                    urlCode,
                                    manifestId)).build();
        } else if (tpaFrom.getStatus().getStatusName().equals(TPAEnum.CLOSED) ||
                tpaTo.getStatus().getStatusName().equals(TPAEnum.CLOSED)) {
            return ResponseEntity.unprocessableEntity().header(errorMessage, "You cannot move manifest within TPA where at least one of them is CLOSED").build();
        }
        tpaFrom = truckService.moveManifestFromCurrentTpaToAnother(warehouse, tpaFrom, tpaTo, manifest);
        return ResponseEntity.ok().header(messageMessage, String.format("Manifest %s was moved from TPA %s to TPA %s", manifest.getManifestCode(), tpaFrom.getName(), tpaTo.getName())).build();
    }
}
