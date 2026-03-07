package com.xx.xianqijava.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 配置类
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园二手交易与共享平台 API")
                        .description("校园二手交易与共享平台接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("许佳宜")
                                .email("xujiayi@example.com")
                                .url("https://github.com/xujiayi"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList("jwt"))
                .components(new Components()
                        .addSecuritySchemes("jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("请输入JWT Token（格式：Bearer {token}）")));
    }

    /**
     * 管理员认证接口分组
     */
    @Bean
    public GroupedOpenApi adminAuthApi() {
        return GroupedOpenApi.builder()
                .group("01-管理员认证")
                .pathsToMatch("/admin/auth/**")
                .build();
    }

    /**
     * 管理员数据统计接口分组
     */
    @Bean
    public GroupedOpenApi adminDashboardApi() {
        return GroupedOpenApi.builder()
                .group("02-数据统计")
                .pathsToMatch("/admin/dashboard/**", "/admin/statistics/**")
                .build();
    }

    /**
     * 管理员用户管理接口分组
     */
    @Bean
    public GroupedOpenApi adminUserApi() {
        return GroupedOpenApi.builder()
                .group("03-用户管理")
                .pathsToMatch("/admin/user/**")
                .build();
    }

    /**
     * 管理员商品管理接口分组
     */
    @Bean
    public GroupedOpenApi adminProductApi() {
        return GroupedOpenApi.builder()
                .group("04-商品管理")
                .pathsToMatch("/admin/product/**", "/admin/product-audit/**")
                .build();
    }

    /**
     * 管理员订单管理接口分组
     */
    @Bean
    public GroupedOpenApi adminOrderApi() {
        return GroupedOpenApi.builder()
                .group("05-订单管理")
                .pathsToMatch("/admin/order/**", "/admin/refund/**")
                .build();
    }

    /**
     * 管理员共享物品接口分组
     */
    @Bean
    public GroupedOpenApi adminShareItemApi() {
        return GroupedOpenApi.builder()
                .group("06-共享物品")
                .pathsToMatch("/admin/share-item/**", "/admin/booking/**")
                .build();
    }

    /**
     * 管理员内容管理接口分组
     */
    @Bean
    public GroupedOpenApi adminContentApi() {
        return GroupedOpenApi.builder()
                .group("07-内容管理")
                .pathsToMatch("/admin/banner/**", "/admin/notification/**",
                              "/admin/hot-tag/**", "/admin/quick-reply/**")
                .build();
    }

    /**
     * 管理员举报与反馈接口分组
     */
    @Bean
    public GroupedOpenApi adminReportApi() {
        return GroupedOpenApi.builder()
                .group("08-举报与反馈")
                .pathsToMatch("/admin/report/**", "/admin/feedback/**")
                .build();
    }

    /**
     * 管理员黑名单接口分组
     */
    @Bean
    public GroupedOpenApi adminBlacklistApi() {
        return GroupedOpenApi.builder()
                .group("09-黑名单管理")
                .pathsToMatch("/admin/blacklist/**")
                .build();
    }

    /**
     * 管理员数据导出接口分组
     */
    @Bean
    public GroupedOpenApi adminExportApi() {
        return GroupedOpenApi.builder()
                .group("10-数据导出")
                .pathsToMatch("/admin/export/**")
                .build();
    }

    /**
     * 管理员消息与评价接口分组
     */
    @Bean
    public GroupedOpenApi adminMessageApi() {
        return GroupedOpenApi.builder()
                .group("11-消息与评价")
                .pathsToMatch("/admin/message/**", "/admin/evaluation/**")
                .build();
    }

    /**
     * 管理员系统配置接口分组
     */
    @Bean
    public GroupedOpenApi adminConfigApi() {
        return GroupedOpenApi.builder()
                .group("12-系统配置")
                .pathsToMatch("/admin/config/**")
                .build();
    }

    /**
     * 管理员日志管理接口分组
     */
    @Bean
    public GroupedOpenApi adminLogApi() {
        return GroupedOpenApi.builder()
                .group("13-日志管理")
                .pathsToMatch("/admin/operation-log/**", "/admin/login-log/**")
                .build();
    }

    /**
     * 管理员管理接口分组
     */
    @Bean
    public GroupedOpenApi adminAdminApi() {
        return GroupedOpenApi.builder()
                .group("14-管理员管理")
                .pathsToMatch("/admin/admin/**")
                .build();
    }
}
