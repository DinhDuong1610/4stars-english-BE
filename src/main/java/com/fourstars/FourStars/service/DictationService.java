package com.fourstars.FourStars.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.DictationSentence;
import com.fourstars.FourStars.domain.DictationTopic;
import com.fourstars.FourStars.domain.request.dictation.DictationTopicRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.dictation.DictationSentenceResponseDTO;
import com.fourstars.FourStars.domain.response.dictation.DictationTopicResponseDTO;
import com.fourstars.FourStars.domain.response.dictation.NlpAnalysisResponse;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.repository.DictationSentenceRepository;
import com.fourstars.FourStars.repository.DictationTopicRepository;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;

@Service
public class DictationService {
    private static final Logger logger = LoggerFactory.getLogger(DictationService.class);

    private final DictationTopicRepository topicRepository;
    private final CategoryRepository categoryRepository;
    private final DictationSentenceRepository sentenceRepository;
    private final NlpApiService nlpApiService;

    public DictationService(DictationTopicRepository topicRepository,
            CategoryRepository categoryRepository,
            DictationSentenceRepository sentenceRepository,
            NlpApiService nlpApiService) {
        this.topicRepository = topicRepository;
        this.categoryRepository = categoryRepository;
        this.sentenceRepository = sentenceRepository;
        this.nlpApiService = nlpApiService;
    }

