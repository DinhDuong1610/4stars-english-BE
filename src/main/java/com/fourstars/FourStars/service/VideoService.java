package com.fourstars.FourStars.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.Video;
import com.fourstars.FourStars.domain.request.video.VideoRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.video.VideoResponseDTO;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.repository.VideoRepository;
import com.fourstars.FourStars.util.constant.CategoryType;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;

@Service
public class VideoService {
    private static final Logger logger = LoggerFactory.getLogger(VideoService.class);

    @Value("${google.api.key}")
    private String apiKey;

    private final VideoRepository videoRepository;
    private final CategoryRepository categoryRepository;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public VideoService(VideoRepository videoRepository, CategoryRepository categoryRepository) {
        this.videoRepository = videoRepository;
        this.categoryRepository = categoryRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    private VideoResponseDTO convertToVideoResponseDTO(Video video) {
        if (video == null)
            return null;
        VideoResponseDTO dto = new VideoResponseDTO();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setUrl(video.getUrl());
        dto.setDescription(video.getDescription());
        dto.setDuration(video.getDuration());
        dto.setSubtitle(video.getSubtitle());

        if (video.getCategory() != null) {
            VideoResponseDTO.CategoryInfoDTO catInfo = new VideoResponseDTO.CategoryInfoDTO();
            catInfo.setId(video.getCategory().getId());
            catInfo.setName(video.getCategory().getName());
            dto.setCategory(catInfo);
        }

        dto.setCreatedAt(video.getCreatedAt());
        dto.setUpdatedAt(video.getUpdatedAt());
        dto.setCreatedBy(video.getCreatedBy());
        dto.setUpdatedBy(video.getUpdatedBy());
        return dto;
    }

    @Transactional
    public VideoResponseDTO createVideo(VideoRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        logger.info("Request to create new video with title: '{}' in category ID: {}", requestDTO.getTitle(),
                requestDTO.getCategoryId());

        if (videoRepository.existsByTitleAndCategoryId(requestDTO.getTitle(), requestDTO.getCategoryId())) {
            throw new DuplicateResourceException("A video with the same title already exists in this category.");
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + requestDTO.getCategoryId()));

        if (category.getType() != CategoryType.VIDEO) {
            throw new BadRequestException("The selected category is not of type 'VIDEO'.");
        }

        Video video = new Video();
        video.setTitle(requestDTO.getTitle());
        video.setUrl(requestDTO.getUrl());
        video.setDescription(requestDTO.getDescription());
        video.setSubtitle(requestDTO.getSubtitle());
        video.setCategory(category);

        String videoUrl = requestDTO.getUrl();
        if (videoUrl != null && (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be"))) {
            try {
                String videoId = extractVideoIdFromUrl(videoUrl);
                if (videoId != null) {
                    String duration = getYouTubeVideoDuration(videoId);
                    video.setDuration(duration);
                    logger.info("Successfully fetched duration for YouTube video {}: {}", videoId, duration);
                }
            } catch (Exception e) {
                logger.error("Failed to fetch duration for YouTube URL: {}. Saving without duration.", videoUrl, e);
            }
        } else {
            video.setDuration(requestDTO.getDuration());
        }

        Video savedVideo = videoRepository.save(video);
        logger.info("Successfully created new video with ID: {}", savedVideo.getId());

        return convertToVideoResponseDTO(savedVideo);
    }

    @Transactional
    public VideoResponseDTO updateVideo(long id, VideoRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        logger.info("Request to update video with ID: {}", id);

        Video videoDB = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));

