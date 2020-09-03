package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.service.SupplierService;

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

    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    /**
     * Get all Suppliers in the System.
     * @return List\<Supplier\>
     */
    @GetMapping
    public List<Supplier> getAllSuppliers(){
        return supplierService.getAllSuppliers();
    }

    /**
     * Get certain Supplier by Id.
     * @param id - id provided by User.
     * @return - 200 and Supplier if was found.
     * 404 - if Supplier wasn't found by given Id.
     */
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

    /**
     * Get only active suppliers.
     * @return List\<Supplier\>
     */
    @GetMapping("/active")
    public List<Supplier> getActiveSuppliers(){
        return supplierService.getSuppliersWhereIsActive(true);
    }

    /**
     * Get only not active suppliers.
     * @return List\<Supplier\>
     */
    @GetMapping("/not_active")
    public List<Supplier> getNotActiveSuppliers(){
        return supplierService.getSuppliersWhereIsActive(false);
    }

    /**
     * Get all suppliers ordered by parameters.
     * @param orderBy : "vendor_code", "name", "country", "post_code", "street".
     * @param direction: asc, desc.
     * @return List\<Supplier\>
     */
    @GetMapping({"/ordered_by/{orderBy}/{direction}", "/ordered_by/{orderBy}"})
    public List<Supplier> getAllSuppliersOrderedBy(@PathVariable String orderBy, @PathVariable String direction){
        return supplierService.getAllSuppliersOrderedBy(orderBy, direction);
    }

    /**
     * Searching for Suppliers within next parameters: name, country, post_code, city, street, email.
     * @param searchString - string to be searched.
     * @return List\<Supplier\>
     */
    @GetMapping("/search/{searchString}")
    @ResponseStatus(HttpStatus.OK)
    public List<Supplier> searchSupplierByString(@PathVariable String searchString){
        return supplierService.search(searchString);
    }
}



