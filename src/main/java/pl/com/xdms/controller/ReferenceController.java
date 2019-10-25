package pl.com.xdms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.service.CustomerAgreementService;
import pl.com.xdms.service.ReferenceService;
import pl.com.xdms.service.SupplierAgreementService;

import java.util.List;

/**
 * Created on 19.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@RestController
@RequestMapping("coordinator/references")
public class ReferenceController {
    private static final Logger LOG = LoggerFactory.getLogger(ReferenceController.class);

    private final ReferenceService referenceService;
    private final SupplierAgreementService supplierAgreementService;
    private final CustomerAgreementService customerAgreementService;

    @Autowired
    public ReferenceController(ReferenceService referenceService,
                               SupplierAgreementService supplierAgreementService,
                               CustomerAgreementService customerAgreementService) {
        this.referenceService = referenceService;
        this.supplierAgreementService = supplierAgreementService;
        this.customerAgreementService = customerAgreementService;
    }

    @GetMapping
    public List<Reference> getAllReferences() {
        return referenceService.getAllReferences();
    }

    @GetMapping({"/{orderBy}/{direction}", "/{orderBy}"})
    public List<Reference> getAllReferences(@PathVariable String orderBy, @PathVariable String direction) {
        return referenceService.getAllReferences(orderBy, direction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reference> getReferenceById(@PathVariable Long id){
        Reference reference = referenceService.getRefById(id);
        if (reference != null){
            LOG.info("Reference found {}", reference);
            return ResponseEntity.ok(reference);
        } else {
            LOG.warn("Reference wasn't found, returning error");
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Reference> updateReference(@RequestBody Reference reference){
        Reference repositoryReference = referenceService.updateReference(reference);
        return (repositoryReference != null)
                ? ResponseEntity.ok(repositoryReference)
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addReference(@RequestBody Reference reference){
        LOG.info("Try to create reference with Id:{} , number:{}",reference.getReferenceID(), reference.getNumber());
        referenceService.save(reference);
    }

    @GetMapping("/search/{searchString}")
    public List<Reference> searchReferencesByString(@PathVariable String searchString){
        return referenceService.search(searchString);
    }



}
