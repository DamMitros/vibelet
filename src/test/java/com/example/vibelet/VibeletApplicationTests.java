package com.example.vibelet;

import com.example.vibelet.dto.RegisterRequest;
import com.example.vibelet.model.User;
import com.example.vibelet.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@AutoConfigureTestDatabase
class VibeletApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
    }

    @Test
    void fullRegistrationFlow() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setUsername("integrationUser");
        register.setEmail("int@test.com");
        register.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register))
                        .with(csrf()))
                .andExpect(status().isCreated());

        assert userRepository.findByUsername("integrationUser").isPresent();
    }

    @Test
    @WithMockUser(username = "existingUser")
    void secureEndpoints_ShouldBeAccessible_ForAuthenticatedUser() throws Exception {
        User user = new User("existingUser", "exist@test.com", "pass");
        userRepository.save(user);

        mockMvc.perform(get("/api/v1/vibes/feed"))
                .andExpect(status().isOk());
    }

    @Test
    void unauthorizedAccess_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/api/v1/vibes/feed"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}