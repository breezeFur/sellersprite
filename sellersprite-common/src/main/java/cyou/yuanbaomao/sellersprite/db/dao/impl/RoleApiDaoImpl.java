package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cyou.yuanbaomao.sellersprite.db.dao.RoleApiDao;
import cyou.yuanbaomao.sellersprite.db.entity.RoleApi;
import cyou.yuanbaomao.sellersprite.db.mapper.RoleApiMapper;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class RoleApiDaoImpl extends ServiceImpl<RoleApiMapper, RoleApi> implements RoleApiDao {

    @Override
    public List<RoleApi> listByRoleIds(Collection<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return lambdaQuery().in(RoleApi::getRoleId, roleIds).list();
    }

    @Override
    public boolean existsByApiId(String apiId) {
        return lambdaQuery().eq(RoleApi::getSysApiId, apiId).exists();
    }

    @Override
    public void replaceByRoleId(String roleId, Collection<RoleApi> roleApis) {
        baseMapper.deletePhysicallyByRoleId(roleId);
        if (roleApis == null || roleApis.isEmpty()) {
            return;
        }
        saveBatch(roleApis);
    }
}
