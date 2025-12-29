package com.example.vibelet.controller;

import com.example.vibelet.dto.RegisterRequest;
import com.example.vibelet.dto.UserSearchDto;
import com.example.vibelet.model.*;
import com.example.vibelet.repository.FriendshipRepository;
import com.example.vibelet.repository.UserRepository;
import com.example.vibelet.repository.VibeRepository;
import com.example.vibelet.service.AuthService;
import com.example.vibelet.service.FriendshipService;
import com.example.vibelet.service.UserService;
import com.example.vibelet.service.VibeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private final VibeService vibeService;
    private final UserService userService;
    private final AuthService authService;
    private final FriendshipService friendshipService;
    private final VibeRepository vibeRepository;
    private final UserRepository userRepository;

    public WebController(VibeService vibeService, UserService userService, AuthService authService, FriendshipService friendshipService, VibeRepository vibeRepository, UserRepository userRepository) {
        this.vibeService = vibeService;
        this.userService = userService;
        this.authService = authService;
        this.friendshipService = friendshipService;
        this.vibeRepository = vibeRepository;
        this.userRepository = userRepository;
    }

    private void addCurrentUserToModel(Model model, Principal principal) {
        if (principal != null) {
            userRepository.findByUsername(principal.getName())
                    .ifPresent(u -> model.addAttribute("currentUser", u));
        }
    }

    @GetMapping("/")
    public String index(Principal principal) {
        return principal != null ? "redirect:/vibes" : "redirect:/login";
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

    @GetMapping("/vibes")
    public String feed(Model model, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Page<Vibe> vibePage = vibeRepository.findFeedForUser(
                currentUser,
                FriendshipStatus.ACCEPTED,
                PrivacyStatus.PRIVATE,
                PageRequest.of(0, 50)
        );

        model.addAttribute("vibes", vibePage.getContent());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("currentUser", currentUser);
        return "vibes";
    }

    @PostMapping("/vibes")
    public String createVibe(@RequestParam("content") String content,
                             @RequestParam(value = "file", required = false) MultipartFile file,
                             @RequestParam("privacy") PrivacyStatus privacy,
                             Principal principal) {
        vibeService.createVibe(principal.getName(), content, file, privacy);
        return "redirect:/vibes";
    }

    @PostMapping("/vibes/{id}/delete")
    public String deleteVibe(@PathVariable Long id, Principal principal) {
        vibeService.deleteVibe(id, principal.getName());
        return "redirect:/vibes";
    }

    @GetMapping("/friends")
    public String friends(Model model, Principal principal) {
        String username = principal.getName();
        addCurrentUserToModel(model, principal);
        model.addAttribute("friends", friendshipService.getAcceptedFriendships(username));
        model.addAttribute("requests", friendshipService.getPendingRequests(username));
        return "friends";
    }

    @GetMapping("/explore")
    public String explore(@RequestParam(required = false) String query, Model model, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        List<UserSearchDto> results;

        if (query == null || query.isBlank()) {
            Page<User> usersPage = userRepository.findAll(PageRequest.of(0, 20));
            results = usersPage.stream()
                    .filter(u -> !u.getId().equals(currentUser.getId()))
                    .map(u -> userService.mapToSearchDto(u, currentUser))
                    .collect(Collectors.toList());
        } else {
            results = userService.searchUsers(query, currentUser.getUsername());
        }

        addCurrentUserToModel(model, principal);
        model.addAttribute("searchResults", results);
        return "explore";
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("friendCount", friendshipService.getAcceptedFriendships(currentUser.getUsername()).size());
        return "profile";
    }

    @GetMapping("/users/{id}")
    public String userProfile(@PathVariable Long id, Model model, Principal principal) {
        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        model.addAttribute("targetUser", targetUser);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("vibeCount", vibeRepository.countByUser(targetUser));
        model.addAttribute("friendCount", friendshipService.getAcceptedFriendships(targetUser.getUsername()).size());
        model.addAttribute("vibes", vibeRepository.findByUserOrderByCreatedAtDesc(targetUser, PageRequest.of(0, 20)));

        String status = "NONE";
        Long friendshipId = null;

        if (targetUser.getId().equals(currentUser.getId())) {
            status = "SELF";
        } else {
            Optional<Friendship> friendshipOpt = friendshipService.getFriendshipRepository().findByRequesterAndReceiver(currentUser, targetUser);
            if (friendshipOpt.isEmpty()) {
                friendshipOpt = friendshipService.getFriendshipRepository().findByRequesterAndReceiver(targetUser, currentUser);
            }

            if (friendshipOpt.isPresent()) {
                Friendship f = friendshipOpt.get();
                friendshipId = f.getId();

                if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                    status = "FRIEND";
                } else if (f.getStatus() == FriendshipStatus.PENDING) {
                    if (f.getRequester().getId().equals(currentUser.getId())) {
                        status = "PENDING_SENT";
                    } else {
                        status = "PENDING_RECEIVED";
                    }
                }
            }
        }
        model.addAttribute("friendshipStatus", status);
        model.addAttribute("friendshipId", friendshipId);

        return "user_profile";
    }
}