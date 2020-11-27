package com.imooc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by liujingmao on 2019-11-11.
 */
@Configuration
@EnableSwagger2
public class Swagger2 {

    // 配置docket
    @Bean
    public Docket createRestAPI(){
        return new Docket(DocumentationType.SWAGGER_2) //指定荥API 类型为swagger2
                .apiInfo(apiInfo())//用于定义API文档汇总
                .select()
                .apis(RequestHandlerSelectors
                        .basePackage("com.imooc.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("天天吃货 电商平台接口api")
                .contact(new Contact("imooc","https://www.imooc.com","abc@imooc.com"))
                .description("专为天天吃货提供的api文档")
                .version("1.0.1")
                .termsOfServiceUrl("https://www.imooc.com")
                .build();

    }


}
