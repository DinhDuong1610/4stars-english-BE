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

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/categories")
// @PreAuthorize("hasAuthority('ROLE_ADMIN')") // Bảo vệ toàn bộ controller cho
// admin
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ApiMessage("Create a new category")
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
        CategoryResponseDTO newCategory = categoryService.createCategory(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
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

    @PutMapping("/{id}")
    @ApiMessage("Update an existing category")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable long id,
            @Valid @RequestBody CategoryRequestDTO requestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        CategoryResponseDTO updatedCategory = categoryService.updateCategory(id, requestDTO);
        return ResponseEntity.ok(updatedCategory);
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

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a category")
    public ResponseEntity<Void> deleteCategory(@PathVariable long id)
            throws ResourceNotFoundException, ResourceInUseException {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
