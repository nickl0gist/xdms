package pl.com.xdms.service.truck;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TpaDaysSetting;
import pl.com.xdms.domain.tpa.WorkingDay;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.WHTypeEnum;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WarehouseManifest;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.service.ManifestReferenceService;
import pl.com.xdms.service.ManifestService;

import javax.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created on 08.12.2019
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Slf4j
@Data
public class TruckService {
    private final TPAService tpaService;
    private final TTTService tttService;
    private final TpaDaysSettingsService tpaDaysSettingsService;
    private final WorkingDayService workingDayService;
    private final ManifestService manifestService;
    private final ManifestReferenceService manifestReferenceService;

    @Autowired
    public TruckService(TPAService tpaService,
                        TTTService tttService,
                        TpaDaysSettingsService tpaDaysSettingsService,
                        WorkingDayService workingDayService,
                        ManifestService manifestService,
                        ManifestReferenceService manifestReferenceService) {

        this.tpaService = tpaService;
        this.tttService = tttService;
        this.tpaDaysSettingsService = tpaDaysSettingsService;
        this.workingDayService = workingDayService;
        this.manifestService = manifestService;
        this.manifestReferenceService = manifestReferenceService;

    }

    /**
     * Calculates ZonedDateTime of ETD manifest from Warehouse according to existing TPA days settings for
     * current Warehouse and Customer.
     *
     * @param dateTimeETD Estimated DateTime of manifest Departure from Warehouse
     * @param whCustomer  - the pair of Warehouse and Customer (WhCustomer)
     * @return map with one element inside. @code key is ZonedDateTime of the moment when tpa will be closed,
     * @code Value is TpaDaysSetting chosen from the list
     */
    public Map<ZonedDateTime, TpaDaysSetting> getAppropriateTpaSetting(ZonedDateTime dateTimeETD, WhCustomer whCustomer) {

        // if etdDayOfWeek is Saturday or Sunday move it to friday before
        if (dateTimeETD.getDayOfWeek().equals(DayOfWeek.of(6))) {
            dateTimeETD = dateTimeETD.minusDays(1).withHour(23).withMinute(59);
        } else if (dateTimeETD.getDayOfWeek().equals(DayOfWeek.of(7))) {
            dateTimeETD = dateTimeETD.minusDays(2).withHour(23).withMinute(59);
        }

        //Getting Working day from DB by dateTimeETD day Of Week
        WorkingDay workingDay = workingDayService.getWorkingDayByNumber((long) dateTimeETD.getDayOfWeek().getValue());

        //Getting list of TpaDaysSetting for current Wh_Customer and Working day
        List<TpaDaysSetting> listOfTpaSettingsForDepartureDay = tpaDaysSettingsService.getTpaDaySettingsByWhCustomerAndWorkingDay(whCustomer, workingDay);

        log.info("The list of TpaDaysSettings - {}", listOfTpaSettingsForDepartureDay);

        TpaDaysSetting chosenSetting = getTpaDaysSetting(dateTimeETD, listOfTpaSettingsForDepartureDay);

        for (int i = 0; i < 5 && chosenSetting == null; i++) {

            if (dateTimeETD.getDayOfWeek().getValue() == 1) {
                dateTimeETD = ZonedDateTime.of(dateTimeETD.minusDays(3).toLocalDate(), LocalTime.of(23, 59), dateTimeETD.getZone());
            } else {
                dateTimeETD = ZonedDateTime.of(dateTimeETD.minusDays(1).toLocalDate(), LocalTime.of(23, 59), dateTimeETD.getZone());
            }

            workingDay = workingDayService.getWorkingDayByNumber((long) dateTimeETD.getDayOfWeek().getValue());
            chosenSetting = getTpaDaysSetting(dateTimeETD, tpaDaysSettingsService.getTpaDaySettingsByWhCustomerAndWorkingDay(whCustomer, workingDay));
        }
        chosenSetting = chosenSetting == null ? new TpaDaysSetting() : chosenSetting;
        dateTimeETD = ZonedDateTime.of(dateTimeETD.toLocalDate(), getLocalTimeFromString(chosenSetting.getLocalTime()), dateTimeETD.getZone());
        Map<ZonedDateTime, TpaDaysSetting> result = new HashMap<>();
        result.put(dateTimeETD, chosenSetting);
        return result;
    }

