package com.fourstars.FourStars.controller.admin;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.category.CategoryRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.category.CategoryResponseDTO;
import com.fourstars.FourStars.service.CategoryService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.constant.CategoryType;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceInUseException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/categories")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Category Management API", description = "APIs for managing content categories (Vocabulary, Grammar, etc.)")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "Create a new category", description = "Creates a new content category. Can be a top-level or a sub-category.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "409", description = "Category with the same name, type, and parent already exists")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Create a new category")
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
        CategoryResponseDTO newCategory = categoryService.createCategory(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
    }

    @Operation(summary = "Get a category by ID", description = "Retrieves details of a specific category. Use 'deep=true' to fetch its sub-category tree.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved category"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch a category by its ID")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(
            @PathVariable long id,
            @RequestParam(name = "deep", defaultValue = "true") boolean deep) // Thêm param để lấy cây
            throws ResourceNotFoundException {
        CategoryResponseDTO category = categoryService.fetchCategoryById(id, deep);
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "Update an existing category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request (e.g., setting a category as its own parent)"),
            @ApiResponse(responseCode = "404", description = "Category or parent category not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Update an existing category")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable long id,
            @Valid @RequestBody CategoryRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        CategoryResponseDTO updatedCategory = categoryService.updateCategory(id, requestDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    @Operation(summary = "Get top-level categories (paginated)", description = "Retrieves a paginated list of top-level categories (those without a parent).")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch all categories with pagination")
    public ResponseEntity<ResultPaginationDTO<CategoryResponseDTO>> getAllCategories(
            Pageable pageable,
            @RequestParam(name = "type", required = false) CategoryType type) {
        ResultPaginationDTO<CategoryResponseDTO> result = categoryService.fetchAllCategories(pageable, type);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get all categories as a tree", description = "Retrieves all categories structured as a tree, with pagination applied only to the top-level items.")
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch all categories as a paginated tree structure")
    public ResponseEntity<ResultPaginationDTO<CategoryResponseDTO>> getAllCategoriesAsTree(
            @RequestParam(name = "type", required = false) CategoryType type,
            Pageable pageable) {
        ResultPaginationDTO<CategoryResponseDTO> categoryTree = categoryService.fetchAllCategoriesAsTree(type,
                pageable);
        return ResponseEntity.ok(categoryTree);
    }

    @Operation(summary = "Delete a category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete category because it has sub-categories or contains content")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Delete a category")
    public ResponseEntity<Void> deleteCategory(@PathVariable long id)
            throws ResourceNotFoundException, ResourceInUseException {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
