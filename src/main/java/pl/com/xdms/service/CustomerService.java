package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created on 02.11.2019
 *
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final WhCustomerService whCustomerService;
    private WarehouseService warehouseService;

    @Autowired
    public CustomerService(CustomerRepository customerRepository,
                           WhCustomerService whCustomerService) {
        this.customerRepository = customerRepository;
        this.whCustomerService = whCustomerService;
    }

    @Autowired
    public void setWarehouseService(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    public Customer getCustomerByName(String name) {
        Optional<Customer> customerOptional = customerRepository.findByName(name);
        return customerOptional.orElse(null);
    }

    public Customer getCustomerById(Long id) {
        Optional<Customer> customerOptional = customerRepository.findById(id);
        return customerOptional.orElse(null);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> getCustomersWhereIsActive(Boolean isActive) {
        return customerRepository.findAllByIsActiveEquals(isActive);
    }

    public List<Customer> getAllCustomersOrderedBy(String orderBy, String direction) {
        switch (orderBy) {
            case "customer_code":
                return "asc".equals(direction)
                        ? customerRepository.findAllByOrderByCustomerCodeAsc()
                        : customerRepository.findAllByOrderByCustomerCodeDesc();
            case "name":
                return "asc".equals(direction)
                        ? customerRepository.findAllByOrderByNameAsc()
                        : customerRepository.findAllByOrderByNameDesc();
            case "country":
                return "asc".equals(direction)
                        ? customerRepository.findAllByOrderByCountryAsc()
                        : customerRepository.findAllByOrderByCountryDesc();
            case "post_code":
                return "asc".equals(direction)
                        ? customerRepository.findAllByOrderByPostCodeAsc()
                        : customerRepository.findAllByOrderByPostCodeDesc();
            case "street":
                return "asc".equals(direction)
                        ? customerRepository.findAllByOrderByStreetAsc()
                        : customerRepository.findAllByOrderByStreetDesc();
            default:
                return getAllCustomers();
        }
    }

    public List<Customer> search(String searchString) {
        return customerRepository.findCustomerInSearch(searchString);
    }

    public void save(Customer customer) {
        log.info("Customer {} is persisted in DB, from {}", customer.getName(), customer.getCountry());
        Customer customerPersisted = customerRepository.save(customer);
        whCustomerConnectionsCreation(customerPersisted);
    }

    /**
     * Creates connections with all warehouses in DB
     * @param customer - new persisted Customer Entity in DB. It will get new connections with all Warehouses
     *                 except the Warehouses which have their <tt>url_code</tt>
     *                 the same as <tt>customer_code</tt> od customer.
     */
    private void whCustomerConnectionsCreation(Customer customer) {
        log.info("Customer {} will be connected with Warehouses:{}", customer.getName());
        List<Warehouse> warehouseList = warehouseService.getAllWarehouses();
        warehouseList.stream()
                .filter(w -> !w.getUrlCode().equals(customer.getCustomerCode()))
                .forEach(x -> whCustomerService.createWhCustomer(x, customer));
    }

    public void save(List<Customer> customerList) {
        customerRepository.saveAll(customerList);
    }

    public Customer updateCustomer(Customer customer) {
        Optional<Customer> supplierOptional = customerRepository.findById(customer.getCustomerID());
        if (supplierOptional.isPresent()) {
            customerRepository.save(customer);
        }
        return customerRepository.findById(customer.getCustomerID()).orElse(null);
    }
}
