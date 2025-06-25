package com.fourstars.FourStars.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fourstars.FourStars.domain.Permission;
import com.fourstars.FourStars.domain.Role;
import com.fourstars.FourStars.domain.request.role.RoleRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.permission.PermissionResponseDTO;
import com.fourstars.FourStars.domain.response.role.RoleResponseDTO;
import com.fourstars.FourStars.repository.PermissionRepository;
import com.fourstars.FourStars.repository.RoleRepository;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class RoleService {
    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    private RoleResponseDTO convertToRoleResponseDTO(Role role) {
        RoleResponseDTO dto = new RoleResponseDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setDescription(role.getDescription());
        dto.setActive(role.isActive());
        dto.setCreatedAt(role.getCreatedAt());
        dto.setUpdatedAt(role.getUpdatedAt());
        dto.setCreatedBy(role.getCreatedBy());
        dto.setUpdatedBy(role.getUpdatedBy());

        if (role.getPermissions() != null) {
            List<PermissionResponseDTO> permissionDTOs = role.getPermissions().stream()
                    .map(permission -> {
                        PermissionResponseDTO pDto = new PermissionResponseDTO();
                        pDto.setId(permission.getId());
                        pDto.setName(permission.getName());
                        pDto.setApiPath(permission.getApiPath());
                        pDto.setMethod(permission.getMethod());
                        pDto.setModule(permission.getModule());
                        return pDto;
                    })
                    .collect(Collectors.toList());
            dto.setPermissions(permissionDTOs);
        }

        return dto;
    }

    @Transactional
    public RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO) throws DuplicateResourceException {
        logger.info("Request to create new role with name: '{}'", roleRequestDTO.getName());

        if (roleRepository.existsByName(roleRequestDTO.getName())) {
            throw new DuplicateResourceException("Role name '" + roleRequestDTO.getName() + "' already exists.");
        }

        Role role = new Role();
        role.setName(roleRequestDTO.getName());
        role.setDescription(roleRequestDTO.getDescription());
        role.setActive(roleRequestDTO.isActive());

        if (roleRequestDTO.getPermissionIds() != null && !roleRequestDTO.getPermissionIds().isEmpty()) {
            logger.debug("Assigning {} permissions to new role '{}'", roleRequestDTO.getPermissionIds().size(),
                    roleRequestDTO.getName());
            List<Permission> permissions = permissionRepository.findAllById(roleRequestDTO.getPermissionIds());
            role.setPermissions(permissions);
        }

        Role savedRole = roleRepository.save(role);
        logger.info("Successfully created new role with ID: {}", savedRole.getId());

        return convertToRoleResponseDTO(savedRole);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#id")
    public RoleResponseDTO fetchRoleById(long id) throws ResourceNotFoundException {
        logger.debug("Request to fetch role by ID: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
        return convertToRoleResponseDTO(role);
    }

    @Transactional(readOnly = true)
    public Role getRoleEntityById(long id) throws ResourceNotFoundException {
        logger.debug("Request to fetch role ENTITY by ID: {}", id);

        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    @Transactional
    @CacheEvict(value = "roles", key = "#id")
    public RoleResponseDTO updateRole(long id, RoleRequestDTO roleRequestDTO)
            throws ResourceNotFoundException, DuplicateResourceException {
        logger.info("Request to update role with ID: {}", id);

        Role roleDB = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (!roleDB.getName().equals(roleRequestDTO.getName())
                && roleRepository.existsByNameAndIdNot(roleRequestDTO.getName(), id)) {
            throw new DuplicateResourceException(
                    "Role name '" + roleRequestDTO.getName() + "' already exists for another role.");
        }

        roleDB.setName(roleRequestDTO.getName());
        roleDB.setDescription(roleRequestDTO.getDescription());
        roleDB.setActive(roleRequestDTO.isActive());

        if (roleRequestDTO.getPermissionIds() != null) {
            logger.debug("Updating permissions for role ID: {}. New permission count: {}", id,
                    roleRequestDTO.getPermissionIds().size());
            List<Permission> newPermissions = permissionRepository.findAllById(roleRequestDTO.getPermissionIds());
            roleDB.setPermissions(newPermissions);
        } else {
            logger.debug("Clearing all permissions for role ID: {}", id);
            roleDB.getPermissions().clear();
        }

        Role updatedRole = roleRepository.save(roleDB);
        logger.info("Successfully updated role with ID: {}", updatedRole.getId());

        return convertToRoleResponseDTO(updatedRole);
    }

    @Transactional
    @CacheEvict(value = "roles", key = "#id")
    public void deleteRole(long id) throws ResourceNotFoundException, DuplicateResourceException {
        logger.info("Request to delete role with ID: {}", id);
        Role roleToDelete = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        if (roleToDelete.getUsers() != null && !roleToDelete.getUsers().isEmpty()) {
            throw new DuplicateResourceException(
                    "Role '" + roleToDelete.getName() + "' is currently assigned to users and cannot be deleted.");
        }
        logger.debug("Clearing all permissions from role {} before deletion.", id);

        roleToDelete.getPermissions().clear();

        roleRepository.delete(roleToDelete);
        logger.info("Successfully deleted role with ID: {}", id);

    }

    @Transactional(readOnly = true)
    public ResultPaginationDTO<RoleResponseDTO> fetchAllRoles(Pageable pageable) {
        logger.debug("Request to fetch all roles, page: {}, size: {}", pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Role> pageRole = roleRepository.findAll(pageable);
        List<RoleResponseDTO> roleDTOs = pageRole.getContent().stream()
                .map(this::convertToRoleResponseDTO)
                .collect(Collectors.toList());

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta(
                pageable.getPageNumber() + 1,
                pageable.getPageSize(),
                pageRole.getTotalPages(),
                pageRole.getTotalElements());
        return new ResultPaginationDTO<>(meta, roleDTOs);
    }
}
