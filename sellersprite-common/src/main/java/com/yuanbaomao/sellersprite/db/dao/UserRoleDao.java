package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.UserRole;
import java.util.List;

public interface UserRoleDao extends IService<UserRole> {

    List<UserRole> listByUserId(String userId);
}
