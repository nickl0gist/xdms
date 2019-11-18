package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.com.xdms.domain.supplier.Supplier;

import java.util.List;
import java.util.Optional;

/**
 * Created on 02.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByName(String name);

    @Query(nativeQuery = true)
    List<Supplier> findSupplierInSearch(String searchWord);

    @Query(nativeQuery = true)
    List<Supplier> findAllByIsActiveEquals(Boolean isActive);

    List<Supplier> findAllByOrderByVendorCodeAsc();
    List<Supplier> findAllByOrderByVendorCodeDesc();

    List<Supplier> findAllByOrderByNameAsc();
    List<Supplier> findAllByOrderByNameDesc();

    List<Supplier> findAllByOrderByCountryAsc();
    List<Supplier> findAllByOrderByCountryDesc();

    List<Supplier> findAllByOrderByPostCodeAsc();
    List<Supplier> findAllByOrderByPostCodeDesc();

    List<Supplier> findAllByOrderByStreetAsc();
    List<Supplier> findAllByOrderByStreetDesc();
}
