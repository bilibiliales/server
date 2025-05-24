// JWT拦截器服务
package com.bookshell.server.bookshell;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private SecretKey secretKey;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        String jwt = authHeader.substring(7);
        String path = request.getRequestURI();

        try {
            Jws<Claims> claims = Jwts.parser()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(jwt);

            String role = (String) claims.getBody().get("role");
            String keyPrefix = "admin".equals(role) ? "admin_session:" : "session:";

            // 验证Redis中的会话有效性
            if (!redisTemplate.hasKey(keyPrefix + jwt)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired");
                return false;
            }

            request.setAttribute("userId", claims.getBody().getSubject());
            request.setAttribute("role", role);
            return true;
        } catch (JwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return false;
        }
    }
}