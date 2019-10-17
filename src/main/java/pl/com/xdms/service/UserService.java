package pl.com.xdms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.domain.user.User;
import pl.com.xdms.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private UserRepository userRepository;
    private RoleService roleService;

    @Autowired
    public UserService(UserRepository userRepository, RoleService roleService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            return null;
        }
    }

    public void saveUser(User user) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(user.getFirstName()).append(" ");
        stringBuilder.append(user.getLastName()).append(" ");
        stringBuilder.append(user.getUsername()).append(" ");
        LOG.info(stringBuilder.toString());
        userRepository.save(user);
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
        LOG.info(user.toString());
        userRepository.save(user);
    }
}
