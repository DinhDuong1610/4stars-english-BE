package com.fourstars.FourStars.controller.client;

import java.util.List;

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

import com.fourstars.FourStars.domain.UserVocabulary;
import com.fourstars.FourStars.domain.request.quiz.QuizDTO;
import com.fourstars.FourStars.domain.request.vocabulary.SubmitReviewRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.vocabulary.VocabularyResponseDTO;
import com.fourstars.FourStars.service.VocabularyService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController("clientVocabularyController")
@RequestMapping("/api/v1/vocabularies")
@Tag(name = "Client - Vocabulary Management API", description = "APIs for managing vocabulary words and their details")
public class VocabularyController {
    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @Operation(summary = "Get a vocabulary word by ID", description = "Public endpoint to retrieve the details of a single word.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved vocabulary"),
            @ApiResponse(responseCode = "404", description = "Vocabulary not found")
    })
    @GetMapping("/{id}")
    @ApiMessage("Fetch a vocabulary by its ID")
    public ResponseEntity<VocabularyResponseDTO> getVocabularyById(@PathVariable long id)
            throws ResourceNotFoundException {
        VocabularyResponseDTO vocab = vocabularyService.fetchVocabularyById(id);
        return ResponseEntity.ok(vocab);
    }

    @Operation(summary = "Search and get all vocabulary", description = "Public endpoint to search for words with pagination and filtering.")
    @GetMapping
    @ApiMessage("Fetch all vocabularies with pagination and filtering")
    public ResponseEntity<ResultPaginationDTO<VocabularyResponseDTO>> getAllVocabularies(
            Pageable pageable,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "word", required = false) String word) {
        ResultPaginationDTO<VocabularyResponseDTO> result = vocabularyService.fetchAllVocabularies(pageable, categoryId,
                word);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get words for review", description = "Fetches a list of vocabulary words that are due for review for the authenticated user, based on the SM-2 algorithm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved review list"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/review")
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Fetch all vocabularies for review with pagination and filtering")
    public ResponseEntity<List<VocabularyResponseDTO>> getVocabulariesForReview() {
        List<VocabularyResponseDTO> result = vocabularyService.getVocabulariesForReview(1000);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Generate a personalized review quiz", description = "Fetches words that are due for review for the current user and automatically creates a new quiz from them.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review quiz created successfully and returned"),
            @ApiResponse(responseCode = "200", description = "OK - No words to review at the moment (returns empty body)"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/review/generate-quiz")
    @ApiMessage("Generate a personalized review quiz from due vocabularies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<QuizDTO> generateReviewQuiz() {
        QuizDTO reviewQuiz = vocabularyService.createReviewQuiz();

        if (reviewQuiz == null) {
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewQuiz);
    }

    @Operation(summary = "Submit a vocabulary review", description = "Submits the result of a vocabulary review. The 'quality' score (0-5) is used by the SM-2 algorithm to calculate the next review date.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review submitted successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data (e.g., quality score out of range)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/submit-review")
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Submit a vocabulary review")
    public ResponseEntity<UserVocabulary> submitVocabularyReview(
            @Valid @RequestBody SubmitReviewRequestDTO reviewDTO) {
        UserVocabulary result = vocabularyService.submitVocabularyReview(reviewDTO);
        return ResponseEntity.ok(result);
    }

}
