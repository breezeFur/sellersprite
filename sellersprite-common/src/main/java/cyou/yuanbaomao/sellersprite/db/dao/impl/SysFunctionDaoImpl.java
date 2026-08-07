package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cyou.yuanbaomao.sellersprite.db.dao.SysFunctionDao;
import cyou.yuanbaomao.sellersprite.db.entity.SysFunction;
import cyou.yuanbaomao.sellersprite.db.mapper.SysFunctionMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class SysFunctionDaoImpl extends ServiceImpl<SysFunctionMapper, SysFunction> implements SysFunctionDao {

    private static final int ENABLED_STATUS = 1;

    @Override
    public List<SysFunction> listEnabled() {
        return lambdaQuery()
                .eq(SysFunction::getStatus, ENABLED_STATUS)
                .orderByAsc(SysFunction::getSortOrder)
                .list();
    }

    @Override
    public List<SysFunction> listAll() {
        return lambdaQuery().orderByAsc(SysFunction::getSortOrder).orderByAsc(SysFunction::getSysFunctionId).list();
    }

    @Override
    public boolean existsByFunctionCode(String functionCode) {
        return lambdaQuery().eq(SysFunction::getFunctionCode, functionCode).exists();
    }

    @Override
    public boolean existsByFunctionCodeExcludingId(String functionCode, String functionId) {
        return lambdaQuery().eq(SysFunction::getFunctionCode, functionCode)
                .ne(functionId != null, SysFunction::getSysFunctionId, functionId).exists();
    }

    @Override
    public boolean existsByPermissionCodeExcludingId(String permissionCode, String functionId) {
        return lambdaQuery().eq(SysFunction::getPermissionCode, permissionCode)
                .ne(functionId != null, SysFunction::getSysFunctionId, functionId).exists();
    }

    @Override
    public boolean existsByParentId(String parentId) {
        return lambdaQuery().eq(SysFunction::getParentId, parentId).exists();
    }
}
