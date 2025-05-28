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

import com.fourstars.FourStars.domain.request.role.RoleRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.role.RoleResponseDTO;
import com.fourstars.FourStars.service.RoleService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    @ApiMessage("Create a new role")
    public ResponseEntity<RoleResponseDTO> createRole(@Valid @RequestBody RoleRequestDTO roleRequestDTO)
            throws DuplicateResourceException {
        RoleResponseDTO createdRole = roleService.createRole(roleRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch a role by its ID")
    public ResponseEntity<RoleResponseDTO> getRoleById(@PathVariable long id) throws ResourceNotFoundException {
        RoleResponseDTO role = roleService.fetchRoleById(id);
        return ResponseEntity.ok(role);
    }

    @PutMapping("/{id}")
    @ApiMessage("Update an existing role")
    public ResponseEntity<RoleResponseDTO> updateRole(
            @PathVariable long id,
            @Valid @RequestBody RoleRequestDTO roleRequestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
        RoleResponseDTO updatedRole = roleService.updateRole(id, roleRequestDTO);

        return ResponseEntity.ok(updatedRole);
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Delete a role")
    public ResponseEntity<Void> deleteRole(@PathVariable long id)
            throws ResourceNotFoundException, DuplicateResourceException {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @ApiMessage("Fetch all roles with pagination")
    public ResponseEntity<ResultPaginationDTO<RoleResponseDTO>> getAllRoles(Pageable pageable) {
        ResultPaginationDTO<RoleResponseDTO> result = roleService.fetchAllRoles(pageable);
        return ResponseEntity.ok(result);
    }
}
