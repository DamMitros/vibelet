package com.example.vibelet.controller;

import com.example.vibelet.dto.RegisterRequest;
import com.example.vibelet.dto.UserProfileUpdateDto;
import com.example.vibelet.model.PrivacyStatus;
import com.example.vibelet.model.User;
import com.example.vibelet.service.AuthService;
import com.example.vibelet.service.FriendshipService;
import com.example.vibelet.service.UserService;
import com.example.vibelet.service.VibeService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
public class WebController {

    private final VibeService vibeService;
    private final UserService userService;
    private final AuthService authService;
    private final FriendshipService friendshipService;

    public WebController(VibeService vibeService, UserService userService, AuthService authService, FriendshipService friendshipService) {
        this.vibeService = vibeService;
        this.userService = userService;
        this.authService = authService;
        this.friendshipService = friendshipService;
    }

    @GetMapping("/")
    public String index(Model model, Principal principal) {
        if (principal != null) {
            return "redirect:/feed";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute RegisterRequest registerRequest, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            authService.registerUser(registerRequest.getUsername(), registerRequest.getEmail(), registerRequest.getPassword());
            return "redirect:/login?success";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/feed")
    public String feed(Model model, Principal principal, @RequestParam(defaultValue = "0") int page) {
        String username = principal.getName();
        model.addAttribute("vibes", vibeService.getFeed(username, page, 20));
        model.addAttribute("currentUser", userService.searchUsers(username, username).get(0));
        return "feed";
    }

    @PostMapping("/vibes")
    public String createVibe(@RequestParam("content") String content,
                             @RequestParam(value = "file", required = false) MultipartFile file,
                             @RequestParam("privacy") PrivacyStatus privacy,
                             Principal principal) {
        vibeService.createVibe(principal.getName(), content, file, privacy);
        return "redirect:/feed";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        // TODO: dodać metodę do UserService, która pobiera pełny profil użytkownika

        return "profile";
    }

}