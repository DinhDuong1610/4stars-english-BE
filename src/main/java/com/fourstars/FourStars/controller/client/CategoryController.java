package com.fourstars.FourStars.controller.client;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.category.CategoryResponseDTO;
import com.fourstars.FourStars.service.CategoryService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.constant.CategoryType;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@RestController("clientCategoryController")
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch a category by its ID")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @PathVariable long id,
            @RequestParam(name = "deep", defaultValue = "true") boolean deep) // Thêm param để lấy cây
            throws ResourceNotFoundException {
        CategoryResponseDTO category = categoryService.fetchCategoryById(id, deep);
        return ResponseEntity.ok(category);
    }

    @GetMapping
    @ApiMessage("Fetch all categories with pagination")
    public ResponseEntity<ResultPaginationDTO<CategoryResponseDTO>> getAllCategories(
            Pageable pageable,
            @RequestParam(name = "type", required = false) CategoryType type) {
        ResultPaginationDTO<CategoryResponseDTO> result = categoryService.fetchAllCategories(pageable, type);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/tree")
    @ApiMessage("Fetch all categories as a paginated tree structure")
    public ResponseEntity<ResultPaginationDTO<CategoryResponseDTO>> getAllCategoriesAsTree(
            @RequestParam(name = "type", required = false) CategoryType type,
            Pageable pageable) {
        ResultPaginationDTO<CategoryResponseDTO> categoryTree = categoryService.fetchAllCategoriesAsTree(type,
                pageable);
        return ResponseEntity.ok(categoryTree);
    }
}
