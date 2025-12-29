package com.example.vibelet.repository;

import com.example.vibelet.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VibeRepositoryTest {
    @Autowired
    private VibeRepository vibeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @Test
    void findFeedForUser_ShouldReturnFriendsPostsAndMyPostsAndPublicPosts() {
        User me = userRepository.save(new User("me", "me@test.com", "pass"));
        User friend = userRepository.save(new User("friend", "friend@test.com", "pass"));
        User stranger = userRepository.save(new User("stranger", "stranger@test.com", "pass"));

        Friendship f = new Friendship();
        f.setRequester(me);
        f.setReceiver(friend);
        f.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(f);

        createVibe(me, "My vibe", PrivacyStatus.PUBLIC);
        createVibe(friend, "Friend vibe", PrivacyStatus.FRIENDS_ONLY);
        createVibe(friend, "Secret vibe", PrivacyStatus.PRIVATE);
        createVibe(stranger, "Stranger vibe", PrivacyStatus.PUBLIC);

        Page<Vibe> feed = vibeRepository.findFeedForUser(
                me,
                FriendshipStatus.ACCEPTED,
                PrivacyStatus.PRIVATE,
                PageRequest.of(0, 10)
        );

        assertThat(feed.getContent()).hasSize(3);
        assertThat(feed.getContent()).extracting(Vibe::getContent)
                .containsExactlyInAnyOrder("My vibe", "Friend vibe", "Stranger vibe");
    }

    private Vibe createVibe(User user, String content, PrivacyStatus privacy) {
        Vibe v = new Vibe();
        v.setUser(user);
        v.setContent(content);
        v.setPrivacyStatus(privacy);
        return vibeRepository.save(v);
    }
}