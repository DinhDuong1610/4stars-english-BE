package com.fourstars.FourStars.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Permission;
import com.fourstars.FourStars.domain.Role;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.permission.PermissionResponseDTO;
import com.fourstars.FourStars.repository.PermissionRepository;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class PermissionService {
    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public boolean isPermissionExist(Permission permission) {
        return this.permissionRepository.existsByModuleAndApiPathAndMethod(permission.getModule(),
                permission.getApiPath(), permission.getMethod());
    }

    @Transactional
    public Permission create(Permission permission) throws DuplicateResourceException {
        logger.info("Attempting to create a new permission: [Method: {}, Path: {}]", permission.getMethod(),
                permission.getApiPath());

        if (this.isPermissionExist(permission)) {
            throw new DuplicateResourceException(
                    "Permission with specified module, API path, and method already exists.");
        }
        Permission savedPermission = this.permissionRepository.save(permission);
        logger.info("Successfully created new permission with ID: {}", savedPermission.getId());
        return savedPermission;
    }

    @Transactional(readOnly = true)
    public Permission fetchById(long id) {
        logger.debug("Request to fetch permission with ID: {}", id);

        return this.permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
    }

    @Transactional
    public Permission update(Permission permissionDetails)
            throws ResourceNotFoundException, DuplicateResourceException {
        logger.info("Attempting to update permission with ID: {}", permissionDetails.getId());

        Permission permissionDB = this.permissionRepository.findById(permissionDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permission not found with id: " + permissionDetails.getId()));

        boolean uniqueFieldsChanged = !permissionDB.getModule().equals(permissionDetails.getModule()) ||
                !permissionDB.getApiPath().equals(permissionDetails.getApiPath()) ||
                !permissionDB.getMethod().equals(permissionDetails.getMethod());

        if (uniqueFieldsChanged) {
            if (this.permissionRepository.existsByModuleAndApiPathAndMethodAndIdNot(
                    permissionDetails.getModule(),
                    permissionDetails.getApiPath(),
                    permissionDetails.getMethod(),
                    permissionDetails.getId())) {
                throw new DuplicateResourceException(
                        "Another permission already exists with the specified module, API path, and method.");
            }
        }

        permissionDB.setName(permissionDetails.getName());
        permissionDB.setModule(permissionDetails.getModule());
        permissionDB.setApiPath(permissionDetails.getApiPath());
        permissionDB.setMethod(permissionDetails.getMethod());

        Permission updatedPermission = this.permissionRepository.save(permissionDB);
        logger.info("Successfully updated permission with ID: {}", updatedPermission.getId());
        return updatedPermission;
    }

    @Transactional
    public void delete(long id) throws ResourceNotFoundException {
        logger.info("Attempting to delete permission with ID: {}", id);

        Permission permissionToDelete = this.permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));

        if (permissionToDelete.getRoles() != null && !permissionToDelete.getRoles().isEmpty()) {
            List<Role> associatedRoles = new ArrayList<>(permissionToDelete.getRoles());
            for (Role role : associatedRoles) {
                if (role.getPermissions() != null) {
                    role.getPermissions().remove(permissionToDelete);
                }
            }
            permissionToDelete.getRoles().clear();
        }

        this.permissionRepository.delete(permissionToDelete);
        logger.info("Successfully deleted permission with ID: {}", id);

    }

    public ResultPaginationDTO<PermissionResponseDTO> fetchAll(Pageable pageable) {
        logger.debug("Request to fetch all permissions for page: {}, size: {}", pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Permission> pagePermission = this.permissionRepository.findAll(pageable);

        List<PermissionResponseDTO> permissionDTOs = pagePermission.getContent().stream()
                .map(this::convertToPermissionResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pagePermission.getTotalPages(),
                pagePermission.getTotalElements());

        return new ResultPaginationDTO<>(meta, permissionDTOs);
    }

    @Transactional(readOnly = true)
    private PermissionResponseDTO convertToPermissionResponseDTO(Permission permission) {
        PermissionResponseDTO dto = new PermissionResponseDTO();
        dto.setId(permission.getId());
        dto.setName(permission.getName());
        dto.setApiPath(permission.getApiPath());
        dto.setMethod(permission.getMethod());
        dto.setModule(permission.getModule());

        return dto;
    }
}
