package com.example.vibelet.service;

import com.example.vibelet.model.Comment;
import com.example.vibelet.model.User;
import com.example.vibelet.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void updateComment_ShouldUpdate_WhenUserIsOwner() {
        User user = new User();
        user.setUsername("me");

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setContent("old");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.updateComment(1L, "new content", "me");

        verify(commentRepository).save(comment);
        verify(commentRepository).findById(1L);
    }

    @Test
    void updateComment_ShouldThrow_WhenContentIsNull() {
        assertThatThrownBy(() ->
                commentService.updateComment(1L, null, "me")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Content cannot be empty");

        verifyNoInteractions(commentRepository);
    }

    @Test
    void updateComment_ShouldThrow_WhenContentIsBlank() {
        assertThatThrownBy(() ->
                commentService.updateComment(1L, "   ", "me")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Content cannot be empty");

        verifyNoInteractions(commentRepository);
    }

    @Test
    void updateComment_ShouldThrow_WhenNotOwner() {
        User owner = new User();
        owner.setUsername("owner");

        Comment comment = new Comment();
        comment.setUser(owner);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() ->
                commentService.updateComment(1L, "new", "me")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Not authorized");

        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_ShouldThrow_WhenCommentNotFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                commentService.updateComment(1L, "new", "me")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Comment not found");
    }

    @Test
    void deleteComment_ShouldDelete_WhenUserIsOwner() {
        User user = new User();
        user.setUsername("me");

        Comment comment = new Comment();
        comment.setUser(user);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        commentService.deleteComment(1L, "me");

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_ShouldThrow_WhenNotOwner() {
        User owner = new User();
        owner.setUsername("owner");

        Comment comment = new Comment();
        comment.setUser(owner);

        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        assertThatThrownBy(() ->
                commentService.deleteComment(1L, "me")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Not authorized");

        verify(commentRepository, never()).delete(any());
    }

    @Test
    void deleteComment_ShouldThrow_WhenCommentNotFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                commentService.deleteComment(1L, "me")
        ).isInstanceOf(RuntimeException.class)
                .hasMessage("Comment not found");
    }
}