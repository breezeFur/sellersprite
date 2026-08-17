package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.log.mybatis.entity.OperationLogEntity;
import cyou.yuanbaomao.sellersprite.db.dao.OperationLogQueryDao;
import cyou.yuanbaomao.sellersprite.db.mapper.OperationLogQueryMapper;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OperationLogQueryDaoImpl implements OperationLogQueryDao {
    private final OperationLogQueryMapper mapper;

    @Override
    public long countFailedByCreatedAtRange(long startTime, long endTime) {
        return mapper.countFailedByCreatedAtRange(startTime, endTime);
    }

    @Override
    public List<OperationLogEntity> listRecent(int limit) {
        return mapper.listRecent(limit);
    }

    @Override
    public Page<OperationLogEntity> page(String userId, String username, String moduleName, String operationType,
                                         Integer success, String traceId, Long startTime, Long endTime,
                                         long current, long size) {
        return mapper.page(Page.of(current, size), userId, username, moduleName, operationType,
                success, traceId, startTime, endTime);
    }

    @Override
    public Optional<OperationLogEntity> findById(String operationLogId) {
        return Optional.ofNullable(mapper.findById(operationLogId));
    }
}