    @Transactional
    public DictationTopicResponseDTO createDictationTopic(DictationTopicRequestDTO requestDTO) {
        logger.info("Admin creating new dictation topic with title: '{}'", requestDTO.getTitle());
        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + requestDTO.getCategoryId()));

        DictationTopic topic = new DictationTopic();
        topic.setTitle(requestDTO.getTitle());
        topic.setDescription(requestDTO.getDescription());
        topic.setCategory(category);

        requestDTO.getSentences().forEach(sentenceDTO -> {
            DictationSentence sentence = new DictationSentence();
            sentence.setAudioUrl(sentenceDTO.getAudioUrl());
            sentence.setCorrectText(sentenceDTO.getCorrectText());
            sentence.setOrderIndex(sentenceDTO.getOrderIndex());
            topic.addSentence(sentence);
        });

        DictationTopic savedTopic = topicRepository.save(topic);
        logger.info("Successfully created dictation topic with ID: {}", savedTopic.getId());
        return convertToAdminDTO(savedTopic);
    }

    @Transactional
    public DictationTopicResponseDTO updateDictationTopic(long topicId, DictationTopicRequestDTO requestDTO) {
        logger.info("Admin updating dictation topic with ID: {}", topicId);
        DictationTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Dictation topic not found with id: " + topicId));

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + requestDTO.getCategoryId()));

        topic.setTitle(requestDTO.getTitle());
        topic.setDescription(requestDTO.getDescription());
        topic.setCategory(category);

        topic.getSentences().clear();
        requestDTO.getSentences().forEach(sentenceDTO -> {
            DictationSentence sentence = new DictationSentence();
            sentence.setAudioUrl(sentenceDTO.getAudioUrl());
            sentence.setCorrectText(sentenceDTO.getCorrectText());
            sentence.setOrderIndex(sentenceDTO.getOrderIndex());
            topic.addSentence(sentence);
        });

        DictationTopic updatedTopic = topicRepository.save(topic);
        logger.info("Successfully updated dictation topic with ID: {}", updatedTopic.getId());
        return convertToAdminDTO(updatedTopic);
    }

    @Transactional
    public void deleteDictationTopic(long topicId) {
        logger.info("Admin deleting dictation topic with ID: {}", topicId);
        if (!topicRepository.existsById(topicId)) {
            throw new ResourceNotFoundException("Dictation topic not found with id: " + topicId);
        }
        topicRepository.deleteById(topicId);
        logger.info("Successfully deleted dictation topic with ID: {}", topicId);
    }

    @Transactional(readOnly = true)
    public DictationTopicResponseDTO getDictationTopicById(long topicId) {
        logger.debug("Fetching dictation topic with ID: {}", topicId);
        DictationTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Dictation topic not found with id: " + topicId));
        return convertToAdminDTO(topic);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<DictationTopicResponseDTO> fetchAllTopics(
            Pageable pageable,
            Long categoryId,
            String title,
            LocalDate startCreatedAt,
            LocalDate endCreatedAt) {
        logger.debug("Fetching all dictation topics with filters");

        Specification<DictationTopic> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + title.trim().toLowerCase() + "%"));
            }
            if (startCreatedAt != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"),
                        startCreatedAt.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            if (endCreatedAt != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"),
                        endCreatedAt.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<DictationTopic> topicPage = topicRepository.findAll(spec, pageable);

        List<DictationTopicResponseDTO> dtoList = topicPage.getContent().stream()
                .map(this::convertToAdminDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                topicPage.getNumber() + 1,
                topicPage.getSize(),
                topicPage.getTotalPages(),
                topicPage.getTotalElements());
        return new ResultPaginationDTO<>(meta, dtoList);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<DictationTopicResponseDTO> fetchAllTopicsForUser(
            Pageable pageable,
            Long categoryId,
            String title,
            LocalDate startCreatedAt,
            LocalDate endCreatedAt) {
        logger.debug("Fetching all dictation topics with filters");

        Specification<DictationTopic> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + title.trim().toLowerCase() + "%"));
            }
            if (startCreatedAt != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"),
                        startCreatedAt.atStartOfDay(ZoneId.systemDefault()).toInstant()));
            }
            if (endCreatedAt != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"),
                        endCreatedAt.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<DictationTopic> topicPage = topicRepository.findAll(spec, pageable);

        List<DictationTopicResponseDTO> dtoList = topicPage.getContent().stream()
                .map(topic -> convertToUserResponseDTO(topic, false))
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                topicPage.getNumber() + 1,
                topicPage.getSize(),
                topicPage.getTotalPages(),
                topicPage.getTotalElements());
        return new ResultPaginationDTO<>(meta, dtoList);
    }

    @Transactional(readOnly = true)
    public DictationTopicResponseDTO getDictationTopicForUser(long topicId) {
        logger.debug("User fetching dictation topic with ID: {}", topicId);
        DictationTopic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Dictation topic not found with id: " + topicId));
        return convertToAdminDTO(topic);
    }

    @Transactional(readOnly = true)
    public NlpAnalysisResponse submitAndAnalyze(long sentenceId, String userText) {
        logger.info("User submitting answer for sentence ID: {}", sentenceId);
        DictationSentence sentence = sentenceRepository.findById(sentenceId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Dictation sentence not found with id: " + sentenceId));
        return nlpApiService.getAnalysis(userText, sentence.getCorrectText());
    }

    private DictationTopicResponseDTO convertToUserResponseDTO(DictationTopic topic, boolean includeSentences) {
        DictationTopicResponseDTO topicDto = new DictationTopicResponseDTO();
        topicDto.setId(topic.getId());
        topicDto.setTitle(topic.getTitle());
        topicDto.setDescription(topic.getDescription());
        if (topic.getCategory() != null) {
            DictationTopicResponseDTO.CategoryInfoDTO catInfo = new DictationTopicResponseDTO.CategoryInfoDTO();
            catInfo.setId(topic.getCategory().getId());
            catInfo.setName(topic.getCategory().getName());
            topicDto.setCategory(catInfo);
        }
        topicDto.setCreatedAt(topic.getCreatedAt());
        topicDto.setUpdatedAt(topic.getUpdatedAt());
        topicDto.setCreatedBy(topic.getCreatedBy());
        topicDto.setUpdatedBy(topic.getUpdatedBy());
        if (includeSentences) {
            List<DictationSentenceResponseDTO> sentenceDtos = topic.getSentences().stream()
                    .map(sentence -> {
                        DictationSentenceResponseDTO sentenceDto = new DictationSentenceResponseDTO();
                        sentenceDto.setId(sentence.getId());
                        sentenceDto.setAudioUrl(sentence.getAudioUrl());
                        sentenceDto.setOrderIndex(sentence.getOrderIndex());
                        return sentenceDto;
                    })
                    .collect(Collectors.toList());
            topicDto.setSentences(sentenceDtos);
        }
        return topicDto;
    }

    private DictationTopicResponseDTO convertToAdminDTO(DictationTopic topic) {
        DictationTopicResponseDTO topicDto = new DictationTopicResponseDTO();
        topicDto.setId(topic.getId());
        topicDto.setTitle(topic.getTitle());
        topicDto.setDescription(topic.getDescription());
        if (topic.getCategory() != null) {
            DictationTopicResponseDTO.CategoryInfoDTO catInfo = new DictationTopicResponseDTO.CategoryInfoDTO();
            catInfo.setId(topic.getCategory().getId());
            catInfo.setName(topic.getCategory().getName());
            topicDto.setCategory(catInfo);
        }
        topicDto.setCreatedAt(topic.getCreatedAt());
        topicDto.setUpdatedAt(topic.getUpdatedAt());
        topicDto.setCreatedBy(topic.getCreatedBy());
        topicDto.setUpdatedBy(topic.getUpdatedBy());

        List<DictationSentenceResponseDTO> sentenceDtos = topic.getSentences().stream()
                .map(sentence -> {
                    DictationSentenceResponseDTO sentenceDto = new DictationSentenceResponseDTO();
                    sentenceDto.setId(sentence.getId());
                    sentenceDto.setCorrectText(sentence.getCorrectText());
                    sentenceDto.setAudioUrl(sentence.getAudioUrl());
                    sentenceDto.setOrderIndex(sentence.getOrderIndex());
                    return sentenceDto;
                })
                .collect(Collectors.toList());

        topicDto.setSentences(sentenceDtos);
        return topicDto;
    }
}
