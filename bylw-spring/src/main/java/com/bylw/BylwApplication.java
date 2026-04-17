package com.bylw;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.bylw.mapper")
@EnableScheduling
public class BylwApplication {
    public static void main(String[] args) {
        SpringApplication.run(BylwApplication.class, args);
    }
}
