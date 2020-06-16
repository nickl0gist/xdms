package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.xdms.domain.tpa.TpaDaysSetting;
import pl.com.xdms.domain.tpa.WorkingDay;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.truck.TruckService;

import java.util.List;

/**
 * Created on 14.06.2020
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping
public class TpaDaysSettingController {

    private final TruckService truckService;
    private final WarehouseService warehouseService;

    @Autowired
    public TpaDaysSettingController(TruckService truckService, WarehouseService warehouseService) {
        this.truckService = truckService;
        this.warehouseService = warehouseService;
    }

    /**
     * Endpoint dedicated for receiving List of schedule of outbound trucks from certain warehouse to particular Customer
     * depending of WorkingDay.
     * @param whCustomerId - Long Id of the WhCustomer
     * @param workingDayId - Long Id of the Working Day
     * @return List Of TpaDaySetting for given parameters. Response status:
     *          - 200 - if given Parameters were given in accordance of conditions, and return if any TpaDaySettings
     *                  were found;
     *          - 404 - if WhCustomer id, or id of the WorkingDay were not found in DB;
     *          - 422 - if received WhCustomer entity has status isActive = false.
     */
    @GetMapping("tpa_settings/{whCustomerId:^[0-9]*$}/{workingDayId:^[0-9]*$}")
    public ResponseEntity<List<TpaDaysSetting>> getAllTpaDaysSettingsByWhCustomerIdAndWorkingDayId(@PathVariable Long whCustomerId, @PathVariable Long workingDayId){
        WhCustomer whCustomer = warehouseService.getWhCustomerById(whCustomerId);
        WorkingDay workingDay = warehouseService.getWorkingDayById(workingDayId);
        if(whCustomer == null || workingDay == null){
            log.info("The wrong parameters were passed to request whCustomerId={}, workingDayId={}; WhCustomer is null:{}", whCustomerId, workingDayId, whCustomer == null);
            return ResponseEntity.notFound().header("Error:", "Wrong parameters passed to request").build();
        }
        if(!whCustomer.getIsActive()){
            return ResponseEntity.unprocessableEntity().header("Warning:", String.format("The Connection of Warehouse=%s and Customer=%s is not active", whCustomer.getWarehouse().getName(), whCustomer.getCustomer().getName())).build();
        }
        List<TpaDaysSetting> tpaDaysSettings = truckService.getTpaDaysSettingsService().getTpaDaySettingsByWhCustomerAndWorkingDay(whCustomer, workingDay);
        return ResponseEntity.ok(tpaDaysSettings);
    }
}
