// Generated from SellerSprite official documentation on 2026-07-10.
package cyou.yuanbaomao.sellersprite.api.tool.service.impl;

import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteClient;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteRequestEncoder;
import cyou.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;
import cyou.yuanbaomao.sellersprite.api.tool.model.dto.OcrRequest;
import cyou.yuanbaomao.sellersprite.api.tool.model.vo.OcrVo;
import cyou.yuanbaomao.sellersprite.api.tool.service.ToolService;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

/**
 * SellerSprite 数据工具接口实现，所有请求统一委派给 SellerSpriteClient。
 */
@Service
@RequiredArgsConstructor
public class ToolServiceImpl implements ToolService {

    private final SellerSpriteClient client;

    @Override
    public OcrVo recognizeImageText(OcrRequest request) {
        return client.postMultipart(SellerSpriteOperation.OCR, SellerSpriteRequestEncoder.toMultipart(request),
                new ParameterizedTypeReference<SellerSpriteResponse<OcrVo>>() {
                });
    }

}
