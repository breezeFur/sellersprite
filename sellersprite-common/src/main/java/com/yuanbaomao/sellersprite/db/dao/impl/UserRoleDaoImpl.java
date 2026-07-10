package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.UserRoleDao;
import com.yuanbaomao.sellersprite.db.entity.UserRole;
import com.yuanbaomao.sellersprite.db.mapper.UserRoleMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class UserRoleDaoImpl extends ServiceImpl<UserRoleMapper, UserRole> implements UserRoleDao {

    @Override
    public List<UserRole> listByUserId(String userId) {
        return lambdaQuery().eq(UserRole::getUserId, userId).list();
    }
}
