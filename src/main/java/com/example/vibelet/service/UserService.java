package com.example.vibelet.service;

import com.example.vibelet.dto.UserSearchDto;
import com.example.vibelet.model.Friendship;
import com.example.vibelet.model.FriendshipStatus;
import com.example.vibelet.model.User;
import com.example.vibelet.repository.FriendshipRepository;
import com.example.vibelet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public UserService(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
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

        return foundUsers.stream().map(user -> {
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
        }).collect(Collectors.toList());
    }
}