package com.fourstars.FourStars.controller.client;

import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.quiz.QuizDTO;
import com.fourstars.FourStars.domain.request.quiz.SubmitQuizRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.quiz.QuizAttemptResponseDTO;
import com.fourstars.FourStars.domain.response.quiz.QuizForUserAttemptDTO;
import com.fourstars.FourStars.service.QuizService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController("clientQuizController")
@RequestMapping("/api/v1/quizzes")
@Tag(name = "Client - Quiz Management API", description = "APIs for creating, managing, and taking quizzes")
public class QuizController {
    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @Operation(summary = "Start a quiz attempt", description = "Creates a new attempt for a quiz and returns the questions without their answers.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz attempt started successfully"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    @PostMapping("/{id}/start")
    @ApiMessage("Start a quiz attempt")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<QuizForUserAttemptDTO> startQuiz(@PathVariable("id") long quizId) {
        return ResponseEntity.ok(quizService.startQuiz(quizId));
    }

    @Operation(summary = "Submit quiz answers", description = "Submits the user's answers for an attempt. This is processed asynchronously.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Submission received and is being processed"),
            @ApiResponse(responseCode = "400", description = "Invalid submission data"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @PostMapping("/submit")
    @ApiMessage("Submit answers for a quiz attempt")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<Map<String, String>> submitQuiz(@Valid @RequestBody SubmitQuizRequestDTO submitDTO) {
        quizService.acceptQuizSubmission(submitDTO);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(Map.of("message",
                        "Your submission has been received and is being processed. You will be notified when the results are ready."));
    }

    @Operation(summary = "Get quiz attempt results", description = "Retrieves the detailed results of a completed quiz attempt.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved results"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Trying to access another user's attempt"),
            @ApiResponse(responseCode = "404", description = "Quiz attempt not found")
    })
    @GetMapping("/attempts/{attemptId}")
    @ApiMessage("Get the result of a quiz attempt")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<QuizAttemptResponseDTO> getQuizResult(@PathVariable long attemptId) {
        return ResponseEntity.ok(quizService.getQuizResult(attemptId));
    }

    @Operation(summary = "Get a quiz by ID for editing", description = "Retrieves the full details of a quiz, including all questions and correct answer information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved quiz details"),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    @GetMapping("/{id}")
    @ApiMessage("Get a quiz by id for admin")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable("id") long id) {
        return ResponseEntity.ok(quizService.getQuizForAdmin(id));
    }

    @Operation(summary = "Get all quizzes with pagination")
    @GetMapping
    @ApiMessage("Get all quizzes for admin with filtering")
    public ResponseEntity<ResultPaginationDTO<QuizDTO>> getAllQuizzes(
            Pageable pageable,
            @RequestParam(name = "categoryId", required = false) Long categoryId) {

        return ResponseEntity.ok(quizService.getAllQuizzesForAdmin(pageable, categoryId));
    }
}
