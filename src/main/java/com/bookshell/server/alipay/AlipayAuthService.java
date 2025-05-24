// 支付宝互联登录服务
package com.bookshell.server.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayApiException;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.bookshell.server.config.AlipayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AlipayAuthService {

    private final AlipayClient alipayClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AlipayConfig alipayConfig;

    @Autowired
    public AlipayAuthService(AlipayClient alipayClient,
                             RedisTemplate<String, Object> redisTemplate,
                             AlipayConfig alipayConfig) {
        this.alipayClient = alipayClient;
        this.redisTemplate = redisTemplate;
        this.alipayConfig = alipayConfig;
    }

    // 生成支付宝登录链接
    public String generateLoginRequest() {
        String requestId = UUID.randomUUID().toString();
        // Redis记录请求登录ID
        redisTemplate.opsForValue().set("alipay:req:" + requestId, "created", 5, TimeUnit.MINUTES);
        // URL编码回调链接
        String encodedRedirectUri = URLEncoder.encode(alipayConfig.getAuthCallbackUrl(), StandardCharsets.UTF_8);
        // 返回登录链接
        return alipayConfig.getGateAuthUrl() + "?app_id=" + alipayConfig.getAuthAppId() + "&scope=auth_user&redirect_uri="
                + encodedRedirectUri + "&state=" + requestId;
    }

    // 返回查询的用户的OpenId及昵称+图片信息（如有）
    public String handleCallback(String authCode, String requestId) throws AlipayApiException {
        // 验证请求ID有效性
        if (!redisTemplate.hasKey("alipay:req:" + requestId)) {
            throw new RuntimeException("Invalid login request");
        }

        // 获取access token
        AlipaySystemOauthTokenRequest tokenRequest = new AlipaySystemOauthTokenRequest();
        tokenRequest.setCode(authCode);
        tokenRequest.setGrantType("authorization_code");
        AlipaySystemOauthTokenResponse tokenResponse = alipayClient.execute(tokenRequest);

        if (!tokenResponse.isSuccess()) {
            throw new RuntimeException("Failed to get access token: " + tokenResponse.getSubMsg());
        }

        // 尝试获取用户信息
        try {
            AlipayUserInfoShareRequest userInfoRequest = new AlipayUserInfoShareRequest();
            AlipayUserInfoShareResponse userInfoResponse = alipayClient.execute(userInfoRequest);

            if (userInfoResponse.isSuccess() && userInfoResponse.getUserId() != null) {
                // 用户授权了信息，返回用户ID
                return userInfoResponse.getUserId();
            }
        } catch (AlipayApiException e) {
            // 捕获异常，继续返回用户ID
            System.err.println("Failed to get user info: " + e.getMessage());
        }

        // 返回用户ID
        return tokenResponse.getUserId();
    }
}
