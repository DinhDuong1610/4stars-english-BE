package com.fourstars.FourStars.config;

import com.fourstars.FourStars.domain.Permission;
import com.fourstars.FourStars.domain.Role;
import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.repository.PermissionRepository;
import com.fourstars.FourStars.repository.RoleRepository;
import com.fourstars.FourStars.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;

    public DataSeeder(RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        logger.info("========== STARTING DATA SEEDING PROCESS ==========");

        Role adminRole = createRoleIfNotFound("ADMIN", "Quản trị viên hệ thống");
        Role premiumRole = createRoleIfNotFound("PREMIUM", "Người dùng trả phí");
        Role userRole = createRoleIfNotFound("USER", "Người dùng thường");

        if (permissionRepository.count() == 0) {
            logger.info("No permissions found, creating them from API endpoints...");
            createPermissions();
        } else {
            logger.info("Permissions already exist, skipping creation.");

        }

        if (adminRole != null && (adminRole.getPermissions() == null || adminRole.getPermissions().isEmpty())) {
            logger.info("Assigning all permissions to ADMIN role...");
            List<Permission> allPermissions = permissionRepository.findAll();
            adminRole.setPermissions(allPermissions);
            roleRepository.save(adminRole);
            logger.info("Assigned {} permissions to ADMIN role.", allPermissions.size());

        } else {
            logger.info("ADMIN role already has permissions, skipping assignment.");

        }

        if (userRepository.count() == 0) {
            logger.info("No users found, creating default ADMIN user...");
            User adminUser = new User();
            adminUser.setName("Admin");
            adminUser.setEmail("admin@gmail.com");
            adminUser.setPassword(passwordEncoder.encode("123456789"));
            adminUser.setActive(true);
            adminUser.setRole(adminRole);
            userRepository.save(adminUser);
            logger.info("Default ADMIN user created.");

        } else {
            logger.info("Users already exist, skipping default user creation.");

        }

        logger.info("========== DATA SEEDING PROCESS FINISHED ==========");
    }

    private Role createRoleIfNotFound(String name, String description) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(name);
                    newRole.setDescription(description);
                    newRole.setActive(true);
                    return roleRepository.save(newRole);
                });
    }

    private void createPermissions() {
        Map<RequestMappingInfo, org.springframework.web.method.HandlerMethod> handlerMethods = this.requestMappingHandlerMapping
                .getHandlerMethods();

        Set<String> uniquePermissions = new HashSet<>();

        for (Map.Entry<RequestMappingInfo, org.springframework.web.method.HandlerMethod> entry : handlerMethods
                .entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();

            Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
            if (methods.isEmpty()) {
                continue;
            }

            Set<String> patterns = new HashSet<>();
            if (mappingInfo.getPathPatternsCondition() != null) {
                patterns = mappingInfo.getPathPatternsCondition().getPatternValues();
            } else if (mappingInfo.getPatternsCondition() != null) {
                patterns = mappingInfo.getPatternsCondition().getPatterns();
            }

            if (patterns.isEmpty()) {
                continue;
            }

            String path = patterns.iterator().next();
            String method = methods.iterator().next().name();

            if (path.startsWith("/error") || path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
                continue;
            }

            String permissionIdentifier = method + ":" + path;
            if (uniquePermissions.contains(permissionIdentifier)) {
                continue;
            }

            Permission permission = new Permission();
            permission.setName("Access " + method + " " + path);
            permission.setApiPath(path);
            permission.setMethod(method);
            permission.setModule(extractModuleFromPath(path));
            permissionRepository.save(permission);
            uniquePermissions.add(permissionIdentifier);
        }
        logger.info("Created {} unique permissions.", uniquePermissions.size());
    }

    private String extractModuleFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length > 3) {
            String moduleName = parts[3];
            return moduleName.substring(0, 1).toUpperCase() + moduleName.substring(1);
        }
        return "General";
    }
}