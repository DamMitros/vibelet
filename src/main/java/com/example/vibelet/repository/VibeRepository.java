package com.example.vibelet.repository;

import com.example.vibelet.model.User;
import com.example.vibelet.model.Vibe;
import com.example.vibelet.model.PrivacyStatus;
import com.example.vibelet.model.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface VibeRepository extends JpaRepository<Vibe, Long> {

    @Query("SELECT v FROM Vibe v WHERE " +
            "v.user = :user " +
            "OR " +
            "(" +
            "  (v.user IN (SELECT f.receiver FROM Friendship f WHERE f.requester = :user AND f.status = :acceptedStatus) " +
            "   OR " +
            "   v.user IN (SELECT f.requester FROM Friendship f WHERE f.receiver = :user AND f.status = :acceptedStatus)) " +
            "  AND v.privacyStatus <> :privateStatus " +
            ") " +
            "ORDER BY v.createdAt DESC")
    Page<Vibe> findFeedForUser(
            @Param("user") User user,
            @Param("acceptedStatus") FriendshipStatus acceptedStatus,
            @Param("privateStatus") PrivacyStatus privateStatus,
            Pageable pageable
    );

    Page<Vibe> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    boolean existsByUserAndContentAndCreatedAt(User user, String content, LocalDateTime createdAt);
}