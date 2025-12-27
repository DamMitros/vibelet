package com.example.vibelet.model;

import com.example.vibelet.dto.*;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ModelsTest {
    @Test
    void testUserEntity() {
        User u = new User("user", "mail", "pass");
        u.setId(1L);
        u.setBio("bio");
        u.setStatus("active");
        u.setAvatarUrl("url");
        u.setVibes(new ArrayList<>());
        u.setComments(new ArrayList<>());
        u.setLikes(new ArrayList<>());
        u.setSentFriendships(new ArrayList<>());
        u.setReceivedFriendships(new ArrayList<>());

        assertAll(
                ()-> assertNotNull(u.getId()),
                ()-> assertEquals("user", u.getUsername()),
                ()-> assertEquals("mail", u.getEmail()),
                ()-> assertEquals("bio", u.getBio())
        );
        u.onCreate();
    }

    @Test
    void testVibeEntity() {
        Vibe v = new Vibe();
        v.setId(1L);
        v.setContent("c");
        v.setImageUrl("img");
        v.setPrivacyStatus(PrivacyStatus.PUBLIC);
        v.setCreatedAt(LocalDateTime.now());
        v.setComments(new ArrayList<>());
        v.setLikes(new ArrayList<>());

        assertAll(
                ()->assertEquals(1L, v.getId()),
                ()->assertEquals("c", v.getContent())
        );
    }

    @Test
    void testLoginRequest(){
        LoginRequest lr = new LoginRequest();
        lr.setUsername("user");
        lr.setPassword("pass");
        assertEquals("user", lr.getUsername());
    }

    @Test
    void testRegisterRequest(){
        RegisterRequest rr = new RegisterRequest();
        rr.setUsername("user");
        rr.setEmail("mail");
        rr.setPassword("pass");
        assertEquals("mail", rr.getEmail());
    }

    @Test
    void testUserSearchDto(){
        UserSearchDto us = new UserSearchDto(1L, "u", "a", "s");
        assertEquals("s", us.getFriendshipStatus());
    }

    @Test
    void testDataExportDto(){
        DataExportDto de = new DataExportDto();
        de.setBio("b");
        assertEquals("b", de.getBio());
    }
}