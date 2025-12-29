package com.example.vibelet.controller;

import com.example.vibelet.model.Comment;
import com.example.vibelet.model.User;
import com.example.vibelet.service.CommentService;
import com.example.vibelet.service.VibeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InteractionController.class)
class InteractionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VibeService vibeService;

    @MockitoBean
    private CommentService commentService;

    @Test
    @WithMockUser(username = "me")
    void toggleLike_ShouldReturnOk() throws Exception {
        doNothing().when(vibeService).toggleLike(1L, "me");

        mockMvc.perform(post("/api/v1/interactions/vibe/1/like")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "me")
    void addComment_ShouldReturnOk() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("me");

        Comment comment = new Comment();
        comment.setId(10L);
        comment.setUser(user);
        comment.setContent("hello");
        comment.setCreatedAt(LocalDateTime.now());

        given(vibeService.addComment(eq(1L), eq("me"), eq("hello")))
                .willReturn(comment);

        mockMvc.perform(post("/api/v1/interactions/vibe/1/comment")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "content": "hello"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "me")
    void addBlankComment_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/interactions/vibe/1/comment")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                  "content": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "me")
    void addEmptyComment_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/interactions/vibe/1/comment")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "me")
    void updateComment_ShouldReturnOk() throws Exception {
        doNothing().when(commentService).updateComment(1L, "updated", "me");

        mockMvc.perform(put("/api/v1/comments/1")
                        .with(csrf())
                        .contentType("application/json")
                        .content("""
                                {
                                 "content": "updated"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "me")
    void deleteComment_ShouldReturnOk() throws Exception {
        doNothing().when(commentService).deleteComment(1L, "me");

        mockMvc.perform(delete("/api/v1/comments/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}