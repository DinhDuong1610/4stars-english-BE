package com.fourstars.FourStars.controller.client;

import org.springframework.data.domain.Pageable;
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

@RestController("clientVideoController")
@RequestMapping("/api/v1/videos")
public class VideoController {

    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
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
