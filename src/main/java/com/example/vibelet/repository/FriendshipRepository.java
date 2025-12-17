package com.example.vibelet.repository;

import com.example.vibelet.model.Friendship;
import com.example.vibelet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    boolean existsByRequesterAndReceiver(User requester, User receiver);
    Optional<Friendship> findByRequesterAndReceiver(User requester, User receiver);
}