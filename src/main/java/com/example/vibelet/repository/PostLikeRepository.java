package com.example.vibelet.repository;

import com.example.vibelet.model.PostLike;
import com.example.vibelet.model.User;
import com.example.vibelet.model.Vibe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserAndVibe(User user, Vibe vibe);
    boolean existsByUserAndVibe(User user, Vibe vibe);
}