package com.zz.zzojcodesandbox;

import com.zz.zzojcodesandbox.model.ExecuteCodeRequest;
import com.zz.zzojcodesandbox.model.ExecuteCodeResponse;

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
