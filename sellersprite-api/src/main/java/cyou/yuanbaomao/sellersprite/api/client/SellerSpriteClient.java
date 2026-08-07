package cyou.yuanbaomao.sellersprite.api.client;

import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import cyou.yuanbaomao.sellersprite.common.result.ResultCode;

import lombok.extern.slf4j.Slf4j;

/**
 * SellerSprite Open API 统一 HTTP Client。
 *
 * <p>业务域只能通过本类访问外部服务，不能自行拼装密钥、请求 ID 或错误降级。</p>
 */
@Slf4j
public class SellerSpriteClient {

    private static final String SUCCESS_CODE = "OK";

    private final RestClient restClient;
    private final SellerSpriteAuthStrategy authStrategy;
    private final SellerSpriteDictionaryResolver dictionaryResolver;

    public SellerSpriteClient(RestClient restClient, SellerSpriteAuthStrategy authStrategy) {
        this(restClient, authStrategy, null);
    }

    public SellerSpriteClient(RestClient restClient, SellerSpriteAuthStrategy authStrategy,
            SellerSpriteDictionaryResolver dictionaryResolver) {
        this.restClient = restClient;
        this.authStrategy = authStrategy;
        this.dictionaryResolver = dictionaryResolver;
    }

    /**
     * 调用不含路径变量和查询参数的 GET 接口。
     *
     * @param path SellerSprite 官方相对路径
     * @param responseType 包含外层响应信封的泛型类型
     * @param <T> data 字段类型
     * @return 已校验并拆除外层信封的 data
     */
    public <T> T get(String path, ParameterizedTypeReference<SellerSpriteResponse<T>> responseType) {
        AtomicReference<String> requestId = new AtomicReference<>();
        return execute(HttpMethod.GET, path, Map.of("uri", path), requestId, () -> restClient.get()
                .uri(path)
                .headers(headers -> requestId.set(authStrategy.apply(headers)))
                .retrieve()
                .body(responseType));
    }

    /**
     * 调用操作枚举声明的 GET 接口，并统一展开路径变量和编码重复查询参数。
     *
     * @param operation GET 操作
     * @param uriVariables URL 路径占位符值
     * @param queryParams 查询参数，集合值应编码为重复键
     * @param responseType 包含外层响应信封的泛型类型
     * @param <T> data 字段类型
     * @return 已校验并拆除外层信封的 data
     */
    public <T> T get(SellerSpriteOperation operation, Map<String, ?> uriVariables,
            MultiValueMap<String, String> queryParams,
            ParameterizedTypeReference<SellerSpriteResponse<T>> responseType) {
        requireMethod(operation, HttpMethod.GET);
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromPath(operation.getPath());
        if (queryParams != null) {
            uriBuilder.queryParams(queryParams);
        }
        Map<String, ?> variables = uriVariables == null ? Map.of() : uriVariables;
        String uri = uriBuilder.buildAndExpand(variables).encode().toUriString();
        return get(uri, responseType);
    }

    /**
     * 调用 JSON POST 接口。
     *
     * @param path SellerSprite 官方相对路径
     * @param request 强类型请求体
     * @param responseType 包含外层响应信封的泛型类型
     * @param <T> data 字段类型
     * @return 已校验并拆除外层信封的 data
     */
    public <T> T post(String path, Object request,
            ParameterizedTypeReference<SellerSpriteResponse<T>> responseType) {
        AtomicReference<String> requestId = new AtomicReference<>();
        Object resolvedRequest = dictionaryResolver == null ? request : dictionaryResolver.resolveRequest(request);
        return execute(HttpMethod.POST, path, resolvedRequest, requestId, () -> restClient.post()
                .uri(path)
                .headers(headers -> requestId.set(authStrategy.apply(headers)))
                .body(resolvedRequest)
                .retrieve()
                .body(responseType));
    }

    /**
     * 使用操作枚举调用 JSON POST 接口，并校验枚举声明的方法类型。
     *
     * @param operation POST 操作
     * @param request 强类型请求体
     * @param responseType 包含外层响应信封的泛型类型
     * @param <T> data 字段类型
     * @return 已校验并拆除外层信封的 data
     */
    public <T> T post(SellerSpriteOperation operation, Object request,
            ParameterizedTypeReference<SellerSpriteResponse<T>> responseType) {
        requireMethod(operation, HttpMethod.POST);
        return post(operation.getPath(), request, responseType);
    }

    /**
     * 调用 multipart/form-data POST 接口，适用于 OCR 和以图搜商标。
     *
     * @param operation multipart POST 操作
     * @param parts 已编码的文本与文件表单项
     * @param responseType 包含外层响应信封的泛型类型
     * @param <T> data 字段类型
     * @return 已校验并拆除外层信封的 data
     */
    public <T> T postMultipart(SellerSpriteOperation operation, MultiValueMap<String, Object> parts,
            ParameterizedTypeReference<SellerSpriteResponse<T>> responseType) {
        requireMethod(operation, HttpMethod.POST);
        AtomicReference<String> requestId = new AtomicReference<>();
        Map<String, Object> requestSummary = summarizeMultipartParts(parts);
        return execute(HttpMethod.POST, operation.getPath(), requestSummary, requestId, () -> restClient.post()
                .uri(operation.getPath())
                .headers(headers -> requestId.set(authStrategy.apply(headers)))
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(parts)
                .retrieve()
                .body(responseType));
    }

