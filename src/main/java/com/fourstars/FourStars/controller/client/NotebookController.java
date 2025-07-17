package com.fourstars.FourStars.controller.client;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.vocabulary.UserVocabularyResponseDTO;
import com.fourstars.FourStars.domain.response.vocabulary.VocabularyResponseDTO;
import com.fourstars.FourStars.service.VocabularyService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notebook")
@Tag(name = "Client - Vocabulary Management API", description = "APIs for managing vocabulary words and their details")
public class NotebookController {

    private final VocabularyService vocabularyService;

    public NotebookController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @Operation(summary = "Add a word to my notebook", description = "Adds a specific vocabulary word to the authenticated user's personal learning list. This creates the initial record for SM-2 spaced repetition tracking.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Word successfully added to the notebook"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "Vocabulary word with the specified ID not found")
    })
    @PostMapping("/add/{vocabularyId}")
    @ApiMessage("Add a vocabulary to the user's personal notebook")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<UserVocabularyResponseDTO> addVocabularyToNotebook(@PathVariable long vocabularyId) {
        UserVocabularyResponseDTO result = vocabularyService.addVocabularyToNotebook(vocabularyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @Operation(summary = "Get recently added words in notebook", description = "Retrieves a paginated list of the most recently added vocabulary words to the user's personal notebook.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved recently added words"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated")
    })
    @GetMapping("/recent")
    @ApiMessage("Fetch recently added vocabularies in notebook")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResultPaginationDTO<VocabularyResponseDTO>> getRecentlyAdded(Pageable pageable) {
        ResultPaginationDTO<VocabularyResponseDTO> result = vocabularyService.fetchRecentlyAddedToNotebook(pageable);
        return ResponseEntity.ok(result);
    }
}
