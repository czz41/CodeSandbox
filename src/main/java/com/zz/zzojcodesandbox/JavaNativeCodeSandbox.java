package com.zz.zzojcodesandbox;

import com.zz.zzojcodesandbox.model.ExecuteCodeRequest;
import com.zz.zzojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

/**
 * java 原生代码沙箱实现（直接复用模板方法）
 */
@Component
public class JavaNativeCodeSandbox extends JavaCodeSandboxTemplate {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
