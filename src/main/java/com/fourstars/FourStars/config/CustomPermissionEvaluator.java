package com.fourstars.FourStars.config;

import com.fourstars.FourStars.domain.Permission;
import com.fourstars.FourStars.domain.User;
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
import java.util.List;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final UserService userService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public CustomPermissionEvaluator(@Lazy UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // Lấy HttpServletRequest từ context của Spring
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();

        String requestPath = request.getRequestURI();
        String requestMethod = request.getMethod();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return false;
        }

        User user = userService.getUserEntityByEmail(authentication.getName());
        if (user == null || user.getRole() == null) {
            return false;
        }

        if ("ADMIN".equalsIgnoreCase(user.getRole().getName())) {
            return true;
        }

        List<Permission> userPermissions = user.getRole().getPermissions();
        if (userPermissions == null) {
            return false;
        }

        for (Permission p : userPermissions) {
            if (p.getMethod().equalsIgnoreCase(requestMethod) && pathMatcher.match(p.getApiPath(), requestPath)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
            Object permission) {
        return false; // Không dùng
    }
}