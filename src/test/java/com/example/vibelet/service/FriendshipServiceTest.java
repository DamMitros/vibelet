package com.example.vibelet.service;

import com.example.vibelet.model.Friendship;
import com.example.vibelet.model.FriendshipStatus;
import com.example.vibelet.model.User;
import com.example.vibelet.repository.FriendshipRepository;
import com.example.vibelet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipServiceTest {
    @Mock
    private FriendshipRepository friendshipRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FriendshipService friendshipService;

    @Test
    void sendFriendRequest_ShouldCreatePendingFriendship() {
        String senderName = "sender";
        Long receiverId = 2L;

        User sender = new User(); sender.setUsername(senderName); sender.setId(1L);
        User receiver = new User(); receiver.setId(receiverId);

        when(userRepository.findByUsername(senderName)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(friendshipRepository.existsByUsers(sender, receiver)).thenReturn(false);

        friendshipService.sendFriendRequest(senderName, receiverId);
        verify(friendshipRepository).save(argThat(f ->
                f.getStatus() == FriendshipStatus.PENDING &&
                        f.getRequester().equals(sender) &&
                        f.getReceiver().equals(receiver)
        ));
    }

    @Test
    void sendFriendRequest_ShouldThrow_WhenFriendshipAlreadyExists() {
        String senderName = "sender";
        Long receiverId = 2L;

        User sender = new User(); sender.setUsername(senderName); sender.setId(1L);
        User receiver = new User(); receiver.setId(receiverId);

        when(userRepository.findByUsername(senderName)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(receiver));
        when(friendshipRepository.existsByUsers(sender, receiver)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            friendshipService.sendFriendRequest(senderName, receiverId);
        });
    }

    @Test
    void sendFriendRequest_ShouldThrow_WhenSenderEqualsReceiver() {
        String senderName = "sender";
        Long receiverId = 1L;
        User sender = new User(); sender.setUsername(senderName); sender.setId(1L);

        when(userRepository.findByUsername(senderName)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.of(sender));
        assertThrows(RuntimeException.class, () -> {
            friendshipService.sendFriendRequest(senderName, receiverId);
        });
    }

    @Test
    void sendFriendRequest_ShouldThrow_WhenReceiverNotFound() {
        String senderName = "sender";
        Long receiverId = 2L;
        User sender = new User(); sender.setUsername(senderName); sender.setId(1L);

        when(userRepository.findByUsername(senderName)).thenReturn(Optional.of(sender));
        when(userRepository.findById(receiverId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            friendshipService.sendFriendRequest(senderName, receiverId);
        });
    }

    @Test
    void sendFriendRequest_ShouldThrow_WhenSenderNotFound() {
        String senderName = "sender";
        Long receiverId = 2L;

        when(userRepository.findByUsername(senderName)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            friendshipService.sendFriendRequest(senderName, receiverId);
        });
    }

    @Test
    void acceptFriendRequest_ShouldChangeStatusToAccepted() {
        String currentUsername = "receiver";
        Long friendshipId = 10L;

        User requester = new User(); requester.setUsername("requester");
        User receiver = new User(); receiver.setUsername(currentUsername);

        Friendship friendship = new Friendship();
        friendship.setId(friendshipId);
        friendship.setRequester(requester);
        friendship.setReceiver(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.of(friendship));
        friendshipService.acceptFriendRequest(currentUsername, friendshipId);

        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
        verify(friendshipRepository).save(friendship);
    }

    @Test
    void acceptFriendRequest_ShouldThrow_WhenUserIsNotReceiver() {
        Long friendshipId = 1L;
        User receiver = new User(); receiver.setUsername("receiver");
        Friendship friendship = new Friendship();
        friendship.setReceiver(receiver);

        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.of(friendship));
        assertThrows(RuntimeException.class, () -> {
            friendshipService.acceptFriendRequest("hacker", friendshipId);
        });
    }

    @Test
    void acceptFriendRequest_ShouldThrow_WhenFriendshipNotFound() {
        Long friendshipId = 1L;

        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            friendshipService.acceptFriendRequest("receiver", friendshipId);
        });
    }

    @Test
    void removeFriendOrRejectRequest_ShouldDelete_WhenUserIsRequester() {
        Long friendshipId = 1L;
        User requester = new User();
        requester.setUsername("requester");
        User receiver = new User();
        receiver.setUsername("receiver");

        Friendship friendship = new Friendship();
        friendship.setId(friendshipId);
        friendship.setRequester(requester);
        friendship.setReceiver(receiver);

        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.of(friendship));
        friendshipService.removeFriendOrRejectRequest("requester", friendshipId);

        verify(friendshipRepository).delete(friendship);
    }

    @Test
    void removeFriendOrRejectRequest_ShouldDelete_WhenUserIsReceiver() {
        Long friendshipId = 1L;
        User requester = new User();
        requester.setUsername("requester");
        User receiver = new User();
        receiver.setUsername("receiver");

        Friendship friendship = new Friendship();
        friendship.setId(friendshipId);
        friendship.setRequester(requester);
        friendship.setReceiver(receiver);

        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.of(friendship));
        friendshipService.removeFriendOrRejectRequest("receiver", friendshipId);

        verify(friendshipRepository).delete(friendship);
    }

    @Test
    void removeFriendOrRejectRequest_ShouldThrow_WhenUserIsNotParticipant() {
        Long friendshipId = 1L;
        User requester = new User();
        requester.setUsername("requester");
        User receiver = new User();
        receiver.setUsername("receiver");

        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setReceiver(receiver);

        when(friendshipRepository.findById(friendshipId)).thenReturn(Optional.of(friendship));
        assertThrows(RuntimeException.class, () ->
                friendshipService.removeFriendOrRejectRequest("intruder", friendshipId)
        );

        verify(friendshipRepository, never()).delete(any());
    }

    @Test
    void removeFriendOrRejectRequest_ShouldThrow_WhenFriendshipNotFound() {
        when(friendshipRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                friendshipService.removeFriendOrRejectRequest("user", 1L)
        );
    }

    @Test
    void getPendingRequests_ShouldReturnPendingRequests() {
        String username = "receiver";
        User user = new User();
        user.setUsername(username);

        Friendship friendship = new Friendship();
        friendship.setStatus(FriendshipStatus.PENDING);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(friendshipRepository.findByReceiverAndStatus(user, FriendshipStatus.PENDING))
                .thenReturn(List.of(friendship));

        List<Friendship> result = friendshipService.getPendingRequests(username);
        assertAll(
                ()->assertEquals(1, result.size()),
                ()->assertEquals(FriendshipStatus.PENDING, result.get(0).getStatus())
        );
    }

    @Test
    void getPendingRequests_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                friendshipService.getPendingRequests("ghost")
        );
    }

    @Test
    void getFriendsList_ShouldReturnFriends_WhenUserIsRequester() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        User friend = new User();
        friend.setId(2L);

        Friendship friendship = new Friendship();
        friendship.setRequester(user);
        friendship.setReceiver(friend);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(friendshipRepository.findAllFriendsOfUser(user))
                .thenReturn(List.of(friendship));

        List<Friendship> result = friendshipService.getAcceptedFriendships("user");
        assertAll(
                ()->assertEquals(1, result.size()),
                ()->assertEquals(friendship, result.get(0))
        );
    }

    @Test
    void getFriendsList_ShouldReturnFriends_WhenUserIsReceiver() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");
        User friend = new User();
        friend.setId(2L);

        Friendship friendship = new Friendship();
        friendship.setRequester(friend);
        friendship.setReceiver(user);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(friendshipRepository.findAllFriendsOfUser(user))
                .thenReturn(List.of(friendship));

        List<Friendship> result = friendshipService.getAcceptedFriendships("user");
        assertAll(
                ()->assertEquals(1, result.size()),
                ()->assertEquals(friendship, result.get(0))
        );
    }

    @Test
    void getFriendsList_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () ->
                friendshipService.getAcceptedFriendships("ghost")
        );
    }

    @Test
    void areFriends_ShouldReturnTrue_WhenAccepted_User1ToUser2() {
        User u1 = new User();
        User u2 = new User();

        Friendship friendship = new Friendship();
        friendship.setStatus(FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findByRequesterAndReceiver(u1, u2))
                .thenReturn(Optional.of(friendship));

        boolean result = friendshipService.areFriends(u1, u2);

        assertTrue(result);
    }

    @Test
    void areFriends_ShouldReturnTrue_WhenAccepted_User2ToUser1() {
        User u1 = new User();
        User u2 = new User();

        Friendship friendship = new Friendship();
        friendship.setStatus(FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findByRequesterAndReceiver(u1, u2))
                .thenReturn(Optional.empty());
        when(friendshipRepository.findByRequesterAndReceiver(u2, u1))
                .thenReturn(Optional.of(friendship));

        boolean result = friendshipService.areFriends(u1, u2);

        assertTrue(result);
    }

    @Test
    void areFriends_ShouldReturnFalse_WhenPending() {
        User u1 = new User();
        User u2 = new User();

        Friendship friendship = new Friendship();
        friendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findByRequesterAndReceiver(u1, u2))
                .thenReturn(Optional.of(friendship));

        boolean result = friendshipService.areFriends(u1, u2);

        assertFalse(result);
    }

    @Test
    void areFriends_ShouldReturnFalse_WhenNoFriendship() {
        User u1 = new User();
        User u2 = new User();

        when(friendshipRepository.findByRequesterAndReceiver(any(), any()))
                .thenReturn(Optional.empty());

        boolean result = friendshipService.areFriends(u1, u2);

        assertFalse(result);
    }

    @Test
    void isPending_ShouldReturnTrue_WhenPending() {
        User sender = new User();
        User receiver = new User();

        Friendship friendship = new Friendship();
        friendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findByRequesterAndReceiver(sender, receiver))
                .thenReturn(Optional.of(friendship));

        boolean result = friendshipService.isPending(sender, receiver);

        assertTrue(result);
    }

    @Test
    void isPending_ShouldReturnFalse_WhenAccepted() {
        User sender = new User();
        User receiver = new User();

        Friendship friendship = new Friendship();
        friendship.setStatus(FriendshipStatus.ACCEPTED);

        when(friendshipRepository.findByRequesterAndReceiver(sender, receiver))
                .thenReturn(Optional.of(friendship));

        boolean result = friendshipService.isPending(sender, receiver);

        assertFalse(result);
    }

    @Test
    void isPending_ShouldReturnFalse_WhenNoFriendship() {
        User sender = new User();
        User receiver = new User();

        when(friendshipRepository.findByRequesterAndReceiver(sender, receiver))
                .thenReturn(Optional.empty());

        boolean result = friendshipService.isPending(sender, receiver);

        assertFalse(result);
    }

    @Test
    void areFriends_ShouldReturnFalse_WhenSecondDirectionExistsButNotAccepted() {
        User u1 = new User();
        User u2 = new User();

        Friendship friendship = new Friendship();
        friendship.setStatus(FriendshipStatus.PENDING);

        when(friendshipRepository.findByRequesterAndReceiver(u1, u2))
                .thenReturn(Optional.empty());
        when(friendshipRepository.findByRequesterAndReceiver(u2, u1))
                .thenReturn(Optional.of(friendship));

        boolean result = friendshipService.areFriends(u1, u2);

        assertFalse(result);
    }
}