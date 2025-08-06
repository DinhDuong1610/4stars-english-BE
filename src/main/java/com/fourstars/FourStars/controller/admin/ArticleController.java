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

import com.fourstars.FourStars.domain.request.article.ArticleRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.article.ArticleResponseDTO;
import com.fourstars.FourStars.service.ArticleService;
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
@RequestMapping("/api/v1/admin/articles")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Article Management API", description = "APIs for managing English reading articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Operation(summary = "Create a new article")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Article created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data (e.g., category is not of type ARTICLE)"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Article with the same title already exists in this category")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Create a new article")
    public ResponseEntity<ArticleResponseDTO> createArticle(@Valid @RequestBody ArticleRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        ArticleResponseDTO newArticle = articleService.createArticle(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newArticle);
    }

    @Operation(summary = "Update an existing article")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Article updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid data"),
            @ApiResponse(responseCode = "404", description = "Article or Category not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Update an existing article")
    public ResponseEntity<ArticleResponseDTO> updateArticle(
            @PathVariable long id,
            @Valid @RequestBody ArticleRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        ArticleResponseDTO updatedArticle = articleService.updateArticle(id, requestDTO);
        return ResponseEntity.ok(updatedArticle);
    }

    @Operation(summary = "Delete an article")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Article deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Article not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Delete an article")
    public ResponseEntity<Void> deleteArticle(@PathVariable long id) throws ResourceNotFoundException {
        articleService.deleteArticle(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Fetch an article by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved article"),
            @ApiResponse(responseCode = "404", description = "Article not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch an article by its ID")
    public ResponseEntity<ArticleResponseDTO> getArticleById(@PathVariable long id) throws ResourceNotFoundException {
        ArticleResponseDTO article = articleService.fetchArticleById(id);
        return ResponseEntity.ok(article);
    }

    @Operation(summary = "Fetch all articles with pagination and filtering")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved article list")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch all articles with pagination and filtering")
    public ResponseEntity<ResultPaginationDTO<ArticleResponseDTO>> getAllArticles(
            Pageable pageable,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "startCreatedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startCreatedAt,
            @RequestParam(name = "endCreatedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endCreatedAt) {
        ResultPaginationDTO<ArticleResponseDTO> result = articleService.fetchAllArticles(pageable, categoryId, title,
                startCreatedAt, endCreatedAt);
        return ResponseEntity.ok(result);
    }
}
