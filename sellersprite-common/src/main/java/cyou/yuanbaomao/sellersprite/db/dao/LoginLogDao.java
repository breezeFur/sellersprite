package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import cyou.yuanbaomao.sellersprite.db.entity.LoginLog;
import java.util.List;
import java.util.Optional;

public interface LoginLogDao extends IService<LoginLog> {

    long countByCreatedAtRange(long startTime, long endTime);

    List<LoginLog> listRecent(int limit);

    Page<LoginLog> page(String userId, String username, Integer success, String loginIp,
                        Long startTime, Long endTime, long current, long size);

    Optional<LoginLog> findById(String loginLogId);
}
