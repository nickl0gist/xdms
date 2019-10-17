package pl.com.xdms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.user.User;
import pl.com.xdms.service.UserService;

import java.util.List;

@RestController
@RequestMapping("admin/user")
public class UserController {

    private final UserService userService;
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("")
    public List<User> getAllUsers(){
        return userService.getUsers();
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    void addUser(@RequestBody User user) {
        LOG.info(user.getRole().toString());
        userService.save(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        if (user != null){
            LOG.info("found {}", user);
            return ResponseEntity.ok(user);
        } else {
            LOG.warn("not found, returning error");
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id){
        boolean success = userService.deleteUser(id);
        if (success){
            LOG.info("User {} deleted", id);
            return ResponseEntity.ok("deleted");
        } else {
            LOG.warn("User wasn't find, returning error");
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUserById(@RequestBody User updatedUser){
        User repositoryUser = userService.updateUser(updatedUser);
        return (repositoryUser != null)
                ? ResponseEntity.ok(repositoryUser)
                : ResponseEntity.notFound().build();
    }

}
