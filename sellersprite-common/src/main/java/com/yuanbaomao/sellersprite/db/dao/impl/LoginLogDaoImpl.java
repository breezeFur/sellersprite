package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.LoginLogDao;
import com.yuanbaomao.sellersprite.db.entity.LoginLog;
import com.yuanbaomao.sellersprite.db.mapper.LoginLogMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class LoginLogDaoImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogDao {

    @Override
    public long countByCreatedAtRange(long startTime, long endTime) {
        return lambdaQuery().ge(LoginLog::getCreatedAt, startTime).lt(LoginLog::getCreatedAt, endTime).count();
    }

    @Override
    public List<LoginLog> listRecent(int limit) {
        return lambdaQuery().orderByDesc(LoginLog::getCreatedAt)
                .page(Page.of(1, limit))
                .getRecords();
    }

    @Override
    public Page<LoginLog> page(String userId, String username, Integer success, String loginIp,
                               Long startTime, Long endTime, long current, long size) {
        return lambdaQuery()
                .eq(userId != null && !userId.isBlank(), LoginLog::getUserId, userId)
                .like(username != null && !username.isBlank(), LoginLog::getUsername, username)
                .eq(success != null, LoginLog::getSuccess, success)
                .like(loginIp != null && !loginIp.isBlank(), LoginLog::getLoginIp, loginIp)
                .ge(startTime != null, LoginLog::getCreatedAt, startTime)
                .le(endTime != null, LoginLog::getCreatedAt, endTime)
                .orderByDesc(LoginLog::getCreatedAt)
                .page(Page.of(current, size));
    }

    @Override
    public Optional<LoginLog> findById(String loginLogId) {
        return Optional.ofNullable(getById(loginLogId));
    }
}
