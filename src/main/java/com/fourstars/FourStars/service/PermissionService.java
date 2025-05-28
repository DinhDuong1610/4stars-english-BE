package com.fourstars.FourStars.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public Permission create(Permission permission) throws DuplicateResourceException {
        if (this.isPermissionExist(permission)) {
            throw new DuplicateResourceException(
                    "Permission with specified module, API path, and method already exists.");
        }

        return this.permissionRepository.save(permission);
    }

    @Transactional(readOnly = true)
    public Permission fetchById(long id) {
        return this.permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));
    }

    @Transactional
    public Permission update(Permission permissionDetails)
            throws ResourceNotFoundException, DuplicateResourceException {
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

        return this.permissionRepository.save(permissionDB);
    }

}
