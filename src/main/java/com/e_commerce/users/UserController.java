package com.e_commerce.users;

import com.e_commerce.users.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping(path = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerUser(@RequestBody @Valid User user) {
        service.registerUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping(path = "/{username}/info", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserInfo retrieveUserInfo(
            @PathVariable
            @Valid
            @Size(max = 64, message = "Username can only contain up to 64 characters.")
            String username) {
        return service.retrieveUserInfo(username);
    }

    @GetMapping(path = "/{username}/email", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserEmail retrieveUserEmail(
            @PathVariable
            @Valid
            @Size(max = 64, message = "Username can only contain up to 64 characters.")
            String username) {
        return service.retrieveUserEmail(username);
    }

    @PutMapping(path = "/update/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeUserPassword(@RequestBody @Valid PasswordChange change) {
        service.updateUserPassword(change);

        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/update/email", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeUserEmail(@RequestBody @Valid EmailChange change) {
        service.updateUserEmail(change);

        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/update/info", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changeUserInfo(@RequestBody @Valid UserInfoChange change) {
        service.updateUserInfo(change);

        return ResponseEntity.noContent().build();
    }
}
