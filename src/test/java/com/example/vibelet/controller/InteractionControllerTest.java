package com.example.vibelet.controller;

import com.example.vibelet.service.VibeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InteractionController.class)
class InteractionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VibeService vibeService;

    @Test
    @WithMockUser
    void addComment_ShouldReturnOk() throws Exception {
        Map<String, String> payload = Map.of("content", "Nice vibe!");

        mockMvc.perform(post("/api/v1/interactions/vibe/1/comment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void addBlankComment_ShouldReturnBadRequest() throws Exception {
        Map<String, String> payload = Map.of("content", "");

        mockMvc.perform(post("/api/v1/interactions/vibe/1/comment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void addEmptyComment_ShouldReturnBadRequest() throws Exception {
        Map<String, String> payload = Map.of();

        mockMvc.perform(post("/api/v1/interactions/vibe/1/comment")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(payload))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void toggleLike_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/interactions/vibe/1/like")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}