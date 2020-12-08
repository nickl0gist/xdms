package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.service.CustomerService;

import java.util.List;

/**
 * Created on 20.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("coordinator/customers")
@CrossOrigin
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(@Lazy CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * The Endpoint used to obtain all the customers from DB
     * @return List\<Customer\>
     */
    @GetMapping
    public List<Customer> getAllCustomers(){
        return customerService.getAllCustomers();
    }

    /**
     * The Endpoint is for getting certain customer from DB
     * @param id - Long Id given by user
     * @return - Status 200 if Customer was found
     * Status 404 - if Customer wasn't found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id){
        Customer customer = customerService.getCustomerById(id);
        if (customer != null){
            log.info("Customer was found {}", customer);
            return ResponseEntity.ok(customer);
        } else {
            log.warn("Customer with id: {} not found, returning error", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get only active customers
     * @return List\<Customer\>
     */
    @GetMapping("/active")
    public List<Customer> getActiveCustomers(){
        return customerService.getCustomersWhereIsActive(true);
    }

    /**
     * Get only not active customers
     * @return List\<Customer\>
     */
    @GetMapping("/not_active")
    public List<Customer> getNotActiveCustomers(){
        return customerService.getCustomersWhereIsActive(false);
    }

    /**
     * Get all customers ordered by parameters.
     * @param orderBy : "customer_code, name, country, post_code, street"
     * @param direction: asc, desc.
     * @return List\<Customer\>
     */
    @GetMapping({"/ordered_by/{orderBy}/{direction}", "/ordered_by/{orderBy}"})
    public List<Customer> getAllCustomersOrderedBy(@PathVariable String orderBy, @PathVariable String direction){
        return customerService.getAllCustomersOrderedBy(orderBy, direction);
    }

    /**
     * Searching for Customer within next parameters: customer_code, name, country, post_code, city, street, email
     * @param searchString - string to be searched
     * @return List\<Customer\>
     */
    @GetMapping("/search/{searchString}")
    @ResponseStatus(HttpStatus.OK)
    public List<Customer> searchCustomerByString(@PathVariable String searchString){
        return customerService.search(searchString);
    }
}
