package com.fourstars.FourStars.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.quiz.QuizDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.service.QuizGenerationService;
import com.fourstars.FourStars.service.QuizService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/quizzes")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Quiz Management API", description = "APIs for creating, managing, and taking quizzes")
public class QuizController {

    private final QuizService quizService;
    private final QuizGenerationService quizGenerationService;

    public QuizController(QuizService quizService, QuizGenerationService quizGenerationService) {
        this.quizService = quizService;
        this.quizGenerationService = quizGenerationService;
    }

    @Operation(summary = "Create a new quiz", description = "Creates a new quiz with a full set of questions and choices.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Quiz created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data provided"),
            @ApiResponse(responseCode = "404", description = "Associated category not found")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Create a new quiz")
    public ResponseEntity<QuizDTO> createQuiz(@Valid @RequestBody QuizDTO quizDTO) {
        QuizDTO createdQuiz = quizService.createQuiz(quizDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }

    @Operation(summary = "Update an existing quiz", description = "Updates a quiz. Note: This completely replaces the old set of questions with the new set provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quiz updated successfully"),
            @ApiResponse(responseCode = "404", description = "Quiz or its category not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Update an existing quiz")
    public ResponseEntity<QuizDTO> updateQuiz(@PathVariable("id") long id, @Valid @RequestBody QuizDTO quizDTO) {
        QuizDTO updatedQuiz = quizService.updateQuiz(id, quizDTO);
        return ResponseEntity.ok(updatedQuiz);
    }

    @Operation(summary = "Delete a quiz", description = "Deletes a quiz and all of its associated questions and user attempts.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Quiz deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Delete a quiz")
    public ResponseEntity<Void> deleteQuiz(@PathVariable("id") long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a quiz by ID for editing", description = "Retrieves the full details of a quiz, including all questions and correct answer information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved quiz details"),
            @ApiResponse(responseCode = "404", description = "Quiz not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get a quiz by id for admin")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable("id") long id) {
        return ResponseEntity.ok(quizService.getQuizForAdmin(id));
    }

    @Operation(summary = "Get all quizzes with pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get all quizzes for admin with filtering")
    public ResponseEntity<ResultPaginationDTO<QuizDTO>> getAllQuizzes(
            Pageable pageable,
            @RequestParam(name = "categoryId", required = false) Long categoryId) {

        return ResponseEntity.ok(quizService.getAllQuizzesForAdmin(pageable, categoryId));
    }

    @Operation(summary = "Generate a comprehensive quiz from a category", description = "Creates a new quiz containing one random question for each vocabulary word in the specified category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comprehensive quiz created successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "400", description = "No questions could be generated for this category")
    })
    @PostMapping("/generate-from-category")
    @ApiMessage("Generate a comprehensive quiz")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<QuizDTO> generateComprehensiveQuiz(@RequestParam Long categoryId) {
        QuizDTO generatedQuizData = quizGenerationService.generateComprehensiveQuizForCategory(categoryId);
        QuizDTO createdQuiz = quizService.createQuiz(generatedQuizData);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuiz);
    }
}
