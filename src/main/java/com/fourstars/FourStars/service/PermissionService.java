package com.fourstars.FourStars.service;

import org.springframework.stereotype.Service;

import com.fourstars.FourStars.domain.Permission;
import com.fourstars.FourStars.repository.PermissionRepository;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public boolean isPermissionExist(Permission permission) {
        return this.permissionRepository.existsByModuleAndApiPathAndMethod(permission.getModule(),
                permission.getApiPath(), permission.getMethod());
    }

    public Permission create(Permission permission) throws DuplicateResourceException {
        if (this.isPermissionExist(permission)) {
            throw new DuplicateResourceException(
                    "Permission with specified module, API path, and method already exists.");
        }

        return this.permissionRepository.save(permission);
    }

    public Permission fetchById(long id) {
        return this.permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
    }

}
