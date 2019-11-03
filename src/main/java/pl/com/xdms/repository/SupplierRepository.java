package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.supplier.Supplier;

import java.util.Optional;

/**
 * Created on 02.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByName(String name);
}
