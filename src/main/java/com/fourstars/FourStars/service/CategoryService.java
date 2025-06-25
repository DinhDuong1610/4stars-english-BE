package com.fourstars.FourStars.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.request.category.CategoryRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.category.CategoryResponseDTO;
import com.fourstars.FourStars.repository.ArticleRepository;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.repository.GrammarRepository;
import com.fourstars.FourStars.repository.VideoRepository;
import com.fourstars.FourStars.repository.VocabularyRepository;
import com.fourstars.FourStars.util.constant.CategoryType;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceInUseException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;

@Service
@CacheConfig(cacheNames = "categories")
public class CategoryService {
    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;
    private final VocabularyRepository vocabularyRepository;
    private final GrammarRepository grammarRepository;
    private final ArticleRepository articleRepository;
    private final VideoRepository videoRepository;

    public CategoryService(CategoryRepository categoryRepository,
            VocabularyRepository vocabularyRepository,
            GrammarRepository grammarRepository,
            ArticleRepository articleRepository,
            VideoRepository videoRepository) {
        this.categoryRepository = categoryRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.grammarRepository = grammarRepository;
        this.articleRepository = articleRepository;
        this.videoRepository = videoRepository;
    }

    private CategoryResponseDTO convertToCategoryResponseDTO(Category category, boolean deep) {
        if (category == null)
            return null;
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setType(category.getType());
        dto.setOrderIndex(category.getOrderIndex());
        if (category.getParentCategory() != null) {
            dto.setParentId(category.getParentCategory().getId());
        }
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        dto.setCreatedBy(category.getCreatedBy());
        dto.setUpdatedBy(category.getUpdatedBy());

        if (deep && category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            List<CategoryResponseDTO> subCategoryDTOs = category.getSubCategories().stream()
                    .map(sub -> convertToCategoryResponseDTO(sub, true)) // Đệ quy để lấy toàn bộ cây
                    .collect(Collectors.toList());
            dto.setSubCategories(subCategoryDTOs);
        } else {
            dto.setSubCategories(new ArrayList<>());
        }
        return dto;
    }

    @Transactional
    @CacheEvict(allEntries = true)
    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
        logger.info("Request to create new category with name: '{}', type: {}", requestDTO.getName(),
                requestDTO.getType());

        if (categoryRepository.existsByNameAndTypeAndParentCategoryId(requestDTO.getName(), requestDTO.getType(),
                requestDTO.getParentId())) {
            throw new DuplicateResourceException("A category with the same name, type, and parent already exists.");
        }

        Category category = new Category();
        category.setName(requestDTO.getName());
        category.setDescription(requestDTO.getDescription());
        category.setType(requestDTO.getType());
        category.setOrderIndex(requestDTO.getOrderIndex());

