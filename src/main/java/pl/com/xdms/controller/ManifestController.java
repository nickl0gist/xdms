package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.manifest.Manifest;
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
    @GetMapping("manifests/abandoned")
    public ResponseEntity<List<Manifest>> getManifestsWithoutTtt() {
        List<Manifest> manifests = manifestService.getAllAbandonedManifests();
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
    @PutMapping("manifest/update")
    public ResponseEntity<Manifest> updateManifestById(@RequestBody @Valid Manifest manifestUpdated, BindingResult bindingResult) {
        log.info("Manifest from request: {}", manifestUpdated);
        Long id = manifestUpdated.getManifestID();
        if (id != null) {
            Manifest manifestFromDb = manifestService.findManifestById(manifestUpdated.getManifestID());
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
            manifestFromDb.setBoxQtyReal(manifestUpdated.getBoxQtyReal());
            manifestFromDb.setPalletQtyReal(manifestUpdated.getPalletQtyReal());
            manifestFromDb.setTotalLdmReal(manifestUpdated.getTotalLdmReal());
            manifestFromDb.setTotalWeightReal(manifestUpdated.getTotalWeightReal());
            return ResponseEntity.ok().header("Message:", String.format("The Manifest with id=%d was successfully updated", id)).body(manifestService.save(manifestFromDb));
        }
        return ResponseEntity.badRequest().header("ERROR", "Not Existing").build();
    }

    /**
     * The endpoint which should be invoke in order to delete Manifest by given Id.
     *
     * @param id - Long id of the Manifest to delete
     * @return ResponseEntity with headers of the result of request.
     * - 200 - if deletion was successful
     * - 422 - if Manifest information about real quantities of pallets or boxes. The manifest would not be deleted
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

    //TODO CREATE -> creation only via ExcelManifestController
}
