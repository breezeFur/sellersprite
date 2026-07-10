package com.yuanbaomao.sellersprite.system.config;

import com.yuanbaomao.sellersprite.system.auth.config.AuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class SystemModuleConfig {
}
