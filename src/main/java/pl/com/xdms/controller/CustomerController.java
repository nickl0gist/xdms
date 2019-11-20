package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.service.CustomerService;
import pl.com.xdms.service.RequestErrorService;

import javax.validation.Valid;
import java.util.List;

/**
 * Created on 20.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@RestController
@RequestMapping("coordinator/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public CustomerController(CustomerService customerService, RequestErrorService requestErrorService) {
        this.customerService = customerService;
        this.requestErrorService = requestErrorService;
    }

    @GetMapping
    public List<Customer> getAllSuppliers(){
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getSupplierById(@PathVariable Long id){
        Customer customer = customerService.getCustomerById(id);
        if (customer != null){
            log.info("Customer was found {}", customer);
            return ResponseEntity.ok(customer);
        } else {
            log.warn("Customer with id: {} not found, returning error", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/active")
    public List<Customer> getActiveCustomers(){
        return customerService.getCustomersWhereIsActive(true);
    }

    @GetMapping("/not_active")
    public List<Customer> getNotActiveSuppliers(){
        return customerService.getCustomersWhereIsActive(false);
    }

    @GetMapping({"/ordered_by/{orderBy}/{direction}", "/ordered_by/{orderBy}"})
    public List<Customer> getAllSuppliersOrderedBy(@PathVariable String orderBy, @PathVariable String direction){
        return customerService.getAllCustomersOrderedBy(orderBy, direction);
    }

    @GetMapping("/search/{searchString}")
    @ResponseStatus(HttpStatus.OK)
    public List<Customer> searchSupplierByString(@PathVariable String searchString){
        return customerService.search(searchString);
    }

    @SuppressWarnings("Duplicates")
    @PutMapping
    public ResponseEntity<Customer> updateSupplier(@RequestBody @Valid Customer customer, BindingResult bindingResult){
        log.info("Try to update customer with Id:{}", customer.getCustomerID());
        if (bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(customer);
        }
        Customer repositoryCustomer = customerService.updateCustomer(customer);
        return (repositoryCustomer != null)
                ? ResponseEntity.ok(repositoryCustomer)
                : ResponseEntity.notFound().build();
    }

    @SuppressWarnings("Duplicates")
    @PostMapping
    public ResponseEntity<Customer> createSupplier(@RequestBody @Valid Customer customer, BindingResult bindingResult){
        log.info("Try to create customer with Name: {}, from: {}", customer.getName(), customer.getCountry());
        if(bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(customer);
        }
        customerService.save(customer);
        return ResponseEntity.status(201).build();
    }
}
