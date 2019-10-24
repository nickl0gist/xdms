package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.supplier.SupplierAgreement;

/**
 * Created on 23.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface SupplierAgreementRepository extends JpaRepository<SupplierAgreement, Long> {

}
