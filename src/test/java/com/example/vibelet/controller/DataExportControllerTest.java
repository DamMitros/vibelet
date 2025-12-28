package com.example.vibelet.controller;

import com.example.vibelet.dto.DataExportDto;
import com.example.vibelet.dto.VibeExportDto;
import com.example.vibelet.model.Friendship;
import com.example.vibelet.model.User;
import com.example.vibelet.model.Vibe;
import com.example.vibelet.repository.FriendshipRepository;
import com.example.vibelet.repository.UserRepository;
import com.example.vibelet.repository.VibeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DataExportController.class)
class DataExportControllerTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private UserRepository userRepository;
    @MockitoBean private VibeRepository vibeRepository;
    @MockitoBean private FriendshipRepository friendshipRepository;

    @Test
    @WithMockUser(username = "exporter")
    void exportData_ShouldReturnJson() throws Exception {
        User user = new User();
        user.setUsername("exporter");
        user.setEmail("ex@test.com");

        given(userRepository.findByUsername("exporter")).willReturn(Optional.of(user));
        given(friendshipRepository.findAllFriendsOfUser(user)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/data/export"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "ghost")
    void exportData_WhenUserNotFound_ShouldThrow() throws Exception {
        given(userRepository.findByUsername("ghost"))
                .willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/data/export"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "exporter")
    void exportData_ShouldMapVibesAndFriends() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("exporter");
        user.setEmail("ex@test.com");

        Vibe vibe = new Vibe();
        vibe.setContent("hello");
        vibe.setCreatedAt(LocalDateTime.now());
        vibe.setUser(user);

        user.setVibes(List.of(vibe));

        User friend = new User();
        friend.setId(2L);
        friend.setUsername("friend");

        Friendship friendship = new Friendship();
        friendship.setRequester(user);
        friendship.setReceiver(friend);

        given(userRepository.findByUsername("exporter")).willReturn(Optional.of(user));
        given(friendshipRepository.findAllFriendsOfUser(user)).willReturn(List.of(friendship));
        mockMvc.perform(get("/api/v1/data/export"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "exporter")
    void exportData_ShouldMapFriend_WhenUserIsReceiver() throws Exception {
        User user = new User();
        user.setId(2L);
        user.setUsername("exporter");

        User requester = new User();
        requester.setId(1L);
        requester.setUsername("requesterUser");

        Friendship friendship = new Friendship();
        friendship.setRequester(requester);
        friendship.setReceiver(user);

        given(userRepository.findByUsername("exporter"))
                .willReturn(Optional.of(user));
        given(friendshipRepository.findAllFriendsOfUser(user))
                .willReturn(List.of(friendship));
        mockMvc.perform(get("/api/v1/data/export"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "importer")
    void importData_ShouldProcessJson() throws Exception {
        User user = new User();
        user.setUsername("importer");

        DataExportDto dto = new DataExportDto();
        dto.setBio("New Bio");
        dto.setFriends(List.of("friend1"));

        given(userRepository.findByUsername("importer")).willReturn(Optional.of(user));
        given(userRepository.findByUsername("friend1")).willReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/v1/data/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "importer")
    void importData_WithNullFields_ShouldSucceed() throws Exception {
        User user = new User();
        user.setUsername("importer");

        DataExportDto dto = new DataExportDto();

        given(userRepository.findByUsername("importer"))
                .willReturn(Optional.of(user));

        mockMvc.perform(post("/api/v1/data/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "importer")
    void importData_WithEmptyFields_ShouldSucceed() throws Exception {
        User user = new User();
        user.setUsername("importer");

        DataExportDto dto = new DataExportDto();
        dto.setFriends(Collections.emptyList());
        dto.setVibes(Collections.emptyList());

        given(userRepository.findByUsername("importer"))
                .willReturn(Optional.of(user));

        mockMvc.perform(post("/api/v1/data/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "importer")
    void importData_WhenVibeAlreadyExists_ShouldSkip() throws Exception {
        User user = new User();
        user.setUsername("importer");

        VibeExportDto vibeDto = new VibeExportDto("dup", LocalDateTime.now().toString());

        DataExportDto dto = new DataExportDto();
        dto.setVibes(List.of(vibeDto));

        given(userRepository.findByUsername("importer")).willReturn(Optional.of(user));
        given(vibeRepository.existsByUserAndContentAndCreatedAt(any(), any(), any()))
                .willReturn(true);

        mockMvc.perform(post("/api/v1/data/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "importer")
    void importData_WhenDateParseFails_ShouldUseNow() throws Exception {
        User user = new User();
        user.setUsername("importer");

        VibeExportDto vibeDto = new VibeExportDto("bad-date", "NOT_A_DATE");

        DataExportDto dto = new DataExportDto();
        dto.setVibes(List.of(vibeDto));

        given(userRepository.findByUsername("importer")).willReturn(Optional.of(user));
        given(vibeRepository.existsByUserAndContentAndCreatedAt(any(), any(), any()))
                .willReturn(false);

        mockMvc.perform(post("/api/v1/data/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "importer")
    void importData_WhenFriendDoesNotExist_ShouldSkip() throws Exception {
        User user = new User();
        user.setUsername("importer");

        DataExportDto dto = new DataExportDto();
        dto.setFriends(List.of("ghost"));

        given(userRepository.findByUsername("importer")).willReturn(Optional.of(user));
        given(userRepository.findByUsername("ghost")).willReturn(Optional.empty());

        mockMvc.perform(post("/api/v1/data/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "importer")
    void importData_WhenFriendshipAlreadyExists_ShouldSkip() throws Exception {
        User user = new User();
        user.setUsername("importer");

        User friend = new User();
        friend.setUsername("friend");

        DataExportDto dto = new DataExportDto();
        dto.setFriends(List.of("friend"));

        given(userRepository.findByUsername("importer")).willReturn(Optional.of(user));
        given(userRepository.findByUsername("friend")).willReturn(Optional.of(friend));
        given(friendshipRepository.existsByUsers(user, friend)).willReturn(true);

        mockMvc.perform(post("/api/v1/data/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "importer")
    void importData_WhenUsernameDoesNotExist_ShouldReturnNotFound() throws Exception {
        given(userRepository.findByUsername("importer"))
                .willReturn(Optional.empty());

        DataExportDto dto = new DataExportDto();

        mockMvc.perform(post("/api/v1/data/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }
}