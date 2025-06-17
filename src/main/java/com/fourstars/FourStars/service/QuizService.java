package com.fourstars.FourStars.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.Question;
import com.fourstars.FourStars.domain.QuestionChoice;
import com.fourstars.FourStars.domain.Quiz;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.UserAnswer;
import com.fourstars.FourStars.domain.UserQuizAttempt;
import com.fourstars.FourStars.domain.Vocabulary;
import com.fourstars.FourStars.domain.request.quiz.QuestionChoiceDTO;
import com.fourstars.FourStars.domain.request.quiz.QuestionDTO;
import com.fourstars.FourStars.domain.request.quiz.QuizDTO;
import com.fourstars.FourStars.domain.request.quiz.SubmitQuizRequestDTO;
import com.fourstars.FourStars.domain.request.quiz.UserAnswerRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.quiz.QuestionAnswerDetailDTO;
import com.fourstars.FourStars.domain.response.quiz.QuestionChoiceForUserDTO;
import com.fourstars.FourStars.domain.response.quiz.QuestionForUserDTO;
import com.fourstars.FourStars.domain.response.quiz.QuizAttemptResponseDTO;
import com.fourstars.FourStars.domain.response.quiz.QuizForUserAttemptDTO;
import com.fourstars.FourStars.domain.response.quiz.UserAnswerResponseDTO;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.repository.QuestionRepository;
import com.fourstars.FourStars.repository.QuizRepository;
import com.fourstars.FourStars.repository.UserQuizAttemptRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.repository.VocabularyRepository;
import com.fourstars.FourStars.util.SecurityUtil;
import com.fourstars.FourStars.util.constant.QuestionType;
import com.fourstars.FourStars.util.constant.QuizStatus;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;

