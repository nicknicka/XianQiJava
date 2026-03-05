package com.xx.xianqijava.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Web MVC 配置类
 * 配置静态资源映射和 HTTP 消息转换器
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:/tmp/uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    /**
     * 配置路径匹配规则
     * 确保后缀匹配不启用，避免静态资源处理器拦截API请求
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 不使用后缀匹配模式，避免将 /api/public/hot-tags 当作静态资源处理
        configurer.setUseSuffixPatternMatch(false);
        // 不使用尾斜杠匹配
        configurer.setUseTrailingSlashMatch(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传文件的访问路径
        // 当访问 /uploads/** 时，从文件系统的 uploadPath 目录中读取文件

        // 确保路径以 / 开头和结尾
        String pattern = urlPrefix;
        if (!pattern.startsWith("/")) {
            pattern = "/" + pattern;
        }
        if (!pattern.endsWith("/")) {
            pattern = pattern + "/";
        }
        pattern += "**";

        // 确保文件路径以 / 结尾
        String location = uploadPath;
        if (!location.endsWith("/") && !location.endsWith("\\")) {
            location = location + "/";
        }
        if (!location.startsWith("file:")) {
            location = "file:" + location;
        }

        registry.addResourceHandler(pattern)
                .addResourceLocations(location);

        // 兼容旧格式的图片URL：/uploads/products/{filename}.jpg
        // 映射 /uploads/products/** 到上传目录的 products 子目录
        String productsPattern = "/uploads/products/**";
        String productsLocation = location + "products/";
        registry.addResourceHandler(productsPattern)
                .addResourceLocations(productsLocation);

        System.out.println("========================================");
        System.out.println("静态资源映射已配置:");
        System.out.println("URL路径: " + pattern);
        System.out.println("文件路径: " + location);
        System.out.println("访问示例: http://localhost:8080/uploads/xxx.png");
        System.out.println("");
        System.out.println("旧格式兼容映射:");
        System.out.println("URL路径: " + productsPattern);
        System.out.println("文件路径: " + productsLocation);
        System.out.println("访问示例: http://localhost:8080/uploads/products/2-book-1.jpg");
        System.out.println("========================================");
    }

    /**
     * 配置 HTTP 消息转换器，确保使用 UTF-8 编码
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 添加字符串转换器，使用 UTF-8
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false); // 避免在响应头中添加 charset
        converters.add(0, stringConverter);

        // 配置 JSON 转换器
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
        converters.add(1, jsonConverter);
    }
}
