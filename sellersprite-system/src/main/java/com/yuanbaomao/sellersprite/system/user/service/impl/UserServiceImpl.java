package com.yuanbaomao.sellersprite.system.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.common.result.ResultCode;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.dao.UserTokenDao;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.base.exception.BizException;
import com.yuanbaomao.base.context.RequestContextHolder;
import com.yuanbaomao.sellersprite.system.constants.SystemBusinessConstants;
import com.yuanbaomao.sellersprite.system.convert.SystemConverter;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserCreateRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserPageRequest;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import com.yuanbaomao.sellersprite.system.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final UserTokenDao userTokenDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserDetailVo create(UserCreateRequest request) {
        if (userDao.existsByUsername(request.getUsername())) {
            throw new BizException(ResultCode.USERNAME_ALREADY_EXISTS);
        }
        User entity = new User();
        entity.setUsername(request.getUsername());
        entity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        entity.setNickname(defaultString(request.getNickname()));
        entity.setRealName(defaultString(request.getRealName()));
        entity.setAvatarUrl("");
        entity.setMobile(request.getMobile());
        entity.setEmail(request.getEmail());
        entity.setGender(0);
        entity.setPrimaryDeptId(request.getPrimaryDeptId());
        entity.setStatus(SystemBusinessConstants.STATUS_ENABLED);
        entity.setPasswordUpdatedAt(System.currentTimeMillis());
        entity.setRemark("");
        userDao.save(entity);
        return SystemConverter.toUserDetailVo(entity);
    }

    @Override
    public UserDetailVo detail(String userId) {
        User entity = userDao.getById(userId);
        if (entity == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return SystemConverter.toUserDetailVo(entity);
    }

    @Override
    public PageResult<UserDetailVo> page(UserPageRequest request) {
        Page<User> page = userDao.lambdaQuery()
                .like(request.getUsername() != null && !request.getUsername().isBlank(), User::getUsername,
                        request.getUsername())
                .eq(request.getStatus() != null, User::getStatus, request.getStatus())
                .page(Page.of(request.getCurrent(), request.getSize()));
        List<UserDetailVo> records = page.getRecords().stream()
                .map(SystemConverter::toUserDetailVo)
                .toList();
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String userId, Integer status) {
        if (!Integer.valueOf(SystemBusinessConstants.STATUS_ENABLED).equals(status)
                && !Integer.valueOf(SystemBusinessConstants.STATUS_DISABLED).equals(status)) {
            throw new BizException(ResultCode.PARAM_INVALID);
        }
        User user = requireUser(userId);
        user.setStatus(status);
        userDao.updateById(user);
        if (Integer.valueOf(SystemBusinessConstants.STATUS_DISABLED).equals(status)) {
            revokeUserSessions(userId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new BizException(ResultCode.PARAM_INVALID);
        }
        User user = requireUser(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordUpdatedAt(System.currentTimeMillis());
        userDao.updateById(user);
        revokeUserSessions(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(String userId) {
        String currentUserId = RequestContextHolder.get().map(context -> context.getUserId()).orElse(null);
        if (userId != null && userId.equals(currentUserId)) {
            throw new BizException(ResultCode.CURRENT_USER_OPERATION_FORBIDDEN);
        }
        requireUser(userId);
        revokeUserSessions(userId);
        userDao.removeById(userId);
    }

    private User requireUser(String userId) {
        User user = userDao.getById(userId);
        if (user == null) {
            throw new BizException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    private void revokeUserSessions(String userId) {
        userTokenDao.revokeByUserId(userId, System.currentTimeMillis(),
                SystemBusinessConstants.TOKEN_REVOKE_REASON_USER_SECURITY_CHANGED);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
