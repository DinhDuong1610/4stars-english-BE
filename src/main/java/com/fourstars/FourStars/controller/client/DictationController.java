package com.fourstars.FourStars.controller.client;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.dictation.DictationSubmissionDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.dictation.DictationTopicResponseDTO;
import com.fourstars.FourStars.domain.response.dictation.NlpAnalysisResponse;
import com.fourstars.FourStars.service.DictationService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("ClientDictationController")
@RequestMapping("/api/v1/dictations")
@Tag(name = "Learning - Dictation API", description = "APIs for listening dictation exercises")
public class DictationController {

    private final DictationService dictationService;

    public DictationController(DictationService dictationService) {
        this.dictationService = dictationService;
    }

    @Operation(summary = "Get all dictation topics with filtering and pagination")
    @GetMapping
    @ApiMessage("Fetch all dictation topics")
    public ResponseEntity<ResultPaginationDTO<DictationTopicResponseDTO>> getAllTopics(
            Pageable pageable,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by a search term in the title") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by start creation date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startCreatedAt,
            @Parameter(description = "Filter by end creation date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endCreatedAt) {
        return ResponseEntity
                .ok(dictationService.fetchAllTopicsForUser(pageable, categoryId, title, startCreatedAt, endCreatedAt));
    }

    @Operation(summary = "Get a specific dictation exercise", description = "Retrieves a single dictation topic with its sentences (audio only, no answers) for the user to start.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Topic not found") })
    @GetMapping("/{id}")
    @ApiMessage("Get a specific dictation exercise")
    public ResponseEntity<DictationTopicResponseDTO> getTopicForUser(
            @Parameter(description = "ID of the topic to retrieve") @PathVariable long id) {
        return ResponseEntity.ok(dictationService.getDictationTopicForUser(id));
    }

    @Operation(summary = "Submit a dictated sentence for analysis", description = "Submits a user's dictated text for a specific sentence to be analyzed by the NLP model.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Analysis successful") })
    @PostMapping("/submit")
    @ApiMessage("Submit a dictated sentence for analysis")
    public ResponseEntity<NlpAnalysisResponse> submitAnswer(@RequestBody DictationSubmissionDTO submissionDTO) {
        NlpAnalysisResponse analysis = dictationService.submitAndAnalyze(
                submissionDTO.getSentenceId(),
                submissionDTO.getUserText());
        return ResponseEntity.ok(analysis);
    }
}