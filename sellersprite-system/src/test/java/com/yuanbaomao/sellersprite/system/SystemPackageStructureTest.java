package com.yuanbaomao.sellersprite.system;

import static org.assertj.core.api.Assertions.assertThat;

import com.yuanbaomao.sellersprite.system.auth.controller.AuthController;
import com.yuanbaomao.sellersprite.system.dept.controller.DeptController;
import com.yuanbaomao.sellersprite.system.dict.controller.DictController;
import com.yuanbaomao.sellersprite.system.permission.controller.PermissionController;
import com.yuanbaomao.sellersprite.system.role.controller.RoleController;
import com.yuanbaomao.sellersprite.system.user.controller.UserController;
import org.junit.jupiter.api.Test;

class SystemPackageStructureTest {

    private static final String SYSTEM_PACKAGE_PREFIX = "com.yuanbaomao.sellersprite.system.";

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
