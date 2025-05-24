package com.bookshell.server.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
@Component
@ConfigurationProperties(prefix = "alipay")
public class AlipayConfig {
    private String appId;
    private String appPrivateKey;
    private String alipayPublicKey;
    private String notifyUrl;
    private String returnUrl;
    private String gatewayUrl;
    private String authAppId;
    private String authPrivateKey;
    private String authPublicKey;
    private String authCallbackUrl;
    private String gateAuthUrl;
    private String shareAuthUrl;

    @Bean
    public AlipayClient alipayClient() {
        return new DefaultAlipayClient(
                gatewayUrl,
                appId,
                appPrivateKey,
                "JSON",
                "UTF-8",
                alipayPublicKey,
                "RSA2"
        );
    }

    @Bean
    public AlipayClient alipayAuthClient() {
        return new DefaultAlipayClient(
                shareAuthUrl,
                authAppId,
                authPrivateKey,
                "JSON",
                "UTF-8",
                authPublicKey,
                "RSA2"
        );
    }


    public String getAppId() {
        return appId;
    }

    public String getAppPrivateKey() {
        return appPrivateKey;
    }

    public String getAlipayPublicKey() {
        return alipayPublicKey;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public void setAppPrivateKey(String appPrivateKey) {
        this.appPrivateKey = appPrivateKey;
    }

    public void setAlipayPublicKey(String alipayPublicKey) {
        this.alipayPublicKey = alipayPublicKey;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public void setGatewayUrl(String gatewayUrl) {
        this.gatewayUrl = gatewayUrl;
    }

    public String getAuthCallbackUrl() {
        return authCallbackUrl;
    }

    public void setAuthCallbackUrl(String authCallbackUrl) {
        this.authCallbackUrl = authCallbackUrl;
    }

    public String getGateAuthUrl() {
        return gateAuthUrl;
    }

    public void setGateAuthUrl(String gateAuthUrl) {
        this.gateAuthUrl = gateAuthUrl;
    }

    public String getAuthAppId() {
        return authAppId;
    }

    public void setAuthAppId(String authAppId) {
        this.authAppId = authAppId;
    }

    public String getAuthPrivateKey() {
        return authPrivateKey;
    }

    public void setAuthPrivateKey(String authPrivateKey) {
        this.authPrivateKey = authPrivateKey;
    }

    public String getAuthPublicKey() {
        return authPublicKey;
    }

    public void setAuthPublicKey(String authPublicKey) {
        this.authPublicKey = authPublicKey;
    }

    public String getShareAuthUrl() {
        return shareAuthUrl;
    }

    public void setShareAuthUrl(String shareAuthUrl) {
        this.shareAuthUrl = shareAuthUrl;
    }
}
