package com.xx.xianqijava.agent.context;

/**
 * AI 调用上下文
 * 用于在 Agent 工具函数中获取当前请求的用户ID
 *
 * @author Claude
 * @since 2026-03-31
 */
public class AIContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    /**
     * 清除当前用户ID
     */
    public static void clearCurrentUserId() {
        CURRENT_USER_ID.remove();
    }
}
