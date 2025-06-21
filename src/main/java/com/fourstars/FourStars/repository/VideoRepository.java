package com.fourstars.FourStars.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Video;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long>, JpaSpecificationExecutor<Video> {
    @Override
    @EntityGraph(attributePaths = { "category" })
    Page<Video> findAll(Specification<Video> spec, Pageable pageable);

    boolean existsByTitleAndCategoryId(String title, Long categoryId);

    boolean existsByTitleAndCategoryIdAndIdNot(String title, Long categoryId, Long id);

    boolean existsByCategoryId(Long categoryId);
}
