package com.example.vibelet.controller;

import com.example.vibelet.service.VibeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/interactions")
public class InteractionController {
    private final VibeService vibeService;

    public InteractionController(VibeService vibeService) {
        this.vibeService = vibeService;
    }

    @PostMapping("/vibe/{vibeId}/comment")
    public ResponseEntity<String> addComment(@PathVariable Long vibeId,
                                             @RequestBody Map<String, String> payload,
                                             Principal principal) {
        String content = payload.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body("Comment content cannot be empty");
        }

        vibeService.addComment(vibeId, principal.getName(), content);
        return ResponseEntity.ok("Comment added.");
    }

    @PostMapping("/vibe/{vibeId}/like")
    public ResponseEntity<String> toggleLike(@PathVariable Long vibeId, Principal principal) {
        vibeService.toggleLike(vibeId, principal.getName());
        return ResponseEntity.ok("Like toggled.");
    }
}