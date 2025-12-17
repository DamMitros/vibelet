package com.example.vibelet.model;

import jakarta.persistence.*;

@Entity
@Table(name = "post_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"vibe_id", "user_id"})
})
public class PostLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vibe_id", nullable = false)
    private Vibe vibe;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Long getId() {
        return id;
    }
    public Vibe getVibe() {
        return vibe;
    }
    public User getUser() {
        return user;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public void setVibe(Vibe vibe) {
        this.vibe = vibe;
    }
    public void setUser(User user) {
        this.user = user;
    }
}