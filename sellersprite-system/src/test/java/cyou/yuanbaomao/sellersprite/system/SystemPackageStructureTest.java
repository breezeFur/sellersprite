package cyou.yuanbaomao.sellersprite.system;

import static org.assertj.core.api.Assertions.assertThat;

import cyou.yuanbaomao.sellersprite.system.auth.controller.AuthController;
import cyou.yuanbaomao.sellersprite.system.dept.controller.DeptController;
import cyou.yuanbaomao.sellersprite.system.dict.controller.DictController;
import cyou.yuanbaomao.sellersprite.system.permission.controller.PermissionController;
import cyou.yuanbaomao.sellersprite.system.role.controller.RoleController;
import cyou.yuanbaomao.sellersprite.system.user.controller.UserController;
import org.junit.jupiter.api.Test;

class SystemPackageStructureTest {

    private static final String SYSTEM_PACKAGE_PREFIX = "cyou.yuanbaomao.sellersprite.system.";

    @Test
    void shouldOrganizeControllersByBusinessDomain() {
        assertThat(AuthController.class.getPackageName()).isEqualTo(SYSTEM_PACKAGE_PREFIX + "auth.controller");
        assertThat(UserController.class.getPackageName()).isEqualTo(SYSTEM_PACKAGE_PREFIX + "user.controller");
        assertThat(RoleController.class.getPackageName()).isEqualTo(SYSTEM_PACKAGE_PREFIX + "role.controller");
        assertThat(DeptController.class.getPackageName()).isEqualTo(SYSTEM_PACKAGE_PREFIX + "dept.controller");
        assertThat(DictController.class.getPackageName()).isEqualTo(SYSTEM_PACKAGE_PREFIX + "dict.controller");
        assertThat(PermissionController.class.getPackageName())
                .isEqualTo(SYSTEM_PACKAGE_PREFIX + "permission.controller");
    }
}
