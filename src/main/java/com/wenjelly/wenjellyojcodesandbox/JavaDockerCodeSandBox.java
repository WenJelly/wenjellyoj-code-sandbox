package com.wenjelly.wenjellyojcodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ArrayUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.wenjelly.wenjellyojcodesandbox.model.ExecuteMessage;
import com.wenjelly.wenjellyojcodesandbox.model.ExecutedCodeRequest;
import com.wenjelly.wenjellyojcodesandbox.model.ExecutedCodeResponse;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 使用模板方法来实现
 */
@Component
public class JavaDockerCodeSandBox extends JavaCodeSandBoxTemplate {

    public static final String GLOBAL_CODE_DIR_NAME = "tmpCode";

    public static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    // 程序最大运行时间，超过这个时间不就让他运行
    public static final long TIME_OUT = 10000L;

    // SecurityManage的class路径
    public static final String SECURITY_MANAGER_PATH = "D:\\Development\\IDEAJavaProjects\\wenjellyoj-code-sandbox\\src\\main\\resources\\security";

    public static final String SECURITY_MANAGER_CLASS_NAME = "MySecurity";

    // 第一次初始化镜像
    public static final Boolean FIRST_INIT = true;

    // 试运行代码
    public static void main(String[] args) {
        JavaDockerCodeSandBox javaDockerCodeSandBox = new JavaDockerCodeSandBox();
        ExecutedCodeRequest executedCodeRequest = new ExecutedCodeRequest();
        executedCodeRequest.setInputList(Arrays.asList("1 2", "1 3"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        executedCodeRequest.setCode(code);
        executedCodeRequest.setLanguage("java");
        ExecutedCodeResponse executedCodeResponse = javaDockerCodeSandBox.executedCode(executedCodeRequest);
        System.out.println(executedCodeResponse);
    }

    /**
     * 重新模板的方法，这个是Docker实现的，用于创建Docker容器后在容器里面执行Java代码
     * 3. 创建容器，把文件复制到容器内
     *
     * @param userCodeFile
     * @param inputList
     * @return
     */
    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {

        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        // 创建容器，把文件复制到容器内
        // 获取默认的DockerClient
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        // 创建镜像
        String image = "openjdk:17-alpine";
        if (FIRST_INIT) {
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            // 创建回调，当进程执行完之后就会执行回调
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像" + item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd
                        .exec(pullImageResultCallback)
                        // 意思是如果没下载完就会一直阻塞在这
                        .awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
        }
        System.out.println("下载完成");
        // 创建容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        hostConfig.withMemory(100 * 1000 * 1000L);
        // 内存交换
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        // 安全管理字符串需要替换成对应的
//        hostConfig.withSecurityOpts(Arrays.asList("seccomp=安全管理字符串"));
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                // 禁用容器使用网络
                .withNetworkDisabled(true)
                // 限制用户不能向root根目录写文件（java安全管理器）
                .withReadonlyRootfs(true)
                // 创建可交互容器
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .withCmd("/bin/sh")
                .exec();
        System.out.println(createContainerResponse);

        String containerId = createContainerResponse.getId();
        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();

        // 执行容器
        // 获取输入参数,执行代码
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            // 获取程序执行时间
            StopWatch stopWatch = new StopWatch();
            // 通过空格来分割需要传入被执行的args参数列表
            String[] inputArgsArray = inputArgs.split(" ");
            // 执行java代码的指令
            String[] cmdArray = ArrayUtil.append(new String[]{"java", "-cp", "/app", "Main"}, inputArgsArray);
            // 创建Docker容器执行指令
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    // 具体指令
                    .withCmd(cmdArray)
                    // 创建交互式Docker Terminal
                    .withAttachStdin(true)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    // 执行
                    .exec();
            System.out.println("创建命令" + execCreateCmdResponse);

            // 返回值
            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] message = {null};
            final String[] errorMessage = {null};
            // 程序执行时间
            long time = 0l;
            // 默认为ture，表示默认为超时，如果在规定时间内完成了程序，就设置为false
            final boolean[] timeout = {true};
            String id = execCreateCmdResponse.getId();

            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {

                // 当程序执行完后会执行这个方法
                @Override
                public void onComplete() {
                    // 如果在规定时间内完成了程序，就设置为false
                    timeout[0] = false;
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessage[0] = new String(frame.getPayload());
                        System.out.println("输出错误结果:" + errorMessage[0]);
                    } else {
                        message[0] = new String(frame.getPayload());
                        System.out.println("输出结果:" + message[0]);
                    }
                    super.onNext(frame);
                }
            };
            final long[] maxMemory = {0l};
            // 获取占用的内存
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用" + statistics.getMemoryStats().getUsage());
                    maxMemory[0] = Math.max(statistics.getMemoryStats().getUsage(), maxMemory[0]);
                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }

                @Override
                public void close() throws IOException {

                }
            });
            try {
                stopWatch.start();
                dockerClient.execStartCmd(id)
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                statsCmd.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            executeMessage.setMessage(message[0]);
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessageList.add(executeMessage);
            executeMessage.setTime(time);
            executeMessage.setMemory(maxMemory[0]);
        }
        return executeMessageList;
    }
}