    private void requireMethod(SellerSpriteOperation operation, HttpMethod expectedMethod) {
        if (operation.getMethod() != expectedMethod) {
            throw new IllegalArgumentException(
                    "SellerSprite operation " + operation.name() + " must use " + operation.getMethod());
        }
    }

    private <T> T execute(HttpMethod method, String path, Object requestParameters,
            AtomicReference<String> requestId, Supplier<SellerSpriteResponse<T>> request) {
        long startedAt = System.nanoTime();
        log.info("SellerSprite 外部接口调用入参 method={}, path={}, request={}",
                method, path, requestParameters);
        try {
            SellerSpriteResponse<T> response = request.get();
            log.info("SellerSprite 外部接口调用出参 requestId={}, method={}, path={}, response={}, elapsedMs={}",
                    requestId.get(), method, path, response, elapsedMillis(startedAt));
            if (response == null || !StringUtils.hasText(response.getCode())) {
                throw protocolException(requestId.get(), null);
            }
            if (!SUCCESS_CODE.equals(response.getCode())) {
                ResultCode resultCode = mapProviderCode(response.getCode());
                log.warn("SellerSprite 请求失败 requestId={}, method={}, path={}, providerCode={}, elapsedMs={}",
                        requestId.get(), method, path, response.getCode(), elapsedMillis(startedAt));
                throw new SellerSpriteApiException(resultCode, response.getMessage(), response.getCode(),
                        requestId.get(), null);
            }
            if (response.getData() == null) {
                throw protocolException(requestId.get(), null);
            }
            return response.getData();
        } catch (SellerSpriteApiException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            log.warn("SellerSprite HTTP 请求失败 requestId={}, method={}, path={}, status={}, response={}, elapsedMs={}",
                    requestId.get(), method, path, exception.getStatusCode(), exception.getResponseBodyAsString(),
                    elapsedMillis(startedAt));
            throw new SellerSpriteApiException(ResultCode.SELLERSPRITE_HTTP_ERROR, null, requestId.get(), exception);
        } catch (ResourceAccessException exception) {
            ResultCode resultCode = isTimeout(exception)
                    ? ResultCode.SELLERSPRITE_TIMEOUT
                    : ResultCode.SELLERSPRITE_HTTP_ERROR;
            log.warn("SellerSprite 网络请求失败 requestId={}, method={}, path={}, errorCode={}, elapsedMs={}",
                    requestId.get(), method, path, resultCode.getCode(), elapsedMillis(startedAt));
            throw new SellerSpriteApiException(resultCode, null, requestId.get(), exception);
        } catch (RestClientException exception) {
            throw protocolException(requestId.get(), exception);
        }
    }

    private Map<String, Object> summarizeMultipartParts(MultiValueMap<String, Object> parts) {
        if (parts == null) {
            return Map.of();
        }
        Map<String, Object> summary = new LinkedHashMap<>();
        parts.forEach((name, values) -> summary.put(name,
                values.stream().map(this::summarizeMultipartValue).toList()));
        return summary;
    }

    private Object summarizeMultipartValue(Object value) {
        if (value instanceof Resource resource) {
            Map<String, Object> resourceSummary = new LinkedHashMap<>();
            String resourceType = resource.getClass().getSimpleName();
            resourceSummary.put("type",
                    StringUtils.hasText(resourceType) ? resourceType : Resource.class.getSimpleName());
            resourceSummary.put("filename", resource.getFilename());
            return resourceSummary;
        }
        if (value instanceof byte[] bytes) {
            return Map.of("type", "byte[]", "size", bytes.length);
        }
        return value;
    }

    private SellerSpriteApiException protocolException(String requestId, Throwable cause) {
        return new SellerSpriteApiException(ResultCode.SELLERSPRITE_PROTOCOL_ERROR, null, requestId, cause);
    }

    private ResultCode mapProviderCode(String providerCode) {
        return switch (providerCode) {
            case "ERROR_PARAM" -> ResultCode.SELLERSPRITE_PARAM_ERROR;
            case "ERROR_SECRET_KEY", "ERROR_SECRET_KEY_OVERDUE", "ERROR_SECRET_KEY_INVALID" ->
                    ResultCode.SELLERSPRITE_AUTH_ERROR;
            case "ERROR_VISIT_MAX" -> ResultCode.SELLERSPRITE_QUOTA_EXHAUSTED;
            default -> ResultCode.SELLERSPRITE_UPSTREAM_ERROR;
        };
    }

    private boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException || current instanceof HttpTimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }
}