@Service
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final VocabularyRepository vocabularyRepository;
    private final VocabularyService vocabularyService;

    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository,
            UserQuizAttemptRepository userQuizAttemptRepository, UserRepository userRepository,
            CategoryRepository categoryRepository, VocabularyRepository vocabularyRepository,
            VocabularyService vocabularyService) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.userQuizAttemptRepository = userQuizAttemptRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.vocabularyService = vocabularyService;
    }

    private User getCurrentAuthenticatedUser() {
        return SecurityUtil.getCurrentUserLogin()
                .flatMap(userRepository::findByEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not authenticated. Please login."));
    }

    @Transactional
    public QuizDTO createQuiz(QuizDTO quizDto) {
        Category category = categoryRepository.findById(quizDto.getCategoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + quizDto.getCategoryId()));

        Quiz quiz = new Quiz();
        quiz.setTitle(quizDto.getTitle());
        quiz.setDescription(quizDto.getDescription());
        quiz.setCategory(category);

        if (quizDto.getQuestions() != null) {
            Set<Question> questions = quizDto.getQuestions().stream()
                    .map(qDto -> convertQuestionDtoToEntity(qDto, quiz))
                    .collect(Collectors.toSet());
            quiz.setQuestions(questions);
        }

        Quiz savedQuiz = quizRepository.save(quiz);
        return convertToQuizDTO(savedQuiz);
    }

    @Transactional
    public QuizDTO updateQuiz(long quizId, QuizDTO quizDto) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        Category category = categoryRepository.findById(quizDto.getCategoryId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Category not found with id: " + quizDto.getCategoryId()));

        quiz.setTitle(quizDto.getTitle());
        quiz.setDescription(quizDto.getDescription());
        quiz.setCategory(category);

        quiz.getQuestions().clear();

        if (quizDto.getQuestions() != null) {
            Set<Question> newQuestions = quizDto.getQuestions().stream()
                    .map(qDto -> convertQuestionDtoToEntity(qDto, quiz))
                    .collect(Collectors.toSet());

            quiz.getQuestions().addAll(newQuestions);
        }

        Quiz updatedQuiz = quizRepository.save(quiz);

        return convertToQuizDTO(updatedQuiz);
    }

    @Transactional
    public void deleteQuiz(long quizId) {
        if (!quizRepository.existsById(quizId)) {
            throw new ResourceNotFoundException("Quiz not found with id: " + quizId);
        }

        quizRepository.deleteById(quizId);
    }

    @Transactional(readOnly = true)
    public QuizDTO getQuizForAdmin(long quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));
        return convertToQuizDTO(quiz);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<QuizDTO> getAllQuizzesForAdmin(Pageable pageable, Long categoryId) {
        Specification<Quiz> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Quiz> quizPage = quizRepository.findAll(spec, pageable);

        List<QuizDTO> quizDTOs = quizPage.getContent().stream()
                .map(this::convertToQuizDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                quizPage.getNumber() + 1,
                quizPage.getSize(),
                quizPage.getTotalPages(),
                quizPage.getTotalElements());

        return new ResultPaginationDTO<>(meta, quizDTOs);
    }

    @Transactional
    public QuizForUserAttemptDTO startQuiz(long quizId) {
        User currentUser = getCurrentAuthenticatedUser();
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found with id: " + quizId));

        UserQuizAttempt attempt = new UserQuizAttempt();
        attempt.setUser(currentUser);
        attempt.setQuiz(quiz);
        attempt.setStatus(QuizStatus.IN_PROGRESS);
        attempt.setStartedAt(Instant.now());

        UserQuizAttempt savedAttempt = userQuizAttemptRepository.save(attempt);

        List<QuestionForUserDTO> questionForUserDTOs = quiz.getQuestions().stream()
                .map(this::convertToQuestionForUserDTO)
                .collect(Collectors.toList());

        return new QuizForUserAttemptDTO(savedAttempt.getId(), quiz.getTitle(), questionForUserDTOs);
    }

    @Transactional
    public QuizAttemptResponseDTO submitQuiz(SubmitQuizRequestDTO submitDTO) {
        User currentUser = getCurrentAuthenticatedUser();
        UserQuizAttempt attempt = userQuizAttemptRepository
                .findByIdAndUserId(submitDTO.getUserQuizAttemptId(), currentUser.getId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Quiz attempt not found or you don't have permission."));

        if (attempt.getStatus() == QuizStatus.COMPLETED) {
            throw new BadRequestException("This quiz has already been completed.");
        }

        attempt.getUserAnswers().clear();
        int totalScore = 0;

        for (UserAnswerRequestDTO ansReq : submitDTO.getAnswers()) {
            Question question = questionRepository.findById(ansReq.getQuestionId())
                    .orElseThrow(() -> new ResourceNotFoundException("Question not found: " + ansReq.getQuestionId()));

            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setUserQuizAttempt(attempt);
            userAnswer.setQuestion(question);
            userAnswer.setUserAnswerText(ansReq.getUserAnswerText());

            boolean isCorrect = false;
            if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE_TEXT
                    || question.getQuestionType() == QuestionType.MULTIPLE_CHOICE_IMAGE) {
                QuestionChoice correctChoice = question.getChoices().stream()
                        .filter(c -> c.isCorrect())
                        .findFirst()
                        .orElse(null);

                if (correctChoice != null && ansReq.getSelectedChoiceId() != null
                        && correctChoice.getId() == ansReq.getSelectedChoiceId()) {
                    isCorrect = true;
                }
                if (ansReq.getSelectedChoiceId() != null) {
                    QuestionChoice choiceRef = new QuestionChoice();
                    choiceRef.setId(ansReq.getSelectedChoiceId());
                    userAnswer.setSelectedChoice(choiceRef);
                }
            } else {
                if (question.getCorrectSentence() != null
                        && question.getCorrectSentence().equalsIgnoreCase(ansReq.getUserAnswerText())) {
                    isCorrect = true;
                }
            }

            userAnswer.setCorrect(isCorrect);
            if (isCorrect) {
                userAnswer.setPointsAwarded(question.getPoints());
                totalScore += question.getPoints();
            }

            attempt.getUserAnswers().add(userAnswer);
        }

        attempt.setScore(totalScore);
        attempt.setStatus(QuizStatus.COMPLETED);
        attempt.setCompletedAt(Instant.now());

        UserQuizAttempt savedAttempt = userQuizAttemptRepository.save(attempt);

        Set<Long> relatedVocabularyIds = new HashSet<>();
        for (UserAnswer answer : savedAttempt.getUserAnswers()) {
            if (answer.getQuestion().getRelatedVocabulary() != null) {
                relatedVocabularyIds.add(answer.getQuestion().getRelatedVocabulary().getId());
            }
        }

        relatedVocabularyIds.forEach(vocabId -> {
            try {
                vocabularyService.addVocabularyToNotebook(vocabId);
            } catch (Exception e) {
                System.err.println("Could not add vocabulary " + vocabId + " to notebook. Error: " + e.getMessage());
            }
        });

        return convertToQuizAttemptResponseDTO(savedAttempt);
    }

    @Transactional(readOnly = true)
    public QuizAttemptResponseDTO getQuizResult(long attemptId) {
        User currentUser = getCurrentAuthenticatedUser();
        UserQuizAttempt attempt = userQuizAttemptRepository.findByIdAndUserId(attemptId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz attempt result not found or you don't have permission."));

        return convertToQuizAttemptResponseDTO(attempt);
    }

    private QuizDTO convertToQuizDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setId(quiz.getId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        if (quiz.getCategory() != null) {
            dto.setCategoryId(quiz.getCategory().getId());
            dto.setCategoryName(quiz.getCategory().getName());
        }
        dto.setCreatedAt(quiz.getCreatedAt());
        dto.setUpdatedAt(quiz.getUpdatedAt());
        if (quiz.getQuestions() != null) {
            dto.setQuestions(quiz.getQuestions().stream().map(this::convertToQuestionDTO).collect(Collectors.toSet()));
        }
        return dto;
    }

    private QuestionDTO convertToQuestionDTO(Question q) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(q.getId());
        dto.setQuestionType(q.getQuestionType());
        dto.setPrompt(q.getPrompt());
        dto.setImageUrl(q.getImageUrl());
        dto.setAudioUrl(q.getAudioUrl());
        dto.setTextToFill(q.getTextToFill());
        dto.setCorrectSentence(q.getCorrectSentence());
        dto.setPoints(q.getPoints());
        dto.setQuestionOrder(q.getQuestionOrder());

        if (q.getRelatedVocabulary() != null) {
            dto.setRelatedVocabularyId(q.getRelatedVocabulary().getId());
        }

        if (q.getChoices() != null) {
            dto.setChoices(q.getChoices().stream().map(this::convertToQuestionChoiceDTO).collect(Collectors.toSet()));
        }
        return dto;
    }

    private QuestionChoiceDTO convertToQuestionChoiceDTO(QuestionChoice c) {
        QuestionChoiceDTO dto = new QuestionChoiceDTO();
        dto.setId(c.getId());
        dto.setContent(c.getContent());
        dto.setImageUrl(c.getImageUrl());
        dto.setIsCorrect(c.isCorrect());
        return dto;
    }

    private Question convertQuestionDtoToEntity(QuestionDTO qDto, Quiz parentQuiz) {
        Question q = new Question();
        q.setId(qDto.getId());
        q.setQuiz(parentQuiz);
        q.setQuestionType(qDto.getQuestionType());
        q.setPrompt(qDto.getPrompt());
        q.setImageUrl(qDto.getImageUrl());
        q.setAudioUrl(qDto.getAudioUrl());
        q.setTextToFill(qDto.getTextToFill());
        q.setCorrectSentence(qDto.getCorrectSentence());
        q.setPoints(qDto.getPoints());
        q.setQuestionOrder(qDto.getQuestionOrder());

        if (qDto.getRelatedVocabularyId() != null) {
            Vocabulary vocabulary = vocabularyRepository.findById(qDto.getRelatedVocabularyId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Vocabulary not found with id: " + qDto.getRelatedVocabularyId()));
            q.setRelatedVocabulary(vocabulary);
        }
        if (qDto.getChoices() != null) {
            q.setChoices(qDto.getChoices().stream().map(cDto -> convertChoiceDtoToEntity(cDto, q))
                    .collect(Collectors.toSet()));
        }
        return q;
    }

    private QuestionChoice convertChoiceDtoToEntity(QuestionChoiceDTO cDto, Question parentQuestion) {
        QuestionChoice c = new QuestionChoice();
        c.setId(cDto.getId());
        c.setQuestion(parentQuestion);
        c.setContent(cDto.getContent());
        c.setImageUrl(cDto.getImageUrl());
        c.setCorrect(cDto.getIsCorrect());
        return c;
    }

    private QuestionForUserDTO convertToQuestionForUserDTO(Question q) {
        QuestionForUserDTO dto = new QuestionForUserDTO();
        dto.setId(q.getId());
        dto.setQuestionType(q.getQuestionType());
        dto.setPrompt(q.getPrompt());
        dto.setImageUrl(q.getImageUrl());
        dto.setAudioUrl(q.getAudioUrl());
        dto.setTextToFill(q.getTextToFill());
        dto.setPoints(q.getPoints());
        if (q.getChoices() != null) {
            dto.setChoices(q.getChoices().stream()
                    .map(c -> new QuestionChoiceForUserDTO(c.getId(), c.getContent(), c.getImageUrl()))
                    .collect(Collectors.toSet()));
        }
        return dto;
    }

    private QuizAttemptResponseDTO convertToQuizAttemptResponseDTO(UserQuizAttempt attempt) {
        QuizAttemptResponseDTO dto = new QuizAttemptResponseDTO();
        dto.setId(attempt.getId());
        dto.setQuizId(attempt.getQuiz().getId());
        dto.setQuizTitle(attempt.getQuiz().getTitle());
        dto.setStatus(attempt.getStatus());
        dto.setScore(attempt.getScore());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setCompletedAt(attempt.getCompletedAt());

        int totalPoints = attempt.getQuiz().getQuestions().stream().mapToInt(Question::getPoints).sum();
        dto.setTotalPoints(totalPoints);

        List<UserAnswerResponseDTO> answerDTOs = attempt.getUserAnswers().stream().map(ans -> {
            UserAnswerResponseDTO ansDto = new UserAnswerResponseDTO();
            ansDto.setQuestionId(ans.getQuestion().getId());
            ansDto.setQuestionPrompt(ans.getQuestion().getPrompt());
            ansDto.setUserAnswerText(ans.getUserAnswerText());
            if (ans.getSelectedChoice() != null) {
                ansDto.setSelectedChoiceId(ans.getSelectedChoice().getId());
            }
            ansDto.setCorrect(ans.isCorrect());
            ansDto.setPointsAwarded(ans.getPointsAwarded());

            QuestionAnswerDetailDTO correctAnswerDetail = new QuestionAnswerDetailDTO();
            correctAnswerDetail.setCorrectText(ans.getQuestion().getCorrectSentence());
            QuestionChoiceDTO correctChoiceDto = ans.getQuestion().getChoices().stream()
                    .filter(c -> c.isCorrect())
                    .findFirst()
                    .map(this::convertToQuestionChoiceDTO)
                    .orElse(null);

            correctAnswerDetail.setCorrectChoice(correctChoiceDto);
            ansDto.setCorrectAnswer(correctAnswerDetail);

            return ansDto;
        }).collect(Collectors.toList());

        dto.setUserAnswers(answerDTOs);

        return dto;
    }

}
