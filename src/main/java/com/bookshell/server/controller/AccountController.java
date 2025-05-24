package com.bookshell.server.controller;

import com.bookshell.server.alipay.AlipayAuthService;
import com.bookshell.server.bookshell.UserService;
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
public class AccountController {

    private final AlipayAuthService alipayAuthService;
    private final UserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SecretKey secretKey;

    @Autowired
    public AccountController(AlipayAuthService alipayAuthService,
                             UserService userService,
                             RedisTemplate<String, Object> redisTemplate,
                             SecretKey secretKey) {
        this.alipayAuthService = alipayAuthService;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.secretKey = secretKey;
    }

    // 创建登录ID
    @GetMapping("/alipay")
    public ResponseEntity<?> startAlipayLogin() {
        String authUrl = alipayAuthService.generateLoginRequest();
        return ResponseEntity.ok(Collections.singletonMap("auth_url", authUrl));
    }

    // 支付宝授权登录回调
    @GetMapping("/callback")
    public ResponseEntity<?> handleAlipayCallback(
            @RequestParam String auth_code,
            @RequestParam String state) {
        try {
            Map<String, String> alipayUserInfo = alipayAuthService.handleCallback(auth_code, state);
            String userId = userService.getUidByAlipay(alipayUserInfo.get("openid"));

            String jwt = generateJwt(userId);
            storeSession(jwt, userId);

            // 返回用户登录会话ID（JWT）
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("user_id", userId);
            responseBody.put("nick_name", alipayUserInfo.get("nickName"));
            responseBody.put("avatar_url", alipayUserInfo.get("avatar"));

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, jwt)
                    .body(responseBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 用户名密码登录
    @PostMapping("/login")
    public ResponseEntity<?> passwordLogin(@RequestBody Map<String, String> credentials) {
        String userId = userService.verifyCredentials(
                credentials.get("username"),
                credentials.get("password")
        );

        String jwt = generateJwt(userId);
        storeSession(jwt, userId);

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwt)
                .body(Collections.singletonMap("user_id", userId));
    }

    // 生成JWT令牌（用户ID、时间、有效期）
    private String generateJwt(String userId) {
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", "user")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(secretKey)
                .compact();
    }

    // redis存储令牌（有效期均为1小时）
    private void storeSession(String jwt, String userId) {
        redisTemplate.opsForValue().set(
                "session:" + jwt,
                userId,
                Duration.ofHours(1)
        );
    }
}