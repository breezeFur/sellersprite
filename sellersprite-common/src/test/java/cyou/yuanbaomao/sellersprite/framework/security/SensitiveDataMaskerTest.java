package cyou.yuanbaomao.sellersprite.framework.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveDataMaskerTest {

    @Test
    void shouldMaskStructuredAndHeaderSecretsCaseInsensitively() {
        String source = "{\"password\":\"secret-value\",\"Authorization\":\"Bearer access-token\","
                + "\"apiKey\":\"sk-private\",\"username\":\"yuanbao\"}";

        String masked = SensitiveDataMasker.mask(source);

        assertThat(masked).contains("yuanbao", SensitiveDataMasker.REDACTED_VALUE);
        assertThat(masked).doesNotContain("secret-value", "access-token", "sk-private");
    }

    @Test
    void shouldMaskQueryStyleSecrets() {
        String source = "username=yuanbao&refresh_token=refresh-secret&cookie=session-secret";

        String masked = SensitiveDataMasker.mask(source);

        assertThat(masked).contains("username=yuanbao");
        assertThat(masked).doesNotContain("refresh-secret", "session-secret");
    }

    @Test
    void shouldReturnTruncationMetadataAfterMasking() {
        SensitiveDataMasker.MaskedText result = SensitiveDataMasker.maskAndTruncate(
                "password=secret&prompt=abcdefghijklmnopqrstuvwxyz", 24);

        assertThat(result.content()).hasSize(24).doesNotContain("secret");
        assertThat(result.truncated()).isTrue();
    }

    @Test
    void shouldHandleBlankText() {
        SensitiveDataMasker.MaskedText result = SensitiveDataMasker.maskAndTruncate(" ", 20);

        assertThat(result.content()).isEmpty();
        assertThat(result.truncated()).isFalse();
    }
}