    /**
     * Checks received List of TpaDaysSettings with ETD and calculate appropriate TpaDaysSettings.
     * If there no suitable TPAs for ETD day null will be returned
     *
     * @param dateTimeETD                      - ZonedDateTime of estimated departure from Warehouse
     * @param listOfTpaSettingsForDepartureDay - list of settings supposed TpaDaySettings.
     * @return calculated TpaDaySettings or null if there no suitable settings found.
     */
    private TpaDaysSetting getTpaDaysSetting(ZonedDateTime dateTimeETD, List<TpaDaysSetting> listOfTpaSettingsForDepartureDay) {
        TpaDaysSetting chosenSetting = null;

        //approx 1 year in minutes
        long i = -500000;

        for (TpaDaysSetting daysSetting : listOfTpaSettingsForDepartureDay) {
            ZonedDateTime tpaDateTime = ZonedDateTime.of(dateTimeETD.toLocalDate(), getLocalTimeFromString(daysSetting.getLocalTime()), dateTimeETD.getZone());
            long checkSum = Duration.between(dateTimeETD, tpaDateTime).toMinutes();
            log.info("dateTimeETD = {} -=- tpaDateTime = {}, -=- checkSum = {}", dateTimeETD, tpaDateTime, checkSum);
            if (checkSum <= 0 && checkSum > i) {
                i = checkSum;
                chosenSetting = daysSetting;
            }
        }
        log.info("Calculated chosenSetting = {}", chosenSetting);
        return chosenSetting;
    }

    /**
     * Saves TTT TPA entities to Database. Connect TPAs and TTTs with Manifests. Save all Manifests and receive these
     * Manifests as DB entities. Add TPA to each ManifestReferences. Save all and receive them as entities from DB
     *
     * @param manifestMapDTO          - Map of manifest to be saved in Database
     * @param tpaSetDTO               - set of TPA entities to be saved in Database
     * @param tttSetDTO               - set of TTT entities to be saved in Database
     * @param manifestReferenceSetDTO - set of manifestReference entities to be saved in Database
     */
    @Transactional
    public void saveElements(Map<Long, Manifest> manifestMapDTO, Set<TPA> tpaSetDTO, Set<TruckTimeTable> tttSetDTO, Set<ManifestReference> manifestReferenceSetDTO) {
        //1. Save TTTs in DB
        List<TruckTimeTable> truckTimeTableListFromDB = tttService.saveAll(new ArrayList<>(tttSetDTO)
                .stream()
                .filter(TruckTimeTable::getIsActive)
                .collect(Collectors.toList()));
        //2. Save all TPAs in DB
        List<TPA> tpaListFromDB = tpaService.saveAll(new ArrayList<>(tpaSetDTO)
                .stream()
                .filter(TPA::getIsActive)
                .collect(Collectors.toList()));

        //3. Connect TPAs and TTTs with Manifests. Save all Manifests and receive these Manifests as DB entities
        List<Manifest> manifestsFromDB = connectManifestWithTpaAndTtt(new ArrayList<>(manifestMapDTO.values()), truckTimeTableListFromDB, tpaListFromDB)
                .stream()
                .filter(Manifest::getIsActive)
                .collect(Collectors.toList());
        manifestsFromDB = manifestService.saveAll(manifestsFromDB);
        //manifestService.createWarehouseManifestEntities(manifestsFromDB);

        //4. Add TPA and Manifest to each ManifestReferences. Save all and receive them as entities from DB
        List<ManifestReference> manifestReferencesFromDB = connectManiRefToManifestAndTPA(new ArrayList<>(manifestReferenceSetDTO), manifestsFromDB, tpaListFromDB)
                .stream()
                .filter(manref -> manref.getIsActive() && manref.getManifest() != null)
                .collect(Collectors.toList());
        manifestReferencesFromDB = manifestReferenceService.saveAll(manifestReferencesFromDB);

        //5. Create Connections Warehouse-Manifest-TTT-TPA in WarehouseManifest table in DB
        //manifestService.getWarehouseManifestService().createWarehouseReferenceRecords(manifestsFromDB, manifestReferencesFromDB);
        manifestService.createWarehouseManifestEntities(manifestsFromDB);

    }

