package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pl.com.xdms.domain.customer.Customer;

import java.util.List;
import java.util.Optional;

/**
 * Created on 02.11.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByName(String name);

    @Query(nativeQuery = true)
    List<Customer> findCustomerInSearch(String searchWord);

    @Query(nativeQuery = true)
    List<Customer> findAllByIsActiveEquals(Boolean isActive);

    List<Customer> findAllByOrderByNameAsc();
    List<Customer> findAllByOrderByNameDesc();

    List<Customer> findAllByOrderByCustomerCodeAsc();
    List<Customer> findAllByOrderByCustomerCodeDesc();

    List<Customer> findAllByOrderByCountryAsc();
    List<Customer> findAllByOrderByCountryDesc();

    List<Customer> findAllByOrderByPostCodeAsc();
    List<Customer> findAllByOrderByPostCodeDesc();

    List<Customer> findAllByOrderByStreetAsc();
    List<Customer> findAllByOrderByStreetDesc();

}
