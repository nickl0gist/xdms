package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
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
@RequestMapping
public class ManifestController {
    private final TruckService truckService;
    private final WarehouseService warehouseService;
    private final RequestErrorService requestErrorService;
    private final ManifestService manifestService;

    @Autowired
    public ManifestController(TruckService truckService, WarehouseService warehouseService,
                              RequestErrorService requestErrorService, ManifestService manifestService) {
        this.truckService = truckService;
        this.warehouseService = warehouseService;
        this.requestErrorService = requestErrorService;
        this.manifestService = manifestService;
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
     * @param id - id of the Manifest in DB
     * @return - Manifest if it was founded, status 404 if wasn't.
     */
    @GetMapping("manifest/{id:^\\d+$}")
    public ResponseEntity<Manifest> getManifestById(@PathVariable Long id) {
        Manifest manifest = manifestService.findManifestById(id);
        if (manifest == null) {
            return ResponseEntity.notFound().header("Error:", String.format("The manifest with id=%d is not existing", id)).build();
        }
        return ResponseEntity.ok(manifest);
    }

    /**
     * THe endpoint dedicated to adding the the reference to particular manifest
     * @param id - Long id of the manifest where the reference should be added
     * @param manifestReference - the entity of ManifestReference with all required information
     * @param bindingResult - BindingResult entity to check annotation conditions in given manifestReference
     * @return Response with Manifest in the body. Possible statuses of response:
     * - 200 - if updating of the manifest was successful;
     * - 404 - if no manifests were found by the given id;
     * - 422 - if any of the annotation conditions in ManifestReference class were violated.
     */
    @PutMapping("manifest/{id:^\\d+$}/addReference")
    public ResponseEntity<Manifest> addReferenceToManifest(@PathVariable Long id, @RequestBody @Valid ManifestReference manifestReference, BindingResult bindingResult) {
        Manifest manifest = manifestService.findManifestById(id);
        if(manifest == null) {
            log.info("Manifest with id={} wasn't found", id);
            return ResponseEntity.notFound().header("Error:", String.format("Manifest with id=%d wasn't found", id)).build();
        } else if (bindingResult.hasErrors()) {
            //if given entity doesn't correspond conditions of parameters annotation in ManifestReference class
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.unprocessableEntity().headers(headers).body(manifest); //422
        }
        manifest.getManifestsReferenceSet().add(manifestReference);
        manifest = manifestService.save(manifest);
        log.info("Reference {} was added to Manifest {}", manifestReference.getReference().getNumber(), manifest.getManifestCode());
        return ResponseEntity.ok().header("Message:", String.format("Reference %s was added to Manifest %s", manifestReference.getReference().getNumber(), manifest.getManifestCode())).body(manifest);
    }

    /**
     * The endpoint dedicated to update the Manifest from DB with information given with Manifest Entity from the request.
     * Only four parameters are allowed to be updated: setBoxQtyReal, setPalletQtyReal, setTotalLdmReal, setTotalWeightReal.
     *
     * @param manifestUpdated Manifest entity given by user in request.
     * @param bindingResult   BindingResult to check if the given Manifest corresponds to annotation conditions.
     * @return Response entity with Manifest in body. Possible cases:
     * - 200 - if The given Entity is corresponding to all required conditions amd updating was successful;
     * - 400 - if id of the given manifest is null;
     * - 404 - if Manifest from Database wasn't found by Id of given manifest;
     * - 412 - if the given manifest does not correspond to annotation conditions of Manifest class
     */
    @PutMapping("ttt/{tttId:^\\d+$}/manifest/update")
    public ResponseEntity<Manifest> updateManifest(@RequestBody @Valid Manifest manifestUpdated, @PathVariable Long tttId, BindingResult bindingResult) {
        Long id = manifestUpdated.getManifestID();
        TruckTimeTable ttt = truckService.getTttService().getTttById(tttId);
        Manifest manifestFromDb = manifestService.findManifestById(manifestUpdated.getManifestID());
        if (ttt != null && ttt.getManifestSet().contains(manifestFromDb)) {
                //If manifest wasn't found by Id
                if (manifestFromDb == null) {
                    log.info("Given Manifest with id={} does not exist in DB and could not be updated", id);
                    return ResponseEntity.notFound().header("Error:", String.format("The manifest with id=%d is not existing", id)).build();
                } else if (bindingResult.hasErrors()) {
                    //if given entity doesn't correspond conditions of parameters annotation in Manifest class
                    HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
                    return ResponseEntity.status(412).headers(headers).body(manifestUpdated);
                }
                //If all conditions are Ok Update manifest with given data
                manifestFromDb = manifestService.updateManifest(manifestUpdated);
                truckService.getTttService().setArrive(ttt);

                return ResponseEntity.ok().header("Message:", String.format("The Manifest with id=%d was successfully updated", id)).body(manifestFromDb);
            }
        return ResponseEntity.badRequest().header("ERROR", "Not Existing").build();
    }

    /**
     * The endpoint which should be invoke in order to delete Manifest by given Id.
     *
     * @param id - Long id of the Manifest to delete
     * @return ResponseEntity with headers of the result of request.
     * - 200 - if deletion was successful
     * - 422 - if Manifest has information about real quantities of pallets or boxes. The manifest would not be deleted
     * - 404 - if no manifest was found by given Id.
     */
    @DeleteMapping("manifest/{id:^\\d+$}")
    public ResponseEntity<String> deleteManifest(@PathVariable Long id) {
        Manifest manifest = manifestService.findManifestById(id);
        HttpHeaders headers = new HttpHeaders();
        String message = "Message:";
        if (manifest == null) {
            log.info("Manifest with id: {} not found, returning error", id);
            headers.set(message, String.format("Manifest with id=%d Not Found", id));
            return ResponseEntity.notFound().headers(headers).build();//404
        } else if (manifest.getPalletQtyReal() != null || manifest.getBoxQtyReal() != null) {
            log.info("Manifest with id: {} has arrived already and couldn't be deleted", id);
            headers.set(message, String.format("Manifest with id=%d arrived already and couldn't be deleted", id));
            return ResponseEntity.unprocessableEntity().headers(headers).build();//422
        }
        manifestService.delete(manifest);
        log.info("Manifest with id: {} was deleted", id);
        headers.set(message, String.format("Manifest with id=%d deleted", id));
        return ResponseEntity.ok().headers(headers).build();//200
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
    public ResponseEntity<Manifest> addManifestInChosenTtt(@PathVariable Long tttId, @RequestBody @Valid Manifest manifest, BindingResult bindingResult) {
        HttpHeaders headers = new HttpHeaders();
        String message = "Message:";
        String error = "Error:";
        TruckTimeTable ttt = truckService.getTttService().getTttById(tttId);
        if (ttt == null) {
            log.info("The TTT with Id={} wasn't found", tttId);
            headers.add(error, String.format("The TTT with Id=%d wasn't found", tttId));
            return ResponseEntity.notFound().headers(headers).build(); // 404
        } else if (bindingResult.hasErrors()) {
            headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(412).headers(headers).body(manifest);
        } else if (manifestService.isManifestExisting(manifest)) {
            log.info("Manifest with code={} is existing in DB already", manifest.getManifestCode());
            headers.add(error, String.format("Manifest with code=%s is existing in DB already", manifest.getManifestCode()));
            return ResponseEntity.status(409).headers(headers).body(manifest); //409 - Conflict
        } else if (!manifest.getCustomer().getIsActive() || !manifest.getSupplier().getIsActive()) {
            log.info("The manifest=\"{}\" has Given Supplier isActive = {}, Customer isActive = {}", manifest.getManifestCode(), manifest.getSupplier().getIsActive(), manifest.getCustomer().getIsActive());
            headers.add(error, String.format("Manifest with code=%s has Given Supplier isActive = %b, Customer isActive = %b", manifest.getManifestCode(), manifest.getSupplier().getIsActive(), manifest.getCustomer().getIsActive()));
            return ResponseEntity.status(409).headers(headers).body(manifest);
        } else {
            Manifest manifestToSave = new Manifest();
            manifestToSave.setManifestCode(manifest.getManifestCode());
            manifestToSave.setBoxQtyPlanned(manifest.getBoxQtyPlanned());
            manifestToSave.setPalletQtyPlanned(manifest.getPalletQtyPlanned());
            manifestToSave.setTotalLdmPlanned(manifest.getTotalLdmPlanned());
            manifestToSave.setTotalWeightPlanned(manifest.getTotalWeightPlanned());
            manifestToSave.setSupplier(manifest.getSupplier());
            manifestToSave.setCustomer(manifest.getCustomer());
            manifestToSave.getTruckTimeTableSet().add(ttt);
            manifestToSave = manifestService.save(manifestToSave);
            log.info("The manifest {} was successfully saved with id={}", manifestToSave.getManifestCode(), manifestToSave.getManifestID());
            headers.add(message, String.format("The manifest %s was successfully saved with id=%d", manifestToSave.getManifestCode(), manifestToSave.getManifestID()));
            return ResponseEntity.ok().headers(headers).body(manifestService.save(manifestToSave));
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
    @DeleteMapping("tpa/{tpaId:^\\d+$}/manifest/{manifestId:^\\d+$}")
    public ResponseEntity<TPA> deleteManifestFromTpa(@PathVariable Long tpaId, @PathVariable Long manifestId) {
        TPA tpa = truckService.getTpaService().getTpaById(tpaId);
        Manifest manifest = manifestService.findManifestById(manifestId);
        HttpHeaders headers = new HttpHeaders();
        if (tpa == null) {
            log.info("TPA with id={} wasn't found", tpaId);
            headers.add("Error:", String.format("TPA with id=%d wasn't found", tpaId));
            return ResponseEntity.notFound().headers(headers).build();
        } else if (tpa.getStatus().getStatusName().equals(TPAEnum.CLOSED)) {
            log.info("TPA with id={} has bean CLOSED already", tpaId);
            headers.add("Error:", String.format("TPA with id=%d has bean already CLOSED", tpaId));
            return ResponseEntity.badRequest().headers(headers).body(tpa);
        } else if (manifest != null) {
            tpa.getManifestSet().remove(manifest);
            truckService.getTpaService().save(tpa);
            log.info("Manifest {} was removed from TPA with id={}", manifest.getManifestCode(), tpaId);
            headers.add("Message:", String.format("Manifest %s was removed from TPA with id=%d", manifest.getManifestCode(), tpaId));
            return ResponseEntity.ok().headers(headers).body(tpa);
        } else {
            log.info("The manifest with id={} wasn't found in DB", manifestId);
            headers.add("Error:", String.format("Manifest with id=%d wasn't found in DB", manifestId));
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
    @DeleteMapping("ttt/{tttId:^\\d+$}/manifest/{manifestId:^\\d+$}")
    public ResponseEntity<TruckTimeTable> deleteManifestFromTtt(@PathVariable Long tttId, @PathVariable Long manifestId) {
        TruckTimeTable ttt = truckService.getTttService().getTttById(tttId);
        Manifest manifest = manifestService.findManifestById(manifestId);
        HttpHeaders headers = new HttpHeaders();
        if (ttt == null) {
            log.info("TTT with id={} wasn't found", tttId);
            headers.add("Error:", String.format("TTT with id=%d wasn't found", tttId));
            return ResponseEntity.notFound().headers(headers).build();
        } else if (manifest != null) {
            ttt.getManifestSet().remove(manifest);
            truckService.getTttService().save(ttt);
            log.info("Manifest {} was removed from TTT with id={}", manifest.getManifestCode(), tttId);
            headers.add("Message:", String.format("Manifest %s was removed from TTT with id=%d", manifest.getManifestCode(), tttId));
            return ResponseEntity.ok().headers(headers).body(ttt);
        } else {
            log.info("The manifest with id={} wasn't found in DB", manifestId);
            headers.add("Error:", String.format("Manifest with id=%d wasn't found in DB", manifestId));
            return ResponseEntity.status(405).headers(headers).body(ttt);//Method Not Allowed
        }
    }
}
