package com.wenjelly.wenjellyojcodesandbox.security;

/*
 * @time 2023/11/27 14:56
 * @package com.wenjelly.wenjellyojcodesandbox.security
 * @project wenjellyoj-code-sandbox
 * @author WenJelly
 */

import java.security.Permission;

public class MySecurityManager extends SecurityManager {
    // 检查所有权限

    @Override
    public void checkPermission(Permission perm) {
        System.out.println("先放行");
//        super.checkPermission(perm);
    }

    // 检查程序是否可执行文件
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("checkExec权限不足" + cmd);
    }

    // 检查程序是否读文件

    @Override
    public void checkRead(String file) {
        throw new SecurityException("checkRead权限不足" + file);
    }

    // 检查程序是否允许写文件
    @Override
    public void checkWrite(String file) {
        throw new SecurityException("checkWrite权限不足" + file);
    }

    // 检查程序是否可删除文件

    @Override
    public void checkDelete(String file) {
        throw new SecurityException("checkDelete权限不足" + file);
    }

    // 检查程序是否可以连接网络

    @Override
    public void checkConnect(String host, int port) {
        throw new SecurityException("checkConnect权限不足" + host + "  " + port);
    }
}
