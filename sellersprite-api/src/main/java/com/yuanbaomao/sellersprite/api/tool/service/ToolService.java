// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.tool.service;

import com.yuanbaomao.sellersprite.api.tool.model.dto.OcrRequest;
import com.yuanbaomao.sellersprite.api.tool.model.vo.OcrVo;

/**
 * SellerSprite 数据工具接口封装。
 */
public interface ToolService {

    /**
     * 图片文字识别。
     *
     * <p>调用 SellerSprite 官方 POST /v1/ocr，认证、超时和错误转换由统一 Client 处理。</p>
     * @param request 图片文字识别的强类型请求参数
     * @return 图片文字识别的强类型响应数据
     */
    OcrVo recognizeImageText(OcrRequest request);

}
