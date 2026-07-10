package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.UserToken;
import java.util.Optional;

public interface UserTokenDao extends IService<UserToken> {

    Optional<UserToken> findValidByAccessTokenHash(String accessTokenHash);

    Optional<UserToken> findByRefreshTokenHash(String refreshTokenHash);

    boolean rotateRefreshToken(String userTokenId, String replacementTokenId, Long usedAt, String revokeReason);

    boolean revokeFamily(String sessionFamilyId, Long revokedAt, String revokeReason);

    boolean revokeByUserId(String userId, Long revokedAt, String revokeReason);
}
