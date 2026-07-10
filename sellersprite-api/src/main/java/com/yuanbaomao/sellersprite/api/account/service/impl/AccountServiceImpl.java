// Generated from SellerSprite official documentation on 2026-07-10.
package com.yuanbaomao.sellersprite.api.account.service.impl;

import com.yuanbaomao.sellersprite.api.account.model.vo.VisitsVo;
import com.yuanbaomao.sellersprite.api.account.service.AccountService;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteClient;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteOperation;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteRequestEncoder;
import com.yuanbaomao.sellersprite.api.client.SellerSpriteResponse;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

/**
 * SellerSprite 账户次数接口实现，所有请求统一委派给 SellerSpriteClient。
 */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final SellerSpriteClient client;

    @Override
    public VisitsVo getVisits() {
        JsonNode details = client.get(SellerSpriteOperation.ACCOUNT_VISITS, Map.of(),
                SellerSpriteRequestEncoder.toQuery(null, Set.of()),
                new ParameterizedTypeReference<SellerSpriteResponse<JsonNode>>() {
                });
        return new VisitsVo(details);
    }

}
