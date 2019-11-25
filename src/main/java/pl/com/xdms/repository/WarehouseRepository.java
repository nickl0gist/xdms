package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.com.xdms.domain.warehouse.Warehouse;

import java.util.List;
import java.util.Optional;

/**
 * Created on 23.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

    @Query(nativeQuery = true)
    List<Warehouse> findAllWarehousesInSearch(String searchWord);

    @Query(nativeQuery = true)
    List<Warehouse> findAllByIsActiveEquals(Boolean isActive);

    List<Warehouse> findAllByOrderByWhTypeAsc();
    List<Warehouse> findAllByOrderByWhTypeDesc();

    List<Warehouse> findAllByOrderByNameAsc();
    List<Warehouse> findAllByOrderByNameDesc();

    List<Warehouse> findAllByOrderByCountryAsc();
    List<Warehouse> findAllByOrderByCountryDesc();

    List<Warehouse> findAllByOrderByPostCodeAsc();
    List<Warehouse> findAllByOrderByPostCodeDesc();

    List<Warehouse> findAllByOrderByStreetAsc();
    List<Warehouse> findAllByOrderByStreetDesc();

    List<Warehouse> findAllByOrderByCityAsc();
    List<Warehouse> findAllByOrderByCityDesc();

    Optional<Warehouse> findByUrlCode(String url);
}
