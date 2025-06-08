package com.fourstars.FourStars.controller.admin;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.video.VideoRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.video.VideoResponseDTO;
import com.fourstars.FourStars.service.VideoService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/videos")
// @PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @PostMapping
    @ApiMessage("Create a new video lesson")
    public ResponseEntity<VideoResponseDTO> createVideo(@Valid @RequestBody VideoRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        VideoResponseDTO newVideo = videoService.createVideo(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newVideo);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update an existing video lesson")
    public ResponseEntity<VideoResponseDTO> updateVideo(
            @PathVariable long id,
            @Valid @RequestBody VideoRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        VideoResponseDTO updatedVideo = videoService.updateVideo(id, requestDTO);
        return ResponseEntity.ok(updatedVideo);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a video lesson")
    public ResponseEntity<Void> deleteVideo(@PathVariable long id) throws ResourceNotFoundException {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch a video lesson by its ID")
    public ResponseEntity<VideoResponseDTO> getVideoById(@PathVariable long id) throws ResourceNotFoundException {
        VideoResponseDTO video = videoService.fetchVideoById(id);
        return ResponseEntity.ok(video);
    }

    @GetMapping
    @ApiMessage("Fetch all video lessons with pagination and filtering")
    public ResponseEntity<ResultPaginationDTO<VideoResponseDTO>> getAllVideos(
            Pageable pageable,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "title", required = false) String title) {
        ResultPaginationDTO<VideoResponseDTO> result = videoService.fetchAllVideos(pageable, categoryId, title);
        return ResponseEntity.ok(result);
    }
}
