package com.example.vibelet.service;

import com.example.vibelet.dto.UserProfileUpdateDto;
import com.example.vibelet.dto.UserSearchDto;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private FriendshipRepository friendshipRepository;
    @InjectMocks private UserService userService;

    @Test
    void updateUserProfile_ShouldUpdateBio() {
        String username = "user";
        User user = new User(); user.setUsername(username);

        UserProfileUpdateDto dto = new UserProfileUpdateDto();
        dto.setBio("New Bio");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.updateUserProfile(username, dto);
        assertEquals("New Bio", user.getBio());
    }

    @Test
    void updateUserProfile_WhenUserNotFound_ShouldThrow() {
        String username = "unknown";
        UserProfileUpdateDto dto = new UserProfileUpdateDto();
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        assertThrows(
                RuntimeException.class, () -> userService.updateUserProfile(username, dto)
        );
    }

    @Test
    void updateUserPofile_ShouldUpdateStatusandAvatar() {
        String username = "user";
        User user = new User(); user.setUsername(username);

        UserProfileUpdateDto dto = new UserProfileUpdateDto();
        dto.setStatus("Online");
        dto.setAvatarUrl("http://avatar.url/image.png");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        userService.updateUserProfile(username, dto);
        assertAll(
                ()->assertEquals("Online", user.getStatus()),
                ()->assertEquals("http://avatar.url/image.png", user.getAvatarUrl())
        );
    }

    @Test
    void deleteUser_ShouldCallRepositoryDelete() {
        String username = "todelete";
        User user = new User();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        userService.deleteUser(username);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_WhenUserNotFound_ShouldThrow() {
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class, () -> userService.deleteUser(username)
        );
    }

    @Test
    void searchUsers_ShouldReturnCorrectStatuses() {
        String myName = "me";
        User me = new User(); me.setId(1L); me.setUsername(myName);

        User friend = new User(); friend.setId(2L); friend.setUsername("friend");
        User stranger = new User(); stranger.setId(3L); stranger.setUsername("stranger");

        Friendship f = new Friendship();
        f.setRequester(me);
        f.setReceiver(friend);
        f.setStatus(FriendshipStatus.ACCEPTED);

        when(userRepository.findByUsername(myName)).thenReturn(Optional.of(me));
        when(userRepository.findByUsernameContainingIgnoreCase("query")).thenReturn(List.of(me, friend, stranger));
        when(friendshipRepository.findByRequesterAndReceiver(me, friend)).thenReturn(Optional.of(f));
        when(friendshipRepository.findByRequesterAndReceiver(me, stranger)).thenReturn(Optional.empty());
        when(friendshipRepository.findByRequesterAndReceiver(stranger, me)).thenReturn(Optional.empty());
        List<UserSearchDto> results = userService.searchUsers("query", myName);

        assertAll(
                ()->assertEquals(3, results.size()),
                ()->assertEquals("SELF", results.stream().filter(u -> u.getUsername().equals("me")).findFirst().get().getFriendshipStatus()),
                ()->assertEquals("FRIEND", results.stream().filter(u -> u.getUsername().equals("friend")).findFirst().get().getFriendshipStatus()),
                ()->assertEquals("NONE", results.stream().filter(u -> u.getUsername().equals("stranger")).findFirst().get().getFriendshipStatus())
        );
    }

    @Test
    void searchUsers_WhenUserNotFound_ShouldThrow() {
        String myName = "unknown";
        when(userRepository.findByUsername(myName)).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class, () -> userService.searchUsers("query", myName)
        );
    }

    @Test
    void searchUsers_ShouldReceivePendingStatus() {
        String myName = "me";
        User me = new User(); me.setId(1L); me.setUsername(myName);

        User pendingUser = new User(); pendingUser.setId(4L); pendingUser.setUsername("pending");

        Friendship f = new Friendship();
        f.setRequester(me);
        f.setReceiver(pendingUser);
        f.setStatus(FriendshipStatus.PENDING);

        when(userRepository.findByUsername(myName)).thenReturn(Optional.of(me));
        when(userRepository.findByUsernameContainingIgnoreCase("query")).thenReturn(List.of(me, pendingUser));
        when(friendshipRepository.findByRequesterAndReceiver(me, pendingUser)).thenReturn(Optional.of(f));
        List<UserSearchDto> results = userService.searchUsers("query", myName);

        assertEquals("PENDING", results.stream().filter(u -> u.getUsername().equals("pending")).findFirst().get().getFriendshipStatus());
    }
}