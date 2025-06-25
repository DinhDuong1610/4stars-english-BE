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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController("clientCategoryController")
@RequestMapping("/api/v1/categories")
@Tag(name = "Client - Category Management API", description = "APIs for managing content categories (Vocabulary, Grammar, etc.)")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Get a category by ID", description = "Retrieves details of a specific category. Use 'deep=true' to fetch its sub-category tree.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved category"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    @ApiMessage("Fetch a category by its ID")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @PathVariable long id,
            @RequestParam(name = "deep", defaultValue = "true") boolean deep) // Thêm param để lấy cây
            throws ResourceNotFoundException {
        CategoryResponseDTO category = categoryService.fetchCategoryById(id, deep);
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "Get top-level categories (paginated)", description = "Retrieves a paginated list of top-level categories (those without a parent).")
    @GetMapping
    @ApiMessage("Fetch all categories with pagination")
    public ResponseEntity<ResultPaginationDTO<CategoryResponseDTO>> getAllCategories(
            Pageable pageable,
            @RequestParam(name = "type", required = false) CategoryType type) {
        ResultPaginationDTO<CategoryResponseDTO> result = categoryService.fetchAllCategories(pageable, type);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get all categories as a tree", description = "Retrieves all categories structured as a tree, with pagination applied only to the top-level items.")
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
