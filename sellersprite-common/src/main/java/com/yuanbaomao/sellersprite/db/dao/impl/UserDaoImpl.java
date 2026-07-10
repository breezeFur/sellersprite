package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.UserDao;
import com.yuanbaomao.sellersprite.db.entity.User;
import com.yuanbaomao.sellersprite.db.mapper.UserMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class UserDaoImpl extends ServiceImpl<UserMapper, User> implements UserDao {

    @Override
    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(lambdaQuery().eq(User::getUsername, username).one());
    }

    @Override
    public boolean existsByUsername(String username) {
        return lambdaQuery().eq(User::getUsername, username).exists();
    }
}
