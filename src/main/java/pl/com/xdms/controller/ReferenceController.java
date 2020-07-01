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

    @GetMapping
    public List<Reference> getAllReferences() {
        return referenceService.getAllReferences();
    }

    @GetMapping({"/orderby/{orderBy}/{direction}", "/orderby/{orderBy}"})
    public List<Reference> getAllReferences(@PathVariable String orderBy, @PathVariable String direction) {
        return referenceService.getAllReferences(orderBy, direction);
    }

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

    @GetMapping("/active")
    public List<Reference> getActiveSuppliers(){
        return referenceService.getReferenceWhereIsActive(true);
    }

    @GetMapping("/supplier/{supplierId}/customer/{customerId}")
    public List<Reference> getReferencesBySupplierAndCustomer(@PathVariable Long supplierId, @PathVariable Long customerId){
        return referenceService.getAllReferencesBySupplierAndCustomer(supplierId, customerId);
    }

    @GetMapping("/not_active")
    public List<Reference> getNotActiveSuppliers(){
        return referenceService.getReferenceWhereIsActive(false);
    }

    @GetMapping("/search/{searchString}")
    @ResponseStatus(HttpStatus.OK)
    public List<Reference> searchReferencesByString(@PathVariable String searchString) {
        return referenceService.search(searchString);
    }

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
