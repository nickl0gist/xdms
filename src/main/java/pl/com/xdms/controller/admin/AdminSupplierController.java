package pl.com.xdms.controller.admin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.SupplierService;

import javax.validation.Valid;

/**
 * Created on 01.09.2020
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RestController
@Slf4j
@RequestMapping("admin/suppliers")
public class AdminSupplierController {
    private final SupplierService supplierService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public AdminSupplierController(SupplierService supplierService, RequestErrorService requestErrorService) {
        this.supplierService = supplierService;
        this.requestErrorService = requestErrorService;
    }

    /**
     * The endpoint is for changing the information about supplier in DB
     * @param supplier - Supplier Entity given by user
     * @param bindingResult - for checking the conditions
     * @return - 200 if update was successful,
     * 422 - if update wasn't successful
     * 404 - if Supplier wasn't found by given id
     */
    @SuppressWarnings("Duplicates")
    @PutMapping
    public ResponseEntity<Supplier> updateSupplier(@RequestBody @Valid Supplier supplier, BindingResult bindingResult){
        log.info("Try to update supplier with Id:{}", supplier.getSupplierID());
        if (bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(supplier);
        }
        Supplier repositorySupplier = supplierService.updateSupplier(supplier);
        return (repositorySupplier != null)
                ? ResponseEntity.ok(repositorySupplier)
                : ResponseEntity.notFound().build();
    }

    /**
     * The endpoint is for creating new Supplier in the DB
     * @param supplier - Supplier Entity given by user
     * @param bindingResult - for checking the conditions
     * @return - 201 if the Supplier was created successfully.
     * 422 - if wasn't due to violated conditions.
     */
    @SuppressWarnings("Duplicates")
    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@RequestBody @Valid Supplier supplier, BindingResult bindingResult){
        log.info("Try to create supplier with Name: {}, from: {}", supplier.getName(), supplier.getCountry());
        if(bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(supplier);
        }
        supplierService.save(supplier);
        return ResponseEntity.status(201).build();
    }
}
