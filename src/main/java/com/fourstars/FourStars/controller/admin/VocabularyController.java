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

import com.fourstars.FourStars.domain.request.vocabulary.VocabularyRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.vocabulary.VocabularyResponseDTO;
import com.fourstars.FourStars.service.VocabularyService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/vocabularies")
// @PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class VocabularyController {
    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @PostMapping
    @ApiMessage("Create a new vocabulary")
    public ResponseEntity<VocabularyResponseDTO> createVocabulary(@Valid @RequestBody VocabularyRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        VocabularyResponseDTO newVocab = vocabularyService.createVocabulary(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newVocab);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update an existing vocabulary")
    public ResponseEntity<VocabularyResponseDTO> updateVocabulary(
            @PathVariable long id,
            @Valid @RequestBody VocabularyRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        VocabularyResponseDTO updatedVocab = vocabularyService.updateVocabulary(id, requestDTO);
        return ResponseEntity.ok(updatedVocab);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a vocabulary")
    public ResponseEntity<Void> deleteVocabulary(@PathVariable long id) throws ResourceNotFoundException {
        vocabularyService.deleteVocabulary(id);
        return ResponseEntity.noContent().build();
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

}