    /**
     * Add TPA and Manifest to each ManifestReferences.
     *
     * @param manifestReferences - List of manifestReferences entities
     * @param manifestsFromDB    - manifests from Database
     * @param tpaListFromDB      - TPA list from DB
     * @return the List of the same ManifestReference entities with added to each of them related Manifest and TPA.
     * It connects Manifest by ManifestCode in manifest and in manifestReference. And by tpaCode from TPA and manifestReference.
     * If it is not possible to find appropriate Manifest or TPA null will be passed instead.
     */
    private List<ManifestReference> connectManiRefToManifestAndTPA(List<ManifestReference> manifestReferences, List<Manifest> manifestsFromDB, List<TPA> tpaListFromDB) {
        for (ManifestReference manifestReference : manifestReferences) {
            TPA tpa = tpaListFromDB.stream()
                    .filter(x -> x.getName().equals(manifestReference.getTpaCode()) && x.getIsActive())
                    .reduce((a, b) -> {
                        throw new IllegalStateException("Multiple TPA entities " + a + " || " + b);
                    }).orElse(null);
            Manifest manifest = manifestsFromDB.stream()
                    .filter(x -> x.getManifestCode().equals(manifestReference.getManifestCode()) && x.getIsActive())
                    .reduce((a, b) -> {
                        throw new IllegalStateException("Multiple manifests " + a + " || " + b);
                    }).orElse(null);
            manifestReference.setTpa(tpa);
            manifestReference.setManifest(manifest);
        }
        return manifestReferences;
    }

    /**
     * Connect TPAs and TTTs from Database with Manifests .
     *
     * @param manifests                - List of manifestReferences entities received from request.
     * @param truckTimeTableListFromDB - TTT list from Database
     * @param tpaListFromDB            - TPA list from Database
     * @return the List of the same Manifests entities with added to each of them related TTT and TPA.
     * Method adds list of TTT related to Manifest by truckName and TttArrivalDatePlan from truckTimeTableListFromDB
     * and indicated in TTT set from Manifest.
     * Method adds list of TPA related to Manifest by Name and DeparturePlan tpaListFromDB
     * and indicated in TPA set from Manifest.
     */
    private List<Manifest> connectManifestWithTpaAndTtt(List<Manifest> manifests, List<TruckTimeTable> truckTimeTableListFromDB, List<TPA> tpaListFromDB) {
        for (Manifest manifest : manifests) {
            Set<TruckTimeTable> newTTTset = new HashSet<>();
            Set<TPA> newTPAset = new HashSet<>();
            for (TruckTimeTable oldTruckTimeTable : manifest.getTruckTimeTableSet()) {
                newTTTset.add(truckTimeTableListFromDB.stream()
                        .filter(x -> x.getTruckName().equals(oldTruckTimeTable.getTruckName()) && x.getTttArrivalDatePlan().equals(oldTruckTimeTable.getTttArrivalDatePlan()) && x.getIsActive())
                        .reduce((a, b) -> {
                            throw new IllegalStateException("Multiple TTT elements " + a + " || " + b);
                        }).orElse(null)
                );
            }
            manifest.setTruckTimeTableSet(newTTTset);
            for (TPA oldTpa : manifest.getTpaSet()) {
                newTPAset.add(tpaListFromDB.stream()
                        .filter(x -> x.getName().equals(oldTpa.getName()) && x.getDeparturePlan().equals(oldTpa.getDeparturePlan()) && x.getIsActive())
                        .reduce((a, b) -> {
                            throw new IllegalStateException("Multiple TPA elements " + a + " || " + b);
                        }).orElse(null)
                );
            }
            manifest.setTpaSet(newTPAset);
        }
        return manifests;
    }

