package com.example.vibelet.controller;

import com.example.vibelet.model.PrivacyStatus;
import com.example.vibelet.model.Vibe;
import com.example.vibelet.service.VibeService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/vibes")
public class VibeController {
    private final VibeService vibeService;

    public VibeController(VibeService vibeService) {
        this.vibeService = vibeService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Vibe> createVibe(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("privacy") PrivacyStatus privacy,
            Principal principal) {

        Vibe createdVibe = vibeService.createVibe(principal.getName(), content, file, privacy);
        return new ResponseEntity<>(createdVibe, HttpStatus.CREATED);
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<Vibe>> getFeed(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Vibe> feed = vibeService.getFeed(principal.getName(), page, size);
        return ResponseEntity.ok(feed);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Vibe>> getUserVibes(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(vibeService.getUserVibes(userId, page, size));
    }
}