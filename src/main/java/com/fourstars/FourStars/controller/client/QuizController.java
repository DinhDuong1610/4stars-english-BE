package com.fourstars.FourStars.controller.client;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.quiz.SubmitQuizRequestDTO;
import com.fourstars.FourStars.domain.response.quiz.QuizAttemptResponseDTO;
import com.fourstars.FourStars.domain.response.quiz.QuizForUserAttemptDTO;
import com.fourstars.FourStars.service.QuizService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

import jakarta.validation.Valid;

@RestController("clientQuizController")
@RequestMapping("/api/v1/quizzes")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/{id}/start")
    @ApiMessage("Start a quiz attempt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizForUserAttemptDTO> startQuiz(@PathVariable("id") long quizId) {
        return ResponseEntity.ok(quizService.startQuiz(quizId));
    }

    @PostMapping("/submit")
    @ApiMessage("Submit answers for a quiz attempt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizAttemptResponseDTO> submitQuiz(@Valid @RequestBody SubmitQuizRequestDTO submitDTO) {
        return ResponseEntity.ok(quizService.submitQuiz(submitDTO));
    }

    @GetMapping("/attempts/{attemptId}")
    @ApiMessage("Get the result of a quiz attempt")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizAttemptResponseDTO> getQuizResult(@PathVariable long attemptId) {
        return ResponseEntity.ok(quizService.getQuizResult(attemptId));
    }
}
