package com.fourstars.FourStars.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.util.constant.CategoryType;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>, JpaSpecificationExecutor<Category> {

    boolean existsByNameAndTypeAndParentCategoryId(String name, CategoryType type, Long parentCategoryId);

    boolean existsByNameAndTypeAndParentCategoryIdAndIdNot(String name, CategoryType type, Long parentCategoryId,
            Long id);

    List<Category> findByParentCategoryIsNull();

    List<Category> findByType(CategoryType type);

    Page<Category> findByType(CategoryType type, Pageable pageable);

}