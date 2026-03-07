package com.xx.xianqijava.security;

import com.xx.xianqijava.entity.Admin;

/**
 * 安全上下文持有者
 * 用于在请求处理过程中存储和获取当前管理员信息
 *
 * @author Claude Code
 * @since 2026-03-07
 */
public class SecurityContextHolder {

    private static final ThreadLocal<Admin> ADMIN_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前管理员
     */
    public static void setAdmin(Admin admin) {
        ADMIN_HOLDER.set(admin);
    }

    /**
     * 获取当前管理员
     */
    public static Admin getAdmin() {
        return ADMIN_HOLDER.get();
    }

    /**
     * 获取当前管理员ID
     */
    public static Long getAdminId() {
        Admin admin = getAdmin();
        return admin != null ? admin.getId() : null;
    }

    /**
     * 获取当前管理员用户名
     */
    public static String getAdminUsername() {
        Admin admin = getAdmin();
        return admin != null ? admin.getUsername() : null;
    }

    /**
     * 清除上下文
     * 请求处理完成后必须调用，避免内存泄漏
     */
    public static void clear() {
        ADMIN_HOLDER.remove();
    }
}
