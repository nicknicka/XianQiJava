package com.xx.xianqijava.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 智谱 AI 配置类
 *
 * @author Claude
 * @since 2026-03-23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "zhipuai")
public class ZhipuAIConfig {

    /**
     * API 密钥
     * 从环境变量或 .env 文件读取
     */
    @Value("$")
    private String apiKey;

    /**
     * 使用的模型
     * glm-4-flash: 标准Flash版本，免费，速度快，并发限制中等（推荐）
     * glm-4-air: 轻量级版本，免费，更低的并发限制
     * glm-4: 标准版，需要付费，能力强
     * glm-4-plus: 增强版，需要付费，能力最强
     */
    private String model = "glm-4-flash";

    /**
     * 视觉识别模型（用于商品图片识别、图像理解）
     * glm-4.6v-flash: 支持视觉识别的免费模型（推荐用于图片识别）
     */
    private String visionModel = "glm-4.6v-flash";

    /**
     * API 基础 URL
     */
    private String baseUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 60000;

    /**
     * 验证配置是否有效
     *
     * @return 配置是否有效
     */
    public boolean isValid() {
        return apiKey != null && !apiKey.trim().isEmpty()
                && model != null && !model.trim().isEmpty();
    }
}
