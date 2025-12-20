package com.example.vibelet.service;

import com.example.vibelet.model.*;
import com.example.vibelet.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Service
public class VibeService {
    private final VibeRepository vibeRepository;
    private final UserRepository userRepository;
    private final Path rootLocation = Paths.get("uploads");
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final FriendshipRepository friendshipRepository;

    public VibeService(VibeRepository vibeRepository, UserRepository userRepository, CommentRepository commentRepository, PostLikeRepository postLikeRepository, FriendshipRepository friendshipRepository) {
        this.vibeRepository = vibeRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.postLikeRepository = postLikeRepository;
        this.friendshipRepository = friendshipRepository;
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

    public void addComment(Long vibeId, String username, String content) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Vibe vibe = vibeRepository.findById(vibeId)
                .orElseThrow(() -> new RuntimeException("Vibe not found"));

        checkAccess(vibe, user);

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setVibe(vibe);
        comment.setContent(content);
        commentRepository.save(comment);
    }

    public void toggleLike(Long vibeId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Vibe vibe = vibeRepository.findById(vibeId)
                .orElseThrow(() -> new RuntimeException("Vibe not found"));

        checkAccess(vibe, user);

        Optional<PostLike> existingLike = postLikeRepository.findByUserAndVibe(user, vibe);
        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
        } else {
            PostLike like = new PostLike();
            like.setUser(user);
            like.setVibe(vibe);
            postLikeRepository.save(like);
        }
    }

    private void checkAccess(Vibe vibe, User currentUser) {
        if (vibe.getUser().getId().equals(currentUser.getId())) {return;}
        if (vibe.getPrivacyStatus() == PrivacyStatus.PUBLIC) {return;}

        if (vibe.getPrivacyStatus() == PrivacyStatus.PRIVATE) {
            throw new RuntimeException("Access denied: This vibe is private.");
        }

        if (vibe.getPrivacyStatus() == PrivacyStatus.FRIENDS_ONLY) {
            boolean isFriend = friendshipRepository.existsByUsers(vibe.getUser(), currentUser);
            if (!isFriend) {
                throw new RuntimeException("Access denied: Friends only.");
            }
        }
    }
}