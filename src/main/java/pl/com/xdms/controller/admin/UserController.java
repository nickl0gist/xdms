package pl.com.xdms.controller.admin;

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

    /**
     * Getting List of All Users in the System
     * @return List\<User\>
     */
    @GetMapping
    public List<User> getAllUsers(){
        return userService.getUsers("default", "default");
    }

    /**
     * Endpoint is for getting Ordered List of Users
     * @param orderBy - "firstname", "lastname", "username", "role"
     * @param direction - asc, desc
     * @return ordered List\<User\>
     */
    @GetMapping({"/orderby/{orderBy}/{direction}", "/ordered/{orderBy}"})
    public List<User> getAllUsers(@PathVariable String orderBy, @PathVariable String direction){
        return userService.getUsers(orderBy, direction);
    }

    /**
     * Endpoint is for getting particular User by his ID
     * @param id - Long Id
     * @return - User
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        if (user != null){
            log.info("User was found {}", user);
            return ResponseEntity.ok(user);
        } else {
            log.warn("User with id: {} not found, returning error", id);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpoint which is used for changing information about User by sending changed JSON entity.
     * @param updatedUser - the updated User Entity.
     * @param bindingResult - for checking conditions.
     * @return Status 200 and the updated User entity if changing was successful.
     * Status 422 if something was wrong.
     * Status 404 if such User with given Id wasn't found.
     */
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

    /**
     * Endpoint is for persisting new User in DB.
     * @param user - User entity received from User.
     * @param bindingResult - for checking conditions.
     * @return - Status 201 if User was successfully created.
     * Status 422 if any condition was violated.
     */
    @PostMapping(headers="Accept=application/json")
    public ResponseEntity<User> addUser(@RequestBody @Valid User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()){
            HttpHeaders headers = requestErrorService.getErrorHeaders(bindingResult);
            return ResponseEntity.status(422).headers(headers).body(user);
        }
        log.info("Try to create user {}",user);
        userService.save(user);
        return ResponseEntity.status(201).build();
    }

    /**
     * Endpoint is for removing existing user from DB.
     * @param id Long Id of the User
     * @return Status 200 if user was removed, status 404 if wasn't so.
     */
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

    /**
     * is for getting List of existing User Roles in the system.
     * @return List \<Role\>
     */
    @GetMapping("/userroles")
    public List<Role> getAllRoles(){
        return userService.getAllRoles();
    }
}
