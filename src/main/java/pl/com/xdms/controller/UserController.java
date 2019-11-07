package pl.com.xdms.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.user.Role;
import pl.com.xdms.domain.user.User;
import pl.com.xdms.service.RequestErrorService;
import pl.com.xdms.service.UserService;

import javax.validation.Valid;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("admin/users")
public class UserController {

    private final UserService userService;
    private final RequestErrorService requestErrorService;

    @Autowired
    public UserController(UserService userService, RequestErrorService requestErrorService) {
        this.userService = userService;
        this.requestErrorService = requestErrorService;
    }

    @GetMapping
    public List<User> getAllUsers(){
        return userService.getUsers("default", "default");
    }

    @GetMapping({"orderby/{orderBy}/{direction}", "ordered/{orderBy}"})
    public List<User> getAllUsers(@PathVariable String orderBy, @PathVariable String direction){
        return userService.getUsers(orderBy, direction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        if (user != null){
            log.info("found {}", user);
            return ResponseEntity.ok(user);
        } else {
            log.warn("not found, returning error");
            return ResponseEntity.notFound().build();
        }
    }

    @SuppressWarnings("Duplicates")
    @PutMapping(headers="Accept=application/json")
    public ResponseEntity<User> updateUser(@RequestBody @Valid User updatedUser, BindingResult bindingResult){
        if (bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(updatedUser);
        }
        User repositoryUser = userService.updateUser(updatedUser);
        return (repositoryUser != null)
                ? ResponseEntity.ok(repositoryUser)
                : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(user);
        }
        log.info("Try to create reference {}",user);
        userService.save(user);
        return ResponseEntity.status(201).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUserById(@PathVariable Long id){
        boolean success = userService.deleteUser(id);
        if (success){
            log.info("User {} deleted", id);
            return ResponseEntity.ok("deleted");
        } else {
            log.info("User wasn't find, returning error");
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/userroles")
    public List<Role> getAllRoles(){
        return userService.getAllRoles();
    }
}
