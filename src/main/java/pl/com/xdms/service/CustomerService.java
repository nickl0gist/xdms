package pl.com.xdms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.repository.CustomerRepository;

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
        if(customerOptional.isPresent()){
            return customerOptional.get();
        } else {
            return null;
        }
    }
}
