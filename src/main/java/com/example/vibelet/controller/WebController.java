package com.example.vibelet.controller;

import com.example.vibelet.dto.RegisterRequest;
import com.example.vibelet.dto.UserSearchDto;
import com.example.vibelet.model.PrivacyStatus;
import com.example.vibelet.model.User;
import com.example.vibelet.repository.UserRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private final VibeService vibeService;
    private final UserService userService;
    private final AuthService authService;
    private final FriendshipService friendshipService;
    private final UserRepository userRepository;

    public WebController(VibeService vibeService, UserService userService, AuthService authService, FriendshipService friendshipService, UserRepository userRepository) {
        this.vibeService = vibeService;
        this.userService = userService;
        this.authService = authService;
        this.friendshipService = friendshipService;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String index(Principal principal) {
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
        User currentUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("currentUser", currentUser);
        return "profile";
    }

    @GetMapping("/explore")
    public String explore(@RequestParam(required = false) String query, Model model, Principal principal) {
        if (query != null && !query.isBlank()) {
            List<UserSearchDto> results = userService.searchUsers(query, principal.getName());
            model.addAttribute("searchResults", results);
        } else {
            model.addAttribute("searchResults", Collections.emptyList());
        }
        return "explore";
    }

    @GetMapping("/friends")
    public String friends(Model model, Principal principal) {
        String username = principal.getName();

        List<String> friends = friendshipService.getFriendsList(username)
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        List<String> requests = friendshipService.getPendingRequests(username)
                .stream()
                .map(f -> f.getRequester().getUsername())
                .collect(Collectors.toList());

        model.addAttribute("friends", friends);
        model.addAttribute("requests", requests);

        return "friends";
    }
}