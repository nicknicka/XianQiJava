package com.xx.xianqijava.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.math.BigDecimal;
import java.util.TimeZone;

/**
 * Jackson 配置
 * 处理 BigDecimal、Long 等类型的序列化
 */
@Configuration
public class JacksonConfig {

    /**
     * 配置 ObjectMapper - Spring Boot 3.x 推荐方式
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
        return new Jackson2ObjectMapperBuilderCustomizer() {
            @Override
            public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
                // 设置时区
                jacksonObjectMapperBuilder.timeZone(TimeZone.getDefault());

                // Long 序列化为字符串（解决雪花ID精度丢失问题）
                jacksonObjectMapperBuilder.serializerByType(Long.class, ToStringSerializer.instance);
                jacksonObjectMapperBuilder.serializerByType(Long.TYPE, ToStringSerializer.instance);

                // BigDecimal 序列化为字符串（避免精度丢失）
                jacksonObjectMapperBuilder.serializerByType(BigDecimal.class, ToStringSerializer.instance);

                // 配置 JavaTimeModule（支持 Java 8 日期时间类型）
                jacksonObjectMapperBuilder.modules(new JavaTimeModule());
            }
        };
    }
}
