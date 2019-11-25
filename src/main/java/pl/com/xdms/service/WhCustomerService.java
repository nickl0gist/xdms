package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;
import pl.com.xdms.repository.WhCustomerRepository;

import java.util.List;

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

}
