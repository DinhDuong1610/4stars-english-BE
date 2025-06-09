package com.fourstars.FourStars.controller.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.post.PostRequestDTO;
import com.fourstars.FourStars.domain.response.post.PostResponseDTO;
import com.fourstars.FourStars.service.PostService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;
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
}
