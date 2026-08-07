package cyou.yuanbaomao.sellersprite.db.dao.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.sellersprite.db.dao.SysApiDao;
import cyou.yuanbaomao.sellersprite.db.entity.SysApi;
import cyou.yuanbaomao.sellersprite.db.mapper.SysApiMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class SysApiDaoImpl extends ServiceImpl<SysApiMapper, SysApi> implements SysApiDao {

    private static final int ENABLED_STATUS = 1;
    private static final String PUBLIC_API_TYPE = "PUBLIC";

    @Override
    public List<SysApi> listPublicApis() {
        return lambdaQuery()
                .eq(SysApi::getApiType, PUBLIC_API_TYPE)
                .eq(SysApi::getStatus, ENABLED_STATUS)
                .list();
    }

    @Override
    public List<SysApi> listEnabledByHttpMethod(String httpMethod) {
        return lambdaQuery()
                .eq(SysApi::getHttpMethod, httpMethod)
                .eq(SysApi::getStatus, ENABLED_STATUS)
                .list();
    }

    @Override
    public boolean existsByApiCode(String apiCode) {
        return lambdaQuery().eq(SysApi::getApiCode, apiCode).exists();
    }

    @Override
    public boolean existsByApiCodeExcludingId(String apiCode, String apiId) {
        return lambdaQuery().eq(SysApi::getApiCode, apiCode)
                .ne(apiId != null, SysApi::getSysApiId, apiId).exists();
    }

    @Override
    public boolean existsByHttpMethodAndPathPattern(String httpMethod, String pathPattern, String apiId) {
        return lambdaQuery().eq(SysApi::getHttpMethod, httpMethod).eq(SysApi::getPathPattern, pathPattern)
                .ne(apiId != null, SysApi::getSysApiId, apiId).exists();
    }

    @Override
    public Page<SysApi> pageApis(String keyword, String apiType, String httpMethod, String moduleName,
            Integer status, long current, long size) {
        return lambdaQuery()
                .and(keyword != null && !keyword.isBlank(), query -> query.like(SysApi::getApiCode, keyword)
                        .or().like(SysApi::getApiName, keyword))
                .eq(apiType != null && !apiType.isBlank(), SysApi::getApiType, apiType)
                .eq(httpMethod != null && !httpMethod.isBlank(), SysApi::getHttpMethod, httpMethod)
                .eq(moduleName != null && !moduleName.isBlank(), SysApi::getModuleName, moduleName)
                .eq(status != null, SysApi::getStatus, status)
                .orderByAsc(SysApi::getModuleName).orderByAsc(SysApi::getApiCode)
                .page(Page.of(current, size));
    }
}
