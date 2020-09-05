package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.service.ReferenceService;
import pl.com.xdms.service.RequestErrorService;

import javax.validation.Valid;
import java.util.List;

/**
 * Created on 19.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RestController
@RequestMapping("coordinator/references")
@Slf4j
public class ReferenceController {

    private final ReferenceService referenceService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public ReferenceController(ReferenceService referenceService, RequestErrorService requestErrorService) {
        this.referenceService = referenceService;
        this.requestErrorService = requestErrorService;
    }

    /**
     * For getting all the References
     * @return List\<Reference\>
     */
    @GetMapping
    public List<Reference> getAllReferences() {
        return referenceService.getAllReferences();
    }

    /**
     * Get All references ordered by certain parameter.
     * @param orderBy one of the parameters: "number, name, hscode, sname, cname"
     * @param direction asc or desc.
     * @return List\<Reference\>
     */
    @GetMapping({"/ordered_by/{orderBy}/{direction}", "/ordered_by/{orderBy}"})
    public List<Reference> getAllReferences(@PathVariable String orderBy, @PathVariable String direction) {
        return referenceService.getAllReferences(orderBy, direction);
    }

    /**
     * Used to obtain certain reference.
     * @param id - Long Id of the reference
     * @return Status 200 if reference was found, 404 - if wasn't.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Reference> getReferenceById(@PathVariable Long id) {
        Reference reference = referenceService.getRefById(id);
        if (reference != null) {
            log.info("Reference found {}", reference);
            return ResponseEntity.ok(reference);
        } else {
            log.warn("Reference wasn't found, returning error");
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Used to get references with status <b>isActive = true</b>
     * @return List\<Reference\>
     */
    @GetMapping("/active")
    public List<Reference> getActiveSuppliers(){
        return referenceService.getReferenceWhereIsActive(true);
    }

    /**
     * Used to get List of the References which are being shipped from certain supplier to certain customer
     * @param supplierId - Long Id of supplier
     * @param customerId - Long Id of the customer
     * @return List\<Reference\>
     */
    @GetMapping("/supplier/{supplierId}/customer/{customerId}")
    public List<Reference> getReferencesBySupplierAndCustomer(@PathVariable Long supplierId, @PathVariable Long customerId){
        return referenceService.getAllReferencesBySupplierAndCustomer(supplierId, customerId);
    }

    /**
     * Used to get references with status <b>isActive = false</b>
     * @return List\<Reference\>
     */
    @GetMapping("/not_active")
    public List<Reference> getNotActiveSuppliers(){
        return referenceService.getReferenceWhereIsActive(false);
    }

    /**
     * Used to search reference using next parameters: r.name, r.hs_code, r.designationen, r.designationru,
     * r.customer_agreement, r.supplier_agreement, c.name, s.name.
     * @param searchString - string used to Search with
     * @return - List\<Reference\>
     */
    @GetMapping("/search/{searchString}")
    @ResponseStatus(HttpStatus.OK)
    public List<Reference> searchReferencesByString(@PathVariable String searchString) {
        return referenceService.search(searchString);
    }

    /**
     * Endpoint used ti update existing reference in DB.
     * @param reference - Reference entity to be updated.
     * @param bindingResult - for checking conditions.
     * @return - Status 200 - if update was successful,
     * 404 - if the id of the reference wasn't found in DB.
     * - 422 - if any of the conditions was corrupted
     */
    @SuppressWarnings("Duplicates")
    @PutMapping
    public ResponseEntity<Reference> updateReference(@RequestBody @Valid Reference reference, BindingResult bindingResult) {
        log.info("Try to update reference ID: {}, number:{}", reference.getReferenceID(), reference.getNumber());
        if (bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(reference);
        }
        Reference repositoryReference = referenceService.updateReference(reference);
        return (repositoryReference != null)
                ? ResponseEntity.ok(repositoryReference)
                : ResponseEntity.notFound().build();
    }

    /**
     * Endpoint is for adding ew reference to DB
     * @param reference - Reference Entity to be saved i DB
     * @param bindingResult - for checking conditions.
     * @return Status 201 - if reference was persisted successfully,
     * - 422 - if any of the conditions was corrupted
     */
    @PostMapping
    public ResponseEntity<Reference> addReference(@RequestBody @Valid Reference reference, BindingResult bindingResult) {
        log.info("Try to create reference number:{}, SA: {}", reference.getNumber(), reference.getSupplierAgreement());
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(422).headers(requestErrorService.getErrorHeaders(bindingResult)).body(reference);
        }
        referenceService.save(reference);
        return ResponseEntity.status(201).build();
    }
}
