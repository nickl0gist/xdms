package pl.com.xdms.service.truck;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.tpa.TpaDaysSetting;
import pl.com.xdms.domain.tpa.WorkingDay;
import pl.com.xdms.domain.warehouse.WhCustomer;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 08.12.2019
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

    @Autowired
    public TruckService(TPAService tpaService,
                        TTTService tttService,
                        TpaDaysSettingsService tpaDaysSettingsService,
                        WorkingDayService workingDayService) {

        this.tpaService = tpaService;
        this.tttService = tttService;
        this.tpaDaysSettingsService = tpaDaysSettingsService;
        this.workingDayService = workingDayService;
    }

    /**
     * Calculates ZonedDateTima of ETD manifest from Warehouse according to existing TPA days settings for
     * current Warehouse and Customer.
     * @param dateTimeETD Estimated DateTime of manifest Departure from Warehouse
     * @param whCustomer - the pair of Warehouse and Customer (WhCustomer)
     * @return map with one element inside. @code key is ZonedDateTime of the moment when tpa will be closed,
     * @code Value is TpaDaysSetting chosen from the list
     */
    public Map<ZonedDateTime,TpaDaysSetting> getAppropriateTpaSetting(ZonedDateTime dateTimeETD, WhCustomer whCustomer) {

        // if etdDayOfWeek is Saturday or Sunday move it to friday before
        if (dateTimeETD.getDayOfWeek().equals(DayOfWeek.of(6))) {
            dateTimeETD = dateTimeETD.minusDays(1);
        } else if (dateTimeETD.getDayOfWeek().equals(DayOfWeek.of(7))) {
            dateTimeETD = dateTimeETD.minusDays(2);
        }

        //Getting Working day from DB by dateTimeETD day Of Week
        WorkingDay workingDay = workingDayService.getWorkingDayByNumber((long) dateTimeETD.getDayOfWeek().getValue());

        //Getting list of TpaDaysSetting for current Wh_Customer and Working day
        List<TpaDaysSetting> listOfTpaSettingsForDepartureDay = tpaDaysSettingsService.getTpaDaySettingsByWarehouseAndWorkingDay(whCustomer, workingDay);

        TpaDaysSetting chosenSetting = getTpaDaysSetting(dateTimeETD, listOfTpaSettingsForDepartureDay);

        for (int i = 0; i < 5 && chosenSetting == null; i++) {

            if (workingDay.getDayName().getValue() == 1) {
                workingDay.setDayName(DayOfWeek.of(5));
                dateTimeETD = dateTimeETD.minusDays(3);
            } else {
                workingDay.setDayName(workingDay.getDayName().minus(1));
                dateTimeETD = dateTimeETD.minusDays(1);
            }
            chosenSetting = getTpaDaysSetting(dateTimeETD, tpaDaysSettingsService.getTpaDaySettingsByWarehouseAndWorkingDay(whCustomer, workingDay));
        }

        Map<ZonedDateTime,TpaDaysSetting> result = new HashMap<>();
        result.put(dateTimeETD, chosenSetting);
        return result;
    }

    /**
     * Checks received List of TpaDaysSettings with ETD and calculate appropriate TpaDaysSettings.
     * If there no suitable TPAs for ETD day null will be returned
     * @param dateTimeETD - ZonedDateTime of estimated departure from Warehouse
     * @param listOfTpaSettingsForDepartureDay - list of settings supposed TpaDaySettings.
     * @return calculated TpaDaySAettings or null if there no suitable settings found.
     */
    private TpaDaysSetting getTpaDaysSetting(ZonedDateTime dateTimeETD, List<TpaDaysSetting> listOfTpaSettingsForDepartureDay) {
        TpaDaysSetting chosenSetting = null;

        //approx 1 year in minutes
        long i = -500000;

        for (TpaDaysSetting daysSetting : listOfTpaSettingsForDepartureDay) {
            ZonedDateTime tpaDateTime = ZonedDateTime.of(dateTimeETD.toLocalDate(), daysSetting.getLocalTime(), dateTimeETD.getZone());
            long checkSum = Duration.between(dateTimeETD, tpaDateTime).toMinutes();
            if (checkSum <= 0 && checkSum > i) {
                i = checkSum;
                chosenSetting = daysSetting;
            }
        }
        return chosenSetting;
    }
}
