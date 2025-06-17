package com.fourstars.FourStars.controller.client;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.vocabulary.UserVocabularyResponseDTO;
import com.fourstars.FourStars.service.VocabularyService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

@RestController
@RequestMapping("/api/v1/notebook")
public class NotebookController {

    private final VocabularyService vocabularyService;

    public NotebookController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @PostMapping("/add/{vocabularyId}")
    @ApiMessage("Add a vocabulary to the user's personal notebook")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserVocabularyResponseDTO> addVocabularyToNotebook(@PathVariable long vocabularyId) {
        UserVocabularyResponseDTO result = vocabularyService.addVocabularyToNotebook(vocabularyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
