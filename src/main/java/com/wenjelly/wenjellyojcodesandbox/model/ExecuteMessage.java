package com.wenjelly.wenjellyojcodesandbox.model;

/*
 * @time 2023/11/25 20:43
 * @package com.wenjelly.wenjellyojcodesandbox.model
 * @project wenjellyoj-code-sandbox
 * @author WenJelly
 */

import lombok.Data;

/**
 * 进程执行信息
 */

@Data
public class ExecuteMessage {

    // 返回码 0为成功 1为失败
    private Integer exitValue;
    // 执行成功后的信息
    private String message;
    // 错误信息
    private String errorMessage;
    // 执行时间
    private Long time;
    // 执行内存
    private Long memory;
}
