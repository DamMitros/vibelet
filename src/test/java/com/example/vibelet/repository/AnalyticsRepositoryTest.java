package com.example.vibelet.repository;

import com.example.vibelet.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
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