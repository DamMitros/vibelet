package com.example.vibelet.controller;

import com.example.vibelet.model.Vibe;
import com.example.vibelet.model.User;
import com.example.vibelet.service.VibeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VibeController.class)
class VibeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VibeService vibeService;

    @Test
    @WithMockUser(username = "tester")
    void getFeed_ShouldReturnOk() throws Exception {
        User u = new User();
        u.setUsername("tester");
        Vibe v = new Vibe();
        v.setUser(u);
        v.setContent("Test content");

        given(vibeService.getFeed("tester", 0, 10))
                .willReturn(new PageImpl<>(List.of(v)));

        mockMvc.perform(get("/api/v1/vibes/feed"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "tester")
    void createVibe_ShouldReturnCreated() throws Exception {
        Vibe created = new Vibe();
        created.setId(1L);

        given(vibeService.createVibe(eq("tester"), any(), any(), any()))
                .willReturn(created);

        mockMvc.perform(multipart("/api/v1/vibes")
                        .param("content", "New Vibe")
                        .param("privacy", "PUBLIC")
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    @Test
    void getFeed_WhenUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/vibes/feed"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getUserVibes_ShouldReturnOk() throws Exception {
        Vibe vibe = new Vibe();
        vibe.setContent("User vibe");

        given(vibeService.getUserVibes(1L, 0, 10))
                .willReturn(new PageImpl<>(List.of(vibe)));

        mockMvc.perform(get("/api/v1/vibes/user/{userId}", 1L))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "tester")
    void deleteVibe_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/v1/vibes/{id}", 1L)
                                .with(csrf())
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteVibe_WhenUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .delete("/api/v1/vibes/{id}", 1L)
                                .with(csrf())
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "tester")
    void updateVibe_ShouldReturnOk() throws Exception {
        Vibe updated = new Vibe();
        updated.setId(1L);
        updated.setContent("Updated content");

        given(vibeService.updateVibe(eq(1L), eq("tester"), any(), any()))
                .willReturn(updated);

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/api/v1/vibes/{id}", 1L)
                                .param("content", "Updated content")
                                .param("privacy", "PUBLIC")
                                .with(csrf())
                )
                .andExpect(status().isOk());
    }

    @Test
    void updateVibe_WhenUnauthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .put("/api/v1/vibes/{id}", 1L)
                                .with(csrf())
                )
                .andExpect(status().isUnauthorized());
    }
}