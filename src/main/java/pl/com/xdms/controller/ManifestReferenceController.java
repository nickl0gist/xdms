package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TPAEnum;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.service.ManifestReferenceService;
import pl.com.xdms.service.WarehouseService;
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
@PropertySource("classpath:messages.properties")
@RequestMapping("warehouse/{urlCode:^[a-z_]{5,8}$}/man_ref")
public class ManifestReferenceController {

    @Value("${error.http.message}")
    String errorMessage;

    @Value("${message.http.message}")
    String messageMessage;

    private final ManifestReferenceService manifestReferenceService;
    private final TruckService truckService;
    private final WarehouseService warehouseService;

    @Autowired
    public ManifestReferenceController(ManifestReferenceService manifestReferenceService, TruckService truckService, WarehouseService warehouseService) {
        this.manifestReferenceService = manifestReferenceService;
        this.truckService = truckService;
        this.warehouseService = warehouseService;
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
    public ResponseEntity<ManifestReference> moveToAnotherTpa(@PathVariable String urlCode, @RequestBody ManifestReference manifestReference, @PathVariable Long tpaId) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        Long id = manifestReference.getManifestReferenceId();
        if (id != null) {
            ManifestReference manifestReferenceFromBase = manifestReferenceService.findById(manifestReference.getManifestReferenceId());
            TPA tpaTo = truckService.getTpaService().getTpaById(tpaId);
            if (manifestReferenceFromBase == null || tpaTo == null || warehouse == null) {
                log.info("The ManifestReference with ID={} wasn't found or TPA where parts should be placed does not exist={}", id, tpaTo == null);
                return ResponseEntity.status(417).header(errorMessage, String.format("The ManifestReference with ID=%d wasn't found or TPA where parts should be placed does not exist=%s", id, tpaTo == null)).build();
            } else if(manifestReferenceFromBase.getTpa().getStatus().getStatusName().equals(TPAEnum.CLOSED) ||
                tpaTo.getStatus().getStatusName().equals(TPAEnum.CLOSED) ||
                !tpaTo.getTpaDaysSetting().getWhCustomer().getWarehouse().equals(warehouse)){
                log.info("The TPA where parts suppose to be taken from or TPA where the parts suppose to be placed are CLOSED");
                return ResponseEntity.status(403).header(errorMessage, "The TPA where parts suppose to be taken from or TPA where the parts suppose to be placed are CLOSED").build();
            } else {

                manifestReferenceFromBase.setTpa(tpaTo);
                manifestReferenceFromBase = manifestReferenceService.save(manifestReferenceFromBase);
                log.info("The ManifestReference with ID={} was moved to TPA ID={}", id, tpaId);
                return ResponseEntity.ok().header(messageMessage, String.format("The ManifestReference with ID=%d was moved to TPA ID=%d", id, tpaId)).body(manifestReferenceFromBase);
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
    public ResponseEntity<List<ManifestReference>> reception(@PathVariable String urlCode, @RequestBody List<@Valid  ManifestReference> manifestReferenceList) {
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        List <ManifestReference> responseList = manifestReferenceService.reception(manifestReferenceList, warehouse);
        return ResponseEntity.ok().header(messageMessage,"The given list of ManifestReferences was successfully updated").body(responseList);
    }
}
