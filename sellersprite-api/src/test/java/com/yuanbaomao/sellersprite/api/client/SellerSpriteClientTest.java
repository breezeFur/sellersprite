package com.yuanbaomao.sellersprite.api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.yuanbaomao.base.id.IdGenerator;

import lombok.Data;

class SellerSpriteClientTest {

    private static final ParameterizedTypeReference<SellerSpriteResponse<TestPayload>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private MockRestServiceServer server;
    private SellerSpriteClient client;

    @BeforeEach
    void setUp() {
        SellerSpriteProperties properties = new SellerSpriteProperties();
        properties.setSecretKey("test-secret");
        IdGenerator idGenerator = mock(IdGenerator.class);
        when(idGenerator.nextId()).thenReturn("01900000-0000-7000-8000-000000000001");
        RestClient.Builder builder = RestClient.builder().baseUrl(properties.getBaseUrl());
        server = MockRestServiceServer.bindTo(builder).build();
        client = new SellerSpriteClient(builder.build(),
                new DefaultSellerSpriteAuthStrategy(properties, idGenerator));
    }

    @Test
    void shouldExecuteAuthenticatedGetAndUnwrapData() {
        server.expect(requestTo("https://api.sellersprite.com/v1/visits"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(SellerSpriteHeaders.SECRET_KEY, "test-secret"))
                .andExpect(header(SellerSpriteHeaders.REQUEST_ID,
                        "01900000-0000-7000-8000-000000000001"))
                .andRespond(withSuccess(
                        "{\"code\":\"OK\",\"message\":\"成功\",\"data\":{\"value\":100}}",
                        MediaType.APPLICATION_JSON));

        TestPayload result = client.get("/v1/visits", RESPONSE_TYPE);

        assertThat(result.getValue()).isEqualTo(100L);
        server.verify();
    }

    @Test
    void shouldExecuteAuthenticatedPostAndSerializeBody() {
        server.expect(requestTo("https://api.sellersprite.com/v1/ocr"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(SellerSpriteHeaders.SECRET_KEY, "test-secret"))
                .andExpect(content().json("{\"value\":1}"))
                .andRespond(withSuccess(
                        "{\"code\":\"OK\",\"message\":\"成功\",\"data\":{\"value\":1}}",
                        MediaType.APPLICATION_JSON));

        TestPayload request = new TestPayload();
        request.setValue(1L);
        TestPayload result = client.post("/v1/ocr", request, RESPONSE_TYPE);

        assertThat(result.getValue()).isEqualTo(1L);
        server.verify();
    }

    @Test
    void shouldExpandPathVariablesAndEncodeQueryParameters() {
        server.expect(requestTo(
                "https://api.sellersprite.com/v1/asin/US/B0TEST?historyDate=202507&types=price&types=bsr"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        "{\"code\":\"OK\",\"message\":\"成功\",\"data\":{\"value\":1}}",
                        MediaType.APPLICATION_JSON));
        MultiValueMap<String, String> query = new LinkedMultiValueMap<>();
        query.add("historyDate", "202507");
        query.add("types", "price");
        query.add("types", "bsr");

        TestPayload result = client.get(SellerSpriteOperation.ASIN_DETAIL,
                Map.of("marketplace", "US", "asin", "B0TEST"), query, RESPONSE_TYPE);

        assertThat(result.getValue()).isEqualTo(1L);
        server.verify();
    }

    @Test
    void shouldUseOperationPathForPost() {
        server.expect(requestTo("https://api.sellersprite.com/v1/product/competitor-lookup"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(
                        "{\"code\":\"OK\",\"message\":\"成功\",\"data\":{\"value\":1}}",
                        MediaType.APPLICATION_JSON));

        TestPayload result = client.post(SellerSpriteOperation.PRODUCT_COMPETITOR_LOOKUP,
                new TestPayload(), RESPONSE_TYPE);

        assertThat(result.getValue()).isEqualTo(1L);
        server.verify();
    }

    @Test
    void shouldExecuteMultipartOperation() {
        server.expect(requestTo("https://api.sellersprite.com/v1/ocr"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(request -> assertThat(request.getHeaders().getContentType())
                        .isNotNull()
                        .matches(contentType -> contentType.isCompatibleWith(MediaType.MULTIPART_FORM_DATA)))
                .andExpect(content().string(containsString("name=\"type\"")))
                .andExpect(content().string(containsString("name=\"image\"")))
                .andRespond(withSuccess(
                        "{\"code\":\"OK\",\"message\":\"成功\",\"data\":{\"value\":1}}",
                        MediaType.APPLICATION_JSON));
        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("type", "1");
        parts.add("image", new ByteArrayResource(new byte[] {1, 2}) {
            @Override
            public String getFilename() {
                return "image.png";
            }
        });

        TestPayload result = client.postMultipart(SellerSpriteOperation.OCR, parts, RESPONSE_TYPE);

        assertThat(result.getValue()).isEqualTo(1L);
        server.verify();
    }

    @Test
    void shouldExposeProviderBusinessError() {
        server.expect(requestTo("https://api.sellersprite.com/v1/visits"))
                .andRespond(withSuccess(
                        "{\"code\":\"ERROR_VISIT_MAX\",\"message\":\"接口访问次数已达上限\",\"data\":null}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.get("/v1/visits", RESPONSE_TYPE))
                .isInstanceOf(SellerSpriteApiException.class)
                .satisfies(error -> {
                    SellerSpriteApiException exception = (SellerSpriteApiException) error;
                    assertThat(exception.getCode()).isEqualTo("S429");
                    assertThat(exception.getProviderCode()).isEqualTo("ERROR_VISIT_MAX");
                    assertThat(exception.getRequestId()).isEqualTo("01900000-0000-7000-8000-000000000001");
                });
        server.verify();
    }

    @ParameterizedTest
    @CsvSource({
            "ERROR_PARAM,S400",
            "ERROR_SECRET_KEY,S401",
            "ERROR_SECRET_KEY_OVERDUE,S401",
            "ERROR_SECRET_KEY_INVALID,S401",
            "ERROR_SERVER_INTERNAL,S502"
    })
    void shouldMapProviderErrorCodes(String providerCode, String expectedCode) {
        server.expect(requestTo("https://api.sellersprite.com/v1/visits"))
                .andRespond(withSuccess(
                        "{\"code\":\"" + providerCode + "\",\"message\":\"上游失败\",\"data\":null}",
                        MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.get("/v1/visits", RESPONSE_TYPE))
                .isInstanceOf(SellerSpriteApiException.class)
                .satisfies(error -> {
                    SellerSpriteApiException exception = (SellerSpriteApiException) error;
                    assertThat(exception.getCode()).isEqualTo(expectedCode);
                    assertThat(exception.getProviderCode()).isEqualTo(providerCode);
                    assertThat(exception.getMessage()).doesNotContain("test-secret");
                });
        server.verify();
    }

    @Test
    void shouldConvertHttpError() {
        server.expect(requestTo("https://api.sellersprite.com/v1/visits"))
                .andRespond(withServerError());

        assertThatThrownBy(() -> client.get("/v1/visits", RESPONSE_TYPE))
                .isInstanceOf(SellerSpriteApiException.class)
                .extracting("code")
                .isEqualTo("S502");
        server.verify();
    }

    @Test
    void shouldRejectEmptyResponse() {
        server.expect(requestTo("https://api.sellersprite.com/v1/visits"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.get("/v1/visits", RESPONSE_TYPE))
                .isInstanceOf(SellerSpriteApiException.class)
                .extracting("code")
                .isEqualTo("S502");
        server.verify();
    }

    @Test
    void shouldRejectMalformedJsonResponse() {
        server.expect(requestTo("https://api.sellersprite.com/v1/visits"))
                .andRespond(withSuccess("{not-json", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.get("/v1/visits", RESPONSE_TYPE))
                .isInstanceOf(SellerSpriteApiException.class)
                .extracting("code")
                .isEqualTo("S502");
        server.verify();
    }

    @Test
    void shouldConvertSocketTimeoutWithoutRetry() {
        RestClient timeoutRestClient = RestClient.builder()
                .baseUrl("https://api.sellersprite.com")
                .requestFactory((uri, httpMethod) -> {
                    throw new SocketTimeoutException("read timed out");
                })
                .build();
        SellerSpriteClient timeoutClient = new SellerSpriteClient(timeoutRestClient, headers -> {
            headers.set(SellerSpriteHeaders.SECRET_KEY, "test-secret");
            headers.set(SellerSpriteHeaders.REQUEST_ID, "timeout-request-id");
            return "timeout-request-id";
        });

        assertThatThrownBy(() -> timeoutClient.get("/v1/visits", RESPONSE_TYPE))
                .isInstanceOf(SellerSpriteApiException.class)
                .satisfies(error -> {
                    SellerSpriteApiException exception = (SellerSpriteApiException) error;
                    assertThat(exception.getCode()).isEqualTo("S504");
                    assertThat(exception.getRequestId()).isEqualTo("timeout-request-id");
                });
    }

    @Data
    static class TestPayload {
        private Long value;
    }
}
