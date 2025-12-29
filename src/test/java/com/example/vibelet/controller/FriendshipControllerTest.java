package com.example.vibelet.controller;

import com.example.vibelet.model.Friendship;
import com.example.vibelet.model.FriendshipStatus;
import com.example.vibelet.model.User;
import com.example.vibelet.service.FriendshipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FriendshipController.class)
class FriendshipControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendshipService friendshipService;

    @Test
    @WithMockUser(username = "me")
    void sendRequest_ShouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/v1/friends/request/2")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "me")
    void getFriends_ShouldReturnList() throws Exception {
        User me = new User();
        me.setUsername("me");

        User friendUser = new User();
        friendUser.setUsername("friend");

        Friendship friendship = new Friendship();
        friendship.setRequester(me);
        friendship.setReceiver(friendUser);
        friendship.setStatus(FriendshipStatus.ACCEPTED);

        given(friendshipService.getAcceptedFriendships("me"))
                .willReturn(List.of(friendship));

        mockMvc.perform(get("/api/v1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("friend"));
    }

    @Test
    @WithMockUser(username = "me")
    void acceptRequest_ShouldReturnOk() throws Exception {
        mockMvc.perform(put("/api/v1/friends/accept/10")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void removeFriend_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/api/v1/friends/5")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getPendingRequests_ShouldReturnOk() throws Exception {
        given(friendshipService.getPendingRequests("me"))
                .willReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/friends/requests"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "me")
    void getPendingRequests_ShouldReturnOk_AndMapRequesterUsername() throws Exception {
        User requester = new User();
        requester.setUsername("john");

        Friendship friendship = new Friendship();
        friendship.setRequester(requester);

        given(friendshipService.getPendingRequests("me"))
                .willReturn(List.of(friendship));

        mockMvc.perform(get("/api/v1/friends/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].requester.username").value("john"));
    }
}