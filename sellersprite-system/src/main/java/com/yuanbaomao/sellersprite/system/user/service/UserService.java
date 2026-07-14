package com.yuanbaomao.sellersprite.system.user.service;

import com.yuanbaomao.base.result.PageResult;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserCreateRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserPageRequest;
import com.yuanbaomao.sellersprite.system.user.model.dto.UserUpdateRequest;
import com.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import java.util.Collection;

public interface UserService {

    UserDetailVo create(UserCreateRequest request);

    UserDetailVo detail(String userId);

    PageResult<UserDetailVo> page(UserPageRequest request);

    UserDetailVo update(String userId, UserUpdateRequest request);

    void updateStatus(String userId, Integer status);

    void resetPassword(String userId, String newPassword);

    void replaceRoles(String userId, Collection<String> roleIds);

    void delete(String userId);
}