        if (requestDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(requestDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + requestDTO.getParentId()));
            category.setParentCategory(parent);
        }

        Category savedCategory = categoryRepository.save(category);

        logger.info("Successfully created new category with ID: {}", savedCategory.getId());

        return convertToCategoryResponseDTO(savedCategory, false);
    }

    @Transactional(readOnly = true)
    @Cacheable(key = "#id")
    public CategoryResponseDTO fetchCategoryById(long id, boolean deep) throws ResourceNotFoundException {

        logger.debug("Request to fetch category by ID: {}, deep fetch: {}", id, deep);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return convertToCategoryResponseDTO(category, deep);
    }

    @Transactional
    @CacheEvict(key = "#id", allEntries = true)
    public CategoryResponseDTO updateCategory(long id, CategoryRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        logger.info("Request to update category with ID: {}", id);

        Category categoryDB = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (categoryRepository.existsByNameAndTypeAndParentCategoryIdAndIdNot(requestDTO.getName(),
                requestDTO.getType(), requestDTO.getParentId(), id)) {
            throw new DuplicateResourceException(
                    "Another category with the same name, type, and parent already exists.");
        }

        if (requestDTO.getParentId() != null && requestDTO.getParentId() == id) {
            throw new BadRequestException("A category cannot be its own parent.");
        }

        categoryDB.setName(requestDTO.getName());
        categoryDB.setDescription(requestDTO.getDescription());
        categoryDB.setType(requestDTO.getType());
        categoryDB.setOrderIndex(requestDTO.getOrderIndex());

        if (requestDTO.getParentId() != null) {
            Category parent = categoryRepository.findById(requestDTO.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + requestDTO.getParentId()));
            categoryDB.setParentCategory(parent);
        } else {
            categoryDB.setParentCategory(null);
        }

        Category updatedCategory = categoryRepository.save(categoryDB);

        logger.info("Successfully updated category with ID: {}", updatedCategory.getId());

        return convertToCategoryResponseDTO(updatedCategory, false);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<CategoryResponseDTO> fetchAllCategories(Pageable pageable, CategoryType type) {
        logger.debug("Request to fetch top-level categories with type: {}", type);

        Specification<Category> spec = (root, query, criteriaBuilder) -> {
            Predicate topLevelPredicate = criteriaBuilder.isNull(root.get("parentCategory"));

            if (type != null) {
                Predicate typePredicate = criteriaBuilder.equal(root.get("type"), type);
                return criteriaBuilder.and(topLevelPredicate, typePredicate);
            }

            return topLevelPredicate;
        };

        Page<Category> pageCategory = categoryRepository.findAll(spec, pageable);
        List<CategoryResponseDTO> categoryDTOs = pageCategory.getContent().stream()
                .map(cat -> convertToCategoryResponseDTO(cat, false))
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pageCategory.getTotalPages(),
                pageCategory.getTotalElements());

        logger.debug("Found {} top-level categories.", pageCategory.getTotalElements());

        return new ResultPaginationDTO<>(meta, categoryDTOs);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<CategoryResponseDTO> fetchAllCategoriesAsTree(CategoryType type, Pageable pageable) {
        logger.debug("Request to fetch all categories as a tree with type: {}", type);

        Specification<Category> spec = (root, query, cb) -> {
            if (type != null) {
                return cb.equal(root.get("type"), type);
            }
            return cb.conjunction();
        };
        List<Category> allCategories = categoryRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "orderIndex"));

        Map<Long, CategoryResponseDTO> categoryMap = allCategories.stream()
                .map(cat -> convertToCategoryResponseDTO(cat, false))
                .collect(Collectors.toMap(
                        CategoryResponseDTO::getId,
                        Function.identity(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new));

        List<CategoryResponseDTO> rootCategories = new ArrayList<>();
        categoryMap.values().forEach(dto -> {
            if (dto.getParentId() != null) {
                CategoryResponseDTO parentDTO = categoryMap.get(dto.getParentId());
                if (parentDTO != null) {
                    if (parentDTO.getSubCategories() == null) {
                        parentDTO.setSubCategories(new ArrayList<>());
                    }
                    parentDTO.getSubCategories().add(dto);
                }
            } else {
                rootCategories.add(dto);
            }
        });

        long totalRootElements = rootCategories.size();
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;

        List<CategoryResponseDTO> paginatedRootCategories;

        if (rootCategories.size() < startItem) {
            paginatedRootCategories = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, rootCategories.size());
            paginatedRootCategories = rootCategories.subList(startItem, toIndex);
        }

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                currentPage + 1,
                pageSize,
                (int) Math.ceil((double) totalRootElements / pageSize),
                totalRootElements);

        logger.debug("Constructed tree with {} root categories.", paginatedRootCategories.size());

        return new ResultPaginationDTO<>(meta, paginatedRootCategories);
    }

    @Transactional
    @CacheEvict(key = "#id", allEntries = true)
    public void deleteCategory(long id) throws ResourceNotFoundException, ResourceInUseException {

        logger.info("Request to delete category with ID: {}", id);

        Category categoryToDelete = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (categoryToDelete.getSubCategories() != null && !categoryToDelete.getSubCategories().isEmpty()) {
            throw new ResourceInUseException("Cannot delete category because it has sub-categories.");
        }

        boolean inUse = false;
        switch (categoryToDelete.getType()) {
            case VOCABULARY:
                inUse = vocabularyRepository.existsByCategoryId(id);
                break;
            case GRAMMAR:
                inUse = grammarRepository.existsByCategoryId(id);
                break;
            case ARTICLE:
                inUse = articleRepository.existsByCategoryId(id);
                break;
            case VIDEO:
                inUse = videoRepository.existsByCategoryId(id);
                break;
        }

        if (inUse) {
            throw new ResourceInUseException(
                    "Cannot delete category because it contains content (e.g., vocabularies, articles).");
        }

        categoryRepository.delete(categoryToDelete);

        logger.info("Successfully deleted category with ID: {}", id);

    }
}
