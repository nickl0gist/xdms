package pl.com.xdms.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.user.Role;
import pl.com.xdms.domain.user.User;
import pl.com.xdms.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserService {
    private UserRepository userRepository;
    private RoleService roleService;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    public List<User> getUsers(String orderBy, String direction) {
        switch (orderBy){
            case "firstname":
                return "asc".equals(direction)
                        ? userRepository.findAllByOrderByFirstNameAsc()
                        : userRepository.findAllByOrderByFirstNameDesc();
            case "lastname":
                return "asc".equals(direction)
                        ? userRepository.findAllByOrderByLastNameAsc()
                        : userRepository.findAllByOrderByLastNameDesc();
            case "username":
                return "asc".equals(direction)
                        ? userRepository.findAllByOrderByUsernameAsc()
                        : userRepository.findAllByOrderByUsernameDesc();
            case "role":
                return "asc".equals(direction)
                        ? userRepository.findAllByOrderByRoleAsc()
                        : userRepository.findAllByOrderByRoleDesc();
            default:
                return userRepository.findAll();
        }
    }

    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.orElse(null);
    }

    public boolean deleteUser(Long id) {
        User user = getUserById(id);
        if(user != null){
            userRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    public User updateUser(User user) {
        Optional<User> updatedUser = userRepository.findById(user.getId());
        if (updatedUser.isPresent()) {
            userRepository.save(user);
        } else {
            return null;
        }
        return userRepository.findById(user.getId()).get();
    }

    public void save(User user) {
        //Taking out the ID of role from request and find existing role
        user.setRole(roleService.getRoleById(user.getRole().getId()));

        String userInfo = "FirstName: " + user.getFirstName()
                + " / LastName:" + user.getLastName()
                + " / UserName: " + user.getUsername();

        log.info("User Creation: {}", userInfo);
        userRepository.save(user);
    }

    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }
}
