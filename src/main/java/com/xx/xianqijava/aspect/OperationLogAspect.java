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
                return "{}";
            }

            // 过滤掉不能序列化的参数（如HttpServletRequest、HttpServletResponse等）
            java.util.Map<String, Object> paramMap = new java.util.LinkedHashMap<>();
            int paramIndex = 0;

            for (Object arg : args) {
                if (arg == null) {
                    paramIndex++;
                    continue;
                }

                // 跳过Web相关的对象
                if (arg instanceof HttpServletRequest ||
                    arg instanceof jakarta.servlet.http.HttpServletResponse ||
                    arg instanceof jakarta.servlet.http.HttpSession) {
                    paramIndex++;
                    continue;
                }

                try {
                    // 尝试将参数转换为可序列化的对象
                    Object paramValue;
                    try {
                        // 先序列化为JSON，再反序列化为通用对象（Map/List等）
                        // 这样可以避免循环引用等问题
                        String json = objectMapper.writeValueAsString(arg);

                        // 如果JSON太长，截断为字符串
                        if (json.length() > 1000) {
                            paramValue = json.substring(0, 1000) + "...";
                        } else {
                            // 正常长度，反序列化为通用对象
                            paramValue = objectMapper.readValue(json, Object.class);
                        }
                    } catch (Exception ex) {
                        // 如果无法序列化/反序列化，使用toString
                        String strValue = arg.toString();
                        paramValue = strValue.length() > 1000 ? strValue.substring(0, 1000) + "..." : strValue;
                    }

                    // 使用参数索引作为key
                    String key = "param" + paramIndex;
                    paramMap.put(key, paramValue);
                    paramIndex++;
                } catch (Exception e) {
                    // 序列化失败，记录类型信息
                    String key = "param" + paramIndex;
                    paramMap.put(key, "[" + arg.getClass().getSimpleName() + "]");
                    paramIndex++;
                }
            }

            // 如果所有参数都被过滤，返回空对象
            if (paramMap.isEmpty()) {
                return "{}";
            }

            // 转换为JSON字符串
            return objectMapper.writeValueAsString(paramMap);
        } catch (Exception e) {
            log.warn("序列化请求参数失败", e);
            return "{}";
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
