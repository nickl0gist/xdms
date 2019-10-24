package pl.com.xdms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.customer.CustomerAgreement;
import pl.com.xdms.repository.CustomerAgreementRepository;

import java.util.Optional;

/**
 * Created on 23.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
public class CustomerAgreementService {
    private static final Logger LOG = LoggerFactory.getLogger(CustomerAgreementService.class);
    private CustomerAgreementRepository customerAgreementRepository;

    @Autowired
    public CustomerAgreementService(CustomerAgreementRepository customerAgreementRepository) {
        this.customerAgreementRepository = customerAgreementRepository;
    }

    private CustomerAgreement getAgreementById(Long id){
        Optional<CustomerAgreement> customerAgreementToFind = customerAgreementRepository.findById(id);
        return customerAgreementToFind.isPresent()
                ? customerAgreementToFind.get()
                : null;
    }
}
