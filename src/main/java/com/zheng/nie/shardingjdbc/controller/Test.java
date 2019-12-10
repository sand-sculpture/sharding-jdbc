package com.zheng.nie.shardingjdbc.controller;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

/**
 * @author: niezheng1
 * @Date: 2019/12/9 19:44
 */
@RestController
public class Test {


    @Autowired
    private StandardServletMultipartResolver multipartResolver;

    @RequestMapping("test")
    public String test(){
        System.out.println(multipartResolver);
        return "sss";
    }

}
