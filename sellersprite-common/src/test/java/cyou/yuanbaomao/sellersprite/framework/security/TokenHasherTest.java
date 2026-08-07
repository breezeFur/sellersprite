package cyou.yuanbaomao.sellersprite.framework.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenHasherTest {

    @Test
    void shouldHashTokenToSha256Hex() {
        TokenHasher hasher = new TokenHasher();

        String hash = hasher.sha256("token");

        assertThat(hash).hasSize(64);
        assertThat(hash).isEqualTo(hasher.sha256("token"));
    }
}
