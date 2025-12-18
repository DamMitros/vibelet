package com.example.vibelet.service;

import com.example.vibelet.model.Friendship;
import com.example.vibelet.model.FriendshipStatus;
import com.example.vibelet.model.User;
import com.example.vibelet.repository.FriendshipRepository;
import com.example.vibelet.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FriendshipService {
    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public FriendshipService(FriendshipRepository friendshipRepository, UserRepository userRepository) {
        this.friendshipRepository = friendshipRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void sendFriendRequest(String requesterUsername, Long receiverId) {
        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (requester.getId().equals(receiver.getId())) {
            throw new RuntimeException("You cannot send a friend request to yourself.");
        }

        if (friendshipRepository.existsByUsers(requester, receiver)) {
            throw new RuntimeException("Friendship or pending request already exists.");
        }

        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);

        friendshipRepository.save(friendship);
    }

    @Transactional
    public void acceptFriendRequest(String currentUsername, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Friendship request not found"));

        if (!friendship.getReceiver().getUsername().equals(currentUsername)) {
            throw new RuntimeException("You are not authorized to accept this request.");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
    }

    @Transactional
    public void removeFriendOrRejectRequest(String currentUsername, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new RuntimeException("Relationship not found"));

        if (!friendship.getRequester().getUsername().equals(currentUsername) &&
                !friendship.getReceiver().getUsername().equals(currentUsername)) {
            throw new RuntimeException("You are not authorized to remove this relationship.");
        }

        friendshipRepository.delete(friendship);
    }

    public List<Friendship> getPendingRequests(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return friendshipRepository.findByReceiverAndStatus(user, FriendshipStatus.PENDING);
    }

    public List<User> getFriendsList(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Friendship> friendships = friendshipRepository.findAllFriendsOfUser(user);

        return friendships.stream()
                .map(f -> f.getRequester().getId().equals(user.getId()) ? f.getReceiver() : f.getRequester())
                .toList();
    }
}