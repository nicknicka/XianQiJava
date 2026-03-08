package com.xx.xianqijava.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云 OSS 配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "aliyun.oss")
public class OssConfig {

    /**
     * 是否启用 OSS
     */
    private Boolean enabled = false;

    /**
     * OSS 端点
     */
    private String endpoint;

    /**
     * 访问密钥 ID
     */
    private String accessKeyId;

    /**
     * 访问密钥密钥
     */
    private String accessKeySecret;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 基础 URL
     */
    private String baseUrl;
}
