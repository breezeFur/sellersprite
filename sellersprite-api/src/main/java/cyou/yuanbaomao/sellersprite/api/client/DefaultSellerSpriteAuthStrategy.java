package cyou.yuanbaomao.sellersprite.api.client;

import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import cyou.yuanbaomao.base.id.IdGenerator;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;

import lombok.RequiredArgsConstructor;

/**
 * SellerSprite 官方默认认证实现。
 *
 * <p>官方当前未声明 HMAC、MD5、时间戳摘要或响应验签，不能在此臆造相关字段。</p>
 */
@RequiredArgsConstructor
public class DefaultSellerSpriteAuthStrategy implements SellerSpriteAuthStrategy {

    private final SellerSpriteProperties properties;
    private final IdGenerator idGenerator;

    @Override
    public String apply(HttpHeaders headers) {
        if (!properties.isEnabled()) {
            throw new SellerSpriteApiException(ResultCode.SELLERSPRITE_DISABLED, null, null, null);
        }
        if (!StringUtils.hasText(properties.getSecretKey())) {
            throw new SellerSpriteApiException(ResultCode.SELLERSPRITE_NOT_CONFIGURED, null, null, null);
        }

        String requestId = idGenerator.nextId();
        headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        headers.set(SellerSpriteHeaders.SECRET_KEY, properties.getSecretKey());
        headers.set(SellerSpriteHeaders.REQUEST_ID, requestId);
        return requestId;
    }
}
