package com.fourstars.FourStars.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    @Override
    @EntityGraph(attributePaths = { "category" })
    Page<Article> findAll(Specification<Article> spec, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByTitleAndCategoryId(String title, Long categoryId);

    boolean existsByTitleAndCategoryIdAndIdNot(String title, Long categoryId, Long id);
}
