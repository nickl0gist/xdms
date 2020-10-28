package pl.com.xdms.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WarehouseManifest;
import pl.com.xdms.domain.warehouse.WarehouseManifestId;
import pl.com.xdms.repository.ManifestRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created on 01.12.2019
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@Service
@Data
public class ManifestService {

    private final ManifestRepository manifestRepository;
    private WarehouseManifestService warehouseManifestService;

    @Autowired
    public ManifestService(ManifestRepository manifestRepository, WarehouseManifestService warehouseManifestService) {
        this.manifestRepository = manifestRepository;
        this.warehouseManifestService = warehouseManifestService;
    }

    public boolean isManifestExisting(Manifest manifest) {
        return findManifest(manifest) != null;
    }

    public Manifest findManifest(Manifest manifest) {
        Optional<Manifest> manifestFromDB = manifestRepository.findByManifestCode(manifest.getManifestCode());
        return manifestFromDB.orElse(null);
    }

    public List<Manifest> saveAll(List<Manifest> manifests) {
        return manifestRepository.saveAll(manifests);
    }

    public List<Manifest> getAllManifests() {
        return manifestRepository.findAll();
    }

    public List<Manifest> getAllTttAbandonedManifests() {
        return manifestRepository.findAllByTruckTimeTableSetIsNull();
    }

    public Manifest findManifestById(Long id) {
        return manifestRepository.findById(id).orElse(null);
    }

    public Manifest save(Manifest manifestFromDb) {
        return manifestRepository.save(manifestFromDb);
    }

    public void delete(Manifest manifest) {
        manifestRepository.delete(manifest);
    }

    public Manifest updateManifest(Manifest manifestUpdated) {
        Manifest manifestFromDb = findManifestById(manifestUpdated.getManifestID());
        manifestFromDb.setBoxQtyReal(manifestUpdated.getBoxQtyReal());
        manifestFromDb.setPalletQtyReal(manifestUpdated.getPalletQtyReal());
        manifestFromDb.setTotalLdmReal(manifestUpdated.getTotalLdmReal());
        manifestFromDb.setTotalWeightReal(manifestUpdated.getTotalWeightReal());
        return save(manifestFromDb);
    }

    public Manifest addManifestToTruckTimeTableWithinWarehouse(Warehouse warehouse, TruckTimeTable ttt, Manifest manifest) {
        Manifest manifestToSave = new Manifest();
        manifestToSave.setManifestCode(manifest.getManifestCode());
        manifestToSave.setBoxQtyPlanned(manifest.getBoxQtyPlanned());
        manifestToSave.setPalletQtyPlanned(manifest.getPalletQtyPlanned());
        manifestToSave.setTotalLdmPlanned(manifest.getTotalLdmPlanned());
        manifestToSave.setTotalWeightPlanned(manifest.getTotalWeightPlanned());
        manifestToSave.setSupplier(manifest.getSupplier());
        manifestToSave.setCustomer(manifest.getCustomer());
        manifestToSave.getTruckTimeTableSet().add(ttt);
        manifestToSave = save(manifestToSave);
        warehouseManifestService.addNewWarehouseManifestTtt(warehouse, ttt, manifestToSave);
        return manifestToSave;
    }

    public WarehouseManifest updateWarehouseManifest(WarehouseManifest warehouseManifestUpdated) {
        WarehouseManifest warehouseManifestFromDb = warehouseManifestService.findByWarehouseAndManifest(warehouseManifestUpdated.getWarehouse(), warehouseManifestUpdated.getManifest());
        Manifest manifest = warehouseManifestFromDb.getManifest();

        manifest.setBoxQtyReal(warehouseManifestUpdated.getBoxQtyReal());
        manifest.setPalletQtyReal(warehouseManifestUpdated.getPalletQty());
        //FIXME
        // manifest.setTotalLdmReal(calculate LDM);
        manifest.setTotalWeightReal(warehouseManifestUpdated.getGrossWeight());

        warehouseManifestFromDb.setPalletQty(warehouseManifestUpdated.getPalletQty());
        warehouseManifestFromDb.setBoxQtyReal(warehouseManifestUpdated.getBoxQtyReal());
        warehouseManifestFromDb.setGrossWeight(warehouseManifestUpdated.getGrossWeight());
        warehouseManifestFromDb.setNetWeightReal(warehouseManifestUpdated.getNetWeightReal());
        warehouseManifestFromDb.setPalletHeight(warehouseManifestUpdated.getPalletHeight());
        warehouseManifestFromDb.setPalletWidth(warehouseManifestUpdated.getPalletWidth());
        warehouseManifestFromDb.setPalletLength(warehouseManifestUpdated.getPalletLength());
        warehouseManifestFromDb.setKpiLabel(warehouseManifestUpdated.getKpiLabel());
        warehouseManifestFromDb.setKpiDocument(warehouseManifestUpdated.getKpiDocument());
        warehouseManifestFromDb.setKpiManifest(warehouseManifestUpdated.getKpiManifest());

        save(manifest);
        return warehouseManifestService.save(warehouseManifestFromDb);
    }

    public WarehouseManifest getWarehouseManifestByTttAndManifest(TruckTimeTable ttt, Manifest manifest) {
        return warehouseManifestService.findByTttAndManifest(ttt, manifest);
    }

    public void removeManifest(Manifest manifest, TruckTimeTable ttt) {
        ttt.getManifestSet().remove(manifest);
        warehouseManifestService.deleteByTttAndManifest(ttt, manifest);
        delete(manifest);
    }

    public WarehouseManifest getWarehouseManifestByWarehouseAndManifest(Warehouse warehouse, Manifest manifest) {
        return warehouseManifestService.findByWarehouseAndManifest(warehouse, manifest);
    }

    public WarehouseManifest saveWarehouseManifest(WarehouseManifest wh) {
        return warehouseManifestService.save(wh);
    }

    public void createWarehouseManifestEntities(List<Manifest> manifests) {
        manifests.forEach(manifest -> manifest.getTruckTimeTableSet()
                .forEach(ttt -> {
                    WarehouseManifest wh = new WarehouseManifest();
                    WarehouseManifestId whId = new WarehouseManifestId(ttt.getWarehouse().getWarehouseID(), manifest.getManifestID());
                    wh.setWarehouseManifestId(whId);
                    wh.setManifest(manifest);
                    wh.setWarehouse(ttt.getWarehouse());
                    wh.setTtt(ttt);
                    TPA tpa = manifest.getTpaSet().stream().filter(t -> t.getTpaDaysSetting()
                            .getWhCustomer()
                            .getWarehouse()
                            .getWarehouseID()
                            .equals(ttt.getWarehouse()
                                    .getWarehouseID()))
                            .findFirst().orElse(null);
                    log.info("TPA {}", tpa);
                    wh.setTpa(tpa);
                    saveWarehouseManifest(wh);
                }));
    }

    public List<WarehouseManifest> getListOfWarehouseManifestByWarehouseAndTtt(Warehouse warehouse, TruckTimeTable truckTimeTable) {
        return warehouseManifestService.findAllByWarehouseAndTtt(warehouse, truckTimeTable);
    }
}