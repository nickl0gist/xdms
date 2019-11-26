package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.repository.WhCustomerRepository;

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

    @Autowired
    public WhCustomerService(WhCustomerRepository whCustomerRepository) {
        this.whCustomerRepository = whCustomerRepository;
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

    public void createWhCustomer(Warehouse warehouse, Customer customer){
        WhCustomer whCustomer = new WhCustomer();
        whCustomer.setCustomer(customer);
        whCustomer.setWarehouse(warehouse);
        whCustomer.setIsActive(false);
        whCustomerRepository.save(whCustomer);
    }
}
