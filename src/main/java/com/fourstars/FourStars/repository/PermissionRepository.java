package com.fourstars.FourStars.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fourstars.FourStars.domain.Permission;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    // Permission findById(long id);

    boolean existsByModuleAndApiPathAndMethod(String module, String apiPath, String method);

    boolean existsByModuleAndApiPathAndMethodAndIdNot(String module, String apiPath, String method, Long id);

    List<Permission> findByIdIn(List<Long> permissions);

}