package com.wenjelly.wenjellyojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.wenjelly.wenjellyojcodesandbox.model.ExecuteMessage;
import com.wenjelly.wenjellyojcodesandbox.model.ExecutedCodeRequest;
import com.wenjelly.wenjellyojcodesandbox.model.ExecutedCodeResponse;
import com.wenjelly.wenjellyojcodesandbox.model.JudgeInfo;
import com.wenjelly.wenjellyojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 模板方法类
 */
@Slf4j
public abstract class JavaCodeSandBoxTemplate implements CodeSandBox {
    public static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    // 程序最大运行时间，超过这个时间不就让他运行
    public static final long TIME_OUT = 10000L;

    // todo 返回给单体后端的响应消息体 后面设置到方法里面
    private ExecutedCodeResponse executedCodeResponse = new ExecutedCodeResponse();

    /**
     * 整体运作流程
     *
     * @param executedCodeRequest
     * @return
     */
    @Override
    public ExecutedCodeResponse executedCode(ExecutedCodeRequest executedCodeRequest) {

        // 得到输入实例
        List<String> inputList = executedCodeRequest.getInputList();
        // 得到用户代码
        String code = executedCodeRequest.getCode();
        // 得到用户编程语言
        String language = executedCodeRequest.getLanguage();

        // 1. 把用户的代码保存为文件
        File userCodeFile = savaCodeToFile(code);

        // 2. 编译代码、得到class文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        System.out.println(compileFileExecuteMessage);

        // 判断是否编译失败，如果编译失败的话，就不用执行下面的语句了
        if (StrUtil.isNotBlank(compileFileExecuteMessage.getErrorMessage())) {
            // 将翻译失败的信息传递给响应消息体，并直接返回
            executedCodeResponse.setMessage("编译错误");
            // 删除文件
            boolean b = deleteFile(userCodeFile);
            if (!b) {
                log.error("deleteFile error,userCodeFilePath = {}", userCodeFile.getAbsoluteFile());
            }
            return executedCodeResponse;
        }

        // 3. 执行代码、得到输出结果
        List<ExecuteMessage> executeMessagesList = runFile(userCodeFile, inputList);

        // 4.收集整理输出结果
        ExecutedCodeResponse outputResponse = getOutputResponse(executeMessagesList);

        // 5. 文件清理
        boolean b = deleteFile(userCodeFile);
        if (!b) {
            log.error("deleteFile error,userCodeFilePath = {}", userCodeFile.getAbsoluteFile());
        }
        return outputResponse;
    }


