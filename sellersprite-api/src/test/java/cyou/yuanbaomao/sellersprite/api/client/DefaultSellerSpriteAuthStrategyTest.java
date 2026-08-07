package cyou.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import cyou.yuanbaomao.base.id.IdGenerator;

class DefaultSellerSpriteAuthStrategyTest {

    private SellerSpriteProperties properties;
    private IdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        properties = new SellerSpriteProperties();
        properties.setSecretKey("test-secret");
        idGenerator = mock(IdGenerator.class);
    }

    @Test
    void shouldApplyOfficialHeadersAndReturnRequestId() {
        when(idGenerator.nextId()).thenReturn("01900000-0000-7000-8000-000000000001");
        DefaultSellerSpriteAuthStrategy strategy = new DefaultSellerSpriteAuthStrategy(properties, idGenerator);
        HttpHeaders headers = new HttpHeaders();

        String requestId = strategy.apply(headers);

        assertThat(requestId).isEqualTo("01900000-0000-7000-8000-000000000001");
        assertThat(headers.getFirst(SellerSpriteHeaders.SECRET_KEY)).isEqualTo("test-secret");
        assertThat(headers.getFirst(SellerSpriteHeaders.REQUEST_ID)).isEqualTo(requestId);
        assertThat(headers.getContentType()).isNotNull();
        assertThat(headers.getContentType().isCompatibleWith(MediaType.APPLICATION_JSON)).isTrue();
        assertThat(headers.getContentType().getCharset()).isEqualTo(StandardCharsets.UTF_8);
        assertThat(headers.getFirst("sign")).isNull();
        assertThat(headers.getFirst("signature")).isNull();
        assertThat(headers.getFirst("timestamp")).isNull();
    }

    @Test
    void shouldGenerateDifferentRequestIdForEveryRequest() {
        when(idGenerator.nextId())
                .thenReturn("01900000-0000-7000-8000-000000000001")
                .thenReturn("01900000-0000-7000-8000-000000000002");
        DefaultSellerSpriteAuthStrategy strategy = new DefaultSellerSpriteAuthStrategy(properties, idGenerator);

        String first = strategy.apply(new HttpHeaders());
        String second = strategy.apply(new HttpHeaders());

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void shouldRejectDisabledIntegration() {
        properties.setEnabled(false);
        DefaultSellerSpriteAuthStrategy strategy = new DefaultSellerSpriteAuthStrategy(properties, idGenerator);

        assertThatThrownBy(() -> strategy.apply(new HttpHeaders()))
                .isInstanceOf(SellerSpriteApiException.class)
                .extracting("code")
                .isEqualTo("S503");
    }

    @Test
    void shouldRejectBlankSecretKey() {
        properties.setSecretKey(" ");
        DefaultSellerSpriteAuthStrategy strategy = new DefaultSellerSpriteAuthStrategy(properties, idGenerator);

        assertThatThrownBy(() -> strategy.apply(new HttpHeaders()))
                .isInstanceOf(SellerSpriteApiException.class)
                .extracting("code")
                .isEqualTo("S503");
    }
}
