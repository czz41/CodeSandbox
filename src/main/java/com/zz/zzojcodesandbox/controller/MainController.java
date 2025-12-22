package com.zz.zzojcodesandbox.controller;

import com.zz.zzojcodesandbox.JavaNativeCodeSandbox;
import com.zz.zzojcodesandbox.model.ExecuteCodeRequest;
import com.zz.zzojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class MainController {

    private static final String AUTH_REQUEST_HEADER="auth";
    private static final String AUTH_REQUEST_SECRET="secretKey";
    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    @PostMapping("/executeCode")
    ExecuteCodeResponse executeCode(@RequestBody  ExecuteCodeRequest executeCodeRequest,
                                    HttpServletRequest request,
                                    HttpServletResponse response){
        //基本的认证
        String authHeader=request.getHeader(AUTH_REQUEST_HEADER);
        if(!AUTH_REQUEST_SECRET.equals(authHeader)){
            response.setStatus(403);
            return null;
        }
        if(executeCodeRequest==null){
            throw new RuntimeException("请求参数为空");
        }
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        return executeCodeResponse;
    }

}
