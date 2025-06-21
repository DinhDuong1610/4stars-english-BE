package com.fourstars.FourStars.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Grammar;

@Repository
public interface GrammarRepository extends JpaRepository<Grammar, Long>, JpaSpecificationExecutor<Grammar> {
    @Override
    @EntityGraph(attributePaths = { "category" })
    Page<Grammar> findAll(Specification<Grammar> spec, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    boolean existsByNameAndCategoryId(String name, Long categoryId);

    boolean existsByNameAndCategoryIdAndIdNot(String name, Long categoryId, Long id);
}
