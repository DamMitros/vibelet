package com.example.vibelet.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vibes")
public class Vibe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrivacyStatus privacyStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "vibe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "vibe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostLike> likes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }
    public String getContent() {
        return content;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public PrivacyStatus getPrivacyStatus() {
        return privacyStatus;
    }
    public User getUser() {
        return user;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public List<Comment> getComments() {
        return comments;
    }
    public List<PostLike> getLikes() {
        return likes;
    }
    public void setLikes(List<PostLike> likes) {
        this.likes = likes;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public void setPrivacyStatus(PrivacyStatus privacyStatus) {
        this.privacyStatus = privacyStatus;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}