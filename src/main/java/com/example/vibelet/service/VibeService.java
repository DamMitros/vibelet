package com.example.vibelet.service;

import com.example.vibelet.model.*;
import com.example.vibelet.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(VibeService.class);
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
            log.error("Could not initialize storage", e);
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
                log.error("Failed to store file for user {}", username, e);
                throw new RuntimeException("Failed to store file", e);
            }
        }

        return vibeRepository.save(vibe);
    }

    public void deleteVibe(Long vibeId, String username) {
        Vibe vibe = vibeRepository.findById(vibeId)
                .orElseThrow(() -> new RuntimeException("Vibe not found"));

        if (!vibe.getUser().getUsername().equals(username)) {
            log.warn("Security Event: User {} tried to delete vibe {} belonging to {}", username, vibeId, vibe.getUser().getUsername());
            throw new RuntimeException("Access denied: You are not the owner of this vibe.");
        }
        if (vibe.getImageUrl() != null) {
            try {
                Files.deleteIfExists(this.rootLocation.resolve(vibe.getImageUrl()));
            } catch (IOException e) {
                log.error("Could not delete file: {}", vibe.getImageUrl(), e);
            }
        }

        vibeRepository.delete(vibe);
        log.info("Vibe {} deleted by user {}", vibeId, username);
    }

    public Vibe updateVibe(Long vibeId, String username, String newContent, PrivacyStatus newPrivacy) {
        Vibe vibe = vibeRepository.findById(vibeId)
                .orElseThrow(() -> new RuntimeException("Vibe not found"));

        if (!vibe.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Access denied: You are not the owner.");
        }
        if (newContent != null && !newContent.isBlank()) {
            vibe.setContent(newContent);
        }
        if (newPrivacy != null) {
            vibe.setPrivacyStatus(newPrivacy);
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