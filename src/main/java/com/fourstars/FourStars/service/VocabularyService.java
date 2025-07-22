package com.fourstars.FourStars.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.config.RabbitMQConfig;
import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.UserVocabulary;
import com.fourstars.FourStars.domain.Vocabulary;
import com.fourstars.FourStars.domain.key.UserVocabularyId;
import com.fourstars.FourStars.domain.request.quiz.QuizDTO;
import com.fourstars.FourStars.domain.request.vocabulary.SubmitReviewRequestDTO;
import com.fourstars.FourStars.domain.request.vocabulary.VocabularyRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.vocabulary.UserVocabularyResponseDTO;
import com.fourstars.FourStars.domain.response.vocabulary.VocabularyResponseDTO;
import com.fourstars.FourStars.messaging.dto.vocabulary.NewVocabularyMessage;
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
    private static final Logger logger = LoggerFactory.getLogger(VocabularyService.class);

    private final VocabularyRepository vocabularyRepository;
    private final CategoryRepository categoryRepository;
    private final UserVocabularyRepository userVocabularyRepository;
    private final UserRepository userRepository;
    private final SM2Service sm2Service;
    private final RabbitTemplate rabbitTemplate;
    private final QuizGenerationService quizGenerationService;
    private final QuizService quizService;

    public VocabularyService(VocabularyRepository vocabularyRepository,
            CategoryRepository categoryRepository,
            UserVocabularyRepository userVocabularyRepository,
            UserRepository userRepository,
            SM2Service sm2Service, RabbitTemplate rabbitTemplate,
            QuizGenerationService quizGenerationService,
            QuizService quizService) {
        this.vocabularyRepository = vocabularyRepository;
        this.categoryRepository = categoryRepository;
        this.userVocabularyRepository = userVocabularyRepository;
        this.userRepository = userRepository;
        this.sm2Service = sm2Service;
        this.rabbitTemplate = rabbitTemplate;
        this.quizGenerationService = quizGenerationService;
        this.quizService = quizService;
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
        logger.info("Admin creating new vocabulary '{}' in category ID {}", requestDTO.getWord(),
                requestDTO.getCategoryId());

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
        logger.info("Successfully created new vocabulary with ID: {}", savedVocab.getId());

        try {
            NewVocabularyMessage message = new NewVocabularyMessage(savedVocab.getId());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.VOCABULARY_EVENT_EXCHANGE,
                    RabbitMQConfig.VOCABULARY_CREATED_ROUTING_KEY,
                    message);
            logger.info("Published new vocabulary event for ID: {}", savedVocab.getId());
        } catch (Exception e) {
            logger.error("Failed to publish new vocabulary event for ID: {}", savedVocab.getId(), e);
        }

        return convertToVocabularyResponseDTO(savedVocab);
    }

    @Transactional
    public VocabularyResponseDTO updateVocabulary(long id, VocabularyRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        logger.info("Admin updating vocabulary with ID: {}", id);

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
        logger.info("Successfully updated vocabulary with ID: {}", updatedVocab.getId());

        return convertToVocabularyResponseDTO(updatedVocab);
    }

    @Transactional
    public void deleteVocabulary(long id) throws ResourceNotFoundException {
        logger.info("Admin deleting vocabulary with ID: {}", id);

        if (!vocabularyRepository.existsById(id)) {
            throw new ResourceNotFoundException("Vocabulary not found with id: " + id);
        }
        logger.warn("Deleting all user progress associated with vocabulary ID: {}", id);

        userVocabularyRepository.deleteByVocabularyId(id);
        vocabularyRepository.deleteById(id);
        logger.info("Successfully deleted vocabulary with ID: {}", id);

    }

    @Transactional(readOnly = true)
    public VocabularyResponseDTO fetchVocabularyById(long id) throws ResourceNotFoundException {
        logger.debug("Fetching vocabulary by ID: {}", id);

        Vocabulary vocab = vocabularyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vocabulary not found with id: " + id));
        return convertToVocabularyResponseDTO(vocab);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<VocabularyResponseDTO> fetchAllVocabularies(Pageable pageable, Long categoryId,
            String word) {
        logger.debug("Fetching all vocabularies with categoryId: {} and word: {}", categoryId, word);

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
        logger.info("User '{}' requesting {} vocabularies for review.", user.getEmail(), limit);

        Pageable pageable = PageRequest.of(0, limit);

        List<Vocabulary> vocabularies = vocabularyRepository.findVocabulariesForReview(user.getId(), Instant.now(),
                pageable);
        logger.info("Found {} vocabularies for user '{}' to review.", vocabularies.size(), user.getEmail());

        return vocabularies.stream()
                .map(this::convertToVocabularyResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public QuizDTO createReviewQuiz() {
        User user = getCurrentAuthenticatedUser();
        logger.info("User '{}' requested a review quiz.", user.getEmail());

        List<Vocabulary> vocabulariesToReview = vocabularyRepository.findVocabulariesForReview(user.getId(),
                Instant.now(), PageRequest.of(0, 1000));

        if (vocabulariesToReview.isEmpty()) {
            logger.info("User '{}' has no vocabularies to review at the moment.", user.getEmail());
            return null;
        }

        logger.info("Found {} vocabularies to generate a review quiz for user '{}'", vocabulariesToReview.size(),
                user.getEmail());

        String title = "Personal Review for " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String description = "This quiz is automatically generated based on words you need to review.";

        QuizDTO generatedQuizData = quizGenerationService.generateQuizFromVocabularyList(vocabulariesToReview, title,
                description, null, 1);

        QuizDTO createdQuiz = quizService.createQuiz(generatedQuizData);

        return createdQuiz;
    }

    private User getCurrentAuthenticatedUser() {
        String currentUserEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated."));
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserEmail));
    }

    @Transactional
    public UserVocabulary submitVocabularyReview(SubmitReviewRequestDTO reviewDTO) {
        User user = getCurrentAuthenticatedUser();
        logger.info("User '{}' submitting review for vocabulary ID: {} with quality: {}",
                user.getEmail(), reviewDTO.getVocabularyId(), reviewDTO.getQuality());
        return processReview(user, reviewDTO);
    }

    @Transactional
    public UserVocabulary submitVocabularyReview(SubmitReviewRequestDTO reviewDTO, User user) {
        logger.info("System submitting auto-review for vocabulary ID: {} with quality: {} on behalf of user '{}'",
                reviewDTO.getVocabularyId(), reviewDTO.getQuality(), user.getEmail());
        return processReview(user, reviewDTO);
    }

    private UserVocabulary processReview(User user, SubmitReviewRequestDTO reviewDTO) {
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
        logger.info("Successfully updated learning progress for user '{}' and vocabulary ID {}", user.getEmail(),
                reviewDTO.getVocabularyId());

        return userVocabulary;
    }

    @Transactional
    public UserVocabularyResponseDTO addVocabularyToNotebook(Long vocabularyId) {

        User user = getCurrentAuthenticatedUser();
        logger.info("User '{}' requesting to add vocabulary ID {} to notebook.", user.getEmail(), vocabularyId);
        return this.createOrGetNotebookEntry(user, vocabularyId);
    }

    @Transactional
    public UserVocabularyResponseDTO addVocabularyToNotebook(Long userId, Long vocabularyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return this.createOrGetNotebookEntry(user, vocabularyId);
    }

    private UserVocabularyResponseDTO createOrGetNotebookEntry(User user, Long vocabularyId) {
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

    @Transactional
    public void removeVocabularyFromNotebook(Long vocabularyId) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User '{}' requesting to remove vocabulary ID {} from notebook.", currentUser.getEmail(),
                vocabularyId);

        UserVocabularyId userVocabularyId = new UserVocabularyId(currentUser.getId(), vocabularyId);

        if (!userVocabularyRepository.existsById(userVocabularyId)) {
            throw new ResourceNotFoundException(
                    "Vocabulary with id " + vocabularyId + " is not in the user's notebook.");
        }

        userVocabularyRepository.deleteById(userVocabularyId);
        logger.info("Successfully removed vocabulary ID {} from notebook for user '{}'.", vocabularyId,
                currentUser.getEmail());
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<VocabularyResponseDTO> fetchRecentlyAddedToNotebook(Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User '{}' fetching recently added vocabularies to notebook.", currentUser.getEmail());

        Page<UserVocabulary> userVocabPage = userVocabularyRepository.findByUserOrderByCreatedAtDesc(currentUser,
                pageable);

        List<VocabularyResponseDTO> vocabDTOs = userVocabPage.getContent().stream()
                .map(userVocab -> convertToVocabularyResponseDTO(userVocab.getVocabulary()))
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                userVocabPage.getNumber() + 1,
                userVocabPage.getSize(),
                userVocabPage.getTotalPages(),
                userVocabPage.getTotalElements());

        logger.debug("Found {} recently added vocabularies for user '{}'", vocabDTOs.size(), currentUser.getEmail());
        return new ResultPaginationDTO<>(meta, vocabDTOs);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<VocabularyResponseDTO> fetchNotebookByLevel(Integer level, Pageable pageable) {
        if (level < 1 || level > 5) {
            throw new BadRequestException("Level must be between 1 and 5.");
        }

        User currentUser = getCurrentAuthenticatedUser();
        logger.info("User '{}' fetching notebook vocabularies with level: {}", currentUser.getEmail(), level);

        Page<UserVocabulary> userVocabPage = userVocabularyRepository
                .findByUserAndLevelOrderByCreatedAtDesc(currentUser, level, pageable);

        List<VocabularyResponseDTO> vocabDTOs = userVocabPage.getContent().stream()
                .map(userVocab -> convertToVocabularyResponseDTO(userVocab.getVocabulary()))
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                userVocabPage.getNumber() + 1,
                userVocabPage.getSize(),
                userVocabPage.getTotalPages(),
                userVocabPage.getTotalElements());

        logger.debug("Found {} vocabularies at level {} for user '{}'", vocabDTOs.size(), level,
                currentUser.getEmail());
        return new ResultPaginationDTO<>(meta, vocabDTOs);
    }

    @Transactional
    public List<VocabularyResponseDTO> createBulkVocabularies(List<VocabularyRequestDTO> requestDTOs)
            throws DuplicateResourceException {
        logger.info("Admin request to create {} vocabularies in bulk", requestDTOs.size());

        if (requestDTOs == null || requestDTOs.isEmpty()) {
            throw new BadRequestException("Vocabulary list cannot be empty.");
        }

        List<Long> categoryIds = requestDTOs.stream()
                .map(VocabularyRequestDTO::getCategoryId)
                .collect(Collectors.toList());
        Map<Long, Category> categoryMap = categoryRepository.findAllById(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, category -> category));

        List<Vocabulary> vocabulariesToSave = new ArrayList<>();

        for (VocabularyRequestDTO dto : requestDTOs) {
            if (vocabularyRepository.existsByWordAndCategoryId(dto.getWord(), dto.getCategoryId())) {
                throw new DuplicateResourceException(
                        "Vocabulary with word '" + dto.getWord() + "' already exists in category ID "
                                + dto.getCategoryId());
            }

            Category category = categoryMap.get(dto.getCategoryId());
            if (category == null) {
                throw new ResourceNotFoundException("Category not found with id: " + dto.getCategoryId());
            }
            if (category.getType() != CategoryType.VOCABULARY) {
                throw new BadRequestException(
                        "The selected category " + category.getName() + " is not of type 'VOCABULARY'.");
            }

            Vocabulary vocab = new Vocabulary();
            vocab.setWord(dto.getWord());
            vocab.setDefinitionEn(dto.getDefinitionEn());
            vocab.setMeaningVi(dto.getMeaningVi());
            vocab.setExampleEn(dto.getExampleEn());
            vocab.setExampleVi(dto.getExampleVi());
            vocab.setPartOfSpeech(dto.getPartOfSpeech());
            vocab.setPronunciation(dto.getPronunciation());
            vocab.setImage(dto.getImage());
            vocab.setAudio(dto.getAudio());
            vocab.setCategory(category);

            vocabulariesToSave.add(vocab);
        }

        List<Vocabulary> savedVocabularies = vocabularyRepository.saveAll(vocabulariesToSave);
        logger.info("Successfully created {} new vocabularies in bulk.", savedVocabularies.size());

        logger.info("Publishing events for bulk-created vocabularies...");
        for (Vocabulary savedVocab : savedVocabularies) {
            try {
                NewVocabularyMessage message = new NewVocabularyMessage(savedVocab.getId());
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.VOCABULARY_EVENT_EXCHANGE,
                        RabbitMQConfig.VOCABULARY_CREATED_ROUTING_KEY,
                        message);
            } catch (Exception e) {
                logger.error("Failed to publish new vocabulary event for bulk-created ID: {}", savedVocab.getId(), e);
            }
        }
        logger.info("Finished publishing {} events.", savedVocabularies.size());

        return savedVocabularies.stream()
                .map(this::convertToVocabularyResponseDTO)
                .collect(Collectors.toList());
    }

}
