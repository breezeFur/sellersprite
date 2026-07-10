// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.account.service;

import com.yuanbaomao.sellersprite.api.account.model.vo.VisitsVo;

/**
 * SellerSprite 账户次数接口封装。
 */
public interface AccountService {

    /**
     * 可用次数查询。
     *
     * <p>调用 SellerSprite 官方 GET /v1/visits，认证、超时和错误转换由统一 Client 处理。</p>
     * @return 可用次数查询的强类型响应数据
     */
    VisitsVo getVisits();

}