    /**
     * Performs deletion of the TTT depending on Warehouse type it regards.
     * If Warehouse is CC type -> deleteTttFromCc
     * If Warehouse is XD type -> deleteTttFromXd
     * If Warehouse is TXD type -> deleteTttFromTxd
     *
     * @param truckTimeTable - to be deleted.
     * @return boolean parameter. True is TTT was deleted successfully, False - if wasn't.
     */
    @Transactional(rollbackOn = RuntimeException.class)
    public boolean deleteTtt(TruckTimeTable truckTimeTable) {
        boolean result = false;
        Warehouse warehouse = truckTimeTable.getWarehouse();
        WHTypeEnum whTypeEnum = warehouse.getWhType().getType();
        if (whTypeEnum.equals(WHTypeEnum.CC)) {
            result = deleteTttFromCc(truckTimeTable, warehouse);
        } else if (whTypeEnum.equals(WHTypeEnum.XD)) {
            result = deleteTttFromXd(truckTimeTable, warehouse);
        } else if (whTypeEnum.equals(WHTypeEnum.TXD)) {
            result = deleteTttFromTxd(truckTimeTable);
        }
        return result;
    }

    /**
     * Performs deletion of the TTT in TXD.
     * Only If each Manifest in Manifest Set of given TTT doesn't have any TPA or the Manifest Set is Empty at all. The TTT
     * will be removed. And all ManifestReferences in each Set of Manifests will be removed from TPA from TXD.
     * @param truckTimeTable - to be deleted.
     * @return boolean.
     *      True - if removing was successful;
     *      False - if wasn't;
     */
    private boolean deleteTttFromTxd(TruckTimeTable truckTimeTable) {
        //Get all Manifests from TTT
        Set<Manifest> manifestSet = truckTimeTable.getManifestSet();
        //Filter ManifestSet to get Any of them which have any TPA in CC or XD
        Set<Manifest> manifestsWithTpa = manifestSet.stream()
                .filter(manifest -> !manifest.getTpaSet().isEmpty())
                .collect(Collectors.toSet());
        //If there are no any XD, CC TPA in any Manifest
        if(manifestsWithTpa.isEmpty()){
            // Delete connection of All ManifestReference entities with their TPA and save them to DB
            manifestSet.stream()
                    .flatMap(manifest -> manifest.getManifestsReferenceSet().stream())
                    .forEach(mR -> {
                        mR.setTpa(null);
                        manifestReferenceService.save(mR);
                    });
            manifestService.getWarehouseManifestService().deleteAllByTtt(truckTimeTable);
            tttService.deleteTtt(truckTimeTable);
            return true;
        }
        return false;
    }

