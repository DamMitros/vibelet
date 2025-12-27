package com.example.vibelet.model;

import jakarta.persistence.*;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String avatarUrl;
    private String bio;
    private String status;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public User() {}

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    public Long getId() {
        return id;
    }
    public String getAvatarUrl() {
        return avatarUrl;
    }
    public String getPassword() {
        return password;
    }
    public String getEmail() {
        return email;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public String getBio() {
        return bio;
    }
    public String getStatus() {
        return status;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setBio(String bio) {
        this.bio = bio;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public List<Vibe> getVibes() {
        return vibes;
    }
    public void setVibes(List<Vibe> vibes) {
        this.vibes = vibes;
    }

    public List<Friendship> getSentFriendships() {
        return sentFriendships;
    }
    public void setSentFriendships(List<Friendship> sentFriendships) {
        this.sentFriendships = sentFriendships;
    }

    public List<Friendship> getReceivedFriendships() {
        return receivedFriendships;
    }
    public void setReceivedFriendships(List<Friendship> receivedFriendships) {
        this.receivedFriendships = receivedFriendships;
    }

    public List<Comment> getComments() {
        return comments;
    }
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<PostLike> getLikes() {
        return likes;
    }
    public void setLikes(List<PostLike> likes) {
        this.likes = likes;
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vibe> vibes = new ArrayList<>();

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> sentFriendships = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Friendship> receivedFriendships = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();
}