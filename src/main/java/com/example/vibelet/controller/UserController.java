package com.example.vibelet.controller;

import com.example.vibelet.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyAccount(Principal principal) {
        userService.deleteUser(principal.getName());
        return ResponseEntity.ok("Account deleted successfully.");
    }
}