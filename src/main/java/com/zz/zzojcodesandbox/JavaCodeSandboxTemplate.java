package com.zz.zzojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.zz.zzojcodesandbox.model.ExecuteCodeRequest;
import com.zz.zzojcodesandbox.model.ExecuteCodeResponse;
import com.zz.zzojcodesandbox.model.ExecuteMessage;
import com.zz.zzojcodesandbox.model.JudgeInfo;
import com.zz.zzojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * java代码沙箱模板方法的实现
 */
@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox{

    private  static final String GLOBAL_CODE_DIR_NAME="tmpCode";

    private  static final String GLOBAL_JAVA_CLASS_NAME="Main.java";

    private static final Long TIME_OUT=5000L;

    /**
     * 1.把用户代码保存为文件
     * @return
     */
    public File saveCodeToFile(String code){
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
        return userCodeFile;
    }

    /**
     * 2.编译代码得到class文件
     * @param userCodeFile
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile){
        //String compiledCmd=String.format("javac -encoding utf-8 -J-Dfile.encoding=UTF-8 %s",userCodeFile.getAbsoluteFile());
        String compiledCmd=String.format("javac -encoding utf-8 -J-Dfile.encoding=UTF-8 %s",userCodeFile.getAbsoluteFile());
        try {
            Process compileProcess=Runtime.getRuntime().exec(compiledCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            if(executeMessage.getExitValue()!=0){
                throw new RuntimeException("编译错误");
            }
            return executeMessage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 3.执行代码得到输出结果
     * @param userCodeFile
     * @param inputList
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile,List<String>inputList){
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage>executeMessageList=new ArrayList<>();
        for(String inputArgs:inputList){
            String runCmd=String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s",userCodeParentPath,inputArgs);
            //String runCmd=String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s",userCodeParentPath,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS_NAME,inputArgs);

            try {
                Process runProcess=Runtime.getRuntime().exec(runCmd);
                new Thread(()->{  //防止恶意死循环代码
                    try {
                        Thread.sleep(TIME_OUT);
                        System.out.println("代码运行超时");
                        runProcess.destroy();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();;
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "运行");
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                throw new RuntimeException("执行错误",e);
            }
        }
        return executeMessageList;
    }

    /**
     * 4.获取输出结果
     * @param executeMessageList
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList){
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
        //judgeInfo.setMemory();
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }

    /**
     * 5.清理文件
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile){
        if(userCodeFile.getParentFile()!=null){
            String userCodeParentPath=userCodeFile.getParentFile().getAbsolutePath();
            boolean del=FileUtil.del(userCodeParentPath);
            System.out.println("删除"+(del?"成功":"失败"));
            return del;
        }
        return true;
    }

    /**
     * 6.获取错误响应
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

    /**
     * 主要流程
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String language = executeCodeRequest.getLanguage();
        String code = executeCodeRequest.getCode();
        //1.把用户代码保存为文件
        File userCodeFile=saveCodeToFile(code);
        //2.编译代码得到class文件
        ExecuteMessage executeMessage=compileFile(userCodeFile);
        System.out.println(executeMessage);
        //3.执行代码得到输出结果
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);
        //4.获取输出结果
        ExecuteCodeResponse outputResponse=getOutputResponse(executeMessageList);
        //5.文件清理
        boolean b=deleteFile(userCodeFile);
        if(!b){
            log.error("deleteFile error,useCodeFilePath = {}",userCodeFile.getAbsoluteFile());
        }
        return outputResponse;
    }

}
