package com.fourstars.FourStars.controller.client;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.UserVocabulary;
import com.fourstars.FourStars.domain.request.vocabulary.SubmitReviewRequestDTO;
import com.fourstars.FourStars.domain.request.vocabulary.VocabularyRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.vocabulary.VocabularyResponseDTO;
import com.fourstars.FourStars.service.VocabularyService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController("clientVocabularyController")
@RequestMapping("/api/v1/vocabularies")
public class VocabularyController {
    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch a vocabulary by its ID")
    public ResponseEntity<VocabularyResponseDTO> getVocabularyById(@PathVariable long id)
            throws ResourceNotFoundException {
        VocabularyResponseDTO vocab = vocabularyService.fetchVocabularyById(id);
        return ResponseEntity.ok(vocab);
    }

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

    // getVocabulariesForReview
    @GetMapping("/review")
    @ApiMessage("Fetch all vocabularies for review with pagination and filtering")
    public ResponseEntity<List<VocabularyResponseDTO>> getVocabulariesForReview() {
        List<VocabularyResponseDTO> result = vocabularyService.getVocabulariesForReview(1000);
        return ResponseEntity.ok(result);
    }

    // submitVocabularyReview
    @PostMapping("/submit-review")
    @ApiMessage("Submit a vocabulary review")
    public ResponseEntity<UserVocabulary> submitVocabularyReview(
            @Valid @RequestBody SubmitReviewRequestDTO reviewDTO) {
        UserVocabulary result = vocabularyService.submitVocabularyReview(reviewDTO);
        return ResponseEntity.ok(result);
    }

}
