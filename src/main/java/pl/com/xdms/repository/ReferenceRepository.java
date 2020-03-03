package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.com.xdms.domain.reference.Reference;

import java.util.List;
import java.util.Optional;

/**
 * Created on 19.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */

public interface ReferenceRepository extends JpaRepository<Reference, Long> {

    @Query(nativeQuery = true)
    List<Reference> findReferenceInSearch(String searchWord);

    @Query(nativeQuery = true)
    List<Reference> findAllByIsActiveEquals(Boolean isActive);

    List<Reference> findAllByOrderByNameAsc();
    List<Reference> findAllByOrderByNameDesc();

    List<Reference> findAllByOrderByNumberAsc();
    List<Reference> findAllByOrderByNumberDesc();

    List<Reference> findAllByOrderByHsCodeAsc();
    List<Reference> findAllByOrderByHsCodeDesc();

    List<Reference> findAllByOrderByCustomerAsc();
    List<Reference> findAllByOrderByCustomerDesc();

    List<Reference> findAllByOrderBySupplierAsc();
    List<Reference> findAllByOrderBySupplierDesc();

    Optional<Reference> findReferenceBySupplierAgreement(String agreement);


}
