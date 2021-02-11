package pl.com.xdms.service.truck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.tpa.*;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.repository.TPARepository;
import pl.com.xdms.repository.TpaStatusRepository;
import pl.com.xdms.service.WhCustomerService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created on 08.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */

@Service
@Slf4j
public class TPAService {
    private final TPARepository tpaRepository;
    private final TpaStatusRepository tpaStatusRepository;
    private final TpaDaysSettingsService tpaDaysSettingsService;
    private final WorkingDayService workingDayService;
    private final WhCustomerService whCustomerService;

    @Autowired
    public TPAService(TPARepository tpaRepository, TpaStatusRepository tpaStatusRepository,
                      TpaDaysSettingsService tpaDaysSettingsService, WorkingDayService workingDayService,
                      WhCustomerService whCustomerService) {
        this.tpaRepository = tpaRepository;
        this.tpaStatusRepository = tpaStatusRepository;
        this.tpaDaysSettingsService = tpaDaysSettingsService;
        this.workingDayService = workingDayService;
        this.whCustomerService = whCustomerService;
    }

    public TpaStatus getTpaStatusByEnum (TPAEnum tpaEnum){
        Optional<TpaStatus> tpaStatusOptional = tpaStatusRepository.findByStatusName(tpaEnum);
        return tpaStatusOptional.orElse(null);
    }

    public boolean isTpaExisting(TPA tpa) {
        return getTpaByTpaNameAndTpaETDPlan(tpa) != null;
    }

    private TPA getTpaByTpaNameAndTpaETDPlan(TPA tpa) {
        Optional<TPA> tpaFromDB = tpaRepository.findByTpaNameAndTpaETDPlan(tpa.getName(),
                tpa.getDeparturePlan());
        return tpaFromDB.orElse(null);
    }

    public List<TPA> saveAll(List<TPA> tpaSet) {
        return tpaRepository.saveAll(tpaSet);
    }

    public TPA save(TPA tpa){
        return tpaRepository.save(tpa);
    }

    public List<TPA> getAllTpa() {
        return tpaRepository.findAll();
    }

    public List<TPA> getTpaByWarehouseAndDay(Warehouse warehouse, String tpaDepartureDatePlan) {
        //convert String to LocalDate
        LocalDate localDate = LocalDate.parse(tpaDepartureDatePlan);

        Long dayNumber = (long) localDate.getDayOfWeek().getValue();
        WorkingDay workingDay = workingDayService.getWorkingDayByNumber(dayNumber);
        List<WhCustomer> whCustomerList = whCustomerService.getAllWhCustomerByWarehouse(warehouse);

        Set<TpaDaysSetting> tpaDaysSettingSet = tpaDaysSettingsService.getAllTpaDaySettingsByListOfWhCustomerAndWorkingDay(whCustomerList, workingDay);
        return tpaRepository.findByTpaDaysSettingInAndDeparturePlanStartsWith(tpaDaysSettingSet, tpaDepartureDatePlan);
    }

    public TPA getTpaByWarehouseAndId(Long tpaId, Warehouse warehouse){
        TPA tpa = getTpaById(tpaId);
        if(tpa != null && !tpa.getTpaDaysSetting().getWhCustomer().getWarehouse().equals(warehouse)){
            return null;
        }
        return tpa;
    }

    public List<TPA> getTpaByWarehouseAndDayLike(Warehouse warehouse, String tpaDepartureDatePlan){
        List<TPA> tpaList = tpaRepository.findAllByDeparturePlanStartingWith(tpaDepartureDatePlan);
        return tpaList.stream()
                .filter(tpa -> tpa.getTpaDaysSetting().getWhCustomer().getWarehouse().equals(warehouse))
                .collect(Collectors.toList());
    }

    public TPA getTpaById(Long id) {
        return tpaRepository.findById(id).orElse(null);
    }

    public void removeTpaById(Long id) {
        tpaRepository.deleteById(id);
    }

    public List<TPA> getAllDelayedForWarehouse(Warehouse warehouse) {
        List<WhCustomer> whCustomerList = whCustomerService.getAllWhCustomerByWarehouse(warehouse);
        List<TpaDaysSetting> tpaDaysSettingList = tpaDaysSettingsService.getAllTpaDaySettingsInWhCustomerList(whCustomerList);
        TpaStatus tpaStatus = tpaStatusRepository.findByStatusName(TPAEnum.DELAYED).orElse(null);
        return tpaRepository.findAllByStatusEqualsAndTpaDaysSettingIn(tpaStatus, tpaDaysSettingList);
    }

    public List<TPA> getAllBufferedForWarehouse(Warehouse warehouse) {
        List<WhCustomer> whCustomerList = whCustomerService.getAllWhCustomerByWarehouse(warehouse);
        List<TpaDaysSetting> tpaDaysSettingList = tpaDaysSettingsService.getAllTpaDaySettingsInWhCustomerList(whCustomerList);
        TpaStatus tpaStatus = tpaStatusRepository.findByStatusName(TPAEnum.BUFFER).orElse(null);
        return tpaRepository.findAllByStatusEqualsAndTpaDaysSettingIn(tpaStatus, tpaDaysSettingList);
    }

    public List<TPA> getAllClosedForWarehouse(Warehouse warehouse) {
        List<WhCustomer> whCustomerList = whCustomerService.getAllWhCustomerByWarehouse(warehouse);
        List<TpaDaysSetting> tpaDaysSettingList = tpaDaysSettingsService.getAllTpaDaySettingsInWhCustomerList(whCustomerList);
        TpaStatus tpaStatus = tpaStatusRepository.findByStatusName(TPAEnum.CLOSED).orElse(null);
        return tpaRepository.findAllByStatusEqualsAndTpaDaysSettingIn(tpaStatus, tpaDaysSettingList);
    }

    public List<TPA> getAllInProgressForWarehouse(Warehouse warehouse) {
        List<WhCustomer> whCustomerList = whCustomerService.getAllWhCustomerByWarehouse(warehouse);
        List<TpaDaysSetting> tpaDaysSettingList = tpaDaysSettingsService.getAllTpaDaySettingsInWhCustomerList(whCustomerList);
        TpaStatus tpaStatus = tpaStatusRepository.findByStatusName(TPAEnum.IN_PROGRESS).orElse(null);
        return tpaRepository.findAllByStatusEqualsAndTpaDaysSettingIn(tpaStatus, tpaDaysSettingList);
    }
    
    public List<TPA> getAllNotClosedForWarehouse(Warehouse warehouse) {
        List<WhCustomer> whCustomerList = whCustomerService.getAllWhCustomerByWarehouse(warehouse);
        List<TpaDaysSetting> tpaDaysSettingList = tpaDaysSettingsService.getAllTpaDaySettingsInWhCustomerList(whCustomerList);
        TpaStatus tpaStatus = tpaStatusRepository.findByStatusName(TPAEnum.CLOSED).orElse(null);
        return tpaRepository.findAllByStatusIsNotInAndTpaDaysSettingIn(tpaStatus, tpaDaysSettingList);
    }

    public List<TPA> getAllNotClosedForWhCustomer(WhCustomer whCustomer) {
        List<TpaDaysSetting> tpaDaysSettingList = tpaDaysSettingsService.getAllTpaDaySettingsInWhCustomerList(Collections.singletonList(whCustomer));
        TpaStatus tpaStatus = tpaStatusRepository.findByStatusName(TPAEnum.CLOSED).orElse(null);
        return tpaRepository.findAllByStatusIsNotInAndTpaDaysSettingIn(tpaStatus, tpaDaysSettingList);
    }
}
