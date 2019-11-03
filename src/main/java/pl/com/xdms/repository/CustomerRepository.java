package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.customer.Customer;

import java.util.Optional;

/**
 * Created on 02.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByName(String name);
}
