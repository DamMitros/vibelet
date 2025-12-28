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
        u.setPassword("pass");
        u.setStatus("active");
        u.setAvatarUrl("url");
        u.setVibes(new ArrayList<>());
        u.setComments(new ArrayList<>());
        u.setLikes(new ArrayList<>());
        u.setSentFriendships(new ArrayList<>());
        u.setReceivedFriendships(new ArrayList<>());

        assertAll(
                ()-> assertNotNull(u.getId()),
                ()-> assertEquals("pass", u.getPassword()),
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
    void testComment(){
        Comment c = new Comment();
        c.setId(1L);
        c.setContent("comm");
        c.setCreatedAt(LocalDateTime.now());

        assertAll(
                ()->assertEquals(1L, c.getId()),
                ()->assertEquals("comm", c.getContent()),
                ()->assertNotNull(c.getCreatedAt()),
                ()->assertNull(c.getVibe(),"Vibe should be null"),
                ()->assertNull(c.getUser(),"User should be null")
        );
    }

    @Test
    void testPostLike(){
        PostLike pl = new PostLike();
        pl.setId(1L);

        assertAll(
                ()-> assertEquals(1L, pl.getId()),
                ()-> assertNull(pl.getUser()),
                ()-> assertNull(pl.getVibe())
        );
    }

    @Test
    void testFriendship(){
        Friendship f = new Friendship();
        f.setId(1L);
        f.setStatus(FriendshipStatus.PENDING);

        LocalDateTime createdAt = LocalDateTime.now();
        f.setCreatedAt(createdAt);

        assertAll(
                ()->assertEquals(1L, f.getId()),
                ()->assertEquals(FriendshipStatus.PENDING, f.getStatus()),
                ()->assertNull(f.getRequester()),
                ()->assertNull(f.getReceiver()),
                ()->assertEquals(createdAt, f.getCreatedAt())
        );
    }

    @Test
    void testUserSearchDto(){
        UserSearchDto us = new UserSearchDto(1L, "u", "a", "s");
        assertAll(
                ()->assertEquals(1L, us.getId()),
                ()->assertEquals("u", us.getUsername()),
                ()->assertEquals("a", us.getAvatarUrl()),
                ()->assertEquals("s", us.getFriendshipStatus())
        );
    }

    @Test
    void testDataExportDto(){
        DataExportDto de = new DataExportDto();
        de.setBio("b");
        assertEquals("b", de.getBio());
    }
}