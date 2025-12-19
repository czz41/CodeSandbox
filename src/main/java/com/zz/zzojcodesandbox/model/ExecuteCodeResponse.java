package com.zz.zzojcodesandbox.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecuteCodeResponse {
    /**
     * 运行结果
     */
    private List<String> outputList;
    /**
     * 状态 0 - 待处理、1 - 执行中、2 - 成功、3 - 失败
     */
    private Integer status;
    /**
     * 判题信息
     */
    private JudgeInfo judgeInfo;
    /**
     * 沙箱错误信息
     */
    private String message;
}
