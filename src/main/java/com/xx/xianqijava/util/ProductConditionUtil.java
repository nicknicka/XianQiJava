package com.xx.xianqijava.util;

/**
 * 商品成色转换工具类
 * 用于前后端成色值的转换
 *
 * 前端使用字符串: 'new', 'almost_new', 'lightly_used', 'obviously_used', 'has_flaws'
 * 后端使用整数: 10, 9, 8, 7, 6
 */
public class ProductConditionUtil {

    /**
     * 前端字符串值转后端整数等级
     *
     * @param conditionStr 前端成色字符串
     * @return 后端成色等级 (1-10)
     */
    public static Integer stringToLevel(String conditionStr) {
        if (conditionStr == null || conditionStr.isEmpty()) {
            return 9; // 默认"几乎全新"
        }

        switch (conditionStr) {
            case "new":
                return 10;
            case "almost_new":
                return 9;
            case "lightly_used":
                return 8;
            case "obviously_used":
                return 7;
            case "has_flaws":
                return 6;
            default:
                return 9; // 未知值默认为"几乎全新"
        }
    }

    /**
     * 后端整数等级转前端字符串值
     *
     * @param level 后端成色等级 (1-10)
     * @return 前端成色字符串
     */
    public static String levelToString(Integer level) {
        if (level == null) {
            return "almost_new";
        }

        switch (level) {
            case 10:
                return "new";
            case 9:
                return "almost_new";
            case 8:
                return "lightly_used";
            case 7:
                return "obviously_used";
            case 6:
                return "has_flaws";
            default:
                return "almost_new"; // 未知值默认为"几乎全新"
        }
    }

    /**
     * 获取成色描述文本
     *
     * @param level 成色等级 (1-10)
     * @return 成色描述
     */
    public static String getConditionDesc(Integer level) {
        if (level == null) {
            return "未描述";
        }

        switch (level) {
            case 10:
                return "全新";
            case 9:
                return "几乎全新";
            case 8:
                return "轻微使用痕迹";
            case 7:
                return "明显使用痕迹";
            case 6:
                return "外观成色一般";
            default:
                return level + "成新";
        }
    }

    /**
     * 验证成色字符串是否有效
     *
     * @param conditionStr 前端成色字符串
     * @return 是否有效
     */
    public static boolean isValidCondition(String conditionStr) {
        if (conditionStr == null || conditionStr.isEmpty()) {
            return false;
        }

        return "new".equals(conditionStr)
                || "almost_new".equals(conditionStr)
                || "lightly_used".equals(conditionStr)
                || "obviously_used".equals(conditionStr)
                || "has_flaws".equals(conditionStr);
    }
}
