package com.fourstars.FourStars.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.category.CategoryRequestDTO;
import com.fourstars.FourStars.domain.response.category.CategoryResponseDTO;
import com.fourstars.FourStars.service.CategoryService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
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
}
