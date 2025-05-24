// 支付宝互联登录服务
package com.bookshell.server.alipay;

import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AlipayAuthService {

    private final AlipayClient alipayAuthClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AlipayConfig alipayConfig;

    @Autowired
    public AlipayAuthService(AlipayClient alipayAuthClient,
                             RedisTemplate<String, Object> redisTemplate,
                             AlipayConfig alipayConfig) {
        this.alipayAuthClient = alipayAuthClient;
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
    public Map handleCallback(String authCode, String requestId) throws AlipayApiException {
        // 验证请求ID有效性
        if (!redisTemplate.hasKey("alipay:req:" + requestId)) {
            throw new RuntimeException("Invalid login request");
        }

        // 获取access token
        AlipaySystemOauthTokenRequest tokenRequest = new AlipaySystemOauthTokenRequest();
        tokenRequest.setCode(authCode);
        tokenRequest.setGrantType("authorization_code");

        // 获取Access_token
        AlipaySystemOauthTokenResponse tokenResponse = alipayAuthClient.execute(tokenRequest);

        if (!tokenResponse.isSuccess()) {
            throw new RuntimeException("获取access_token失败: " + tokenResponse.getSubMsg());
        }

        System.out.println(tokenResponse.getParams());
        // 验签
        boolean signVerified = AlipaySignature.rsaCheckV1(
                tokenResponse.getParams(), // 支付宝返回的所有参数
                alipayConfig.getAuthPublicKey(), // 支付宝公钥
                "UTF-8",
                "RSA2"
        );
        if (!signVerified) {
            throw new RuntimeException("支付宝返回数据签名验证失败");
        }

        // 获取用户信息
        String openid = tokenResponse.getOpenId();
        String accessToken = tokenResponse.getAccessToken();
        if (openid == null) {
            throw new RuntimeException("获取openid失败");
        }
        try{
            AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest();
            AlipayUserInfoShareResponse response = alipayAuthClient.execute(request,accessToken);
            if(!response.isSuccess()){
                throw new RuntimeException("获取用户昵称+头像失败");
            }
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("openid", openid);
            userInfo.put("nickName", response.getNickName());
            userInfo.put("avatar", response.getAvatar());
            return userInfo;
        } catch (Exception e) {
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("openid", openid);
            userInfo.put("nickName", null);
            userInfo.put("avatar", null);
            return userInfo;
        }
    }
}
