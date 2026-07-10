package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import com.yuanbaomao.sellersprite.db.entity.UserToken;
import com.yuanbaomao.sellersprite.db.mapper.UserTokenMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserTokenDaoImpl extends ServiceImpl<UserTokenMapper, UserToken> implements UserTokenDao {

    private static final int VALID_STATUS = 1;

    @Override
    public Optional<UserToken> findValidByAccessTokenHash(String accessTokenHash) {
        return Optional.ofNullable(lambdaQuery()
                .eq(UserToken::getAccessTokenHash, accessTokenHash)
                .eq(UserToken::getStatus, VALID_STATUS)
                .gt(UserToken::getExpiresAt, System.currentTimeMillis())
                .one());
    }

    @Override
    public Optional<UserToken> findByRefreshTokenHash(String refreshTokenHash) {
        return Optional.ofNullable(lambdaQuery()
                .eq(UserToken::getRefreshTokenHash, refreshTokenHash)
                .one());
    }

    @Override
    public boolean rotateRefreshToken(String userTokenId, String replacementTokenId, Long usedAt,
            String revokeReason) {
        return lambdaUpdate()
                .eq(UserToken::getUserTokenId, userTokenId)
                .eq(UserToken::getStatus, VALID_STATUS)
                .isNull(UserToken::getRevokedAt)
                .gt(UserToken::getRefreshExpiresAt, usedAt)
                .set(UserToken::getStatus, 0)
                .set(UserToken::getLastUsedAt, usedAt)
                .set(UserToken::getRevokedAt, usedAt)
                .set(UserToken::getRevokeReason, revokeReason)
                .set(UserToken::getReplacedByTokenId, replacementTokenId)
                .update();
    }

    @Override
    public boolean revokeFamily(String sessionFamilyId, Long revokedAt, String revokeReason) {
        return lambdaUpdate()
                .eq(UserToken::getSessionFamilyId, sessionFamilyId)
                .eq(UserToken::getStatus, VALID_STATUS)
                .set(UserToken::getStatus, 0)
                .set(UserToken::getRevokedAt, revokedAt)
                .set(UserToken::getRevokeReason, revokeReason)
                .update();
    }

    @Override
    public boolean revokeByUserId(String userId, Long revokedAt, String revokeReason) {
        return lambdaUpdate()
                .eq(UserToken::getUserId, userId)
                .eq(UserToken::getStatus, VALID_STATUS)
                .set(UserToken::getStatus, 0)
                .set(UserToken::getRevokedAt, revokedAt)
                .set(UserToken::getRevokeReason, revokeReason)
                .update();
    }
}
