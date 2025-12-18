package com.example.vibelet.controller;

import com.example.vibelet.model.Friendship;
import com.example.vibelet.model.User;
import com.example.vibelet.service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
public class FriendshipController {
    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @PostMapping("/request/{receiverId}")
    public ResponseEntity<String> sendRequest(@PathVariable Long receiverId, Principal principal) {
        friendshipService.sendFriendRequest(principal.getName(), receiverId);
        return ResponseEntity.ok("Friend request sent.");
    }

    @PutMapping("/accept/{friendshipId}")
    public ResponseEntity<String> acceptRequest(@PathVariable Long friendshipId, Principal principal) {
        friendshipService.acceptFriendRequest(principal.getName(), friendshipId);
        return ResponseEntity.ok("Friend request accepted.");
    }

    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<String> removeFriend(@PathVariable Long friendshipId, Principal principal) {
        friendshipService.removeFriendOrRejectRequest(principal.getName(), friendshipId);
        return ResponseEntity.ok("Relationship removed.");
    }

    @GetMapping("/requests")
    public ResponseEntity<List<String>> getPendingRequests(Principal principal) {
        List<String> requests = friendshipService.getPendingRequests(principal.getName())
                .stream()
                .map(f -> f.getRequester().getUsername())
                .toList();
        return ResponseEntity.ok(requests);
    }

    @GetMapping
    public ResponseEntity<List<String>> getFriends(Principal principal) {
        List<String> friends = friendshipService.getFriendsList(principal.getName())
                .stream()
                .map(User::getUsername)
                .toList();
        return ResponseEntity.ok(friends);
    }
}