package com.fourstars.FourStars.config;

import com.fourstars.FourStars.domain.response.permission.UserPermissionDTO;
import com.fourstars.FourStars.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.Serializable;
import java.util.Set;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserService userService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public CustomPermissionEvaluator(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return false;
        }

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();

        UserPermissionDTO permissionData = userService.getPermissionsByEmail(authentication.getName());

        if (permissionData == null) {
            return false;
        }

        if ("ADMIN".equalsIgnoreCase(permissionData.getRoleName())) {
            return true;
        }

        String requiredPermission = requestMethod.toUpperCase() + ":" + requestPath;

        Set<String> userPermissions = permissionData.getPermissions();
        if (userPermissions == null) {
            return false;
        }

        for (String p : userPermissions) {
            String[] parts = p.split(":", 2);
            String dbMethod = parts[0];
            String dbPath = parts[1];

            if (dbMethod.equalsIgnoreCase(requestMethod) && pathMatcher.match(dbPath, requestPath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        return false;
    }
}