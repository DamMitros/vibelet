package com.example.vibelet.service;

import com.example.vibelet.model.User;
import com.example.vibelet.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_ShouldEncodePasswordAndSave() {
        String username = "newUser";
        String rawPass = "secret";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(rawPass)).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        authService.registerUser(username, "email@test.com", rawPass);
        verify(userRepository).save(argThat(user ->
                user.getPassword().equals("encoded_secret") &&
                        user.getUsername().equals(username)
        ));
    }

    @Test
    void registerUser_ShouldThrow_WhenUsernameExists() {
        when(userRepository.existsByUsername("exists")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            authService.registerUser("exists", "mail", "pass");
        });
    }

    @Test
    void registerUser_ShouldThrow_WhenEmailExists() {
        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("exists")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            authService.registerUser("newUser", "exists", "pass");
        });
    }
}