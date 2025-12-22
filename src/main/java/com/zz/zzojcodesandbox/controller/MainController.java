package com.zz.zzojcodesandbox.controller;

import com.zz.zzojcodesandbox.JavaNativeCodeSandbox;
import com.zz.zzojcodesandbox.model.ExecuteCodeRequest;
import com.zz.zzojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("/")
public class MainController {

    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody  ExecuteCodeRequest executeCodeRequest){
        if(executeCodeRequest==null){
            throw new RuntimeException("请求参数为空");
        }
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        return executeCodeResponse;
    }

}
