package cyou.yuanbaomao.sellersprite.system.ops.login.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cyou.yuanbaomao.base.exception.BizException;
import cyou.yuanbaomao.base.result.PageResult;
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
    public PageResult<LoginLogVo> page(LoginLogPageRequest request) {
        if (request.getStartTime() != null && request.getEndTime() != null
                && request.getStartTime() > request.getEndTime()) {
            throw new BizException(ResultCode.PARAM_INVALID, "开始时间不能晚于结束时间");
        }
        Page<LoginLog> page = loginLogDao.page(request.getUserId(), request.getUsername(), request.getSuccess(),
                request.getLoginIp(), request.getStartTime(), request.getEndTime(),
                request.getCurrent(), request.getSize());
        List<LoginLogVo> records = page.getRecords().stream().map(LoginLogConverter::toVo).toList();
        return PageResult.of(page.getCurrent(), page.getSize(), page.getTotal(), records);
    }

    @Override
    public LoginLogVo detail(String loginLogId) {
        LoginLog log = loginLogDao.findById(loginLogId)
                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));
        return LoginLogConverter.toVo(log);
    }
}
