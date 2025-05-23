package com.bookshell.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "xfxh")
public class XfxhConfig {
    private String APIPassword;

    public String getAPIPassword() {
        return APIPassword;
    }

    public void setAPIPassword(String APIPassword) {
        this.APIPassword = APIPassword;
    }
}
