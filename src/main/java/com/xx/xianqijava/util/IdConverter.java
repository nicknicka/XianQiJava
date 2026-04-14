package com.xx.xianqijava.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ID 类型转换工具类
 * 用于 String 和 Long 类型 ID 之间的转换
 */
public class IdConverter {

    private IdConverter() {
        // 工具类不允许实例化
    }

    /**
     * String ID 转 Long
     *
     * @param id String 类型的 ID
     * @return Long 类型的 ID，如果输入为 null 或空则返回 null
     */
    public static Long toLong(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Long ID 转 String
     *
     * @param id Long 类型的 ID
     * @return String 类型的 ID，如果输入为 null 则返回 null
     */
    public static String toString(Long id) {
        return id != null ? id.toString() : null;
    }

    /**
     * String ID 列表转 Long 列表
     *
     * @param ids String 类型的 ID 列表
     * @return Long 类型的 ID 列表
     */
    public static List<Long> toLongList(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .map(IdConverter::toLong)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    /**
     * Long ID 列表转 String 列表
     *
     * @param ids Long 类型的 ID 列表
     * @return String 类型的 ID 列表
     */
    public static List<String> toStringList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .map(IdConverter::toString)
                .filter(id -> id != null)
                .collect(Collectors.toList());
    }

    /**
     * 验证 ID 是否有效
     *
     * @param id String 类型的 ID
     * @return true 如果 ID 有效
     */
    public static boolean isValid(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }
        try {
            Long.parseLong(id);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 验证 ID 是否有效
     *
     * @param id Long 类型的 ID
     * @return true 如果 ID 有效
     */
    public static boolean isValid(Long id) {
        return id != null && id > 0;
    }
}
