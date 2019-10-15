package pl.com.xdms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.com.xdms.domain.user.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findRoleByName(String name);
}
