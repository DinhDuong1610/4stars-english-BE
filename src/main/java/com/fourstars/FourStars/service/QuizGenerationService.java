package com.fourstars.FourStars.service;

import com.fourstars.FourStars.config.RabbitMQConfig;
import com.fourstars.FourStars.domain.Vocabulary;
import com.fourstars.FourStars.domain.request.quiz.QuestionChoiceDTO;
import com.fourstars.FourStars.domain.request.quiz.QuestionDTO;
import com.fourstars.FourStars.domain.request.quiz.QuizDTO;
import com.fourstars.FourStars.messaging.dto.vocabulary.NewVocabularyMessage;
import com.fourstars.FourStars.repository.VocabularyRepository;
import com.fourstars.FourStars.util.constant.QuestionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class QuizGenerationService {
    private static final Logger logger = LoggerFactory.getLogger(QuizGenerationService.class);

    private final VocabularyRepository vocabularyRepository;
    private final QuizService quizService;

    public QuizGenerationService(VocabularyRepository vocabularyRepository, QuizService quizService) {
        this.vocabularyRepository = vocabularyRepository;
        this.quizService = quizService;
    }

    @RabbitListener(queues = RabbitMQConfig.VOCABULARY_CREATED_QUEUE)
    @Transactional
    public void generateQuizForVocabulary(NewVocabularyMessage message) {
        long newVocabId = message.getNewVocabularyId();
        logger.info("Received event to generate quiz for new vocabulary ID: {}", newVocabId);

        try {
            Vocabulary newVocab = vocabularyRepository.findById(newVocabId)
                    .orElseThrow(
                            () -> new RuntimeException("Vocabulary not found for quiz generation. ID: " + newVocabId));

            QuizDTO quizDTO = new QuizDTO();
            quizDTO.setTitle("Bài tập cho từ: '" + newVocab.getWord() + "'");
            quizDTO.setDescription("Bài tập tự động tạo để ôn luyện từ vựng mới.");
            quizDTO.setCategoryId(newVocab.getCategory().getId());

            Set<QuestionDTO> questions = new HashSet<>();

            generateFillInTheBlank(newVocab).ifPresent(questions::add);
            generateMultipleChoiceText(newVocab).ifPresent(questions::add);
            generateMultipleChoiceImage(newVocab).ifPresent(questions::add);
            generateListeningComprehension(newVocab).ifPresent(questions::add);

            if (questions.isEmpty()) {
                logger.warn("Could not generate any questions for vocabulary ID: {}", newVocabId);
                return;
            }

            for (QuestionDTO q : questions) {
                q.setRelatedVocabularyId(newVocab.getId());
            }

            quizDTO.setQuestions(questions);
            quizService.createQuiz(quizDTO);

            logger.info("Successfully generated and saved a new quiz for vocabulary ID: {}", newVocabId);
        } catch (Exception e) {
            logger.error("Failed to generate quiz for vocabulary ID: {}", newVocabId, e);
        }
    }

    private Optional<QuestionDTO> generateFillInTheBlank(Vocabulary vocab) {
        if (vocab.getExampleEn() == null || vocab.getExampleEn().isEmpty()) {
            return Optional.empty();
        }

        String prompt = vocab.getExampleEn().replaceAll("(?i)" + Pattern.quote(vocab.getWord()), "______");

        QuestionDTO q = new QuestionDTO();
        q.setPrompt(prompt);
        q.setCorrectSentence(vocab.getWord());
        q.setQuestionType(QuestionType.FILL_IN_BLANK);
        q.setPoints(10);
        return Optional.of(q);
    }

    private Optional<QuestionDTO> generateMultipleChoiceText(Vocabulary vocab) {
        if (vocab.getExampleEn() == null || vocab.getExampleEn().isEmpty()) {
            return Optional.empty();
        }
        List<Vocabulary> distractors = vocabularyRepository.findRandomWords(vocab.getId(), vocab.getPartOfSpeech());
        if (distractors.size() < 3) {
            return Optional.empty();
        }

        String prompt = "Fill in the blanks: "
                + vocab.getExampleEn().replaceAll("(?i)" + Pattern.quote(vocab.getWord()), "______");

        QuestionDTO q = new QuestionDTO();
        q.setPrompt(prompt);
        q.setQuestionType(QuestionType.MULTIPLE_CHOICE_TEXT);
        q.setPoints(10);

        Set<QuestionChoiceDTO> choices = new HashSet<>();
        choices.add(new QuestionChoiceDTO(0, vocab.getWord(), null, true));
        distractors.forEach(d -> choices.add(new QuestionChoiceDTO(0, d.getWord(), null, false)));

        q.setChoices(choices);
        return Optional.of(q);
    }

    private Optional<QuestionDTO> generateMultipleChoiceImage(Vocabulary vocab) {
        if (vocab.getImage() == null || vocab.getImage().isEmpty() || vocab.getDefinitionEn() == null
                || vocab.getDefinitionEn().isEmpty()) {
            return Optional.empty();
        }

        List<Vocabulary> distractors = vocabularyRepository.findRandomWords(vocab.getId(), vocab.getPartOfSpeech());
        if (distractors.size() < 3) {
            return Optional.empty();
        }

        QuestionDTO q = new QuestionDTO();
        q.setPrompt("Which image best represents the definition: \"" + vocab.getDefinitionEn() + "\"?");
        q.setQuestionType(QuestionType.MULTIPLE_CHOICE_IMAGE);
        q.setPoints(10);

        Set<QuestionChoiceDTO> choices = new HashSet<>();
        choices.add(new QuestionChoiceDTO(0, null, vocab.getImage(), true));
        distractors.forEach(d -> choices.add(new QuestionChoiceDTO(0, null, d.getImage(), false)));

        q.setChoices(choices);
        return Optional.of(q);
    }

    private Optional<QuestionDTO> generateListeningComprehension(Vocabulary vocab) {
        if (vocab.getAudio() == null || vocab.getAudio().isEmpty()) {
            return Optional.empty();
        }

        List<Vocabulary> distractors = vocabularyRepository.findRandomWords(vocab.getId(), vocab.getPartOfSpeech());
        if (distractors.size() < 3) {
            return Optional.empty();
        }

        QuestionDTO q = new QuestionDTO();
        q.setPrompt("Listen to the audio and choose the correct word.");
        q.setAudioUrl(vocab.getAudio());
        q.setQuestionType(QuestionType.LISTENING_COMPREHENSION);
        q.setPoints(10);

        Set<QuestionChoiceDTO> choices = new HashSet<>();
        choices.add(new QuestionChoiceDTO(0, vocab.getWord(), null, true));
        distractors.forEach(d -> choices.add(new QuestionChoiceDTO(0, d.getWord(), null, false)));

        q.setChoices(choices);
        return Optional.of(q);
    }
}
