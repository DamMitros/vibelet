package com.example.vibelet.controller;

import com.example.vibelet.dto.DataExportDto;
import com.example.vibelet.dto.VibeExportDto;
import com.example.vibelet.model.User;
import com.example.vibelet.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/data")
public class DataExportController {
    private final UserRepository userRepository;

    public DataExportController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/export")
    public ResponseEntity<DataExportDto> exportData(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<VibeExportDto> vibesDtoList = user.getVibes().stream()
                .map(v -> new VibeExportDto(v.getContent(), v.getCreatedAt().toString()))
                .collect(Collectors.toList());

        DataExportDto exportData = new DataExportDto(
                user.getUsername(),
                user.getEmail(),
                user.getBio(),
                vibesDtoList
        );

        return ResponseEntity.ok(exportData);
    }

    @PostMapping("/import")
    public ResponseEntity<String> importData(@RequestBody DataExportDto data, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (data.getBio() != null) {
            user.setBio(data.getBio());
        }

        userRepository.save(user);
        return ResponseEntity.ok("Data imported successfully.");
    }
}