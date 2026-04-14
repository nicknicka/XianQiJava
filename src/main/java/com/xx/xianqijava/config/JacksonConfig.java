package com.xx.xianqijava.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.util.TimeZone;

/**
 * Jackson 配置
 * 处理 BigDecimal、Long 等类型的序列化
 *
 * 重要：将 Long 类型序列化为字符串，解决 JavaScript Number 精度丢失问题
 * JavaScript Number 最大安全整数是 2^53-1 (9007199254740991)
 * 雪花算法生成的 ID 是 18 位数字，会超出此限制导致精度丢失
 */
@Slf4j
@Configuration
public class JacksonConfig {

    /**
     * 配置 Jackson2ObjectMapperBuilderCustomizer
     * 这是 Spring Boot 推荐的自定义 ObjectMapper 方式
     * 优先级高于 application.yml 中的 jackson 配置
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        log.info("=== JacksonConfig: 配置 Jackson2ObjectMapperBuilderCustomizer ===");
        return builder -> {
            // 设置时区
            builder.timeZone(TimeZone.getDefault());

            // 配置 JavaTimeModule（支持 Java 8 日期时间类型）
            builder.modules(new JavaTimeModule());

            // 日期不序列化为时间戳
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            // 使用 postConfigurer 直接配置 ObjectMapper
            builder.postConfigurer(objectMapper -> {
                log.info("=== JacksonConfig: 注册 Long 序列化器 ===");
                // 创建 SimpleModule 注册 Long 序列化器
                SimpleModule simpleModule = new SimpleModule();
                simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
                simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
                simpleModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
                objectMapper.registerModule(simpleModule);
            });
        };
    }

    /**
     * 自定义 ObjectMapper Bean
     * 使用 @Primary 确保此 ObjectMapper 被优先使用
     *
     * 注意：这个 Bean 会覆盖 Spring Boot 自动配置的 ObjectMapper
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        log.info("=== JacksonConfig: 创建 @Primary ObjectMapper Bean ===");
        ObjectMapper objectMapper = builder.build();

        // 确保 Long 序列化为字符串（双重保险）
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        simpleModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
        objectMapper.registerModule(simpleModule);

        return objectMapper;
    }
}
