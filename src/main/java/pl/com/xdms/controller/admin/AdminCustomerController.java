package pl.com.xdms.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.service.CustomerService;
import pl.com.xdms.service.RequestErrorService;

import javax.validation.Valid;

/**
 * Created on 02.09.2020
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RestController
@Slf4j
@RequestMapping("admin/customers")
public class AdminCustomerController {
    private final CustomerService customerService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public AdminCustomerController(@Lazy CustomerService customerService, RequestErrorService requestErrorService) {
        this.customerService = customerService;
        this.requestErrorService = requestErrorService;
    }

    /**
     * The Endpoint is for update the information about Customer.
     * @param customer - Customer Entity to be updated
     * @param bindingResult - to check conditions
     * @return - Status 200 if update was successful.
     * - 422 if any condition was violated.
     * - 404 if no Customer was found by given Id.
     */
    @SuppressWarnings("Duplicates")
    @PutMapping
    public ResponseEntity<Customer> updateCustomer(@RequestBody @Valid Customer customer, BindingResult bindingResult){
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

    /**
     * Endpoint is for adding new customer into the system.
     * @param customer -  Customer Entity sent by user which has to be updated
     * @param bindingResult - to check conditions
     * @return - Status 201 if Customer was added successfully
     * 422 - if any condition was violated.
     */
    @SuppressWarnings("Duplicates")
    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody @Valid Customer customer, BindingResult bindingResult){
        log.info("Try to create customer with Name: {}, from: {}", customer.getName(), customer.getCountry());
        if(bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(customer);
        }
        customerService.save(customer);
        return ResponseEntity.status(201).build();
    }

}
