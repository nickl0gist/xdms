package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.tpa.WorkingDay;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.repository.WhCustomerRepository;
import pl.com.xdms.service.truck.TpaDaysSettingsService;
import pl.com.xdms.service.truck.WorkingDayService;

import java.util.List;
import java.util.Optional;

/**
 * Created on 25.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Slf4j
public class WhCustomerService {

    private final WhCustomerRepository whCustomerRepository;
    private final TpaDaysSettingsService tpaDaysSettingsService;
    private final WorkingDayService workingDayService;

    @Autowired
    public WhCustomerService(WhCustomerRepository whCustomerRepository,
                             TpaDaysSettingsService tpaDaysSettingsService,
                             WorkingDayService workingDayService) {
        this.whCustomerRepository = whCustomerRepository;
        this.tpaDaysSettingsService = tpaDaysSettingsService;
        this.workingDayService = workingDayService;
    }

    public List<WhCustomer> getAllWhCustomerByWarehouse(Warehouse warehouse){
        return whCustomerRepository.findAllByWarehouseOrderByCustomer(warehouse);
    }

    public List<WhCustomer> getAllWhCustomersByWarehouseIsActive(Warehouse warehouse){
        return whCustomerRepository.findAllByWarehouseAndIsActiveTrueOrderByCustomer(warehouse);
    }

    public List<WhCustomer> getAllWhCustomersByWarehouseNotActive(Warehouse warehouse){
        return whCustomerRepository.findAllByWarehouseAndIsActiveFalseOrderByCustomer(warehouse);
    }
    
    public void save(WhCustomer whCustomer){
        whCustomerRepository.save(whCustomer);
    }

    /**
     * Takes Id from <tt>whCustomer</tt>, fetch WhCustomer by id from DB.
     * Updates only isActive field in WhCustomer entity from Database.
     * @param whCustomer - WhCustomer Entity from request
     * @return saved <tt>whCustomer</tt> or null if whCustomer wasn't found.
     */
    public WhCustomer update(WhCustomer whCustomer) {
        Long id = whCustomer.getWhCustomerID();
        if(id == null){
            return null;
        }
        Optional<WhCustomer> whCustomerOptional = whCustomerRepository.findById(id);
        WhCustomer whCustomerFromRepository;

        if(whCustomerOptional.isPresent()){
            whCustomerFromRepository = whCustomerOptional.get();
            whCustomerFromRepository.setIsActive(whCustomer.getIsActive());
            save(whCustomerFromRepository);
        } else {
            whCustomerFromRepository = null;
        }
        return whCustomerFromRepository;
    }

    public WhCustomer findWhCustomerById(Long id) {
        Optional<WhCustomer> whCustomerOptional = whCustomerRepository.findById(id);
        return whCustomerOptional.orElse(null);
    }


    void createWhCustomer(Warehouse warehouse, Customer customer){
        WhCustomer whCustomer = new WhCustomer();
        whCustomer.setCustomer(customer);
        whCustomer.setWarehouse(warehouse);
        whCustomer.setIsActive(false);

        WhCustomer whCustomerPersisted = whCustomerRepository.save(whCustomer);

        //Add new TpaDaysSetting to database connection of WarehouseCustomer and Each working day
        workingDayService.getAllWorkingDays().forEach(x -> tpaDaysSettingsService.addNewSetting(whCustomerPersisted, x));

    }

    public WhCustomer findByWarehouseAndCustomer(Warehouse warehouse, Customer customer){
        Optional<WhCustomer> whCustomerOptional = whCustomerRepository.findByWarehouseAndCustomer(warehouse, customer);
        return whCustomerOptional.orElse(null);
    }

    public Long getTTminutes(WhCustomer whCustomer){
        String stamp = whCustomer.getTransitTime();
        long minutes = Long.parseLong(stamp.substring(stamp.indexOf('H')+1, stamp.indexOf('M')));
        long hours = Long.parseLong(stamp.substring(stamp.indexOf('T')+1, stamp.indexOf('H')));
        long days = Long.parseLong(stamp.substring(stamp.indexOf('P')+1, stamp.indexOf('D')));
        minutes += hours * 60 + days * 1440;

        return minutes;
    }

    public WorkingDay getWorkingDyById(Long workingDayId) {
        return workingDayService.getWorkingDayByNumber(workingDayId);
    }
}
