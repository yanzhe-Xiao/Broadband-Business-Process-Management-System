package com.xyz.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
/**
 * <p>Package Name: com.xyz.utils </p>
 * <p>Description:  </p>
 * <p>Create Time: 2025/9/10 </p>
 *
 * @author <a href="https://github.com/yanzhe-xiao">YANZHE XIAO</a>
 * @version 1.0
 * @since
 */
public class UserAuth {
    public static String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof Jwt jwt) {
            // 从 JWT 的 subject(sub) 拿用户名
            return jwt.getSubject();
        }
        // 如果以后换成 UserDetailsService，这里 principal 就会是 UserDetails
        return auth.getName();
    }
}
