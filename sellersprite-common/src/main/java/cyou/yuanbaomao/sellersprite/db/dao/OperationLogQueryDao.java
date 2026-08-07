package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.log.mybatis.entity.OperationLogEntity;
import java.util.List;
import java.util.Optional;

public interface OperationLogQueryDao {
    long countFailedByCreatedAtRange(long startTime, long endTime);
    List<OperationLogEntity> listRecent(int limit);

    Page<OperationLogEntity> page(String userId, String username, String moduleName, String operationType,
                                  Integer success, String trackId, Long startTime, Long endTime,
                                  long current, long size);

    Optional<OperationLogEntity> findById(String operationLogId);
}
