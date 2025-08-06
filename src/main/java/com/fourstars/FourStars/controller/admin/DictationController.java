package com.fourstars.FourStars.controller.admin;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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

import com.fourstars.FourStars.domain.request.dictation.DictationTopicRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.dictation.DictationTopicResponseDTO;
import com.fourstars.FourStars.service.DictationService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/dictations")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Dictation API", description = "APIs for listening dictation exercises")
public class DictationController {

    private final DictationService dictationService;

    public DictationController(DictationService dictationService) {
        this.dictationService = dictationService;
    }

    @Operation(summary = "Create a new dictation topic", description = "Creates a new dictation topic along with all its sentences.")
    @ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Topic created successfully") })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Create a new dictation topic")
    public ResponseEntity<DictationTopicResponseDTO> createDictationTopic(
            @RequestBody DictationTopicRequestDTO requestDTO) {
        DictationTopicResponseDTO dication = dictationService.createDictationTopic(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(dication);
    }

    @Operation(summary = "Update a dictation topic")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Update a dictation topic")
    public ResponseEntity<DictationTopicResponseDTO> updateDictationTopic(
            @Parameter(description = "ID of the topic to update") @PathVariable long id,
            @Valid @RequestBody DictationTopicRequestDTO requestDTO) {
        return ResponseEntity.ok(dictationService.updateDictationTopic(id, requestDTO));
    }

    @Operation(summary = "Delete a dictation topic")
    @ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Topic deleted successfully") })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Delete a dictation topic")
    public ResponseEntity<Void> deleteDictationTopic(
            @Parameter(description = "ID of the topic to delete") @PathVariable long id) {
        dictationService.deleteDictationTopic(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get a dictation topic by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Get a dictation topic by ID")
    public ResponseEntity<DictationTopicResponseDTO> getDictationTopic(
            @Parameter(description = "ID of the topic to retrieve") @PathVariable long id) {
        return ResponseEntity.ok(dictationService.getDictationTopicById(id));
    }

    @Operation(summary = "Get all dictation topics with filtering and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch all dictation topics")
    public ResponseEntity<ResultPaginationDTO<DictationTopicResponseDTO>> getAllTopics(
            Pageable pageable,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by a search term in the title") @RequestParam(required = false) String title,
            @Parameter(description = "Filter by start creation date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startCreatedAt,
            @Parameter(description = "Filter by end creation date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endCreatedAt) {
        return ResponseEntity
                .ok(dictationService.fetchAllTopics(pageable, categoryId, title, startCreatedAt, endCreatedAt));
    }
}
