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

import com.fourstars.FourStars.domain.request.grammar.GrammarRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.grammar.GrammarResponseDTO;
import com.fourstars.FourStars.service.GrammarService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/grammars")
// @PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class GrammarController {

    private final GrammarService grammarService;

    public GrammarController(GrammarService grammarService) {
        this.grammarService = grammarService;
    }

    @PostMapping
    @ApiMessage("Create a new grammar lesson")
    public ResponseEntity<GrammarResponseDTO> createGrammar(@Valid @RequestBody GrammarRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        GrammarResponseDTO newGrammar = grammarService.createGrammar(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newGrammar);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update an existing grammar lesson")
    public ResponseEntity<GrammarResponseDTO> updateGrammar(
            @PathVariable long id,
            @Valid @RequestBody GrammarRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        GrammarResponseDTO updatedGrammar = grammarService.updateGrammar(id, requestDTO);
        return ResponseEntity.ok(updatedGrammar);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a grammar lesson")
    public ResponseEntity<Void> deleteGrammar(@PathVariable long id) throws ResourceNotFoundException {
        grammarService.deleteGrammar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch a grammar lesson by its ID")
    public ResponseEntity<GrammarResponseDTO> getGrammarById(@PathVariable long id) throws ResourceNotFoundException {
        GrammarResponseDTO grammar = grammarService.fetchGrammarById(id);
        return ResponseEntity.ok(grammar);
    }

    @GetMapping
    @ApiMessage("Fetch all grammar lessons with pagination and filtering")
    public ResponseEntity<ResultPaginationDTO<GrammarResponseDTO>> getAllGrammars(
            Pageable pageable,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "name", required = false) String name) {
        ResultPaginationDTO<GrammarResponseDTO> result = grammarService.fetchAllGrammars(pageable, categoryId, name);
        return ResponseEntity.ok(result);
    }
}