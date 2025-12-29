package com.example.vibelet.controller;

import com.example.vibelet.dto.UserSearchDto;
import com.example.vibelet.model.*;
import com.example.vibelet.repository.UserRepository;
import com.example.vibelet.repository.VibeRepository;
import com.example.vibelet.security.CustomUserDetailsService;
import com.example.vibelet.security.SecurityConfig;
import com.example.vibelet.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebController.class)
@Import(SecurityConfig.class)
class WebControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VibeService vibeService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private FriendshipService friendshipService;
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private VibeRepository vibeRepository;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User currentUser;
    private User otherUser;

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
                        public String getContentType() {
                            return "text/html";
                        }

                        @Override
                        public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) {
                        }
                    };
                }
            };
        }
    }

    @BeforeEach
    void setupUsers() {
        currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("user");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other");
    }

    @Test
    void index_Unauthenticated_RedirectLogin() throws Exception {
        mockMvc.perform(get("/").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "user")
    void index_Authenticated_RedirectFeed() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vibes"));
    }

    @Test
    void login_ReturnsLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void register_ReturnsRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void registerSubmit_ServiceThrows_ReturnsRegisterWithError() throws Exception {
        doThrow(new RuntimeException("boom"))
                .when(authService).registerUser(anyString(), anyString(), anyString());

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
    void registerSubmit_Success_RedirectsLogin() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "newUser")
                        .param("email", "new@test.com")
                        .param("password", "password123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?success"));
    }

    @Test
    void registerSubmit_ValidationFails_ReturnsRegister() throws Exception {
        mockMvc.perform(post("/register")
                        .param("username", "")
                        .param("email", "bad")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    @WithMockUser(username = "user")
    void feed_ReturnsFeedView() throws Exception {
        given(userRepository.findByUsername("user")).willReturn(Optional.of(currentUser));
        given(vibeRepository.findFeedForUser(any(), any(), any(), any()))
                .willReturn(Page.empty());

        mockMvc.perform(get("/vibes"))
                .andExpect(status().isOk())
                .andExpect(view().name("vibes"))
                .andExpect(model().attributeExists("vibes"))
                .andExpect(model().attributeExists("currentUser"));
    }

    @Test
    @WithMockUser(username = "user")
    void createVibe_RedirectsFeed() throws Exception {
        mockMvc.perform(post("/vibes")
                        .param("content", "hello")
                        .param("privacy", "PUBLIC")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vibes"));
    }

    @Test
    @WithMockUser(username = "user")
    void deleteVibe_RedirectsFeed() throws Exception {
        mockMvc.perform(post("/vibes/{id}/delete", 1L)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/vibes"));
    }

    @Test
    @WithMockUser(username = "user")
    void profile_ReturnsProfileView() throws Exception {
        given(userRepository.findByUsername("user")).willReturn(Optional.of(currentUser));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("currentUser"));
    }

    @Test
    @WithMockUser(username = "user")
    void profile_UserNotFound_Throws() throws Exception {
        given(userRepository.findByUsername("user")).willReturn(Optional.empty());

        mockMvc.perform(get("/profile"))
                .andExpect(result -> assertInstanceOf(RuntimeException.class, result.getResolvedException()));
    }

    @Test
    @WithMockUser(username = "user")
    void explore_WithQuery_ReturnsResults() throws Exception {
        given(userRepository.findByUsername("user")).willReturn(Optional.of(currentUser));
        given(userService.searchUsers("query", "user"))
                .willReturn(List.of(new UserSearchDto(otherUser.getId(), "other", null, "NONE")));

        mockMvc.perform(get("/explore").param("query", "query"))
                .andExpect(status().isOk())
                .andExpect(view().name("explore"))
                .andExpect(model().attributeExists("searchResults"));
    }

    @Test
    @WithMockUser(username = "user")
    void explore_NoQuery_ReturnsAllUsersExceptCurrent() throws Exception {
        given(userRepository.findByUsername("user")).willReturn(Optional.of(currentUser));
        given(userRepository.findAll(any(PageRequest.class)))
                .willReturn(new PageImpl<>(List.of(currentUser, otherUser)));
        given(userService.mapToSearchDto(eq(currentUser), any())).willReturn(new UserSearchDto(currentUser.getId(), "user", null, "SELF"));
        given(userService.mapToSearchDto(eq(otherUser), any())).willReturn(new UserSearchDto(otherUser.getId(), "other", null, "NONE"));

        mockMvc.perform(get("/explore"))
                .andExpect(status().isOk())
                .andExpect(view().name("explore"))
                .andExpect(model().attributeExists("searchResults"));
    }

    @Test
    @WithMockUser(username = "user")
    void friends_ReturnsFriendsAndRequests() throws Exception {
        given(friendshipService.getAcceptedFriendships("user"))
                .willReturn(List.of(new Friendship() {{
                    setRequester(currentUser);
                    setReceiver(otherUser);
                }}));
        given(friendshipService.getPendingRequests("user"))
                .willReturn(List.of(new Friendship() {{
                    setRequester(otherUser);
                    setReceiver(currentUser);
                }}));

        mockMvc.perform(get("/friends"))
                .andExpect(status().isOk())
                .andExpect(view().name("friends"))
                .andExpect(model().attributeExists("friends"))
                .andExpect(model().attributeExists("requests"));
    }

    @Test
    @WithMockUser(username = "user")
    void userProfile_ReturnsUserProfile() throws Exception {
        given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));
        given(userRepository.findByUsername("user")).willReturn(Optional.of(currentUser));
        given(friendshipService.areFriends(currentUser, otherUser)).willReturn(true);
        given(friendshipService.getAcceptedFriendships("other")).willReturn(List.of());
        given(vibeRepository.findByUserOrderByCreatedAtDesc(eq(otherUser), any(PageRequest.class)))
                .willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/users/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(view().name("user_profile"))
                .andExpect(model().attributeExists("targetUser"))
                .andExpect(model().attributeExists("currentUser"))
                .andExpect(model().attributeExists("friendshipStatus"))
                .andExpect(model().attributeExists("vibes"))
                .andExpect(model().attributeExists("friendCount"))
                .andExpect(model().attributeExists("vibeCount"));
    }
}