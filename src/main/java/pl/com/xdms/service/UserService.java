package pl.com.xdms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.com.xdms.controller.UserController;
import pl.com.xdms.domain.user.User;
import pl.com.xdms.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()){
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
        LOG.warn(stringBuilder.toString());
        userRepository.save(user);
    }

    public void deleteUser(User user){
        userRepository.deleteById(user.getId());
    }

    public User updateUser(User user){
        Optional<User> updatedUser = userRepository.findById(user.getId());
        if (updatedUser.isPresent()){
            userRepository.save(user);
        } else {
            return null;
        }
        return user;
    }
}
