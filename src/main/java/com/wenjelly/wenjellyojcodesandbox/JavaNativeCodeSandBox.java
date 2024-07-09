package com.wenjelly.wenjellyojcodesandbox;

import cn.hutool.core.io.resource.ResourceUtil;
import com.wenjelly.wenjellyojcodesandbox.model.ExecutedCodeRequest;
import com.wenjelly.wenjellyojcodesandbox.model.ExecutedCodeResponse;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Java原生实现，不对模板做修改
 */
@Component
public class JavaNativeCodeSandBox extends JavaCodeSandBoxTemplate {

    // 试运行代码
    public static void main(String[] args) {
        JavaNativeCodeSandBox javaNativeCodeSandBox = new JavaNativeCodeSandBox();
        ExecutedCodeRequest executedCodeRequest = new ExecutedCodeRequest();
        executedCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        executedCodeRequest.setCode(code);
        executedCodeRequest.setLanguage("java");
        ExecutedCodeResponse executedCodeResponse = javaNativeCodeSandBox.executedCode(executedCodeRequest);
        System.out.println(executedCodeResponse);
    }

    @Override
    public File savaCodeToFile(String code) {
        File file = super.savaCodeToFile(code);
        System.out.println("做你想做的事情");
        return file;
    }

    @Override
    public ExecutedCodeResponse executedCode(ExecutedCodeRequest executedCodeRequest) {
        return super.executedCode(executedCodeRequest);
    }
}
