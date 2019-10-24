package pl.com.xdms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.supplier.SupplierAgreement;
import pl.com.xdms.repository.SupplierAgreementRepository;

import java.util.Optional;

/**
 * Created on 23.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
public class SupplierAgreementService {
    private static final Logger LOG = LoggerFactory.getLogger(SupplierAgreementService.class);
    private SupplierAgreementRepository supplierAgreementRepository;

    @Autowired
    public SupplierAgreementService(SupplierAgreementRepository supplierAgreementRepository) {
        this.supplierAgreementRepository = supplierAgreementRepository;
    }

    public SupplierAgreement getAgreementById(Long id){
        Optional<SupplierAgreement> supplierAgreementToFind = supplierAgreementRepository.findById(id);
        return supplierAgreementToFind.isPresent()
                ? supplierAgreementToFind.get()
                : null;
    }
}
