package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WarehouseManifest;
import pl.com.xdms.domain.warehouse.WarehouseManifestId;
import pl.com.xdms.repository.WarehouseManifestRepository;

import java.util.List;

/**
 * Created on 13.09.2020
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Slf4j
public class WarehouseManifestService {
    private WarehouseManifestRepository warehouseManifestRepository;

    @Autowired
    public WarehouseManifestService(WarehouseManifestRepository warehouseManifestRepository) {
        this.warehouseManifestRepository = warehouseManifestRepository;
    }

/*    public void createWarehouseReferenceRecords(List<Manifest> manifestsFromDB, List<ManifestReference> manifestReferencesFromDB) {
        manifestsFromDB.forEach(manifest -> {
            //1. find stop at CC warehouse
            createConnectionForCC(manifest);
            //2. find stop at XD warehouse
            createConnectionForXD(manifest);
            //3. find stop at XD warehouse
            createConnectionForTXD(manifestReferencesFromDB, manifest);
        });
    }

    private void createConnectionForCC(Manifest manifest) {
        TruckTimeTable tttCC;
        TPA tpaCC;
        Warehouse warehouseCC = manifest.getTruckTimeTableSet().stream()
                .map(TruckTimeTable::getWarehouse)
                .filter(wh -> wh.getWhType().getType().equals(WHTypeEnum.CC))
                .findFirst()
                .orElse(new Warehouse());
        if (warehouseCC.getWarehouseID() != null) {
            tttCC = manifest.getTruckTimeTableSet().stream()
                    .filter(ttt -> ttt.getWarehouse().getWhType().getType().equals(WHTypeEnum.CC))
                    .findFirst()
                    .orElse(new TruckTimeTable());

            tpaCC = manifest.getTpaSet().stream()
                    .filter(tpa -> tpa.getTpaDaysSetting().getWhCustomer().getWarehouse().getWhType().getType().equals(WHTypeEnum.CC))
                    .findFirst()
                    .orElse(new TPA());

            persistWarehouseManifest(manifest, tttCC, tpaCC, warehouseCC);
        }
    }

    private void createConnectionForXD(Manifest manifest) {
        TruckTimeTable tttXD;
        TPA tpaXD;
        Warehouse warehouseXD = manifest.getTruckTimeTableSet().stream()
                .map(TruckTimeTable::getWarehouse)
                .filter(wh -> wh.getWhType().getType().equals(WHTypeEnum.XD))
                .findFirst()
                .orElse(new Warehouse());
        if (warehouseXD.getWarehouseID() != null) {
            tttXD = manifest.getTruckTimeTableSet().stream()
                    .filter(ttt -> ttt.getWarehouse().getWhType().getType().equals(WHTypeEnum.XD))
                    .findFirst()
                    .orElse(new TruckTimeTable());
            tpaXD = manifest.getTpaSet().stream()
                    .filter(tpa -> tpa.getTpaDaysSetting().getWhCustomer().getWarehouse().getWhType().getType().equals(WHTypeEnum.XD))
                    .findFirst()
                    .orElse(new TPA());
            persistWarehouseManifest(manifest, tttXD, tpaXD, warehouseXD);
        }
    }

    private void createConnectionForTXD(List<ManifestReference> manifestReferencesFromDB, Manifest manifest) {
        TruckTimeTable tttTXD;
        TPA tpaTXD;
        //3. find Stop at TXD warehouse
        Warehouse warehouseTXD = manifest.getTruckTimeTableSet().stream()
                .map(TruckTimeTable::getWarehouse)
                .filter(wh -> wh.getWhType().getType().equals(WHTypeEnum.TXD))
                .findFirst()
                .orElse(new Warehouse());

        if (warehouseTXD.getWarehouseID() != null) {
            tttTXD = manifest.getTruckTimeTableSet().stream()
                    .filter(ttt -> ttt.getWarehouse().getWhType().getType().equals(WHTypeEnum.TXD))
                    .findFirst()
                    .orElse(new TruckTimeTable());


            ManifestReference manifestReference = manifestReferencesFromDB.stream()
                    .filter(mr -> mr.getManifest().getManifestCode().equals(manifest.getManifestCode()))
                    .findFirst()
                    .orElse(new ManifestReference());

            tpaTXD = manifestReference.getTpa();
            persistWarehouseManifest(manifest, tttTXD, tpaTXD, warehouseTXD);
        }
    }

    private void persistWarehouseManifest(Manifest manifest, TruckTimeTable tttXD, TPA tpaXD, Warehouse warehouseXD) {
        if (tttXD.getTttID() != null && tpaXD.getTpaID() != null) {
            WarehouseManifest warehouseManifest = new WarehouseManifest();
            warehouseManifest.setTtt(tttXD);
            warehouseManifest.setTpa(tpaXD);
            warehouseManifest.setManifest(manifest);
            warehouseManifest.setWarehouse(warehouseXD);
            warehouseManifestRepository.save(warehouseManifest);
        }
    }
*/

    public WarehouseManifest addNewWarehouseManifestTtt(Warehouse warehouse, TruckTimeTable ttt, Manifest manifest){
        WarehouseManifest warehouseManifest = new WarehouseManifest();
        warehouseManifest.setWarehouseManifestId(new WarehouseManifestId(warehouse.getWarehouseID(), manifest.getManifestID()));
        warehouseManifest.setWarehouse(warehouse);
        warehouseManifest.setManifest(manifest);
        warehouseManifest.setTtt(ttt);
        return warehouseManifestRepository.save(warehouseManifest);
    }

    public List<WarehouseManifest> findAllByWarehouseAndTtt(Warehouse warehouse, TruckTimeTable ttt){
        return warehouseManifestRepository.findAllByWarehouseAndTtt(warehouse, ttt);
    }

    public List<WarehouseManifest> findAllByTtt(TruckTimeTable ttt){
        return warehouseManifestRepository.findAllByTtt(ttt);
    }

    public List<WarehouseManifest> findAllByTpa(TPA tpa){
        return warehouseManifestRepository.findAllByTpa(tpa);
    }

    public void deleteAllByTtt(TruckTimeTable truckTimeTable) {
        warehouseManifestRepository.deleteAllByTtt(truckTimeTable);
    }

    @Transactional
    public void deleteByTttAndManifest(TruckTimeTable ttt, Manifest manifest){
        warehouseManifestRepository.deleteByTttAndManifest(ttt, manifest);
    }

    public WarehouseManifest findByTttAndManifest(TruckTimeTable ttt, Manifest manifest){
        return warehouseManifestRepository.findByTttAndManifest(ttt, manifest).orElse(new WarehouseManifest());
    }

    public WarehouseManifest save(WarehouseManifest warehouseManifest) {
        return warehouseManifestRepository.save(warehouseManifest);
    }

    public WarehouseManifest findByWarehouseAndManifest(Warehouse warehouse, Manifest manifest){
        return warehouseManifestRepository.findByWarehouseAndManifest(warehouse, manifest).orElse(null);
    }

    public List<WarehouseManifest> findAll() {
        return warehouseManifestRepository.findAll();
    }
}
