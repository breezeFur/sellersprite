package cyou.yuanbaomao.sellersprite.system.ops.login.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.sellersprite.common.result.ResultCode;
import cyou.yuanbaomao.sellersprite.db.dao.LoginLogDao;
import cyou.yuanbaomao.sellersprite.db.entity.LoginLog;
import cyou.yuanbaomao.sellersprite.system.ops.login.convert.LoginLogConverter;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.dto.LoginLogPageRequest;
import cyou.yuanbaomao.sellersprite.system.ops.login.model.vo.LoginLogVo;
import cyou.yuanbaomao.sellersprite.system.ops.login.service.LoginLogService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogDao loginLogDao;

    @Override
    public YPage<LoginLogVo> page(YPage<LoginLogVo> page, LoginLogPageRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null
                && request.getStartTime() > request.getEndTime()) {
            throw new BizException(ResultCode.PARAM_INVALID, "开始时间不能晚于结束时间");
        }
        Page<LoginLog> entityPage = loginLogDao.page(request.getUserId(), request.getUsername(), request.getSuccess(),
                request.getLoginIp(), request.getStartTime(), request.getEndTime(),
                page.getCurrent(), page.getSize());
        List<LoginLogVo> records = entityPage.getRecords().stream().map(LoginLogConverter::toVo).toList();
        page.setTotal(entityPage.getTotal());
        page.setRecords(records);
        return page;
    }

    @Override
    public LoginLogVo detail(String loginLogId) {
        LoginLog log = loginLogDao.findById(loginLogId)
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));
        return LoginLogConverter.toVo(log);
    }
}
