package com.zz.zzojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecuteCodeRequest {
    /**
     * 输入用例
     */
    private List<String> inputList;
    /**
     * 编程语言
     */
    private String language;
    /**
     * 用户代码
     */
    private String code;
}
