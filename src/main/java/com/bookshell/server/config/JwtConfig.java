package com.bookshell.server.config;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.crypto.SecretKey;
import java.util.Base64;

@Configuration
public class JwtConfig {

    private final StringRedisTemplate redisTemplate;

    @Autowired
    public JwtConfig(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Bean
    public SecretKey jwtSecretKey() {
        // JWT密钥键值为“jwtSecret”
        String jwtSecretKey = redisTemplate.opsForValue().get("jwtSecret");

        if (jwtSecretKey == null) {
            // 如果没有找到密钥，生成一个新的密钥并存储到 Redis
            SecretKey newKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            jwtSecretKey = Base64.getEncoder().encodeToString(newKey.getEncoded());
            redisTemplate.opsForValue().set("jwtSecret", jwtSecretKey);
        }

        // 解码 Base64 编码的密钥
        byte[] decodedKey = Base64.getDecoder().decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(decodedKey);
    }
}