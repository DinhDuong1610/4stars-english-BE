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

import com.fourstars.FourStars.domain.request.post.PostRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.post.PostResponseDTO;
import com.fourstars.FourStars.service.PostService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/posts")
@Tag(name = "Client - Community Management API", description = "APIs for community interaction (Posts, Comments, Likes)")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "Create a new post", description = "Authenticated users can create a new post with a caption and attachments.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Post created successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @PostMapping
    @ApiMessage("Create a new post")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<PostResponseDTO> createPost(@Valid @RequestBody PostRequestDTO requestDTO)
            throws ResourceNotFoundException {
        PostResponseDTO newPost = postService.createPost(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPost);
    }

    @Operation(summary = "Update an existing post", description = "Allows the post's original author to update its caption.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post updated successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not the owner of the post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @PutMapping("/{id}")
    @ApiMessage("Update an existing post")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable long id,
            @Valid @RequestBody PostRequestDTO requestDTO)
            throws ResourceNotFoundException, BadRequestException {
        PostResponseDTO updatedPost = postService.updatePost(id, requestDTO);
        return ResponseEntity.ok(updatedPost);
    }

    @Operation(summary = "Delete a post", description = "Allows the post's original author or an admin to delete a post.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "User is not the owner of the post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @DeleteMapping("/{id}")
    @ApiMessage("Delete a post")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<Void> deletePost(@PathVariable long id)
            throws ResourceNotFoundException, BadRequestException {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a post by ID", description = "Publicly available endpoint to retrieve a single post and its details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved post"),
            @ApiResponse(responseCode = "404", description = "Post not found")
    })
    @GetMapping("/{id}")
    @ApiMessage("Fetch a post by its ID")
    public ResponseEntity<PostResponseDTO> getPostById(@PathVariable long id) throws ResourceNotFoundException {
        PostResponseDTO post = postService.fetchPostById(id);
        return ResponseEntity.ok(post);
    }

    @Operation(summary = "Get all posts (feed)", description = "Publicly available endpoint to retrieve a paginated feed of all user posts.")
    @GetMapping
    @ApiMessage("Fetch all posts with pagination")
    public ResponseEntity<ResultPaginationDTO<PostResponseDTO>> getAllPosts(Pageable pageable) {
        ResultPaginationDTO<PostResponseDTO> result = postService.fetchAllPosts(pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get all posts (feed)", description = "Publicly available endpoint to retrieve a paginated feed of all user posts.")
    @GetMapping("/me")
    @ApiMessage("Fetch all posts with pagination")
    public ResponseEntity<ResultPaginationDTO<PostResponseDTO>> getAllMyPosts(Pageable pageable) {
        ResultPaginationDTO<PostResponseDTO> result = postService.fetchAllMyPosts(pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Like a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Post liked successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "Post not found"),
            @ApiResponse(responseCode = "409", description = "Post has already been liked by the user")
    })
    @PostMapping("/{id}/like")
    @ApiMessage("Like a post")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<Void> likePost(@PathVariable("id") long postId)
            throws ResourceNotFoundException, DuplicateResourceException {
        postService.handleLikePost(postId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Unlike a post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Post unliked successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "Post not found or not liked by the user")
    })
    @DeleteMapping("/{id}/like")
    @ApiMessage("Unlike a post")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<Void> unlikePost(@PathVariable("id") long postId) throws ResourceNotFoundException {
        postService.handleUnlikePost(postId);
        return ResponseEntity.noContent().build();
    }
}
