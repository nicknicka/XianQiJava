package com.xx.xianqijava.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encodedPassword = encoder.encode(rawPassword);
        
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后的密码: " + encodedPassword);
        System.out.println("密码长度: " + encodedPassword.length());
        
        // 验证加密是否正确
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("验证结果: " + matches);
        
        // 输出 SQL 更新语句
        System.out.println("\n=== SQL 更新语句 ===");
        System.out.println("UPDATE admin SET password = '" + encodedPassword + "' WHERE username = 'admin';");
    }
}
