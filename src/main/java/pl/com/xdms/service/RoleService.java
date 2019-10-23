package pl.com.xdms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.user.Role;
import pl.com.xdms.domain.user.RoleEnum;
import pl.com.xdms.repository.RoleRepository;

import java.util.List;
import java.util.Optional;

/**
 * Created on 15.10.2019
 * @author Mykola Horkov
 * mykola.horkov@gmail.com
 */
@Service
public class RoleService {
    private static final Logger LOG = LoggerFactory.getLogger(RoleService.class);
    private RoleRepository roleRepository;

    @Value("${default.user.role.name}")
    private String defaultUserRole;
    private RoleEnum roleEnum;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role getRoleById(Long id) {
        Optional<Role> roleOpt = roleRepository.findById(id);
        if (roleOpt.isPresent()) {
            return roleOpt.get();
        } else {
            return getDefaultUserRole();
        }
    }

    public Role getDefaultUserRole(){
        this.roleEnum = RoleEnum.valueOf(defaultUserRole);
        return roleRepository.findRoleByName(roleEnum);
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
