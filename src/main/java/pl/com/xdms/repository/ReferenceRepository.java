package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.com.xdms.domain.reference.Reference;

import java.util.List;

/**
 * Created on 19.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */

public interface ReferenceRepository extends JpaRepository<Reference, Long> {

    @Query(nativeQuery = true)
    List<Reference> findReferenceInSearch(String searchWord);

    List<Reference> findAllByOrderByNameAsc();
    List<Reference> findAllByOrderByNameDesc();

    List<Reference> findAllByOrderByNumberAsc();
    List<Reference> findAllByOrderByNumberDesc();

    List<Reference> findAllByOrderByHsCodeAsc();
    List<Reference> findAllByOrderByHsCodeDesc();

    //TODO inspect queries to make propper sorting. resulting JSON is too long, DTO usage?
/*
    @Query(nativeQuery = true)
    List<Reference> findAllByOrdOrderBySupplierNameAsc();
    @Query(nativeQuery = true)
    List<Reference> findAllByOrdOrderBySupplierNameDesc();

    @Query(nativeQuery = true)
    List<Reference> findAllByOrdOrderByCustomerNameAsc();
    @Query(nativeQuery = true)
    List<Reference> findAllByOrdOrderByCustomerNameDesc();
*/

}
