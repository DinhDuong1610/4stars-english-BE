package com.fourstars.FourStars.controller.admin;

import java.util.List;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/vocabularies")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Vocabulary Management API", description = "APIs for managing vocabulary words and their details")
public class VocabularyController {
        private final VocabularyService vocabularyService;

        public VocabularyController(VocabularyService vocabularyService) {
                this.vocabularyService = vocabularyService;
        }

        @Operation(summary = "Create a new vocabulary word")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Vocabulary created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid data (e.g., category is not of type VOCABULARY)"),
                        @ApiResponse(responseCode = "404", description = "Category not found"),
                        @ApiResponse(responseCode = "409", description = "This word already exists in this category")
        })
        @PostMapping
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @ApiMessage("Create a new vocabulary")
        public ResponseEntity<VocabularyResponseDTO> createVocabulary(
                        @Valid @RequestBody VocabularyRequestDTO requestDTO)
                        throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
                VocabularyResponseDTO newVocab = vocabularyService.createVocabulary(requestDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(newVocab);
        }

        @Operation(summary = "Update an existing vocabulary word")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Vocabulary updated successfully"),
                        @ApiResponse(responseCode = "404", description = "Vocabulary or Category not found")
        })
        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @ApiMessage("Update an existing vocabulary")
        public ResponseEntity<VocabularyResponseDTO> updateVocabulary(
                        @PathVariable long id,
                        @Valid @RequestBody VocabularyRequestDTO requestDTO)
                        throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
                VocabularyResponseDTO updatedVocab = vocabularyService.updateVocabulary(id, requestDTO);
                return ResponseEntity.ok(updatedVocab);
        }

        @Operation(summary = "Delete a vocabulary word", description = "Deletes a word and all associated user learning progress.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Vocabulary deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Vocabulary not found")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @ApiMessage("Delete a vocabulary")
        public ResponseEntity<Void> deleteVocabulary(@PathVariable long id) throws ResourceNotFoundException {
                vocabularyService.deleteVocabulary(id);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Get a vocabulary word by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved vocabulary"),
                        @ApiResponse(responseCode = "404", description = "Vocabulary not found")
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @ApiMessage("Fetch a vocabulary by its ID")
        public ResponseEntity<VocabularyResponseDTO> getVocabularyById(@PathVariable long id)
                        throws ResourceNotFoundException {
                VocabularyResponseDTO vocab = vocabularyService.fetchVocabularyById(id);
                return ResponseEntity.ok(vocab);
        }

        @Operation(summary = "Get all vocabulary with pagination and filtering")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved vocabulary list")
        })
        @GetMapping
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @ApiMessage("Fetch all vocabularies with pagination and filtering")
        public ResponseEntity<ResultPaginationDTO<VocabularyResponseDTO>> getAllVocabularies(
                        Pageable pageable,
                        @RequestParam(name = "categoryId", required = false) Long categoryId,
                        @RequestParam(name = "word", required = false) String word) {
                ResultPaginationDTO<VocabularyResponseDTO> result = vocabularyService.fetchAllVocabularies(pageable,
                                categoryId,
                                word);
                return ResponseEntity.ok(result);
        }

        @Operation(summary = "Create multiple vocabulary words in bulk", description = "Creates a list of new vocabulary words. The entire operation is transactional.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "All vocabularies created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid data in the list"),
                        @ApiResponse(responseCode = "409", description = "One or more words already exist in their respective categories")
        })
        @PostMapping("/bulk")
        @ApiMessage("Create a list of vocabularies")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        public ResponseEntity<List<VocabularyResponseDTO>> createBulkVocabularies(
                        @Valid @RequestBody List<VocabularyRequestDTO> vocabularyList)
                        throws DuplicateResourceException {
                List<VocabularyResponseDTO> createdVocabularies = vocabularyService
                                .createBulkVocabularies(vocabularyList);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdVocabularies);
        }

}
