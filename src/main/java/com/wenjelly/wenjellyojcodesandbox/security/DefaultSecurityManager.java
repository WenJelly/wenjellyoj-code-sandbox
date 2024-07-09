package com.wenjelly.wenjellyojcodesandbox.security;

/*
 * @time 2023/11/27 14:43
 * @package com.wenjelly.wenjellyojcodesandbox.security
 * @project wenjellyoj-code-sandbox
 * @author WenJelly
 */

import java.security.Permission;

public class DefaultSecurityManager extends SecurityManager {

    // 检查所有权限
    @Override
    public void checkPermission(Permission perm) {
        System.out.println("默认不做任何权限限制");
//        super.checkPermission(perm);
        // 如果有想要不让用户做的权限就抛异常
        throw new SecurityException("权限不足" + perm.getActions());
    }
}
