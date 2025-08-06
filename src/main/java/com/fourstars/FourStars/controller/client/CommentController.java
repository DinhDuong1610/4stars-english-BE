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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/comments")
@Tag(name = "Client - Comment Management API", description = "APIs for community interaction Comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Create a new comment or reply", description = "Authenticated users can add a comment to a post. To reply to another comment, provide the `parentCommentId` in the request body.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comment created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data (e.g., parent comment does not belong to the post)"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "Post or parent comment not found")
    })
    @PostMapping
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Create a new comment or a reply")
    public ResponseEntity<CommentResponseDTO> createComment(@Valid @RequestBody CommentRequestDTO requestDTO)
            throws ResourceNotFoundException, BadRequestException {
        CommentResponseDTO newComment = commentService.createComment(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newComment);
    }

    @Operation(summary = "Update an existing comment", description = "Allows the comment's original author to update its content.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comment updated successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not the owner of the comment"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Update an existing comment")
    public ResponseEntity<CommentResponseDTO> updateComment(
            @PathVariable long id,
            @RequestBody @Valid CommentRequestDTO requestDTO)
            throws ResourceNotFoundException, BadRequestException {
        CommentResponseDTO updatedComment = commentService.updateComment(id, requestDTO);
        return ResponseEntity.ok(updatedComment);
    }

    @Operation(summary = "Delete a comment", description = "Allows the comment's author or the post's author to delete a comment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comment deleted successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User does not have permission to delete"),
            @ApiResponse(responseCode = "404", description = "Comment not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Delete a comment")
    public ResponseEntity<Void> deleteComment(@PathVariable long id)
            throws ResourceNotFoundException, BadRequestException {
        commentService.deleteComment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get comments for a post", description = "Publicly available endpoint to retrieve a paginated list of top-level comments for a post. Replies are nested within each comment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved comments"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/post/{postId}")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<ResultPaginationDTO<CommentResponseDTO>> getCommentsByPost(
            @PathVariable long postId,
            Pageable pageable) {
        ResultPaginationDTO<CommentResponseDTO> result = commentService.fetchCommentsByPost(postId, pageable);
        return ResponseEntity.ok(result);
    }
}
