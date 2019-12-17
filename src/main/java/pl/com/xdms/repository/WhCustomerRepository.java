package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.customer.Customer;
import pl.com.xdms.domain.warehouse.Warehouse;
import pl.com.xdms.domain.warehouse.WhCustomer;

import java.util.List;
import java.util.Optional;

/**
 * Created on 25.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface WhCustomerRepository extends JpaRepository<WhCustomer, Long> {

    List<WhCustomer> findAllByWarehouseOrderByCustomer (Warehouse warehouse);

    List<WhCustomer> findAllByWarehouseAndIsActiveTrueOrderByCustomer(Warehouse warehouse);

    List<WhCustomer> findAllByWarehouseAndIsActiveFalseOrderByCustomer(Warehouse warehouse);

    Optional<WhCustomer> findByWarehouseAndCustomer(Warehouse warehouse, Customer customer);
}
