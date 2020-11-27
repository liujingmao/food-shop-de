package com.imooc.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by liujingmao on 2019-11-09.
 */

@RestController
public class HelloController {
    @GetMapping("/hello")
    public Object hello(){
       return "Hello World";
    }
}
