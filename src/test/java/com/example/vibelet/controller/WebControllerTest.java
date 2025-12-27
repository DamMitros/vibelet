package com.example.vibelet.controller;

import com.example.vibelet.model.Friendship;
import com.example.vibelet.model.User;
import com.example.vibelet.security.CustomUserDetailsService;
import com.example.vibelet.security.SecurityConfig;
import com.example.vibelet.service.*;
import com.example.vibelet.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebController.class)
@Import(SecurityConfig.class)
class WebControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockitoBean private VibeService vibeService;
    @MockitoBean private UserService userService;
    @MockitoBean private AuthService authService;
    @MockitoBean private FriendshipService friendshipService;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;

    @TestConfiguration
    static class MockViewConfig {
        @Bean
        @Order(Ordered.HIGHEST_PRECEDENCE)
        public ViewResolver viewResolver() {
            return new ViewResolver() {
                @Override
                public View resolveViewName(String viewName, Locale locale) {
                    return new View() {
                        @Override
                        public String getContentType() { return "text/html"; }
                        @Override
                        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
                        }
                    };
                }
            };
        }
    }

    @Test
    void index_WhenUnauthenticated_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void index_WhenAuthenticated_ShouldRedirectToFeed() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));
    }

    @Test
    void login_ShouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void register_ShouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void registerSubmit_ShouldReturnRegister_WhenServiceThrows() throws Exception {
        doThrow(new RuntimeException("boom"))
                .when(authService)
                .registerUser(anyString(), anyString(), anyString());

        mockMvc.perform(post("/register")
                        .param("username", "user")
                        .param("email", "a@b.com")
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void registerSubmit_ShouldRedirect_WhenSuccess() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "newUser")
                        .param("email", "new@test.com")
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?success"));
    }

    @Test
    void registerSubmit_ShouldReturnView_WhenValidationFails() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "newUser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @WithMockUser(username = "user")
    void feed_ShouldReturnFeedView() throws Exception {
        given(vibeService.getFeed(anyString(), anyInt(), anyInt()))
                .willReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/feed"))
                .andExpect(status().isOk())
                .andExpect(view().name("feed"))
                .andExpect(model().attributeExists("vibes"));
    }

    @Test
    @WithMockUser(username = "user")
    void createVibe_ShouldRedirectToFeed() throws Exception {
        mockMvc.perform(post("/vibes")
                        .param("content", "hello")
                        .param("privacy", "PUBLIC")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));
    }

    @Test
    @WithMockUser(username = "user")
    void deleteVibe_ShouldRedirectToFeed() throws Exception {
        mockMvc.perform(post("/vibes/{id}/delete", 1L)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/feed"));
    }

    @Test
    @WithMockUser(username = "user")
    void profile_ShouldReturnProfileView() throws Exception {
        User user = new User();
        user.setUsername("user");
        given(userRepository.findByUsername("user")).willReturn(Optional.of(user));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"));
    }

    @Test
    @WithMockUser(username = "user")
    void profile_WhenUserNotFound_ShouldThrow() throws Exception {
        given(userRepository.findByUsername("user"))
                .willReturn(Optional.empty());

        mockMvc.perform(get("/profile"))
                .andExpect(result ->
                        assertTrue(result.getResolvedException() instanceof RuntimeException)
                );
    }

    @Test
    @WithMockUser(username = "user")
    void explore_ShouldReturnExploreView() throws Exception {
        given(userService.searchUsers(any(), any())).willReturn(Collections.emptyList());

        mockMvc.perform(get("/explore").param("query", "search"))
                .andExpect(status().isOk())
                .andExpect(view().name("explore"));
    }

    @Test
    @WithMockUser(username = "user")
    void explore_WhenNoQuery_ShouldReturnEmptyResults() throws Exception {
        mockMvc.perform(get("/explore"))
                .andExpect(status().isOk())
                .andExpect(view().name("explore"))
                .andExpect(model().attributeExists("searchResults"));
    }

    @Test
    @WithMockUser(username="user")
    void explore_WhenEmptyQuery_ShouldReturnEmptyResults() throws Exception {
        mockMvc.perform(get("/explore").param("query", "   "))
                .andExpect(status().isOk())
                .andExpect(view().name("explore"))
                .andExpect(model().attributeExists("searchResults"));
    }

    @Test
    @WithMockUser(username = "user")
    void friends_ShouldReturnFriendsAndRequests() throws Exception {
        User friend = new User();
        friend.setUsername("alice");

        User requester = new User();
        requester.setUsername("bob");

        Friendship friendship = new com.example.vibelet.model.Friendship();
        friendship.setRequester(requester);

        given(friendshipService.getFriendsList("user"))
                .willReturn(List.of(friend));

        given(friendshipService.getPendingRequests("user"))
                .willReturn(List.of(friendship));

        mockMvc.perform(get("/friends"))
                .andExpect(status().isOk())
                .andExpect(view().name("friends"))
                .andExpect(model().attributeExists("friends"))
                .andExpect(model().attributeExists("requests"));
    }
}