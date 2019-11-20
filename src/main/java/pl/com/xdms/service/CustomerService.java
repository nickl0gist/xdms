package pl.com.xdms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created on 02.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer getCustomerByName(String name){
        Optional<Customer> customerOptional = customerRepository.findByName(name);
        return  customerOptional.orElse(null);
    }

    public Customer getCustomerById (Long id){
        Optional<Customer> customerOptional = customerRepository.findById(id);
        return  customerOptional.orElse(null);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> getCustomersWhereIsActive(Boolean isActive) {
        return customerRepository.findAllByIsActiveEquals(isActive);
    }

    public List<Customer> getAllCustomersOrderedBy(String orderBy, String direction) {
        switch (orderBy){
            case"customer_code":
                return "asc".equals(direction)
                        ? customerRepository.findAllByOrderByCustomerCodeAsc()
                        : customerRepository.findAllByOrderByCustomerCodeDesc();
            case"name":
                return "asc".equals(direction)
                        ? customerRepository.findAllByOrderByNameAsc()
                        : customerRepository.findAllByOrderByNameDesc();
            case"country":
                return "asc".equals(direction)
                        ? customerRepository.findAllByOrderByCountryAsc()
                        : customerRepository.findAllByOrderByCountryDesc();
            case"post_code":
                return "asc".equals(direction)
                        ? customerRepository.findAllByOrderByPostCodeAsc()
                        : customerRepository.findAllByOrderByPostCodeDesc();
            case"street":
                return "asc".equals(direction)
                        ? customerRepository.findAllByOrderByStreetAsc()
                        : customerRepository.findAllByOrderByStreetDesc();
            default: return getAllCustomers();
        }
    }

    public List<Customer> search(String searchString) {
        return customerRepository.findCustomerInSearch(searchString);
    }

    public void save(Customer customer) {
        customerRepository.save(customer);
    }

    public void save(List<Customer> customerList) {
        customerRepository.saveAll(customerList);
    }

    public Customer updateCustomer(Customer customer) {
        Optional<Customer> supplierOptional = customerRepository.findById(customer.getCustomerID());
        if(supplierOptional.isPresent()){
            customerRepository.save(customer);
        }
        return customerRepository.findById(customer.getCustomerID()).orElse(null);
    }
}
