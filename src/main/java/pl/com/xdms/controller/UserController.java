package pl.com.xdms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.com.xdms.domain.user.User;
import pl.com.xdms.service.UserService;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("admin/users/getall")
    public List<User> showAllUsers(){
        return userService.getUsers();
    }

    @PostMapping("admin/users/create")
    @ResponseStatus(HttpStatus.CREATED)
    void addUser(@RequestBody User user) {
        LOG.info(user.getRole().toString());
        userService.save(user);
    }


}
