package com.example.vibelet.controller;

import com.example.vibelet.model.Friendship;
import com.example.vibelet.model.User;
import com.example.vibelet.service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/friends")
public class FriendshipController {
    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping("/request/{userId}")
    public ResponseEntity<?> sendRequest(@PathVariable Long userId, Principal principal) {
        friendshipService.sendFriendRequest(principal.getName(), userId);
        return ResponseEntity.ok("Request sent");
    }

    @PutMapping("/accept/{friendshipId}")
    public ResponseEntity<?> acceptRequest(@PathVariable Long friendshipId, Principal principal) {
        friendshipService.acceptFriendRequest(principal.getName(), friendshipId);
        return ResponseEntity.ok("Friendship accepted");
    }

    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<?> removeFriend(@PathVariable Long friendshipId, Principal principal) {
        friendshipService.removeFriendOrRejectRequest(principal.getName(), friendshipId);
        return ResponseEntity.ok("Removed");
    }

    @GetMapping
    public List<User> getFriends(Principal principal) {
        String username = principal.getName();
        return friendshipService.getAcceptedFriendships(username).stream()
                .map(f -> f.getRequester().getUsername().equals(username) ? f.getReceiver() : f.getRequester())
                .collect(Collectors.toList());
    }

    @GetMapping("/requests")
    public List<Friendship> getRequests(Principal principal) {
        return friendshipService.getPendingRequests(principal.getName());
    }
}