package com.example.vibelet.controller;

import com.example.vibelet.dto.UserSearchDto;
import com.example.vibelet.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserSearchDto>> searchUsers(@RequestParam String query, Principal principal) {
        return ResponseEntity.ok(userService.searchUsers(query, principal.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<com.example.vibelet.model.User> updateMyProfile(
            @RequestBody com.example.vibelet.dto.UserProfileUpdateDto dto,
            java.security.Principal principal) {
        return ResponseEntity.ok(userService.updateUserProfile(principal.getName(), dto));
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyAccount(Principal principal) {
        userService.deleteUser(principal.getName());
        return ResponseEntity.ok("Account deleted successfully.");
    }
}