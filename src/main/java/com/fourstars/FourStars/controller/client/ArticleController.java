package com.fourstars.FourStars.controller.client;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.article.ArticleResponseDTO;
import com.fourstars.FourStars.service.ArticleService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@RestController("clientArticleController")
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch an article by its ID")
    public ResponseEntity<ArticleResponseDTO> getArticleById(@PathVariable long id) throws ResourceNotFoundException {
        ArticleResponseDTO article = articleService.fetchArticleById(id);
        return ResponseEntity.ok(article);
    }
}
