package com.yuanbaomao.sellersprite.system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import java.time.Clock;

@Configuration
public class SystemModuleConfig {

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}
