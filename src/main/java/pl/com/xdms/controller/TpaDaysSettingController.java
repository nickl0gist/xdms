package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.tpa.TpaDaysSetting;
import pl.com.xdms.domain.tpa.WorkingDay;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.WarehouseService;
import pl.com.xdms.service.truck.TruckService;

import javax.validation.Valid;
import java.util.List;

/**
 * Created on 14.06.2020
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@PropertySource("classpath:messages.properties")
@RequestMapping("warehouse/{urlCode:^[a-z_]{5,8}$}/tpa_settings")
public class TpaDaysSettingController {

    @Value("${error.http.message}")
    String errorMessage;

    @Value("${message.http.message}")
    String messageMessage;

    private final TruckService truckService;
    private final WarehouseService warehouseService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public TpaDaysSettingController(TruckService truckService, WarehouseService warehouseService, RequestErrorService requestErrorService) {
        this.truckService = truckService;
        this.warehouseService = warehouseService;
        this.requestErrorService = requestErrorService;
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
    @GetMapping("/{whCustomerId:^[0-9]*$}/{workingDayId:^[1-7]$}")
    public ResponseEntity<List<TpaDaysSetting>> getAllTpaDaysSettingsByWhCustomerIdAndWorkingDayId(@PathVariable String urlCode, @PathVariable Long whCustomerId, @PathVariable Long workingDayId){
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        WhCustomer whCustomer = warehouseService.getWhCustomerById(whCustomerId);
        WorkingDay workingDay = warehouseService.getWorkingDayById(workingDayId);

        if(whCustomer == null || workingDay == null || !whCustomer.getWarehouse().equals(warehouse)){
            log.info("The wrong parameters were passed to request Warehouse {}, whCustomerId={}, workingDayId={}; WhCustomer is null:{}", urlCode, whCustomerId, workingDayId, whCustomer == null);
            return ResponseEntity.notFound().header(errorMessage, "Wrong parameters passed to request").build();
        }
        if(!whCustomer.getIsActive()){
            return ResponseEntity.unprocessableEntity().header(errorMessage, String.format("The Connection of Warehouse=%s and Customer=%s is not active", whCustomer.getWarehouse().getName(), whCustomer.getCustomer().getName())).build();
        }
        List<TpaDaysSetting> tpaDaysSettings = truckService.getTpaDaysSettingsService().getTpaDaySettingsByWhCustomerAndWorkingDay(whCustomer, workingDay);
        return ResponseEntity.ok(tpaDaysSettings);
    }

    /**
     * Endpoint dedicated for updating particular TpaDaysSetting. Only the setLocalTime and The setTransitTime fields
     * may be affected.
     * @param tpaSetting - TpaDaysSetting Entity to be updated.
     * @param bindingResult - for checking conditions
     * @return TpaDaysSetting if was found and updated. Response statuses:
     * - 200 - if TpaDaysSetting was updated;
     * - 400 - if ID of the given entity is Null;
     * - 404 - If Id of given entity wasn't found in DB;
     * - 412 - If annotation conditions of TpaDaysSetting was violated.
     */
    @PutMapping("")
    public ResponseEntity<TpaDaysSetting> updateTpaDaySetting(@PathVariable String urlCode, @RequestBody @Valid TpaDaysSetting tpaSetting, BindingResult bindingResult){
        HttpHeaders headers = new HttpHeaders();
        Warehouse warehouse = warehouseService.getWarehouseByUrl(urlCode);
        Long id = tpaSetting.getId();
        if (id != null){
            TpaDaysSetting tpaDaysSettingFromDb = truckService.getTpaDaysSettingsService().getTpaDaySettingsById(tpaSetting.getId());

            if(tpaDaysSettingFromDb == null || !tpaDaysSettingFromDb.getWhCustomer().getWarehouse().equals(warehouse)){
                log.info("TpaDaysSetting with id={} wasn't found", id);
                headers.add(errorMessage, String.format("TpaDaysSetting with id=%d wasn't found", id));
                return ResponseEntity.notFound().headers(headers).build();
            } else if (bindingResult.hasErrors()) {
                //if given entity doesn't correspond conditions of parameters annotation in TpaDaysConditions class
                headers = requestErrorService.getErrorHeaders(bindingResult);
                return ResponseEntity.status(412).headers(headers).body(tpaSetting);
            }
            tpaDaysSettingFromDb.setLocalTime(tpaSetting.getLocalTime());
            tpaDaysSettingFromDb.setTransitTime(tpaSetting.getTransitTime());
            tpaDaysSettingFromDb = truckService.getTpaDaysSettingsService().update(tpaDaysSettingFromDb);
            log.info("TpaDaysSetting with id={} was updated", id);
            headers.add(messageMessage, String.format("TpaDaysSetting with id=%d was updated", id));
            return ResponseEntity.ok().headers(headers).body(tpaDaysSettingFromDb);
        }
        return ResponseEntity.badRequest().build();
    }
}
