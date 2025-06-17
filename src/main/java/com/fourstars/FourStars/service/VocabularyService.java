package com.fourstars.FourStars.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.UserVocabulary;
import com.fourstars.FourStars.domain.Vocabulary;
import com.fourstars.FourStars.domain.key.UserVocabularyId;
import com.fourstars.FourStars.domain.request.vocabulary.SubmitReviewRequestDTO;
import com.fourstars.FourStars.domain.request.vocabulary.VocabularyRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.vocabulary.UserVocabularyResponseDTO;
import com.fourstars.FourStars.domain.response.vocabulary.VocabularyResponseDTO;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.repository.UserVocabularyRepository;
import com.fourstars.FourStars.repository.VocabularyRepository;
import com.fourstars.FourStars.util.SecurityUtil;
import com.fourstars.FourStars.util.constant.CategoryType;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;

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

    private UserVocabularyResponseDTO convertToUserVocabularyResponseDTO(UserVocabulary userVocab) {
        if (userVocab == null) {
            return null;
        }
        UserVocabularyResponseDTO dto = new UserVocabularyResponseDTO();
        dto.setUserId(userVocab.getId().getUserId());
        dto.setVocabularyId(userVocab.getId().getVocabularyId());
        dto.setLevel(userVocab.getLevel());
        dto.setRepetitions(userVocab.getRepetitions());
        dto.setEaseFactor(userVocab.getEaseFactor());
        dto.setReviewInterval(userVocab.getReviewInterval());
        dto.setLastReviewedAt(userVocab.getLastReviewedAt());
        dto.setNextReviewAt(userVocab.getNextReviewAt());
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

    @Transactional
    public VocabularyResponseDTO updateVocabulary(long id, VocabularyRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        Vocabulary vocabDB = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found with id: " + id));

        if (vocabularyRepository.existsByWordAndCategoryIdAndIdNot(requestDTO.getWord(), requestDTO.getCategoryId(),
                id)) {
            throw new DuplicateResourceException("A vocabulary with the same word already exists in this category.");
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + requestDTO.getCategoryId()));

        if (category.getType() != CategoryType.VOCABULARY) {
            throw new BadRequestException("The selected category is not of type 'VOCABULARY'.");
        }

        vocabDB.setWord(requestDTO.getWord());
        vocabDB.setDefinitionEn(requestDTO.getDefinitionEn());
        vocabDB.setMeaningVi(requestDTO.getMeaningVi());
        vocabDB.setExampleEn(requestDTO.getExampleEn());
        vocabDB.setExampleVi(requestDTO.getExampleVi());
        vocabDB.setPartOfSpeech(requestDTO.getPartOfSpeech());
        vocabDB.setPronunciation(requestDTO.getPronunciation());
        vocabDB.setImage(requestDTO.getImage());
        vocabDB.setAudio(requestDTO.getAudio());
        vocabDB.setCategory(category);

        Vocabulary updatedVocab = vocabularyRepository.save(vocabDB);
        return convertToVocabularyResponseDTO(updatedVocab);
    }

    @Transactional
    public void deleteVocabulary(long id) throws ResourceNotFoundException {
        if (!vocabularyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vocabulary not found with id: " + id);
        }

        userVocabularyRepository.deleteByVocabularyId(id);
        vocabularyRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public VocabularyResponseDTO fetchVocabularyById(long id) throws ResourceNotFoundException {
        Vocabulary vocab = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found with id: " + id));
        return convertToVocabularyResponseDTO(vocab);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<VocabularyResponseDTO> fetchAllVocabularies(Pageable pageable, Long categoryId,
            String word) {
        Specification<Vocabulary> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (word != null && !word.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("word")),
                        "%" + word.trim().toLowerCase() + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Vocabulary> pageVocab = vocabularyRepository.findAll(spec, pageable);
        List<VocabularyResponseDTO> vocabDTOs = pageVocab.getContent().stream()
                .map(this::convertToVocabularyResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pageVocab.getTotalPages(),
                pageVocab.getTotalElements());
        return new ResultPaginationDTO<>(meta, vocabDTOs);
    }

    @Transactional(readOnly = true)
    public List<VocabularyResponseDTO> getVocabulariesForReview(int limit) throws ResourceNotFoundException {
        User user = getCurrentAuthenticatedUser();

        Pageable pageable = PageRequest.of(0, limit);

        List<Vocabulary> vocabularies = vocabularyRepository.findVocabulariesForReview(user.getId(), Instant.now(),
                pageable);

        return vocabularies.stream()
                .map(this::convertToVocabularyResponseDTO)
                .collect(Collectors.toList());
    }

    private User getCurrentAuthenticatedUser() {
        String currentUserEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated."));
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));
    }

    @Transactional
    public UserVocabulary submitVocabularyReview(SubmitReviewRequestDTO reviewDTO)
            throws ResourceNotFoundException {
        User user = getCurrentAuthenticatedUser();
        Long vocabularyId = reviewDTO.getVocabularyId();

        UserVocabularyId userVocabularyId = new UserVocabularyId(user.getId(), vocabularyId);
        Optional<UserVocabulary> optionalUserVocabulary = userVocabularyRepository.findById(userVocabularyId);
        UserVocabulary userVocabulary;

        if (optionalUserVocabulary.isPresent()) {
            userVocabulary = optionalUserVocabulary.get();
        } else {
            Vocabulary vocab = vocabularyRepository.findById(vocabularyId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Vocabulary not found with id: " + vocabularyId));
            userVocabulary = new UserVocabulary(user, vocab);
        }

        SM2Service.SM2InputData sm2Input = new SM2Service.SM2InputData();
        sm2Input.setRepetitions(userVocabulary.getRepetitions());
        sm2Input.setEaseFactor(userVocabulary.getEaseFactor());
        sm2Input.setInterval(userVocabulary.getReviewInterval());
        sm2Input.setQuality(reviewDTO.getQuality());

        SM2Service.SM2Result sm2Result = sm2Service.calculate(sm2Input);

        userVocabulary.setLevel(sm2Result.getNewLevel());
        userVocabulary.setRepetitions(sm2Result.getNewRepetitions());
        userVocabulary.setEaseFactor(sm2Result.getNewEaseFactor());
        userVocabulary.setReviewInterval(sm2Result.getNewInterval());
        userVocabulary.setNextReviewAt(sm2Result.getNextReviewDate());
        userVocabulary.setLastReviewedAt(Instant.now());

        userVocabulary = userVocabularyRepository.save(userVocabulary);

        return userVocabulary;
    }

    @Transactional
    public UserVocabularyResponseDTO addVocabularyToNotebook(Long vocabularyId) {
        User user = getCurrentAuthenticatedUser();

        UserVocabularyId userVocabularyId = new UserVocabularyId(user.getId(), vocabularyId);

        Optional<UserVocabulary> existingEntry = userVocabularyRepository.findById(userVocabularyId);
        if (existingEntry.isPresent()) {
            return convertToUserVocabularyResponseDTO(existingEntry.get());
        }

        Vocabulary vocab = vocabularyRepository.findById(vocabularyId)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found with id: " + vocabularyId));

        UserVocabulary newUserVocabulary = new UserVocabulary(user, vocab);

        UserVocabulary savedEntry = userVocabularyRepository.save(newUserVocabulary);

        return convertToUserVocabularyResponseDTO(savedEntry);
    }
}
