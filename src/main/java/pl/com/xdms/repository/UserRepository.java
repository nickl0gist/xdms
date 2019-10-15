package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.com.xdms.domain.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
