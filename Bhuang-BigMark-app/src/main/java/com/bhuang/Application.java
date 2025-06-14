package com.bhuang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@Configurable
@ComponentScan(basePackages = {"com.bhuang"})  // 扫描所有com.bhuang包下的组件
@MapperScan("com.bhuang.infrastructure.persistent.dao")
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class);
    }

}
