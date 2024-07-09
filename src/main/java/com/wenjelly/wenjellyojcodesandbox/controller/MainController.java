package com.wenjelly.wenjellyojcodesandbox.controller;

/*
 * @time 2023/11/25 16:58
 * @package com.wenjelly.wenjellyojcodesandbox.controller
 * @project wenjellyoj-code-sandbox
 * @author WenJelly
 */

import com.wenjelly.wenjellyojcodesandbox.JavaDockerCodeSandBox;
import com.wenjelly.wenjellyojcodesandbox.JavaNativeCodeSandBox;
import com.wenjelly.wenjellyojcodesandbox.model.ExecutedCodeRequest;
import com.wenjelly.wenjellyojcodesandbox.model.ExecutedCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RestController("/")
public class MainController {

    // 保证安全性，增加安全头
    public static final String AUTH_REQUEST_HEADER = "auth";

    public static final String AUTH_REQUEST_SECRET = "secretKey";

    @Resource
//    private CodeSandBox codeSandBox;
    private JavaNativeCodeSandBox javaNativeCodeSandBox;

    @Resource
    private JavaDockerCodeSandBox javaDockerCodeSandBox;

    @GetMapping("/health")
    public String healthController() {
        return "OK";
    }

    /**
     * 需要返回一个ExecutedCodeResponse对象
     *
     * @param executedCodeRequest
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/executeCode")
    ExecutedCodeResponse executedCode(@RequestBody ExecutedCodeRequest executedCodeRequest,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        // 认证
//        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
//        if(!AUTH_REQUEST_HEADER.equals(authHeader)){
//            response.setStatus(403);
//            return null;
//        }

        // 先创建好ExecutedCodeResponse对象
        ExecutedCodeResponse executedCodeResponse = new ExecutedCodeResponse();

        // 先判断请求参数是否为空
        if (executedCodeRequest == null) {
            System.out.println("传递给代码沙箱的ExecutedCodeRequest为空");
            executedCodeResponse.setMessage("传递给代码沙箱的ExecutedCodeRequest为空");
            return executedCodeResponse;
        }
        // 原生代码沙箱实现
        executedCodeResponse = javaNativeCodeSandBox.executedCode(executedCodeRequest);
        // 使用Docker代码沙箱实现
//        ExecutedCodeResponse executedCodeResponse = javaDockerCodeSandBox.executedCode(executedCodeRequest);
        System.out.println(executedCodeResponse);
        return executedCodeResponse;
    }

}
