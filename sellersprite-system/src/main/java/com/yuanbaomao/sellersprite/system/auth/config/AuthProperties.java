package com.yuanbaomao.sellersprite.system.auth.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "sellersprite.auth")
public class AuthProperties {

    private Long accessTokenExpireMinutes = 720L;

    private Long refreshTokenExpireDays = 14L;

    private String refreshCookieName = "sellersprite_refresh_token";

    private boolean refreshCookieSecure;

    private List<String> publicPaths = new ArrayList<>();
}
