package com.example.vibelet.repository;

import com.example.vibelet.model.Friendship;
import com.example.vibelet.model.FriendshipStatus;
import com.example.vibelet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f " +
            "WHERE (f.requester = :user1 AND f.receiver = :user2) " +
            "OR (f.requester = :user2 AND f.receiver = :user1)")
    boolean existsByUsers(@Param("user1") User user1, @Param("user2") User user2);

    Optional<Friendship> findByRequesterAndReceiver(User requester, User receiver);
    List<Friendship> findByReceiverAndStatus(User receiver, FriendshipStatus status);

    @Query("SELECT f FROM Friendship f " +
            "WHERE (f.requester = :user OR f.receiver = :user) " +
            "AND f.status = 'ACCEPTED'")
    List<Friendship> findAllFriendsOfUser(@Param("user") User user);
}