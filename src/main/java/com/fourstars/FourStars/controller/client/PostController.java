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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    @ApiMessage("Create a new post")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponseDTO> createPost(@Valid @RequestBody PostRequestDTO requestDTO)
            throws ResourceNotFoundException {
        PostResponseDTO newPost = postService.createPost(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newPost);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update an existing post")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable long id,
            @Valid @RequestBody PostRequestDTO requestDTO)
            throws ResourceNotFoundException, BadRequestException {
        PostResponseDTO updatedPost = postService.updatePost(id, requestDTO);
        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a post")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(@PathVariable long id)
            throws ResourceNotFoundException, BadRequestException {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch a post by its ID")
    public ResponseEntity<PostResponseDTO> getPostById(@PathVariable long id) throws ResourceNotFoundException {
        PostResponseDTO post = postService.fetchPostById(id);
        return ResponseEntity.ok(post);
    }

    @GetMapping
    @ApiMessage("Fetch all posts with pagination")
    public ResponseEntity<ResultPaginationDTO<PostResponseDTO>> getAllPosts(Pageable pageable) {
        ResultPaginationDTO<PostResponseDTO> result = postService.fetchAllPosts(pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/like")
    @ApiMessage("Like a post")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> likePost(@PathVariable("id") long postId)
            throws ResourceNotFoundException, DuplicateResourceException {
        postService.handleLikePost(postId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    @ApiMessage("Unlike a post")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unlikePost(@PathVariable("id") long postId) throws ResourceNotFoundException {
        postService.handleUnlikePost(postId);
        return ResponseEntity.noContent().build();
    }
}
