package com.xx.xianqijava.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {

    /**
     * 操作模块
     */
    String module();

    /**
     * 操作类型
     */
    String action();

    /**
     * 操作描述
     */
    String description() default "";
}
