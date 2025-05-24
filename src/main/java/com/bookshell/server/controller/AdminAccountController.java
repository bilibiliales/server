package com.bookshell.server.controller;

import com.bookshell.server.alipay.AlipayAuthService;
import com.bookshell.server.bookshell.AdminService;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AdminAccountController {

    private final AlipayAuthService alipayAuthService;
    private final AdminService adminService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecretKey secretKey;

    @Autowired
    public AdminAccountController(AlipayAuthService alipayAuthService,
                                  AdminService adminService,
                                  RedisTemplate<String, Object> redisTemplate,
                                  SecretKey secretKey) {
        this.alipayAuthService = alipayAuthService;
        this.adminService = adminService;
        this.redisTemplate = redisTemplate;
        this.secretKey = secretKey;
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@RequestBody Map<String, String> credentials) {
        String adminId = adminService.verifyAdminCredentials(
                credentials.get("username"),
                credentials.get("password")
        );

        String jwt = generateJwt(adminId);
        storeSession(jwt, adminId);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Collections.singletonMap("admin_id", adminId));
    }

    // 生成JWT令牌（管理员ID、时间、有效期）
    private String generateJwt(String adminId) {
        return Jwts.builder()
                .setSubject(adminId)
                .claim("role", "admin")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000)) // 1小时有效期
                .signWith(secretKey)
                .compact();
    }

    // redis存储令牌
    private void storeSession(String jwt, String adminId) {
        redisTemplate.opsForValue().set(
                "admin_session:" + jwt,
                adminId,
                Duration.ofHours(1)
        );
    }
}