package com.example.vibelet.service;

import com.example.vibelet.dto.UserProfileUpdateDto;
import com.example.vibelet.dto.UserSearchDto;
import com.example.vibelet.dto.UserSecurityUpdateDto;
import com.example.vibelet.model.Friendship;
import com.example.vibelet.model.FriendshipStatus;
import com.example.vibelet.model.User;
import com.example.vibelet.repository.FriendshipRepository;
import com.example.vibelet.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PasswordEncoder passwordEncoder;
    private final Path rootLocation = Paths.get("uploads");

    public UserService(UserRepository userRepository, FriendshipRepository friendshipRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void deleteUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    public List<UserSearchDto> searchUsers(String query, String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        List<User> foundUsers = userRepository.findByUsernameContainingIgnoreCase(query);

        return foundUsers.stream()
                .map(user -> mapToSearchDto(user, currentUser))
                .collect(Collectors.toList());
    }

    public UserSearchDto mapToSearchDto(User user, User currentUser) {
        String status = "NONE";

        if (user.getId().equals(currentUser.getId())) {
            status = "SELF";
        } else {
            Optional<Friendship> friendship = friendshipRepository.findByRequesterAndReceiver(currentUser, user);
            if (friendship.isEmpty()) {
                friendship = friendshipRepository.findByRequesterAndReceiver(user, currentUser);
            }

            if (friendship.isPresent()) {
                Friendship f = friendship.get();
                if (f.getStatus() == FriendshipStatus.ACCEPTED) {
                    status = "FRIEND";
                } else if (f.getStatus() == FriendshipStatus.PENDING) {
                    status = "PENDING";
                }
            }
        }
        return new UserSearchDto(user.getId(), user.getUsername(), user.getAvatarUrl(), status);
    }

    public User updateUserProfile(String username, UserProfileUpdateDto dto, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (dto.getBio() != null) user.setBio(dto.getBio());
        if (dto.getStatus() != null) user.setStatus(dto.getStatus());
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = "avatar_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Files.copy(file.getInputStream(), this.rootLocation.resolve(fileName));
                user.setAvatarUrl(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Failed to store avatar", e);
            }
        } else if (dto.getAvatarUrl() != null && !dto.getAvatarUrl().isBlank()) {
            user.setAvatarUrl(dto.getAvatarUrl());
        }

        return userRepository.save(user);
    }

    public void updateSecurityDetails(String username, UserSecurityUpdateDto dto) {
        User user = userRepository.findByUsername(username).orElseThrow();
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("INVALID PASSWORD");
        }
        if (dto.getNewUsername() != null && !dto.getNewUsername().isBlank()) {
            if (userRepository.existsByUsername(dto.getNewUsername())) throw new RuntimeException("USERNAME TAKEN");
            user.setUsername(dto.getNewUsername());
        }
        if (dto.getNewEmail() != null && !dto.getNewEmail().isBlank()) {
            if (userRepository.existsByEmail(dto.getNewEmail())) throw new RuntimeException("EMAIL TAKEN");
            user.setEmail(dto.getNewEmail());
        }
        if (dto.getNewPassword() != null && !dto.getNewPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        }

        userRepository.save(user);
    }

    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }
}