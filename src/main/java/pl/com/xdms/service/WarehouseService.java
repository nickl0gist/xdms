package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.warehouse.WHType;
import pl.com.xdms.domain.warehouse.WHTypeEnum;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.repository.WHTypeRepository;
import pl.com.xdms.repository.WarehouseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created on 23.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Slf4j
public class WarehouseService {

    @Value("${default.whType.name}")
    private String defaultWHType;

    private final WarehouseRepository warehouseRepository;
    private final WHTypeRepository whTypeRepository;
    private final WhCustomerService whCustomerService;
    private CustomerService customerService;

    @Autowired
    public WarehouseService(WarehouseRepository warehouseRepository,
                            WHTypeRepository whTypeRepository,
                            WhCustomerService whCustomerService) {
        this.warehouseRepository = warehouseRepository;
        this.whTypeRepository = whTypeRepository;
        this.whCustomerService = whCustomerService;
    }

    @Autowired
    public void setCustomerService(CustomerService customerService){
        this.customerService = customerService;
    }

    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAll();
    }

    public Warehouse getWarehouseById(Long id) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findById(id);
        return warehouseOptional.orElse(null);
    }

    public List<Warehouse> getSuppliersWhereIsActive(Boolean isActive) {
        return warehouseRepository.findAllByIsActiveEquals(isActive);
    }

    public List<Warehouse> getAllSuppliersOrderedBy(String orderBy, String direction) {
        switch (orderBy) {
            case "city":
                return "asc".equals(direction)
                        ? warehouseRepository.findAllByOrderByCityAsc()
                        : warehouseRepository.findAllByOrderByCityDesc();
            case "country":
                return "asc".equals(direction)
                        ? warehouseRepository.findAllByOrderByCountryAsc()
                        : warehouseRepository.findAllByOrderByCountryDesc();
            case "street":
                return "asc".equals(direction)
                        ? warehouseRepository.findAllByOrderByStreetAsc()
                        : warehouseRepository.findAllByOrderByStreetDesc();
            case "post_code":
                return "asc".equals(direction)
                        ? warehouseRepository.findAllByOrderByPostCodeAsc()
                        : warehouseRepository.findAllByOrderByPostCodeDesc();
            case "name":
                return "asc".equals(direction)
                        ? warehouseRepository.findAllByOrderByNameAsc()
                        : warehouseRepository.findAllByOrderByNameDesc();
            default:
                return getAllWarehouses();
        }
    }

    public List<Warehouse> search(String searchString) {
        return warehouseRepository.findAllWarehousesInSearch(searchString);
    }

    public Warehouse updateWarehouse(Warehouse warehouse) {
        Optional<Warehouse> supplierOptional = warehouseRepository.findById(warehouse.getWarehouseID());
        if(supplierOptional.isPresent()){
            warehouseRepository.save(warehouse);
        }
        return warehouseRepository.findById(warehouse.getWarehouseID()).orElse(null);
    }

    public void save(Warehouse warehouse) {
        //If the WHType is entity with nulled params the default WHType will be assigned
        if(warehouse.getWhType().getWhTypeID() == null){
            warehouse.setWhType(getDefaultWHType());
        } else {
            //Taking out the ID of role from request and find existing role
            warehouse.setWhType(getWHTypeByID(warehouse.getWhType().getWhTypeID()));
        }
        String warehouseInfo = "WH Name: " + warehouse.getName()
                + " / Country:" + warehouse.getCountry()
                + " / City: " + warehouse.getCity();

        log.info("Warehouse Creation: {}", warehouseInfo);
        whCustomerConnectionsCreation(warehouseRepository.save(warehouse));
    }

    /**
     * New warehouse will be persisted in Customer table with the same info.
     * But it shouldn't have connection with itself in table WhCustomer. All warehouses, which are already existed
     * will have this reflection of Warehouse like Customer, will have connection with Customer created from Warehouse.
     * @param warehouse - new Entity which will get new connections.
     */
    private void whCustomerConnectionsCreation(Warehouse warehouse) {
        log.info("Warehouse gets list of customers:{}", warehouse.getUrlCode());
        List<Customer> customerList = customerService.getAllCustomers();
        customerList.forEach(x -> whCustomerService.createWhCustomer(warehouse, x));
        //warehouse
        warehouseToCustomer(warehouse);
    }

    /**
     * Converter Warehouse to Customer.
     * @param warehouse - Warehouse Entity
     */
    private void warehouseToCustomer(Warehouse warehouse) {
        Customer customerFromWarehouse = new Customer();
        customerFromWarehouse.setCustomerCode(warehouse.getUrlCode());
        customerFromWarehouse.setName(warehouse.getName());
        customerFromWarehouse.setIsActive(true);
        customerFromWarehouse.setCity(warehouse.getCity());
        customerFromWarehouse.setCountry(warehouse.getCountry());
        customerFromWarehouse.setEmail(warehouse.getEmail());
        customerFromWarehouse.setPostCode(warehouse.getPostCode());
        customerFromWarehouse.setStreet(warehouse.getStreet());
        customerFromWarehouse.setTimeZone(warehouse.getTimeZone());
        customerService.save(customerFromWarehouse);
    }

    private WHType getWHTypeByID(Long id){
        return whTypeRepository.findById(id).orElse(getDefaultWHType());
    }

    private WHType getDefaultWHType() {
        WHTypeEnum whTypeEnum = WHTypeEnum.valueOf(defaultWHType);
        return whTypeRepository.findWHTypeByType(whTypeEnum);
    }

    public Warehouse getWarehouseByUrl(String wh_url) {
        Optional<Warehouse> warehouseOptional = warehouseRepository.findByUrlCode(wh_url);
        return warehouseOptional.orElse(null);
    }

    //TODO Change not found from null to empty entity?
    public Warehouse getWarehouseByName(String name){
        Optional<Warehouse> warehouse = warehouseRepository.findByName(name);
        return warehouse.orElse(null);
    }

}
