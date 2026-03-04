package com.xx.xianqijava.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * 初始化器基类
 * 所有模块初始化器继承此类，提供统一的初始化日志和异常处理
 */
public abstract class BaseInitializer implements ApplicationRunner {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 获取初始化器名称
     */
    protected abstract String getName();

    /**
     * 执行初始化逻辑（由子类实现）
     */
    protected abstract void doInit();

    @Override
    public final void run(ApplicationArguments args) {
        String name = getName();
        log.info("========================================");
        log.info("开始初始化模块: {}", name);
        log.info("========================================");

        long startTime = System.currentTimeMillis();

        try {
            // 检查是否需要初始化
            if (!shouldInit()) {
                log.info("模块 {} 跳过初始化", name);
                return;
            }

            // 执行初始化
            doInit();

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("========================================");
            log.info("模块 {} 初始化完成，耗时: {} ms", name, duration);
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("模块 {} 初始化失败", name, e);
            log.error("========================================");
            // 不抛出异常，避免影响应用启动和其他模块初始化
        }
    }

    /**
     * 判断是否需要执行初始化
     * 子类可以重写此方法来实现条件判断
     *
     * @return true 表示需要初始化，false 表示跳过
     */
    protected boolean shouldInit() {
        return true;  // 默认需要初始化
    }
}
