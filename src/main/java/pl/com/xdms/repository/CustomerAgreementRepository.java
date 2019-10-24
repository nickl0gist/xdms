package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.customer.CustomerAgreement;

/**
 * Created on 23.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface CustomerAgreementRepository extends JpaRepository<CustomerAgreement, Long> {

}
