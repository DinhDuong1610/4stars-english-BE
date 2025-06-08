package com.fourstars.FourStars.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.Vocabulary;
import com.fourstars.FourStars.domain.request.vocabulary.VocabularyRequestDTO;
import com.fourstars.FourStars.domain.response.vocabulary.VocabularyResponseDTO;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.repository.UserVocabularyRepository;
import com.fourstars.FourStars.repository.VocabularyRepository;
import com.fourstars.FourStars.util.constant.CategoryType;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class VocabularyService {
    private final VocabularyRepository vocabularyRepository;
    private final CategoryRepository categoryRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final UserRepository userRepository;
    private final SM2Service sm2Service;

    public VocabularyService(VocabularyRepository vocabularyRepository,
            CategoryRepository categoryRepository,
            UserVocabularyRepository userVocabularyRepository,
            UserRepository userRepository,
            SM2Service sm2Service) {
        this.vocabularyRepository = vocabularyRepository;
        this.categoryRepository = categoryRepository;
        this.userVocabularyRepository = userVocabularyRepository;
        this.userRepository = userRepository;
        this.sm2Service = sm2Service;
    }

    private VocabularyResponseDTO convertToVocabularyResponseDTO(Vocabulary vocab) {
        if (vocab == null)
            return null;
        VocabularyResponseDTO dto = new VocabularyResponseDTO();
        dto.setId(vocab.getId());
        dto.setWord(vocab.getWord());
        dto.setDefinitionEn(vocab.getDefinitionEn());
        dto.setMeaningVi(vocab.getMeaningVi());
        dto.setExampleEn(vocab.getExampleEn());
        dto.setExampleVi(vocab.getExampleVi());
        dto.setPartOfSpeech(vocab.getPartOfSpeech());
        dto.setPronunciation(vocab.getPronunciation());
        dto.setImage(vocab.getImage());
        dto.setAudio(vocab.getAudio());

        if (vocab.getCategory() != null) {
            VocabularyResponseDTO.CategoryInfoDTO catInfo = new VocabularyResponseDTO.CategoryInfoDTO();
            catInfo.setId(vocab.getCategory().getId());
            catInfo.setName(vocab.getCategory().getName());
            dto.setCategory(catInfo);
        }

        dto.setCreatedAt(vocab.getCreatedAt());
        dto.setUpdatedAt(vocab.getUpdatedAt());
        dto.setCreatedBy(vocab.getCreatedBy());
        dto.setUpdatedBy(vocab.getUpdatedBy());
        return dto;
    }

    @Transactional
    public VocabularyResponseDTO createVocabulary(VocabularyRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        if (vocabularyRepository.existsByWordAndCategoryId(requestDTO.getWord(), requestDTO.getCategoryId())) {
            throw new DuplicateResourceException("A vocabulary with the same word already exists in this category.");
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + requestDTO.getCategoryId()));

        if (category.getType() != CategoryType.VOCABULARY) {
            throw new BadRequestException("The selected category is not of type 'VOCABULARY'.");
        }

        Vocabulary vocab = new Vocabulary();
        vocab.setWord(requestDTO.getWord());
        vocab.setDefinitionEn(requestDTO.getDefinitionEn());
        vocab.setMeaningVi(requestDTO.getMeaningVi());
        vocab.setExampleEn(requestDTO.getExampleEn());
        vocab.setExampleVi(requestDTO.getExampleVi());
        vocab.setPartOfSpeech(requestDTO.getPartOfSpeech());
        vocab.setPronunciation(requestDTO.getPronunciation());
        vocab.setImage(requestDTO.getImage());
        vocab.setAudio(requestDTO.getAudio());
        vocab.setCategory(category);

        Vocabulary savedVocab = vocabularyRepository.save(vocab);
        return convertToVocabularyResponseDTO(savedVocab);
    }
}
