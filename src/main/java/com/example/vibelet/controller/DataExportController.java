package com.example.vibelet.controller;

import com.example.vibelet.dto.DataExportDto;
import com.example.vibelet.dto.VibeExportDto;
import com.example.vibelet.model.*;
import com.example.vibelet.repository.FriendshipRepository;
import com.example.vibelet.repository.UserRepository;
import com.example.vibelet.repository.VibeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/data")
public class DataExportController {
    private final UserRepository userRepository;
    private final VibeRepository vibeRepository;
    private final FriendshipRepository friendshipRepository;

    public DataExportController(UserRepository userRepository, VibeRepository vibeRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.vibeRepository = vibeRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @GetMapping("/export")
    public ResponseEntity<DataExportDto> exportData(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<VibeExportDto> vibesDtoList = user.getVibes().stream()
                .map(v -> new VibeExportDto(v.getContent(), v.getCreatedAt().toString()))
                .collect(Collectors.toList());

        List<Friendship> friendships = friendshipRepository.findAllFriendsOfUser(user);
        List<String> friendsList = friendships.stream()
                .map(f -> f.getRequester().getId().equals(user.getId()) ? f.getReceiver().getUsername() : f.getRequester().getUsername())
                .collect(Collectors.toList());

        DataExportDto exportData = new DataExportDto(
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                vibesDtoList,
                friendsList
        );

        return ResponseEntity.ok(exportData);
    }

    @PostMapping("/import")
    @Transactional
    public ResponseEntity<String> importData(@RequestBody DataExportDto data, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (data.getBio() != null) {
            user.setBio(data.getBio());
        }
        userRepository.save(user);

        int importedVibesCount = 0;
        if (data.getVibes() != null && !data.getVibes().isEmpty()) {
            for (VibeExportDto vibeDto : data.getVibes()) {
                LocalDateTime createdAt;
                try {
                    createdAt = LocalDateTime.parse(vibeDto.getCreatedAt());
                } catch (Exception e) {
                    createdAt = LocalDateTime.now();
                }

                boolean exists = vibeRepository.existsByUserAndContentAndCreatedAt(
                        user,
                        vibeDto.getContent(),
                        createdAt
                );

                if (!exists) {
                    Vibe newVibe = new Vibe();
                    newVibe.setContent(vibeDto.getContent());
                    newVibe.setCreatedAt(createdAt);
                    newVibe.setUser(user);
                    newVibe.setPrivacyStatus(PrivacyStatus.PRIVATE);
                    vibeRepository.save(newVibe);
                    importedVibesCount++;
                }
            }
        }

        int importedFriendsCount = 0;
        if (data.getFriends() != null) {
            for (String friendUsername : data.getFriends()) {
                Optional<User> friendOpt = userRepository.findByUsername(friendUsername);
                if (friendOpt.isPresent()) {
                    User friend = friendOpt.get();
                    if (!friendshipRepository.existsByUsers(user, friend)) {
                        Friendship friendship = new Friendship();
                        friendship.setRequester(user);
                        friendship.setReceiver(friend);
                        friendship.setStatus(FriendshipStatus.ACCEPTED);
                        friendshipRepository.save(friendship);
                        importedFriendsCount++;
                    }
                }
            }
        }

        return ResponseEntity.ok(String.format("Data imported. Vibes: %d, Friends restored: %d", importedVibesCount, importedFriendsCount));
    }
}