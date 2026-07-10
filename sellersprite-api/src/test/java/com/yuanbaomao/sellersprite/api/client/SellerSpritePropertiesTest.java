package com.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class SellerSpritePropertiesTest {

    @Test
    void shouldExposeSafeDefaults() {
        SellerSpriteProperties properties = new SellerSpriteProperties();

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getBaseUrl()).isEqualTo("https://api.sellersprite.com");
        assertThat(properties.getSecretKey()).isEmpty();
        assertThat(properties.getConnectTimeout()).isEqualTo(Duration.ofSeconds(5));
        assertThat(properties.getReadTimeout()).isEqualTo(Duration.ofSeconds(30));
    }
}
