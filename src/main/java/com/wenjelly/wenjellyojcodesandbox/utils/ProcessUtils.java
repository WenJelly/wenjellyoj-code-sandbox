package com.wenjelly.wenjellyojcodesandbox.utils;

/*
 * @time 2023/11/25 20:41
 * @package com.wenjelly.wenjellyojcodesandbox.utils
 * @project wenjellyoj-code-sandbox
 * @author WenJelly
 */

import cn.hutool.core.util.StrUtil;
import com.wenjelly.wenjellyojcodesandbox.model.ExecuteMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {

    /**
     * 执行进程并获取进程结果信息
     *
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess, String opName) {

        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            // 获取程序执行时间
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            // java获取控制台的输出
            // 意思是等待程序执行完成、得到一个返回码，可以根据返回码来判断程序是正常退出还是异常退出
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            if (exitValue == 0) {
                // 正常退出
                System.out.println(opName + "成功");
                // 分批获取进程的正常输出结果
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                List<String> outputStrList = new ArrayList<>();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }

                // 将程序成功后将执行结果设置到executeMessage中
                executeMessage.setMessage(StringUtils.join(outputStrList, "\n"));
            } else {
                // 异常退出
                System.out.println(opName + "失败，错误码" + exitValue);
                // 分批获取进程的错误输出结果，这里的错误是指答案错误
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));

                List<String> outputStrList = new ArrayList<>();
                // 逐行读取
                String compileOutputLine;
                while ((compileOutputLine = bufferedReader.readLine()) != null) {
                    outputStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(outputStrList, "\n"));


                // 分批获取进程的错误输出，这里是进程错误
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));

                List<String> errorOutputStrList = new ArrayList<>();
                String errorCompileOutputLine;
                while ((errorCompileOutputLine = errorBufferedReader.readLine()) != null) {
                    errorOutputStrList.add(errorCompileOutputLine);
                }
                // 将程序失败后将执行错误信息设置到executeMessage中
                executeMessage.setErrorMessage(StringUtils.join(errorOutputStrList, "\n"));
            }
            stopWatch.stop();
            // 设置程序运行时间
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 返回程序编译、执行后的封装结果
        return executeMessage;
    }


    /**
     * 执行交互式进程
     *
     * @param runProcess
     * @param opName
     * @return
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String opName, String args) {

        ExecuteMessage executeMessage = new ExecuteMessage();

        try {
            InputStream inputStream = runProcess.getInputStream();
            // 向控制台输入程序
            OutputStream outputStream = runProcess.getOutputStream();

            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            String[] s = args.split(" ");
            String join = StrUtil.join("\n" + s + "\n");
            outputStreamWriter.write(join);
            // 相当于按了回车
            outputStreamWriter.flush();

            // 分批获取进程的正常输出
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder compileOutputStringBuilder = new StringBuilder();
            // 逐行读取
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilder.append(compileOutputLine);
            }

            executeMessage.setMessage(compileOutputStringBuilder.toString());
            // 释放资源
            outputStreamWriter.close();
            outputStream.close();
            inputStream.close();
            runProcess.destroy();
        } catch (
                Exception e) {
            e.printStackTrace();
        }

        return executeMessage;
    }

}