    /**
     * Deletes the TTT if it is for Warehouse which has type XD. Also deletes all manifests in TPA this manifests belongs to.
     * If Manifests should go through TXD, method will create new TTT in TXD from CC or Direct connection
     * @param truckTimeTable - TruckTimeTable to delete.
     * @param warehouse      - Warehouse which has this TTT.
     * @return boolean. True - if TTT was deleted, False - wasn't.
     * False:
     * - If manifest has TTT in CC and in XD but not in the TXD.
     * True:
     * - if TTT was deleted.
     */
    private boolean deleteTttFromXd(TruckTimeTable truckTimeTable, Warehouse warehouse) {
        Set<Manifest> manifestSet = truckTimeTable.getManifestSet();

        //if there any manifest which has TTT in CC and doesn't in TXD. If they are, so TTT from XD couldn't be deleted.
        Set<Manifest> manifestSetCcToXD = manifestSet.stream()
                .filter(manifest -> {
                    //filtering of manifests which don't have TTT in TXD
                    Set<WHTypeEnum> whTypeEnums = manifest.getTruckTimeTableSet().stream().map(ttt -> ttt.getWarehouse().getWhType().getType()).collect(Collectors.toSet());
                    return !whTypeEnums.contains(WHTypeEnum.TXD);
                })
                //filtering of manifests which have TTT in CC
                .filter(manifest -> manifest.getTruckTimeTableSet().stream()
                        .anyMatch(ttt -> ttt.getWarehouse().getWhType().getType().equals(WHTypeEnum.CC)))
                .collect(Collectors.toSet());

        if (!manifestSetCcToXD.isEmpty()) {
            log.info("TTT {} has manifests which should be dispatched from XD to customer and these manifests have TTT in CC", truckTimeTable.getTruckName());
            return false;
        }
        //When all conditions are OK:
        String tttName = truckTimeTable.getTruckName();
        manifestSet.forEach(manifest -> {
            //Does manifest have to arrive to TXD?
            TruckTimeTable oldTttTxd = manifest.getTruckTimeTableSet().stream().filter(ttt -> ttt.getWarehouse().getWhType().getType().equals(WHTypeEnum.TXD)).findFirst().orElse(null);
            TruckTimeTable newTtt = new TruckTimeTable();
            if (oldTttTxd != null) {
                //Remove Old TTT for TXD from TttSet in manifest
                manifest.getTruckTimeTableSet().remove(oldTttTxd);
                //Filling info in new TTT
                newTtt = fillingInfoInNewTTT(tttName, oldTttTxd);
                //if such TTT as newTtt is already existing in DB or not
                if (tttService.isTttExisting(newTtt)) {
                    newTtt = tttService.getTttByTruckNameAndTttArrivalDatePlan(newTtt);
                }
                // add manifest to manifestSet and save newTtt
                newTtt.getManifestSet().add(manifest);
                newTtt = tttService.save(newTtt);

                //Change connection WarehouseManifest in next Warehouse (XD or TXD)
                WarehouseManifest warehouseManifest = manifestService.getWarehouseManifestService().findByTttAndManifest(oldTttTxd, manifest);
                warehouseManifest.setTtt(newTtt);
                manifestService.getWarehouseManifestService().save(warehouseManifest);
            }
            //Change TPA set for manifest - delete only TPA from Warehouse(warehouse) the TTT(truckTimeTable) comes to.
            manifest.setTpaSet(manifest.getTpaSet().stream()
                    .filter(tpa -> !tpa.getTpaDaysSetting().getWhCustomer().getWarehouse().equals(warehouse))
                    .collect(Collectors.toSet()));
        });

        manifestService.getWarehouseManifestService().deleteAllByTtt(truckTimeTable);
        tttService.deleteTtt(truckTimeTable);
        manifestService.saveAll(new ArrayList<>(manifestSet));
        return true;
    }

    /**
     * Deletes the TTT if it is for Warehouse which has type CC. Also deletes all manifests in TPA this manifests belongs to.
     * @param truckTimeTable - TruckTimeTable to delete.
     * @param warehouse      - Warehouse which has this TTT.
     * @return boolean. True - if TTT was deleted. Never returns False.
     */
    private boolean deleteTttFromCc(TruckTimeTable truckTimeTable, Warehouse warehouse) {
        Set<Manifest> manifestSet = truckTimeTable.getManifestSet();
        String tttName = truckTimeTable.getTruckName();

        // Stream dedicated to update all manifests from TTT in way to delete them from TPA of the Warehouse.
        manifestSet.forEach(manifest -> {
            //Does manifest have to arrive to TXD?
            TruckTimeTable oldTttTxd = manifest.getTruckTimeTableSet().stream().filter(ttt -> ttt.getWarehouse().getWhType().getType().equals(WHTypeEnum.TXD)).findFirst().orElse(null);
            //Does manifest have to arrive to XD?
            TruckTimeTable oldTtt = manifest.getTruckTimeTableSet().stream().filter(ttt -> ttt.getWarehouse().getWhType().getType().equals(WHTypeEnum.XD)).findFirst().orElse(oldTttTxd);
            //Delete connection WarehouseManifest for this Manifest and current Warehouse
            manifestService.getWarehouseManifestService().deleteByTttAndManifest(truckTimeTable, manifest);

            TruckTimeTable newTtt = new TruckTimeTable();
            if (oldTtt != null) {
                //Remove manifest from set of manifests in Old TTT
                oldTtt.getManifestSet().remove(manifest);
                tttService.save(oldTtt);
                //Filling info in new TTT
                newTtt = fillingInfoInNewTTT(tttName, oldTtt);
                //if such TTT as newTtt is already existing in DB or not
                if (tttService.isTttExisting(newTtt)) {
                    newTtt = tttService.getTttByTruckNameAndTttArrivalDatePlan(newTtt);
                }

                // add manifest to manifestSet and save newTtt
                newTtt.getManifestSet().add(manifest);
                newTtt = tttService.save(newTtt);

                //Change connection WarehouseManifest in next Warehouse (XD or TXD)
                WarehouseManifest warehouseManifest = manifestService.getWarehouseManifestService().findByTttAndManifest(oldTtt, manifest);
                warehouseManifest.setTtt(newTtt);
                manifestService.getWarehouseManifestService().save(warehouseManifest);
            }

            //Change TPA set for manifest - delete only TPA from Warehouse(warehouse) the TTT(truckTimeTable) comes to.
            manifest.setTpaSet(manifest.getTpaSet().stream()
                    .filter(tpa -> !tpa.getTpaDaysSetting().getWhCustomer().getWarehouse().equals(warehouse))
                    .collect(Collectors.toSet()));
        });
        tttService.deleteTtt(truckTimeTable);
        manifestService.saveAll(new ArrayList<>(manifestSet));
        return true;
    }

