package com.fourstars.FourStars.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.DictationSentence;
import com.fourstars.FourStars.domain.DictationTopic;
import com.fourstars.FourStars.domain.request.dictation.DictationTopicRequestDTO;
import com.fourstars.FourStars.domain.response.dictation.DictationSentenceResponseDTO;
import com.fourstars.FourStars.domain.response.dictation.DictationTopicResponseDTO;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.repository.DictationTopicRepository;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class DictationService {
    private static final Logger logger = LoggerFactory.getLogger(DictationService.class);

    private final DictationTopicRepository topicRepository;
    private final CategoryRepository categoryRepository;

    public DictationService(DictationTopicRepository topicRepository,
            CategoryRepository categoryRepository) {
        this.topicRepository = topicRepository;
        this.categoryRepository = categoryRepository;
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

    private DictationTopicResponseDTO convertToUserResponseDTO(DictationTopic topic) {
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
                    sentenceDto.setAudioUrl(sentence.getAudioUrl());
                    sentenceDto.setOrderIndex(sentence.getOrderIndex());
                    return sentenceDto;
                })
                .collect(Collectors.toList());

        topicDto.setSentences(sentenceDtos);
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
