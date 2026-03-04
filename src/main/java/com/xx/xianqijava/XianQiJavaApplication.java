package com.xx.xianqijava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class XianQiJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(XianQiJavaApplication.class, args);
    }

}
