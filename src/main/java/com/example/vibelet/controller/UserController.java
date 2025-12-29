package com.example.vibelet.controller;

import com.example.vibelet.dto.UserProfileUpdateDto;
import com.example.vibelet.dto.UserSearchDto;
import com.example.vibelet.model.User;
import com.example.vibelet.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateMyProfile(
            @ModelAttribute UserProfileUpdateDto dto,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Principal principal) {
        return ResponseEntity.ok(userService.updateUserProfile(principal.getName(), dto, file));
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMyAccount(Principal principal) {
        userService.deleteUser(principal.getName());
        return ResponseEntity.ok("Account deleted successfully.");
    }

    @PutMapping("/me/security")
    public ResponseEntity<?> updateSecurity(
            @RequestBody com.example.vibelet.dto.UserSecurityUpdateDto dto,
            Principal principal) {
        userService.updateSecurityDetails(principal.getName(), dto);
        return ResponseEntity.ok("Security details updated");
    }
}