package com.example.vibelet.service;

import com.example.vibelet.model.FriendshipStatus;
import com.example.vibelet.model.PrivacyStatus;
import com.example.vibelet.model.User;
import com.example.vibelet.model.Vibe;
import com.example.vibelet.repository.UserRepository;
import com.example.vibelet.repository.VibeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class VibeService {
    private final VibeRepository vibeRepository;
    private final UserRepository userRepository;
    private final Path rootLocation = Paths.get("uploads");

    public VibeService(VibeRepository vibeRepository, UserRepository userRepository) {
        this.vibeRepository = vibeRepository;
        this.userRepository = userRepository;
        init();
    }

    private void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public Vibe createVibe(String username, String content, MultipartFile file, PrivacyStatus privacyStatus) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Vibe vibe = new Vibe();
        vibe.setContent(content);
        vibe.setPrivacyStatus(privacyStatus);
        vibe.setUser(user);

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), this.rootLocation.resolve(fileName));
                vibe.setImageUrl(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store file", e);
            }
        }

        return vibeRepository.save(vibe);
    }

    public Page<Vibe> getFeed(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);

        return vibeRepository.findFeedForUser(
                user,
                FriendshipStatus.ACCEPTED,
                PrivacyStatus.PRIVATE,
                pageable
        );
    }

    public Page<Vibe> getUserVibes(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return vibeRepository.findByUserOrderByCreatedAtDesc(user, PageRequest.of(page, size));
    }
}