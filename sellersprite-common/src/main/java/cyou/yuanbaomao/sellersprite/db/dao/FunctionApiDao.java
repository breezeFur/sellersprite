package cyou.yuanbaomao.sellersprite.db.dao;

import com.baomidou.mybatisplus.extension.service.IService;
import cyou.yuanbaomao.sellersprite.db.entity.FunctionApi;
import java.util.Collection;
import java.util.List;

public interface FunctionApiDao extends IService<FunctionApi> {

    List<FunctionApi> listByFunctionIds(Collection<String> functionIds);

    boolean existsByApiId(String apiId);

    void replaceByFunctionId(String functionId, Collection<String> apiIds);
}
