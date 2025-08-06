package com.fourstars.FourStars.controller.client;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.video.VideoResponseDTO;
import com.fourstars.FourStars.service.VideoService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("clientVideoController")
@RequestMapping("/api/v1/videos")
@Tag(name = "Client - Video Management API", description = "APIs for managing video lessons")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @Operation(summary = "Get a video lesson by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved video"),
            @ApiResponse(responseCode = "404", description = "Video not found")
    })
    @GetMapping("/{id}")
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
