package com.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuanbaomao.sellersprite.db.entity.User;
import java.util.Optional;

public interface UserDao extends IService<User> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
