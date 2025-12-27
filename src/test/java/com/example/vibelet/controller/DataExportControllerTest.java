package com.example.vibelet.controller;

import com.example.vibelet.dto.DataExportDto;
import com.example.vibelet.model.User;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
}