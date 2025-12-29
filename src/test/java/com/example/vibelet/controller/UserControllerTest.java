package com.example.vibelet.controller;

import com.example.vibelet.dto.UserProfileUpdateDto;
import com.example.vibelet.model.User;
import com.example.vibelet.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    @WithMockUser
    void searchUsers_ShouldReturnOk() throws Exception {
        given(userService.searchUsers("query", "user"))
                .willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/users/search")
                        .param("query", "query"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "me")
    void updateMyProfile_ShouldReturnOk() throws Exception {
        given(userService.updateUserProfile(anyString(), any(UserProfileUpdateDto.class), any()))
                .willReturn(new User());

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/users/me")
                        .param("bio", "updated bio")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void deleteMyAccount_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/users/me")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}