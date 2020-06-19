package pl.com.xdms.service.truck;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.tpa.TpaDaysSetting;
import pl.com.xdms.domain.tpa.WorkingDay;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.repository.TpaDaysSettingsRepository;

import java.util.List;
import java.util.Set;

/**
 * Created on 08.12.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@Service
public class TpaDaysSettingsService {

    private final TpaDaysSettingsRepository settingsRepository;

    @Value("${default.tpa.time}")
    private String defaultTpaTime;

    @Autowired
    public TpaDaysSettingsService(TpaDaysSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public List<TpaDaysSetting> getTpaDaySettingsByWhCustomerAndWorkingDay(WhCustomer whCustomer, WorkingDay workingDay){
        return settingsRepository.findAllByWhCustomerAndWorkingDay(whCustomer, workingDay);
    }

    /**
     * Connecting WarehouseCustomer With Working Day and save it to Database with default
     * Transit Time set as default value in TpaDaysSetting class
     * @param whCustomer WhCustomer
     * @param workingDay WorkingDay
     */
    public void addNewSetting(WhCustomer whCustomer, WorkingDay workingDay) {
        TpaDaysSetting tpaDaysSetting = new TpaDaysSetting();
        tpaDaysSetting.setLocalTime(defaultTpaTime);
        tpaDaysSetting.setWhCustomer(whCustomer);
        tpaDaysSetting.setWorkingDay(workingDay);
        save(tpaDaysSetting);
    }

    /**
     * Method saves given TpaDaysSetting @code tpaDaysSetting
     * If there is TpaSettings in Database for Wh_Customer and Working day from given @code tpaDaysSetting with the same
     * local time, given @code tpaDaysSetting would not be saved. The already persisted entity will be returned.
     * @param tpaDaysSetting to be persisted in DB.
     * @return persisted @code tpaDaysSetting.
     */
    private TpaDaysSetting save(TpaDaysSetting tpaDaysSetting) {
        List<TpaDaysSetting> tpaDaysSettings = getTpaDaySettingsByWhCustomerAndWorkingDay(tpaDaysSetting.getWhCustomer(), tpaDaysSetting.getWorkingDay());

        for (TpaDaysSetting tpaSetting: tpaDaysSettings) {
            if (tpaSetting.getLocalTime().equals(tpaDaysSetting.getLocalTime())){
                return tpaSetting;
            }
        }
        return settingsRepository.save(tpaDaysSetting);
    }


    public Set<TpaDaysSetting> getAllTpaDaySettingsByListOfWhCustomerAndWorkingDay(List<WhCustomer> whCustomerList, WorkingDay workingDay) {
        return settingsRepository.findAllByWhCustomerInAndWorkingDay(whCustomerList, workingDay);
    }

    public TpaDaysSetting getTpaDaySettingsById(Long id) {
        return settingsRepository.findById(id).orElse(null);
    }

    public TpaDaysSetting update(TpaDaysSetting tpaDaysSetting) {
        return settingsRepository.save(tpaDaysSetting);
    }
}