    /**
     * Filling info in new TTT
     * @param tttName - Name of the New TTT
     * @param oldTtt  - TTT donor of the info
     * @return - New TTT with filled info.
     */
    private TruckTimeTable fillingInfoInNewTTT(String tttName, TruckTimeTable oldTtt) {
        TruckTimeTable newTtt = new TruckTimeTable();
        newTtt.setTruckName(tttName);
        newTtt.setTttArrivalDatePlan(oldTtt.getTttArrivalDatePlan());
        newTtt.setWarehouse(oldTtt.getWarehouse());
        newTtt.setTttStatus(oldTtt.getTttStatus());
        newTtt.setIsActive(true);
        newTtt.setManifestSet(new HashSet<>());
        return newTtt;
    }

    private LocalTime getLocalTimeFromString(String timeString) {
        int hours = Integer.parseInt(timeString.substring(0, 2));
        int minutes = Integer.parseInt(timeString.substring(3));
        return LocalTime.of(hours, minutes);
    }

    public void removeManifestFromWarehouse(Manifest manifest, TruckTimeTable ttt) {
        if(ttt.getManifestSet().contains(manifest)){
            ttt.getManifestSet().remove(manifest);
            WarehouseManifest warehouseManifest = manifestService.getWarehouseManifestService().findByTttAndManifest(ttt, manifest);
            TPA tpa = warehouseManifest.getTpa();
            if(tpa != null){
                tpa.getManifestSet().remove(manifest);
                tpaService.save(tpa);
            }
            manifestService.getWarehouseManifestService().deleteByTttAndManifest(ttt, manifest);
            tttService.save(ttt);
        }
    }

    public void removeManifestFromTpa(WarehouseManifest warehouseManifest, TPA tpa) {
        tpa.getManifestSet().remove(warehouseManifest.getManifest());
        getTpaService().save(tpa);
        warehouseManifest.setTpa(null);
        manifestService.getWarehouseManifestService().save(warehouseManifest);
    }

    public TPA moveManifestFromCurrentTpaToAnother(Warehouse warehouse, TPA tpaFrom, TPA tpaTo, Manifest manifest) {
        WarehouseManifest warehouseManifest = manifestService.getWarehouseManifestByWarehouseAndManifest(warehouse, manifest);
        warehouseManifest.setTpa(tpaTo);
        manifest.getTpaSet().remove(tpaFrom);
        manifest.getTpaSet().add(tpaTo);
        manifestService.save(manifest);
        tpaFrom = tpaService.getTpaById(tpaFrom.getTpaID());
        manifestService.getWarehouseManifestService().save(warehouseManifest);
        return tpaFrom;
    }

    public List<TPA> getNotClosedTpaForCertainWhCustomer(WhCustomer whCustomer) {
        return tpaService.getAllNotClosedForWhCustomer(whCustomer);
    }
}
