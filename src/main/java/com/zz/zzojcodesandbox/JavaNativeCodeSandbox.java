package com.zz.zzojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import com.zz.zzojcodesandbox.model.ExecuteCodeRequest;
import com.zz.zzojcodesandbox.model.ExecuteCodeResponse;
import com.zz.zzojcodesandbox.model.ExecuteMessage;
import com.zz.zzojcodesandbox.model.JudgeInfo;
import com.zz.zzojcodesandbox.utils.ProcessUtils;
import org.springframework.util.StringUtils;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class JavaNativeCodeSandbox implements CodeSandbox {

    private  static final String GLOBAL_CODE_DIR_NAME="tmpCode";
    private  static final String GLOBAL_JAVA_CLASS_NAME="Main.java";

    //测试程序
    public static void main(String[] args) {
        JavaNativeCodeSandbox javaNativeCodeSandbox=new JavaNativeCodeSandbox();
        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 2","2 3"));
        String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String language = executeCodeRequest.getLanguage();
        String code = executeCodeRequest.getCode();
        String userDir= System.getProperty("user.dir");
        String globalCodePathName=userDir+ File.separator+GLOBAL_CODE_DIR_NAME;
        //新建全局代码目录
        if(FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }
        //新建目录，每个用户代码放在一个文件夹下，生成代码文件
        String userCodeParentPath = globalCodePathName+File.separator+ UUID.randomUUID();
        String userCodePath = userCodeParentPath+File.separator+GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile=FileUtil.writeString(code,userCodePath, StandardCharsets.UTF_8);

        //编译代码得到class文件
        String compiledCmd=String.format("javac -encoding utf-8 %s",userCodeFile.getAbsoluteFile());
        try {
            Process compileProcess=Runtime.getRuntime().exec(compiledCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
        } catch (Exception e) {
            return getErrorResponse(e);
        }
        //运行代码
        List<ExecuteMessage>executeMessageList=new ArrayList<>();
        for(String inputArgs:inputList){
            String runCmd=String.format("java -Dfile.encoding=UTF-8 -cp %s Main %s",userCodeParentPath,inputArgs);
            try {
                Process runProcess=Runtime.getRuntime().exec(runCmd);
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                //ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess,inputArgs);
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                return getErrorResponse(e);
            }
        }
        //收集整理输出的结果
        ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
        List<String>outputList=new ArrayList<>();
        //取最大值判断是否超时
        long maxTime=0;
        for(ExecuteMessage executeMessage:executeMessageList){
            String errorMessage=executeMessage.getErrorMessage();
            if(StrUtil.isNotBlank(errorMessage)){
                executeCodeResponse.setMessage(errorMessage);
                //用户提交代码执行中存在错误
                executeCodeResponse.setStatus(3);
                break;
            }
            outputList.add(executeMessage.getMessage());
            Long time=executeMessage.getTime();
            if(time!=null){
                maxTime=Math.max(maxTime,time);
            }
        }
        //正常运行完成
        if(outputList.size()==executeMessageList.size()){
            executeCodeResponse.setStatus(1);
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo=new JudgeInfo();
        judgeInfo.setTime(maxTime);
        //要借助第三方库来获取内存，过于麻烦，不做展示
//        judgeInfo.setMemory();
        executeCodeResponse.setJudgeInfo(judgeInfo);
        //5.文件清理
        if(userCodeFile.getParentFile()!=null){
            boolean del=FileUtil.del(userCodeParentPath);
            System.out.println("删除"+(del?"成功":"失败"));
        }
        //6.错误处理，提升健壮性
        return executeCodeResponse;
    }
    /**
     * 获取错误响应
     * @param e
     * @return
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        //表示代码沙箱错误（可能是编译错误）
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;
    }
}
