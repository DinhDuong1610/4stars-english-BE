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

import com.fourstars.FourStars.domain.request.article.ArticleRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.article.ArticleResponseDTO;
import com.fourstars.FourStars.service.ArticleService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/articles")
// @PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    @ApiMessage("Create a new article")
    public ResponseEntity<ArticleResponseDTO> createArticle(@Valid @RequestBody ArticleRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        ArticleResponseDTO newArticle = articleService.createArticle(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newArticle);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update an existing article")
    public ResponseEntity<ArticleResponseDTO> updateArticle(
            @PathVariable long id,
            @Valid @RequestBody ArticleRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        ArticleResponseDTO updatedArticle = articleService.updateArticle(id, requestDTO);
        return ResponseEntity.ok(updatedArticle);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete an article")
    public ResponseEntity<Void> deleteArticle(@PathVariable long id) throws ResourceNotFoundException {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch an article by its ID")
    public ResponseEntity<ArticleResponseDTO> getArticleById(@PathVariable long id) throws ResourceNotFoundException {
        ArticleResponseDTO article = articleService.fetchArticleById(id);
        return ResponseEntity.ok(article);
    }

    @GetMapping
    @ApiMessage("Fetch all articles with pagination and filtering")
    public ResponseEntity<ResultPaginationDTO<ArticleResponseDTO>> getAllArticles(
            Pageable pageable,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "title", required = false) String title) {
        ResultPaginationDTO<ArticleResponseDTO> result = articleService.fetchAllArticles(pageable, categoryId, title);
        return ResponseEntity.ok(result);
    }
}
