package cyou.yuanbaomao.sellersprite.system.user.service;

import cyou.yuanbaomao.mybatis.result.YPage;
import cyou.yuanbaomao.sellersprite.system.user.model.dto.UserCreateRequest;
import cyou.yuanbaomao.sellersprite.system.user.model.dto.UserUpdateRequest;
import cyou.yuanbaomao.sellersprite.system.user.model.vo.UserDetailVo;
import java.util.Collection;

public interface UserService {

    UserDetailVo create(UserCreateRequest request);

    UserDetailVo detail(String userId);

    YPage<UserDetailVo> page(YPage<UserDetailVo> page, String username, Integer status);

    UserDetailVo update(String userId, UserUpdateRequest request);

    void updateStatus(String userId, Integer status);

    void resetPassword(String userId, String newPassword);

    void replaceRoles(String userId, Collection<String> roleIds);

    void delete(String userId);
}
