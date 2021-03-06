package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.reference.Reference;
import pl.com.xdms.domain.supplier.Supplier;
import pl.com.xdms.repository.ReferenceRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created on 19.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Slf4j
@Service
public class ReferenceService {

    private final ReferenceRepository referenceRepository;
    private final CustomerService customerService;
    private final SupplierService supplierService;

    @Autowired
    public ReferenceService(ReferenceRepository referenceRepository, CustomerService customerService, SupplierService supplierService) {
        this.referenceRepository = referenceRepository;
        this.customerService = customerService;
        this.supplierService = supplierService;
    }

    public List<Reference> getAllReferences() {
        return referenceRepository.findAll();
    }

    public Reference getRefById(Long id) {
        Optional<Reference> refOpt = referenceRepository.findById(id);
        return refOpt.orElse(null);
    }

    public Reference updateReference(Reference reference) {
        Optional<Reference> referenceToUpdate = referenceRepository.findById(reference.getReferenceID());
        if (referenceToUpdate.isPresent()) {
            referenceRepository.save(reference);
        } else {
            return null;
        }
        return referenceRepository.findById(reference.getReferenceID()).orElse(null);
    }

    public void save(Reference reference) {
        referenceRepository.save(reference);
    }

    public void save(List<Reference> references) {
        referenceRepository.saveAll(references);
    }

    public List<Reference> search(String searchString) {
        return referenceRepository.findReferenceInSearch(searchString);
    }

    public List<Reference> getAllReferences(String orderBy, String direction) {
        switch (orderBy) {
            case "number":
                return "asc".equals(direction)
                        ? referenceRepository.findAllByOrderByNumberAsc()
                        : referenceRepository.findAllByOrderByNumberDesc();
            case "name":
                return "asc".equals(direction)
                        ? referenceRepository.findAllByOrderByNameAsc()
                        : referenceRepository.findAllByOrderByNameDesc();
            case "hscode":
                return "asc".equals(direction)
                        ? referenceRepository.findAllByOrderByHsCodeAsc()
                        : referenceRepository.findAllByOrderByHsCodeDesc();
            case "sname":
                return "asc".equals(direction)
                        ? referenceRepository.findAllByOrderBySupplierAsc()
                        : referenceRepository.findAllByOrderBySupplierDesc();
            case "cname":
                return "asc".equals(direction)
                        ? referenceRepository.findAllByOrderByCustomerAsc()
                        : referenceRepository.findAllByOrderByCustomerDesc();
                default:
                    return  referenceRepository.findAll();
        }
    }

    public List<Reference> getReferenceWhereIsActive(Boolean isActive) {
        return referenceRepository.findAllByIsActiveEquals(isActive);
    }

    public Reference getRefByAgreement(String agreement) {
        log.info("Searching for agreement {}", agreement);
        Optional<Reference> refOpt = referenceRepository.findReferenceBySupplierAgreement(agreement);
        return refOpt.orElse(null);
    }

    public List<Reference> getAllReferencesBySupplierAndCustomer(Long supplierId, Long customerId) {
        Supplier supplier = supplierService.getSupplierById(supplierId);
        Customer customer = customerService.getCustomerById(customerId);

        if(supplier == null || customer ==null)
            return new ArrayList<>();

        return referenceRepository.findAllByCustomerAndSupplier(customer, supplier);
    }
}
