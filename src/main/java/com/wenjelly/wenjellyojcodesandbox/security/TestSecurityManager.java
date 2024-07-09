package com.wenjelly.wenjellyojcodesandbox.security;

/*
 * @time 2023/11/27 15:01
 * @package com.wenjelly.wenjellyojcodesandbox.security
 * @project wenjellyoj-code-sandbox
 * @author WenJelly
 */

import cn.hutool.core.io.FileUtil;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestSecurityManager {

    public static void main(String[] args) {
        System.setSecurityManager(new MySecurityManager());

        List<String> s = FileUtil.readLines("D:\\Development\\IDEAJavaProjects\\wenjellyoj-code-sandbox\\src\\main\\resources\\application.yml", StandardCharsets.UTF_8);
        System.out.println(s);
    }

}
