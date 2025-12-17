package com.example.vibelet.service;

import com.example.vibelet.model.User;
import com.example.vibelet.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already taken!");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already taken!");
        }

        User user = new User(username, email, passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }
}