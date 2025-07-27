package com.fourstars.FourStars.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.dictation.DictationTopicRequestDTO;
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
    @ApiMessage("Update a dictation topic")
    public ResponseEntity<DictationTopicResponseDTO> updateDictationTopic(
            @Parameter(description = "ID of the topic to update") @PathVariable long id,
            @Valid @RequestBody DictationTopicRequestDTO requestDTO) {
        return ResponseEntity.ok(dictationService.updateDictationTopic(id, requestDTO));
    }
}
