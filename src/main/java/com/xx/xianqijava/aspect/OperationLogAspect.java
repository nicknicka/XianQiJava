package com.xx.xianqijava.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.xianqijava.annotation.OperationLog;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.service.OperationLogService;
import com.xx.xianqijava.service.UserService;
import com.xx.xianqijava.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志切面
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    @Pointcut("@annotation(com.xx.xianqijava.annotation.OperationLog)")
    public void operationLogPointcut() {
    }

    @Around("operationLogPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Integer status = 1; // 成功
        String errorMessage = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Exception e) {
            status = 0; // 失败
            errorMessage = e.getMessage();
            throw e;
        } finally {
            long executeTime = System.currentTimeMillis() - startTime;
            try {
                recordLog(joinPoint, executeTime, status, errorMessage);
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        }
    }

    private void recordLog(ProceedingJoinPoint joinPoint, long executeTime,
                          Integer status, String errorMessage) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();

        // 获取注解信息
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        OperationLog annotation = signature.getMethod()
                .getAnnotation(OperationLog.class);

        // 获取用户信息
        Long userId = 0L;
        String username = "系统";
        try {
            userId = SecurityUtil.getCurrentUserId();
            if (userId != null) {
                User user = userService.getById(userId);
                if (user != null) {
                    username = user.getUsername();
                }
            }
        } catch (Exception e) {
            // 未登录用户
            userId = 0L;
            username = "游客";
        }

        // 获取请求参数
        String requestParams = getRequestParams(joinPoint);

        // 获取请求URL
        String requestUrl = request.getRequestURI();

        // 获取IP地址
        String ipAddress = getClientIpAddress(request);

        // 获取User-Agent
        String userAgent = request.getHeader("User-Agent");

        // 记录日志
        operationLogService.recordLog(
                userId,
                username,
                annotation.module(),
                annotation.action(),
                annotation.description(),
                request.getMethod(),
                requestUrl,
                requestParams,
                ipAddress,
                userAgent,
                executeTime,
                status,
                errorMessage
        );
    }

    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) {
                return "";
            }

            // 过滤掉不能序列化的参数（如HttpServletRequest、HttpServletResponse等）
            StringBuilder params = new StringBuilder();
            for (Object arg : args) {
                if (arg == null) {
                    continue;
                }
                try {
                    String json = objectMapper.writeValueAsString(arg);
                    if (json.length() > 500) {
                        json = json.substring(0, 500) + "...";
                    }
                    params.append(json).append("; ");
                } catch (Exception e) {
                    params.append("[").append(arg.getClass().getSimpleName()).append("]; ");
                }
            }
            return params.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多个代理的情况，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