    /**
     * 1. 把用户的代码保存为文件
     *
     * @param code
     * @return
     */
    public File savaCodeToFile(String code) {
        // 获取用户当前的根目录 记住，不要有空格，不然编译时会报错，找不到文件路径
        String userDir = System.getProperty("user.dir");
//        String userDir = "D:/Development";
        String globalCodePathName = userDir + File.separator + GLOBAL_CODE_DIR_NAME;
        // 判断全局代码目录是否存在
        if (!FileUtil.exist(globalCodePathName)) {
            // 如果不存在就新建
            FileUtil.mkdir(globalCodePathName);
        }
        // 把用户的代码隔离存放
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    /**
     * 2. 编译代码、得到class文件
     *
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile) {

        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsolutePath());
        try {
            // java执行程序
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            // 操作控制台，执行操作，并获取操作台信息
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            // 根据返回码来判断程序是正常退出还是异常退出，不如不等于0就是异常退出，说明编译失败
            if (executeMessage.getExitValue() != 0) {
                // 设置编译错误状态信息
//                executeMessage.setErrorMessage("编译错误");
            }
            return executeMessage;
        } catch (Exception e) {
            // 抛异常
            throw new RuntimeException(e);
        }
    }

    /**
     * 3. 执行代码、得到输出结果
     *
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        // 获取待执行文件路径
        File userCodeParentPath = userCodeFile.getParentFile().getAbsoluteFile();
        // 创建一个执行信息封装列表（因为可能会输出多条执行结果）
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        // 对每一次输入用例进行一次执行
        for (String inputArgs : inputList) {
            // -Defile.encoding=UTF-8 解决乱码
            // -Xmx1024m 是指最大堆空间为1024兆
            String runCmd = String.format("java -Xmx1024m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);

            // 检查权限（不建议使用SecurityManager）
            // 只限制子程序，也就是用户的程序
//        System.setSecurityManager(new DefaultSecurityManager());
            //  可以加入到命令中
//            String runCmd = String.format("java -Xmx1024m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s", userCodeParentPath, inputArgs,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS_NAME);
            try {

                // 得到一个运行进程
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                // 这是一个监控线程，超时控制
                new Thread(() -> {
                    try {
                        // 先让监控线程睡最大时间
                        Thread.sleep(TIME_OUT);
                        System.out.println("超时了，中断");
                        // 睡醒后直接杀死子进程
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();

                // 交互式的执行
//                ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, "运行", inputArgs);
                // 非交互式的
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                // 如果此次执行中含有错误信息，就停止循环，并将错误信息直接返回
                if (StrUtil.isNotBlank(executeMessage.getErrorMessage())) {
                    System.out.println(executeMessage);
                    // 将每次信息都加入到列表中
                    executeMessageList.add(executeMessage);
                    return executeMessageList;
                }
                System.out.println(executeMessage);
                // 将每次信息都加入到列表中
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                throw new RuntimeException("执行错误：", e);
            }
        }
        return executeMessageList;
    }

    /**
     * 4. 收集整理输出结果
     *
     * @param executeMessageList
     * @return
     */
    public ExecutedCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecutedCodeResponse executedCodeResponse = new ExecutedCodeResponse();
        List<String> outputList = new ArrayList<>();
        // 求所有用例中最大的运行时长
        Long maxTime = 0l;
        for (ExecuteMessage executeMessage : executeMessageList) {
            // todo 如果有错误信息
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                // 上面设置了如果编译错误就直接返回来，不再执行代码了，所以能进入到这个语句说明执行代码中出现错误，将运行错误设置到消息体中
                executedCodeResponse.setMessage("运行错误");
                // 然后直接把executedCodeResponse返回
                return executedCodeResponse;
            }
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null) {
                // 原始值和新值进行比较
                maxTime = Math.max(maxTime, time);
            }
        }
        // 每条都正常输出
        if (outputList.size() == executeMessageList.size()) {
            // todo 这里还不知道要干嘛
//            executedCodeResponse.setStatus(1);
        }
        // 这代码好高级，从list取出每一个输出信息,从中取出输出结果
//        List<String> outputList = executeMessageList.stream().map(ExecuteMessage::getMessage)
//                .collect(Collectors.toList());
        executedCodeResponse.setOutputList(outputList);
        // 表示正常执行
        JudgeInfo judgeInfo = new JudgeInfo();
        // 获取程序的执行内存（超级麻烦），要借助第三方库
//        judgeInfo.setMemory();
        // 获取程序的执行时间
        judgeInfo.setTime(maxTime);
        executedCodeResponse.setJudgeInfo(judgeInfo);
        executedCodeResponse.setMessage("运行成功");
        return executedCodeResponse;
    }

    /**
     * 5. 文件清理
     *
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            File userCodeParentPath = userCodeFile.getParentFile().getAbsoluteFile();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    /**
     * 6. 获取错误响应
     *
     * @param e
     * @return
     */
    private ExecutedCodeResponse getErrorResponse(Throwable e) {
        ExecutedCodeResponse executedCodeResponse = new ExecutedCodeResponse();
        executedCodeResponse.setOutputList(new ArrayList<>());
        executedCodeResponse.setMessage(e.getMessage());
        // 表示代码沙箱错误
        executedCodeResponse.setStatus(2);
        executedCodeResponse.setJudgeInfo(new JudgeInfo());

        return executedCodeResponse;

    }
}
