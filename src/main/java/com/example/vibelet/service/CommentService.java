package com.example.vibelet.service;

import com.example.vibelet.model.Comment;
import com.example.vibelet.repository.CommentRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService {
    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public void updateComment(Long commentId, String content, String username) {
        if (content == null || content.isBlank()) {
            throw new RuntimeException("Content cannot be empty");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Not authorized");
        }

        comment.setContent(content);
        commentRepository.save(comment);
    }

    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Not authorized");
        }

        commentRepository.delete(comment);
    }
}