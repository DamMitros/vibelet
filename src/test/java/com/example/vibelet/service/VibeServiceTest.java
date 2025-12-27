package com.example.vibelet.service;

import com.example.vibelet.model.*;
import com.example.vibelet.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VibeServiceTest {
    @Mock private VibeRepository vibeRepository;
    @Mock private UserRepository userRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private PostLikeRepository postLikeRepository;
    @Mock private FriendshipRepository friendshipRepository;

    @InjectMocks private VibeService vibeService;

    @Test
    void createVibe_NoFile_NullFile() {
        User user = new User(); user.setUsername("u");
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(vibeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Vibe vibe = vibeService.createVibe("u", "c", null, PrivacyStatus.PUBLIC);
        assertNull(vibe.getImageUrl());
    }

    @Test
    void createVibe_FileEmpty_ShouldSkip() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        User user = new User(); user.setUsername("u");
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(vibeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Vibe vibe = vibeService.createVibe("u", "c", file, PrivacyStatus.PUBLIC);
        assertNull(vibe.getImageUrl());
    }

    @Test
    void createVibe_FilePresent_ShouldStore() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("img.png");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("x".getBytes()));

        User user = new User(); user.setUsername("u");
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(vibeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Vibe vibe = vibeService.createVibe("u", "c", file, PrivacyStatus.PUBLIC);
        assertNotNull(vibe.getImageUrl());
    }

    @Test
    void createVibe_FileIOException_ShouldThrow() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("x.png");
        when(file.getInputStream()).thenThrow(new IOException());

        User user = new User(); user.setUsername("u");
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> vibeService.createVibe("u", "c", file, PrivacyStatus.PUBLIC));
    }

    @Test
    void createVibe_UserNotFound() {
        when(userRepository.findByUsername("x")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> vibeService.createVibe("x", "c", null, PrivacyStatus.PUBLIC));
    }

    @Test
    void deleteVibe_NoImage() {
        User owner = new User(); owner.setUsername("owner");
        Vibe vibe = new Vibe(); vibe.setUser(owner);
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));
        vibeService.deleteVibe(1L, "owner");

        verify(vibeRepository).delete(vibe);
    }

    @Test
    void deleteVibe_Image_DeleteSuccess() throws Exception {
        User owner = new User(); owner.setUsername("owner");
        Vibe vibe = new Vibe(); vibe.setUser(owner); vibe.setImageUrl("file.png");
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));

        try (MockedStatic<java.nio.file.Files> files = mockStatic(java.nio.file.Files.class)) {
            files.when(() -> java.nio.file.Files.deleteIfExists(any())).thenReturn(true);
            vibeService.deleteVibe(1L, "owner");
        }

        verify(vibeRepository).delete(vibe);
    }

    @Test
    void deleteVibe_Image_DeleteIOException_Ignored() throws Exception {
        User owner = new User(); owner.setUsername("owner");
        Vibe vibe = new Vibe(); vibe.setUser(owner); vibe.setImageUrl("file.png");
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));

        try (MockedStatic<java.nio.file.Files> files = mockStatic(java.nio.file.Files.class)) {
            files.when(() -> java.nio.file.Files.deleteIfExists(any()))
                    .thenThrow(new IOException());
            vibeService.deleteVibe(1L, "owner");
        }

        verify(vibeRepository).delete(vibe);
    }

    @Test
    void deleteVibe_NotOwner() {
        User owner = new User(); owner.setUsername("owner");
        Vibe vibe = new Vibe(); vibe.setUser(owner);
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));

        assertThrows(RuntimeException.class,
                () -> vibeService.deleteVibe(1L, "hacker"));
    }

    @Test
    void updateVibe_ContentUpdated() {
        User owner = new User(); owner.setUsername("owner");
        Vibe vibe = new Vibe(); vibe.setUser(owner); vibe.setContent("old");
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));
        when(vibeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Vibe updated = vibeService.updateVibe(1L, "owner", "new", null);
        assertEquals("new", updated.getContent());
    }

    @Test
    void updateVibe_NullContent_Ignored() {
        User owner = new User(); owner.setUsername("owner");
        Vibe vibe = new Vibe(); vibe.setUser(owner); vibe.setContent("x");
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));
        when(vibeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Vibe updated = vibeService.updateVibe(1L, "owner", null, null);
        assertEquals("x", updated.getContent());
    }

    @Test
    void updateVibe_BlankContent_Ignored() {
        User owner = new User(); owner.setUsername("owner");
        Vibe vibe = new Vibe(); vibe.setUser(owner); vibe.setContent("x");
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));
        when(vibeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Vibe updated = vibeService.updateVibe(1L, "owner", "   ", null);
        assertEquals("x", updated.getContent());
    }

    @Test
    void updateVibe_NotOwner() {
        User owner = new User(); owner.setUsername("owner");
        Vibe vibe = new Vibe(); vibe.setUser(owner);
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));

        assertThrows(RuntimeException.class,
                () -> vibeService.updateVibe(1L, "hacker", "x", null));
    }

    @Test
    void updateVibe_NotFound() {
        when(vibeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> vibeService.updateVibe(1L, "u", "x", null));
    }

    @Test
    void getUserVibes_Ok() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(vibeRepository.findByUserOrderByCreatedAtDesc(eq(user), any()))
                .thenReturn(new PageImpl<>(List.of()));

        assertNotNull(vibeService.getUserVibes(1L, 0, 5));
    }

    @Test
    void getUserVibes_UserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> vibeService.getUserVibes(1L, 0, 5));
    }

    @Test
    void addComment_PublicAllowed() {
        User user = new User(); user.setId(2L);
        User author = new User(); author.setId(1L);

        Vibe vibe = new Vibe();
        vibe.setUser(author);
        vibe.setPrivacyStatus(PrivacyStatus.PUBLIC);

        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));
        vibeService.addComment(1L, "u", "ok");

        verify(commentRepository).save(any());
    }

    @Test
    void addComment_PrivateDenied() {
        User user = new User(); user.setId(2L);
        User author = new User(); author.setId(1L);

        Vibe vibe = new Vibe();
        vibe.setUser(author);
        vibe.setPrivacyStatus(PrivacyStatus.PRIVATE);

        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));
        assertThrows(RuntimeException.class,
                () -> vibeService.addComment(1L, "u", "x"));
    }

    @Test
    void addComment_FriendsOnly_NotFriend() {
        User user = new User(); user.setId(2L);
        User author = new User(); author.setId(1L);

        Vibe vibe = new Vibe();
        vibe.setUser(author);
        vibe.setPrivacyStatus(PrivacyStatus.FRIENDS_ONLY);

        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));
        when(friendshipRepository.existsByUsers(author, user)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> vibeService.addComment(1L, "u", "x"));
    }

    @Test
    void toggleLike_AddLike() {
        User user = new User(); user.setId(2L); user.setUsername("u");
        User owner = new User(); owner.setId(1L);

        Vibe vibe = new Vibe();
        vibe.setUser(owner);
        vibe.setPrivacyStatus(PrivacyStatus.PUBLIC);

        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));
        when(postLikeRepository.findByUserAndVibe(user, vibe))
                .thenReturn(Optional.empty());

        vibeService.toggleLike(1L, "u");
        verify(postLikeRepository).save(any());
    }

    @Test
    void toggleLike_RemoveLike() {
        User user = new User(); user.setId(2L); user.setUsername("u");
        User owner = new User(); owner.setId(1L);

        Vibe vibe = new Vibe();
        vibe.setUser(owner);
        vibe.setPrivacyStatus(PrivacyStatus.PUBLIC);

        PostLike like = new PostLike();
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(vibeRepository.findById(1L)).thenReturn(Optional.of(vibe));
        when(postLikeRepository.findByUserAndVibe(user, vibe))
                .thenReturn(Optional.of(like));

        vibeService.toggleLike(1L, "u");
        verify(postLikeRepository).delete(like);
    }

    @Test
    void toggleLike_UserNotFound() {
        when(userRepository.findByUsername("u")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> vibeService.toggleLike(1L, "u"));
    }

    @Test
    void toggleLike_VibeNotFound() {
        User user = new User(); user.setUsername("u");
        when(userRepository.findByUsername("u")).thenReturn(Optional.of(user));
        when(vibeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
                () -> vibeService.toggleLike(1L, "u"));
    }
}
