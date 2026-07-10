package com.yuanbaomao.sellersprite.system.user.service;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserCreateRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserPageRequest;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;

public interface UserService {

    UserDetailVo create(UserCreateRequest request);

    UserDetailVo detail(String userId);

    PageResult<UserDetailVo> page(UserPageRequest request);

    void updateStatus(String userId, Integer status);

    void resetPassword(String userId, String newPassword);

    void delete(String userId);
}
