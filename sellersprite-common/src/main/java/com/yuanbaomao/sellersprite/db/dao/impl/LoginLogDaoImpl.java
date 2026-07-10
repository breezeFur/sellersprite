package com.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuanbaomao.sellersprite.db.dao.LoginLogDao;
import com.yuanbaomao.sellersprite.db.entity.LoginLog;
import com.yuanbaomao.sellersprite.db.mapper.LoginLogMapper;
import org.springframework.stereotype.Repository;

@Repository
public class LoginLogDaoImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements LoginLogDao {
}
