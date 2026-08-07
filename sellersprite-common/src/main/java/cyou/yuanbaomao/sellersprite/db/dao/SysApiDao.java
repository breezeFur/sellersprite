package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.sellersprite.db.entity.SysApi;
import java.util.List;

public interface SysApiDao extends IService<SysApi> {

    List<SysApi> listPublicApis();

    List<SysApi> listEnabledByHttpMethod(String httpMethod);

    boolean existsByApiCode(String apiCode);

    boolean existsByApiCodeExcludingId(String apiCode, String apiId);

    boolean existsByHttpMethodAndPathPattern(String httpMethod, String pathPattern, String apiId);

    Page<SysApi> pageApis(String keyword, String apiType, String httpMethod, String moduleName,
            Integer status, long current, long size);
}
