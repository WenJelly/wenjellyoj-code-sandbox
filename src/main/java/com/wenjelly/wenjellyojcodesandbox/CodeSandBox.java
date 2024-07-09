package com.wenjelly.wenjellyojcodesandbox;

/*
 * @time 2023/11/24 0:58
 * @package com.yupi.wenjellyoj.judge.codesandbox
 * @project wenjellyoj-backend
 * @author WenJelly
 */


import com.wenjelly.wenjellyojcodesandbox.model.ExecutedCodeRequest;
import com.wenjelly.wenjellyojcodesandbox.model.ExecutedCodeResponse;

public interface CodeSandBox {

    /**
     * 执行代码
     *
     * @param executedCodeRequest
     * @return
     */
    ExecutedCodeResponse executedCode(ExecutedCodeRequest executedCodeRequest);
}
