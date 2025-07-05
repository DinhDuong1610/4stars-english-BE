package com.fourstars.FourStars.controller.client;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.article.ArticleResponseDTO;
import com.fourstars.FourStars.service.ArticleService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("clientArticleController")
@RequestMapping("/api/v1/articles")
@Tag(name = "Client - Article Management API", description = "APIs for managing English reading articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Operation(summary = "Fetch an article by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved article"),
            @ApiResponse(responseCode = "404", description = "Article not found")
    })
    @GetMapping("/{id}")
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