        if (videoRepository.existsByTitleAndCategoryIdAndIdNot(requestDTO.getTitle(), requestDTO.getCategoryId(), id)) {
            throw new DuplicateResourceException("A video with the same title already exists in this category.");
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + requestDTO.getCategoryId()));

        if (category.getType() != CategoryType.VIDEO) {
            throw new BadRequestException("The selected category is not of type 'VIDEO'.");
        }

        videoDB.setTitle(requestDTO.getTitle());
        videoDB.setUrl(requestDTO.getUrl());
        videoDB.setDescription(requestDTO.getDescription());
        videoDB.setSubtitle(requestDTO.getSubtitle());
        videoDB.setCategory(category);

        String videoUrl = requestDTO.getUrl();
        if (videoUrl != null && (videoUrl.contains("youtube.com") || videoUrl.contains("youtu.be"))) {
            try {
                String videoId = extractVideoIdFromUrl(videoUrl);
                if (videoId != null) {
                    String duration = getYouTubeVideoDuration(videoId);
                    videoDB.setDuration(duration);
                    logger.info("Automatically updated duration for YouTube video {}: {}", videoId, duration);
                }
            } catch (Exception e) {
                logger.error("Failed to fetch duration for YouTube URL during update: {}. Duration might be outdated.",
                        videoUrl, e);
                videoDB.setDuration(requestDTO.getDuration());
            }
        } else {
            videoDB.setDuration(requestDTO.getDuration());
        }

        Video updatedVideo = videoRepository.save(videoDB);
        logger.info("Successfully updated video with ID: {}", updatedVideo.getId());

        return convertToVideoResponseDTO(updatedVideo);
    }

    @Transactional
    public void deleteVideo(long id) throws ResourceNotFoundException {
        logger.info("Request to delete video with ID: {}", id);

        Video videoToDelete = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));

        videoRepository.delete(videoToDelete);
        logger.info("Successfully deleted video with ID: {}", id);

    }

    @Transactional(readOnly = true)
    public VideoResponseDTO fetchVideoById(long id) throws ResourceNotFoundException {
        logger.debug("Request to fetch video by ID: {}", id);

        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
        return convertToVideoResponseDTO(video);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<VideoResponseDTO> fetchAllVideos(Pageable pageable, Long categoryId, String title,
            LocalDate startCreatedAt, LocalDate endCreatedAt) {
        logger.debug("Request to fetch all videos with categoryId: {} and title: {}", categoryId, title);

        Specification<Video> spec = (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + title.trim().toLowerCase() + "%"));
            }
            if (startCreatedAt != null) {
                Instant startInstant = startCreatedAt.atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startInstant));
            }

            if (endCreatedAt != null) {
                Instant endInstant = endCreatedAt.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endInstant));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Video> pageVideo = videoRepository.findAll(spec, pageable);
        List<VideoResponseDTO> videoDTOs = pageVideo.getContent().stream()
                .map(this::convertToVideoResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pageVideo.getTotalPages(),
                pageVideo.getTotalElements());
        return new ResultPaginationDTO<>(meta, videoDTOs);
    }

    private String getYouTubeVideoDuration(String videoId) throws Exception {
        String apiUrl = String.format(
                "https://www.googleapis.com/youtube/v3/videos?part=contentDetails&id=%s&key=%s",
                videoId, apiKey);

        logger.debug("Calling YouTube API: {}", apiUrl);

        String jsonResponse = restTemplate.getForObject(apiUrl, String.class);
        if (jsonResponse == null) {
            logger.warn("Received null response from YouTube API for video ID: {}", videoId);
            return null;
        }

        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode items = root.path("items");

        if (items.isArray() && !items.isEmpty()) {
            String isoDuration = items.get(0).path("contentDetails").path("duration").asText(null);
            if (isoDuration != null) {
                return formatDuration(isoDuration);
            }
        }

        logger.warn("Could not find duration in YouTube API response for video ID: {}", videoId);
        return null;
    }

    private String extractVideoIdFromUrl(String url) {
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%2Fvideos%2F|youtu.be%2F|\\/v%2F)[^#\\&\\?\\n]*";
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private String formatDuration(String isoDuration) {
        long seconds = Duration.parse(isoDuration).getSeconds();
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
}
