package com.fourstars.FourStars.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final VideoRepository videoRepository;
    private final CategoryRepository categoryRepository;

    public VideoService(VideoRepository videoRepository, CategoryRepository categoryRepository) {
        this.videoRepository = videoRepository;
        this.categoryRepository = categoryRepository;
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
        video.setDuration(requestDTO.getDuration());
        video.setSubtitle(requestDTO.getSubtitle());
        video.setCategory(category);

        Video savedVideo = videoRepository.save(video);
        return convertToVideoResponseDTO(savedVideo);
    }

    @Transactional
    public VideoResponseDTO updateVideo(long id, VideoRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
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
        videoDB.setDuration(requestDTO.getDuration());
        videoDB.setSubtitle(requestDTO.getSubtitle());
        videoDB.setCategory(category);

        Video updatedVideo = videoRepository.save(videoDB);
        return convertToVideoResponseDTO(updatedVideo);
    }

    @Transactional
    public void deleteVideo(long id) throws ResourceNotFoundException {
        Video videoToDelete = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));

        videoRepository.delete(videoToDelete);
    }

    @Transactional(readOnly = true)
    public VideoResponseDTO fetchVideoById(long id) throws ResourceNotFoundException {
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video not found with id: " + id));
        return convertToVideoResponseDTO(video);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<VideoResponseDTO> fetchAllVideos(Pageable pageable, Long categoryId, String title) {
        Specification<Video> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + title.trim().toLowerCase() + "%"));
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
}
