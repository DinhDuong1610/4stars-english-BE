package com.fourstars.FourStars.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Category;
import com.fourstars.FourStars.domain.request.category.CategoryRequestDTO;
import com.fourstars.FourStars.domain.response.category.CategoryResponseDTO;
import com.fourstars.FourStars.repository.ArticleRepository;
import com.fourstars.FourStars.repository.CategoryRepository;
import com.fourstars.FourStars.repository.GrammarRepository;
import com.fourstars.FourStars.repository.VideoRepository;
import com.fourstars.FourStars.repository.VocabularyRepository;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class CategoryService {
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
    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
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
        return convertToCategoryResponseDTO(savedCategory, false);
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO fetchCategoryById(long id, boolean deep) throws ResourceNotFoundException {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return convertToCategoryResponseDTO(category, deep);
    }

}
