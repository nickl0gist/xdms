package pl.com.xdms.service.truck;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.manifest.Manifest;
import pl.com.xdms.domain.manifest.ManifestReference;
import pl.com.xdms.domain.tpa.TPA;
import pl.com.xdms.domain.tpa.TpaDaysSetting;
import pl.com.xdms.domain.tpa.WorkingDay;
import pl.com.xdms.domain.trucktimetable.TruckTimeTable;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.service.ManifestReferenceService;
import pl.com.xdms.service.ManifestService;

import javax.transaction.Transactional;
import java.time.*;
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
        List<TpaDaysSetting> listOfTpaSettingsForDepartureDay = tpaDaysSettingsService.getTpaDaySettingsByWarehouseAndWorkingDay(whCustomer, workingDay);

        log.info("The list of TpaDaysSettings - {}", listOfTpaSettingsForDepartureDay);

        TpaDaysSetting chosenSetting = getTpaDaysSetting(dateTimeETD, listOfTpaSettingsForDepartureDay);

        for (int i = 0; i < 5 && chosenSetting == null; i++) {

            if (dateTimeETD.getDayOfWeek().getValue() == 1) {
                dateTimeETD = ZonedDateTime.of(dateTimeETD.minusDays(3).toLocalDate(), LocalTime.of(23, 59), dateTimeETD.getZone());
            } else {
                dateTimeETD = ZonedDateTime.of(dateTimeETD.minusDays(1).toLocalDate(), LocalTime.of(23, 59), dateTimeETD.getZone());
            }

            workingDay = workingDayService.getWorkingDayByNumber((long) dateTimeETD.getDayOfWeek().getValue());
            chosenSetting = getTpaDaysSetting(dateTimeETD, tpaDaysSettingsService.getTpaDaySettingsByWarehouseAndWorkingDay(whCustomer, workingDay));
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

        //4. Add TPA and Manifest to each ManifestReferences. Save all and receive them as entities from DB
        List<ManifestReference> manifestReferencesFromDB = connectManiRefToManifestAndTPA(new ArrayList<>(manifestReferenceSetDTO), manifestsFromDB, tpaListFromDB)
                .stream()
                .filter(manref -> manref.getIsActive() && manref.getManifest() != null)
                .collect(Collectors.toList());
        manifestReferencesFromDB = manifestReferenceService.saveAll(manifestReferencesFromDB);
    }

    /**
     * Add TPA and Manifest to each ManifestReferences.
     * @param manifestReferences - List of manifestReferences entities
     * @param manifestsFromDB - manifests from Database
     * @param tpaListFromDB - TPA list from DB
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

    private LocalTime getLocalTimeFromString (String timeString){
        int hours = Integer.parseInt(timeString.substring(0,2));
        int minutes = Integer.parseInt(timeString.substring(3));
        return LocalTime.of(hours, minutes);
    }
}
