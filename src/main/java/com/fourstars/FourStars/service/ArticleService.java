package com.fourstars.FourStars.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Article;
import com.fourstars.FourStars.domain.Badge;
import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.request.article.ArticleRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.article.ArticleResponseDTO;
import com.fourstars.FourStars.repository.ArticleRepository;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.util.constant.CategoryType;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;

@Service
public class ArticleService {
    private static final Logger logger = LoggerFactory.getLogger(ArticleService.class);

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;

    public ArticleService(ArticleRepository articleRepository, CategoryRepository categoryRepository) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
    }

    private ArticleResponseDTO convertToArticleResponseDTO(Article article) {
        if (article == null)
            return null;
        ArticleResponseDTO dto = new ArticleResponseDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setImage(article.getImage());
        dto.setAudio(article.getAudio());

        if (article.getCategory() != null) {
            ArticleResponseDTO.CategoryInfoDTO catInfo = new ArticleResponseDTO.CategoryInfoDTO();
            catInfo.setId(article.getCategory().getId());
            catInfo.setName(article.getCategory().getName());
            dto.setCategory(catInfo);
        }

        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        dto.setCreatedBy(article.getCreatedBy());
        dto.setUpdatedBy(article.getUpdatedBy());
        return dto;
    }

    private String sanitizeHtmlContent(String unsafeHtml) {
        if (unsafeHtml == null || unsafeHtml.isEmpty()) {
            return unsafeHtml;
        }
        return Jsoup.clean(unsafeHtml, Safelist.basicWithImages());
    }

    @Transactional
    public ArticleResponseDTO createArticle(ArticleRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        logger.info("Request to create new article with title: '{}'", requestDTO.getTitle());

        if (articleRepository.existsByTitleAndCategoryId(requestDTO.getTitle(), requestDTO.getCategoryId())) {
            throw new DuplicateResourceException("An article with the same title already exists in this category.");
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + requestDTO.getCategoryId()));

        if (category.getType() != CategoryType.ARTICLE) {
            throw new BadRequestException("The selected category is not of type 'ARTICLE'.");
        }

        Article article = new Article();
        article.setTitle(requestDTO.getTitle());
        article.setContent(sanitizeHtmlContent(requestDTO.getContent())); // Làm sạch HTML
        article.setImage(requestDTO.getImage());
        article.setAudio(requestDTO.getAudio());
        article.setCategory(category);

        Article savedArticle = articleRepository.save(article);

        logger.info("Successfully created new article with ID: {}", savedArticle.getId());

        return convertToArticleResponseDTO(savedArticle);
    }

    @Transactional
    public ArticleResponseDTO updateArticle(long id, ArticleRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        logger.info("Request to update article with ID: {}", id);

        Article articleDB = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));

        if (articleRepository.existsByTitleAndCategoryIdAndIdNot(requestDTO.getTitle(), requestDTO.getCategoryId(),
                id)) {
            throw new DuplicateResourceException("An article with the same title already exists in this category.");
        }

        Category category = categoryRepository.findById(requestDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + requestDTO.getCategoryId()));

        if (category.getType() != CategoryType.ARTICLE) {
            throw new BadRequestException("The selected category is not of type 'ARTICLE'.");
        }

        articleDB.setTitle(requestDTO.getTitle());
        articleDB.setContent(sanitizeHtmlContent(requestDTO.getContent()));
        articleDB.setImage(requestDTO.getImage());
        articleDB.setAudio(requestDTO.getAudio());
        articleDB.setCategory(category);

        Article updatedArticle = articleRepository.save(articleDB);

        logger.info("Successfully updated article with ID: {}", updatedArticle.getId());

        return convertToArticleResponseDTO(updatedArticle);
    }

    @Transactional
    public void deleteArticle(long id) throws ResourceNotFoundException {
        logger.info("Request to delete article with ID: {}", id);

        Article articleToDelete = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));

        articleRepository.delete(articleToDelete);

        logger.info("Successfully deleted article with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public ArticleResponseDTO fetchArticleById(long id) throws ResourceNotFoundException {
        logger.debug("Request to fetch article by ID: {}", id);

        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article not found with id: " + id));
        return convertToArticleResponseDTO(article);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<ArticleResponseDTO> fetchAllArticles(Pageable pageable, Long categoryId, String title,
            LocalDate startCreatedAt, LocalDate endCreatedAt) {
        logger.debug("Request to fetch all articles with categoryId: {} and title: {}", categoryId, title);

        Specification<Article> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                        "%" + title.trim().toLowerCase() + "%"));
            }
            if (startCreatedAt != null) {
                Instant startInstant = startCreatedAt.atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startInstant));
            }
            if (endCreatedAt != null) {
                Instant endInstant = endCreatedAt.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endInstant));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Article> pageArticle = articleRepository.findAll(spec, pageable);
        List<ArticleResponseDTO> articleDTOs = pageArticle.getContent().stream()
                .map(this::convertToArticleResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pageArticle.getTotalPages(),
                pageArticle.getTotalElements());

        logger.debug("Found {} articles on page {}/{}", articleDTOs.size(), meta.getPage(), meta.getPages());

        return new ResultPaginationDTO<>(meta, articleDTOs);
    }

}
