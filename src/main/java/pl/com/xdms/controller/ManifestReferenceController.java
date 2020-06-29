package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.service.ManifestReferenceService;
import pl.com.xdms.service.truck.TruckService;

import javax.validation.Valid;
import java.util.List;

/**
 * Created on 19.06.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Validated
@RestController
@Slf4j
@RequestMapping("man_ref")
public class ManifestReferenceController {
    private final ManifestReferenceService manifestReferenceService;
    private final TruckService truckService;

    @Autowired
    public ManifestReferenceController(ManifestReferenceService manifestReferenceService, TruckService truckService) {
        this.manifestReferenceService = manifestReferenceService;
        this.truckService = truckService;
    }

    /**
     * The method returns List of ManifestReferences from DB which don't have any TPA connection;
     *
     * @return List of ManifestReferences.
     */
    @GetMapping("/abandoned")
    public List<ManifestReference> getAllManifestReferencesWithoutTpa() {
        return manifestReferenceService.getAbandonedManifestReferences();
    }

    /**
     * Used to move given ManifestReference Entity in the Body Request to TPA with PathVariable id.
     *
     * @param manifestReference - Entity of ManifestReference to be moved
     * @param tpaId             - Long id of the TPA where the ManifestReference should be moved.
     * @return the same ManifestReference entity if was proceeded correctly.
     * Status Response:
     * - 200 - if movement was successful;
     * - 417 - if iD's of the ManifestReference or TPA were not found in DB
     * - 404 - if the ManifestReference doesn't have ID.
     */
    @PutMapping("/move_to_tpa/{tpaId:^\\d+$}")
    public ResponseEntity<ManifestReference> moveToAnotherTpa(@RequestBody ManifestReference manifestReference, @PathVariable Long tpaId) {
        Long id = manifestReference.getManifestReferenceId();
        if (id != null) {
            ManifestReference manifestReferenceFromBase = manifestReferenceService.findById(manifestReference.getManifestReferenceId());
            TPA tpa = truckService.getTpaService().getTpaById(tpaId);
            if (manifestReferenceFromBase == null) {
                log.info("The ManifestReference with ID={} wasn't found", id);
                return ResponseEntity.status(417).header("Error:", String.format("The ManifestReference with ID=%d wasn't found", id)).build();
            } else if (tpa == null) {
                log.info("The TPA with id={} was not found", tpaId);
                return ResponseEntity.status(417).header("Error:", String.format("The TPA with ID=%d wasn't found", tpaId)).build();
            } else {
                manifestReferenceFromBase.setTpa(tpa);
                manifestReferenceFromBase = manifestReferenceService.save(manifestReferenceFromBase);
                log.info("The TPA with id={} was not found", tpaId);
                return ResponseEntity.ok().header("Message:", String.format("The ManifestReference with ID=%d was moved to TPA ID=%d", id, tpaId)).body(manifestReferenceFromBase);
            }
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Endpoint used for updating information about each element in <code>List<ManifestReference></></code>.
     * Only attributes are accessed to be updated:
     * ReceptionNumber, DeliveryNumber, QtyReal, BoxQtyReal, GrossWeightReal, PalletQtyReal, PalletId, Stackability.
     * Response statuses could be returned:
     * - 200 - if no errors occurred while processing updating;
     * - 400 - in case if any of the Listed entities has Annotation condition violations in the Class ManifestReference
     */
    @PutMapping("/reception")
    public ResponseEntity<List<ManifestReference>> reception(@RequestBody List<@Valid  ManifestReference> manifestReferenceList) {
        List <ManifestReference> responseList = manifestReferenceService.reception(manifestReferenceList);
        return ResponseEntity.ok().header("The given list of ManifestReferences was successfully updated").body(responseList);
    }
}
