package com.fourstars.FourStars.controller.admin;

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
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.role.RoleRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.role.RoleResponseDTO;
import com.fourstars.FourStars.service.RoleService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/roles")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Role Management API", description = "APIs for managing user roles and their assigned permissions")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @Operation(summary = "Create a new role", description = "Creates a new role and assigns permissions based on a list of permission IDs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Role created successfully"),
            @ApiResponse(responseCode = "409", description = "Role name already exists")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Create a new role")
    public ResponseEntity<RoleResponseDTO> createRole(@Valid @RequestBody RoleRequestDTO roleRequestDTO)
            throws DuplicateResourceException {
        RoleResponseDTO createdRole = roleService.createRole(roleRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @Operation(summary = "Get a role by ID", description = "Retrieves details of a specific role, including its full list of permissions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the role"),
            @ApiResponse(responseCode = "404", description = "Role not found")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch a role by its ID")
    public ResponseEntity<RoleResponseDTO> getRoleById(@PathVariable long id) throws ResourceNotFoundException {
        RoleResponseDTO role = roleService.fetchRoleById(id);
        return ResponseEntity.ok(role);
    }

    @Operation(summary = "Update an existing role", description = "Updates a role's details and overwrites its permissions with a new list of permission IDs.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "409", description = "Role name already exists for another role")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Update an existing role")
    public ResponseEntity<RoleResponseDTO> updateRole(
            @PathVariable long id,
            @Valid @RequestBody RoleRequestDTO roleRequestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
        RoleResponseDTO updatedRole = roleService.updateRole(id, roleRequestDTO);

        return ResponseEntity.ok(updatedRole);
    }

    @Operation(summary = "Delete a role", description = "Deletes a role. Fails if the role is currently assigned to any users.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Role not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete role, it is in use by users")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Delete a role")
    public ResponseEntity<Void> deleteRole(@PathVariable long id)
            throws ResourceNotFoundException, DuplicateResourceException {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all roles with pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch all roles with pagination")
    public ResponseEntity<ResultPaginationDTO<RoleResponseDTO>> getAllRoles(Pageable pageable) {
        ResultPaginationDTO<RoleResponseDTO> result = roleService.fetchAllRoles(pageable);
        return ResponseEntity.ok(result);
    }
}
