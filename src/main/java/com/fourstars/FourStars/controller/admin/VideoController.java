package com.fourstars.FourStars.controller.admin;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/videos")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Video Management API", description = "APIs for managing video lessons")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @Operation(summary = "Create a new video lesson")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Video created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data (e.g., category is not of type VIDEO)"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Create a new video lesson")
    public ResponseEntity<VideoResponseDTO> createVideo(@Valid @RequestBody VideoRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        VideoResponseDTO newVideo = videoService.createVideo(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newVideo);
    }

    @Operation(summary = "Update an existing video lesson")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Video updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "404", description = "Video or Category not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Update an existing video lesson")
    public ResponseEntity<VideoResponseDTO> updateVideo(
            @PathVariable long id,
            @Valid @RequestBody VideoRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        VideoResponseDTO updatedVideo = videoService.updateVideo(id, requestDTO);
        return ResponseEntity.ok(updatedVideo);
    }

    @Operation(summary = "Delete a video lesson")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Video deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Delete a video lesson")
    public ResponseEntity<Void> deleteVideo(@PathVariable long id) throws ResourceNotFoundException {
        videoService.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a video lesson by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved video"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch a video lesson by its ID")
    public ResponseEntity<VideoResponseDTO> getVideoById(@PathVariable long id) throws ResourceNotFoundException {
        VideoResponseDTO video = videoService.fetchVideoById(id);
        return ResponseEntity.ok(video);
    }

    @Operation(summary = "Get all video lessons with pagination and filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved video list")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch all video lessons with pagination and filtering")
    public ResponseEntity<ResultPaginationDTO<VideoResponseDTO>> getAllVideos(
            Pageable pageable,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "startCreatedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startCreatedAt,
            @RequestParam(name = "endCreatedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endCreatedAt) {
        ResultPaginationDTO<VideoResponseDTO> result = videoService.fetchAllVideos(pageable, categoryId, title,
                startCreatedAt, endCreatedAt);
        return ResponseEntity.ok(result);
    }
}
