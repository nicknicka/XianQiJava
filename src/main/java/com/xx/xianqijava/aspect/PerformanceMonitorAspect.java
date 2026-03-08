package com.xx.xianqijava.aspect;

import com.xx.xianqijava.service.SystemMonitorService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 性能监控切面
 * 自动记录接口的执行时间和状态
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PerformanceMonitorAspect {

    private final SystemMonitorService systemMonitorService;

    /**
     * 拦截所有控制器方法
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        int status = 1; // 成功

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = 0; // 失败
            throw e;
        } finally {
            long executeTime = System.currentTimeMillis() - startTime;

            try {
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String endpoint = request.getRequestURI();
                    String method = request.getMethod();

                    // 记录性能指标（仅记录 POST、PUT、DELETE 请求和 GET 查询请求）
                    if (isImportantRequest(method, endpoint)) {
                        String key = method + " " + endpoint;
                        systemMonitorService.recordPerformance(key, executeTime, status);

                        // 慢查询警告（超过3秒）
                        if (executeTime > 3000) {
                            log.warn("慢接口警告: {} 执行时间: {}ms", key, executeTime);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("记录性能指标失败", e);
            }
        }
    }

    /**
     * 判断是否是重要的请求
     * 只记录 POST、PUT、DELETE 和部分 GET 请求
     */
    private boolean isImportantRequest(String method, String endpoint) {
        // 不记录健康检查和监控接口
        if (endpoint.contains("/monitor/") || endpoint.contains("/health")) {
            return false;
        }

        // 记录所有非 GET 请求
        if (!"GET".equalsIgnoreCase(method)) {
            return true;
        }

        // 对于 GET 请求，只记录列表和详情查询
        return endpoint.contains("/list") ||
                endpoint.contains("/detail") ||
                endpoint.matches(".*/\\d+$"); // 以ID结尾的GET请求
    }
}
