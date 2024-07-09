package com.wenjelly.wenjellyojcodesandbox.security;

/*
 * @time 2023/11/27 14:43
 * @package com.wenjelly.wenjellyojcodesandbox.security
 * @project wenjellyoj-code-sandbox
 * @author WenJelly
 */

import java.security.Permission;

/**
 * 所有权限都拒绝
 */
public class DenySecurityManager extends SecurityManager {

    // 检查所有权限
    @Override
    public void checkPermission(Permission perm) {
        throw new SecurityException("权限不足" + perm.getActions());
    }
}
