package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.SupplierService;

import javax.validation.Valid;
import java.util.List;

/**
 * Created on 17.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */

@Slf4j
@RestController
@RequestMapping("coordinator/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public SupplierController(SupplierService supplierService, RequestErrorService requestErrorService) {
        this.supplierService = supplierService;
        this.requestErrorService = requestErrorService;
    }

    @GetMapping
    public List<Supplier> getAllSuppliers(){
        return supplierService.getAllSuppliers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id){
        Supplier supplier = supplierService.getSupplierById(id);
        if (supplier != null){
            log.info("Supplier was found {}", supplier);
            return ResponseEntity.ok(supplier);
        } else {
            log.warn("Supplier with id: {} not found, returning error", id);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/active")
    public List<Supplier> getActiveSuppliers(){
        return supplierService.getSuppliersWhereIsActive(true);
    }

    @GetMapping("/not_active")
    public List<Supplier> getNotActiveSuppliers(){
        return supplierService.getSuppliersWhereIsActive(false);
    }

    @GetMapping({"ordered_by/{orderBy}/{direction}", "ordered_by/{orderBy}"})
    public List<Supplier> getAllSuppliersOrderedBy(@PathVariable String orderBy, @PathVariable String direction){
        return supplierService.getAllSuppliersOrderedBy(orderBy, direction);
    }

    @GetMapping("/search/{searchString}")
    @ResponseStatus(HttpStatus.OK)
    public List<Supplier> searchSupplierByString(@PathVariable String searchString){
        return supplierService.search(searchString);
    }

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



