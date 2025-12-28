package com.example.vibelet.repository;

import com.example.vibelet.model.PrivacyStatus;
import com.example.vibelet.model.User;
import com.example.vibelet.model.Vibe;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AnalyticsRepository.class)
class AnalyticsRepositoryTest {
    @Autowired
    private AnalyticsRepository analyticsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void getUserPostCounts_ShouldReturnStats() {
        User user = new User("john", "john@test.com", "pass");
        userRepository.save(user);

        Vibe vibe = new Vibe();
        vibe.setContent("hello");
        vibe.setUser(user);
        vibe.setPrivacyStatus(PrivacyStatus.PUBLIC);
        entityManager.persist(vibe);
        entityManager.flush();

        List<AnalyticsRepository.UserStats> stats =
                analyticsRepository.getUserPostCounts();

        AnalyticsRepository.UserStats stat = stats.stream()
                .filter(s -> s.username.equals("john"))
                .findFirst()
                .orElseThrow();

        assertThat(stat.vibeCount).isEqualTo(1);
    }

    @Test
    void banUser_ShouldUpdateUserStatus() {
        User user = new User("troll", "troll@test.com", "pass");
        user.setStatus("ACTIVE");
        userRepository.save(user);
        entityManager.flush();
        analyticsRepository.banUser("troll");
        entityManager.clear();

        User bannedUser = userRepository.findByUsername("troll").orElseThrow();
        assertThat(bannedUser.getStatus()).isEqualTo("BANNED");
    }
}