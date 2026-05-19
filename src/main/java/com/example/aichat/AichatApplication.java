package com.example.aichat;  // ✅ 必须和项目创建时一致，不要乱改！

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@MapperScan("com.example.aichat.mapper")
@EnableAsync
public class AichatApplication {
    public static void main(String[] args) {
        SpringApplication.run(AichatApplication.class, args);
    }
}