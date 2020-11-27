package com.imooc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * Created by liujingmao on 2019-11-09.
 * 这是Spring的入口类
 */
@SpringBootApplication
//扫描 mybatis 通用mapper所在的包
@MapperScan(basePackages = "com.imooc.mapper")
//扫描所有包及相关组件
@ComponentScan(basePackages = {"com.imooc","org.n3r.idworker"})
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
    }
}
