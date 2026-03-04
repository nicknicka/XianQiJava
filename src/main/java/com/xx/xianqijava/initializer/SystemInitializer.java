package com.xx.xianqijava.initializer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 系统初始化器
 * 在应用启动时执行，负责协调和记录各模块的初始化
 */
@Slf4j
@Component
@Order(0)  // 最先执行，用于记录初始化开始
public class SystemInitializer implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        log.info("========================================");
        log.info("系统初始化开始...");
        log.info("启动时间: {}", new Date());
        log.info("========================================");

        long startTime = System.currentTimeMillis();

        // 各模块的初始化类会通过 Spring 自动执行（FlashSaleInitializer 等）
        // 这里只记录和汇总

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        log.info("========================================");
        log.info("系统初始化完成，耗时: {} ms", duration);
        log.info("========================================");
    }
}
