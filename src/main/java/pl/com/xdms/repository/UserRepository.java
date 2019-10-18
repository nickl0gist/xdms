package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.com.xdms.domain.user.User;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    List<User> findAllByOrderByUsernameAsc();
    List<User> findAllByOrderByUsernameDesc();

    List<User> findAllByOrderByFirstNameAsc();
    List<User> findAllByOrderByFirstNameDesc();

    List<User> findAllByOrderByLastNameAsc();
    List<User> findAllByOrderByLastNameDesc();

    List<User> findAllByOrderByRoleAsc();
    List<User> findAllByOrderByRoleDesc();
}
