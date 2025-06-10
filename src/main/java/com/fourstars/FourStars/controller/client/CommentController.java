package com.fourstars.FourStars.controller.client;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.comment.CommentRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.comment.CommentResponseDTO;
import com.fourstars.FourStars.service.CommentService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    @ApiMessage("Create a new comment or a reply")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponseDTO> createComment(@Valid @RequestBody CommentRequestDTO requestDTO)
            throws ResourceNotFoundException, BadRequestException {
        CommentResponseDTO newComment = commentService.createComment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update an existing comment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable long id,
            @RequestBody @Valid CommentRequestDTO requestDTO)
            throws ResourceNotFoundException, BadRequestException {
        CommentResponseDTO updatedComment = commentService.updateComment(id, requestDTO);
        return ResponseEntity.ok(updatedComment);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a comment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(@PathVariable long id)
            throws ResourceNotFoundException, BadRequestException {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/post/{postId}")
    @ApiMessage("Fetch comments for a specific post")
    public ResponseEntity<ResultPaginationDTO<CommentResponseDTO>> getCommentsByPost(
            @PathVariable long postId,
            Pageable pageable) {
        ResultPaginationDTO<CommentResponseDTO> result = commentService.fetchCommentsByPost(postId, pageable);
        return ResponseEntity.ok(result);
    }
}
