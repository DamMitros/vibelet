package com.example.vibelet.controller;

import com.example.vibelet.dto.DataExportDto;
import com.example.vibelet.dto.VibeExportDto;
import com.example.vibelet.model.PrivacyStatus;
import com.example.vibelet.model.User;
import com.example.vibelet.model.Vibe;
import com.example.vibelet.repository.UserRepository;
import com.example.vibelet.repository.VibeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/data")
public class DataExportController {
    private final UserRepository userRepository;
    private final VibeRepository vibeRepository;

    public DataExportController(UserRepository userRepository, VibeRepository vibeRepository) {
        this.userRepository = userRepository;
        this.vibeRepository = vibeRepository;
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
    @Transactional
    public ResponseEntity<String> importData(@RequestBody DataExportDto data, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (data.getBio() != null) {
            user.setBio(data.getBio());
        }

        userRepository.save(user);

        int importedCount = 0;
        if (data.getVibes() != null && !data.getVibes().isEmpty()) {
            for (VibeExportDto vibeDto : data.getVibes()) {
                LocalDateTime createdAt;
                try {
                    createdAt = LocalDateTime.parse(vibeDto.getCreatedAt());
                } catch (Exception e) {
                    createdAt = LocalDateTime.now();
                }

                boolean exists = vibeRepository.existsByUserAndContentAndCreatedAt(
                        user,
                        vibeDto.getContent(),
                        createdAt
                );

                if (!exists) {
                    Vibe newVibe = new Vibe();
                    newVibe.setContent(vibeDto.getContent());
                    newVibe.setCreatedAt(createdAt);
                    newVibe.setUser(user);
                    newVibe.setPrivacyStatus(PrivacyStatus.PRIVATE);

                    vibeRepository.save(newVibe);
                    importedCount++;
                }
            }
        }

        return ResponseEntity.ok("Data imported successfully. Vibes imported: " + importedCount);
    }
}