package com.fourstars.FourStars.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.Permission;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.permission.PermissionResponseDTO;
import com.fourstars.FourStars.service.PermissionService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.IdInvalidException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
public class PermissionController {
    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping("/permissions")
    @ApiMessage("Create a permission")
    public ResponseEntity<Permission> create(@Valid @RequestBody Permission permission)
            throws IdInvalidException, DuplicateResourceException {

        return ResponseEntity.status(HttpStatus.CREATED).body(this.permissionService.create(permission));
    }

    @GetMapping("/permissions/{id}")
    @ApiMessage("Fetch a permission by ID")
    public ResponseEntity<Permission> getPermissionById(@PathVariable long id) {
        Permission permission = this.permissionService.fetchById(id);

        return ResponseEntity.ok(permission);
    }

    @PutMapping("/permissions/{id}")
    @ApiMessage("Update a permission")
    public ResponseEntity<Permission> update(
            @PathVariable long id,
            @Valid @RequestBody Permission permissionDetails)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        if (permissionDetails.getId() != 0 && permissionDetails.getId() != id) {
            throw new BadRequestException("ID in request body (" + permissionDetails.getId()
                    + ") does not match ID in path variable (" + id + ").");
        }

        permissionDetails.setId(id);
        Permission updatedPermission = this.permissionService.update(permissionDetails);

        return ResponseEntity.ok(updatedPermission);
    }

    @DeleteMapping("/permissions/{id}")
    @ApiMessage("Delete a permission")
    public ResponseEntity<Void> delete(@PathVariable("id") long id) throws ResourceNotFoundException {
        this.permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/permissions")
    @ApiMessage("Fetch all permissions")
    public ResponseEntity<ResultPaginationDTO<PermissionResponseDTO>> getAll(Pageable pageable) {
        ResultPaginationDTO<PermissionResponseDTO> result = this.permissionService.fetchAll(pageable);
        return ResponseEntity.ok(result);
    }
}
