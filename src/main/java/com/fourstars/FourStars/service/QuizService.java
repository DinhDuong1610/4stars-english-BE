package com.fourstars.FourStars.service;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.Question;
import com.fourstars.FourStars.domain.QuestionChoice;
import com.fourstars.FourStars.domain.Quiz;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.Vocabulary;
import com.fourstars.FourStars.domain.request.quiz.QuestionChoiceDTO;
import com.fourstars.FourStars.domain.request.quiz.QuestionDTO;
import com.fourstars.FourStars.domain.request.quiz.QuizDTO;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.repository.QuestionRepository;
import com.fourstars.FourStars.repository.QuizRepository;
import com.fourstars.FourStars.repository.UserQuizAttemptRepository;
import com.fourstars.FourStars.repository.UserRepository;
import com.fourstars.FourStars.repository.VocabularyRepository;
import com.fourstars.FourStars.util.SecurityUtil;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final UserQuizAttemptRepository userQuizAttemptRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final VocabularyRepository vocabularyRepository;

    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository,
            UserQuizAttemptRepository userQuizAttemptRepository, UserRepository userRepository,
            CategoryRepository categoryRepository, VocabularyRepository vocabularyRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.userQuizAttemptRepository = userQuizAttemptRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.vocabularyRepository = vocabularyRepository;
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
        dto.setCorrect(c.isCorrect());
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
        c.setCorrect(cDto.isCorrect());
        return c;
    }
}
