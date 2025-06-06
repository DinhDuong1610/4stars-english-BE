package com.fourstars.FourStars.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Transactional
    public CategoryResponseDTO updateCategory(long id, CategoryRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
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
        return convertToCategoryResponseDTO(updatedCategory, false);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<CategoryResponseDTO> fetchAllCategories(Pageable pageable, CategoryType type) {
        // Tạo một Specification để xây dựng truy vấn động
        Specification<Category> spec = (root, query, criteriaBuilder) -> {
            // Luôn lọc để chỉ lấy các danh mục không có cha (cấp cao nhất)
            Predicate topLevelPredicate = criteriaBuilder.isNull(root.get("parentCategory"));

            // Nếu có tham số 'type' được truyền vào, thêm điều kiện lọc
            if (type != null) {
                Predicate typePredicate = criteriaBuilder.equal(root.get("type"), type);
                return criteriaBuilder.and(topLevelPredicate, typePredicate);
            }

            // Nếu không, chỉ trả về các danh mục cấp cao nhất
            return topLevelPredicate;
        };

        Page<Category> pageCategory = categoryRepository.findAll(spec, pageable);
        List<CategoryResponseDTO> categoryDTOs = pageCategory.getContent().stream()
                .map(cat -> convertToCategoryResponseDTO(cat, false)) // Lấy danh sách nông
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pageCategory.getTotalPages(),
                pageCategory.getTotalElements());
        return new ResultPaginationDTO<>(meta, categoryDTOs);
    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<CategoryResponseDTO> fetchAllCategoriesAsTree(CategoryType type, Pageable pageable) {
        // 1. Lấy tất cả danh mục khớp điều kiện lọc (nếu có)
        Specification<Category> spec = (root, query, cb) -> {
            if (type != null) {
                return cb.equal(root.get("type"), type);
            }
            return cb.conjunction();
        };
        List<Category> allCategories = categoryRepository.findAll(spec, Sort.by(Sort.Direction.ASC, "orderIndex"));

        // 2. Chuyển đổi tất cả entity sang DTO và đưa vào Map để tra cứu nhanh
        Map<Long, CategoryResponseDTO> categoryMap = allCategories.stream()
                .map(cat -> convertToCategoryResponseDTO(cat, false)) // Chuyển đổi dạng nông
                .collect(Collectors.toMap(
                        CategoryResponseDTO::getId,
                        Function.identity(),
                        (v1, v2) -> v1,
                        LinkedHashMap::new));

        // 3. Xây dựng cấu trúc cây và tách ra danh sách các danh mục gốc (root)
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

        // 4. Thực hiện phân trang thủ công trên danh sách các danh mục gốc đã được xây
        // dựng
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

        // 5. Tạo đối tượng Meta cho việc phân trang
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                currentPage + 1,
                pageSize,
                (int) Math.ceil((double) totalRootElements / pageSize), // Tổng số trang của danh mục gốc
                totalRootElements // Tổng số danh mục gốc
        );

        return new ResultPaginationDTO<>(meta, paginatedRootCategories);
    }

    @Transactional
    public void deleteCategory(long id) throws ResourceNotFoundException, ResourceInUseException {
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
    }
}
