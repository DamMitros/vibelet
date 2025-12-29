package com.example.vibelet.controller;

import com.example.vibelet.model.Comment;
import com.example.vibelet.service.CommentService;
import com.example.vibelet.service.VibeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class InteractionController {
    private final VibeService vibeService;
    private final CommentService commentService;

    public InteractionController(VibeService vibeService, CommentService commentService) {
        this.vibeService = vibeService;
        this.commentService = commentService;
    }

    @PostMapping("/interactions/vibe/{vibeId}/like")
    public ResponseEntity<?> likeVibe(@PathVariable Long vibeId, Principal principal) {
        vibeService.toggleLike(vibeId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/interactions/vibe/{vibeId}/comment")
    public ResponseEntity<?> commentVibe(
            @PathVariable Long vibeId,
            @RequestBody Map<String, String> payload,
            Principal principal
    ) {
        String content = payload.get("content");
        if (content == null || content.isBlank()) {
            return ResponseEntity.badRequest().body("Content cannot be empty");
        }

        Comment comment = vibeService.addComment(vibeId, principal.getName(), content);

        return ResponseEntity.ok(Map.of(
                "id", comment.getId(),
                "content", comment.getContent(),
                "username", comment.getUser().getUsername(),
                "userId", comment.getUser().getId(),
                "createdAt", comment.getCreatedAt().toString()
        ));
    }

    @PutMapping("/comments/{id}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            Principal principal
    ) {
        commentService.updateComment(id, payload.get("content"), principal.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long id,
            Principal principal
    ) {
        commentService.deleteComment(id, principal.getName());
        return ResponseEntity.ok().build();
    }
}