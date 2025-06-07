package com.fourstars.FourStars.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long>, JpaSpecificationExecutor<Article> {
    boolean existsByCategoryId(Long categoryId);

    boolean existsByTitleAndCategoryId(String title, Long categoryId);

    boolean existsByTitleAndCategoryIdAndIdNot(String title, Long categoryId, Long id);
}
