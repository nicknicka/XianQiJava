package com.xx.xianqijava.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.DataExportService;
import com.xx.xianqijava.service.StatisticsService;
import com.xx.xianqijava.vo.OrderStatisticsVO;
import com.xx.xianqijava.vo.ProductStatisticsVO;
import com.xx.xianqijava.vo.StatisticsVO;
import com.xx.xianqijava.vo.UserStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 数据导出服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataExportServiceImpl implements DataExportService {

    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final StatisticsService statisticsService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void exportOrdersToCsv(HttpServletResponse response,
                                   String startTime,
                                   String endTime,
                                   Integer status) throws IOException {
        log.info("导出订单数据, startTime={}, endTime={}, status={}", startTime, endTime, status);

        // 设置响应头
        response.setContentType("text/csv; charset=utf-8");
        response.setCharacterEncoding("utf-8");
        String filename = URLEncoder.encode("订单数据_" + DateUtil.today(), StandardCharsets.UTF_8) + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        // 构建查询条件
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (startTime != null && !startTime.isEmpty()) {
            wrapper.ge(Order::getCreateTime, LocalDateTime.parse(startTime, DATE_FORMATTER));
        }
        if (endTime != null && !endTime.isEmpty()) {
            wrapper.le(Order::getCreateTime, LocalDateTime.parse(endTime, DATE_FORMATTER));
        }
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        wrapper.last("LIMIT 10000"); // 最多导出 10000 条

        // 查询数据
        List<Order> orders = orderMapper.selectList(wrapper);

        // 写入 CSV
        try (OutputStream out = response.getOutputStream();
             PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {

            // 写入 BOM（解决 Excel 中文乱码）
            out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            // 写入表头
            writer.println("订单号,商品ID,买家ID,卖家ID,订单状态,交易金额,创建时间,完成时间");

            // 写入数据行
            for (Order order : orders) {
                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                        escapeCsv(order.getOrderNo()),
                        order.getProductId(),
                        order.getBuyerId(),
                        order.getSellerId(),
                        getOrderStatusText(order.getStatus()),
                        order.getAmount(),
                        formatDateTime(order.getCreateTime()),
                        formatDateTime(order.getFinishTime())
                ));
            }

            log.info("导出订单数据成功, count={}", orders.size());
        } catch (Exception e) {
            log.error("导出订单数据失败, error={}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void exportUsersToCsv(HttpServletResponse response,
                                  String keyword,
                                  Integer status) throws IOException {
        log.info("导出用户数据, keyword={}, status={}", keyword, status);

        // 设置响应头
        response.setContentType("text/csv; charset=utf-8");
        response.setCharacterEncoding("utf-8");
        String filename = URLEncoder.encode("用户数据_" + DateUtil.today(), StandardCharsets.UTF_8) + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getNickname, keyword)
                    .or().like(User::getPhone, keyword)
                    .or().like(User::getRealName, keyword));
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);
        wrapper.last("LIMIT 10000"); // 最多导出 10000 条

        // 查询数据
        List<User> users = userMapper.selectList(wrapper);

        // 写入 CSV
        try (OutputStream out = response.getOutputStream();
             PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {

            // 写入 BOM
            out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            // 写入表头
            writer.println("用户ID,用户名,昵称,手机号,真实姓名,学院,专业,信用分,状态,创建时间");

            // 写入数据行
            for (User user : users) {
                writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                        user.getUserId(),
                        escapeCsv(user.getUsername()),
                        escapeCsv(user.getNickname()),
                        escapeCsv(user.getPhone()),
                        escapeCsv(user.getRealName()),
                        escapeCsv(user.getCollege()),
                        escapeCsv(user.getMajor()),
                        user.getCreditScore(),
                        user.getStatus() == 0 ? "正常" : "封禁",
                        formatDateTime(user.getCreateTime())
                ));
            }

            log.info("导出用户数据成功, count={}", users.size());
        } catch (Exception e) {
            log.error("导出用户数据失败, error={}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void exportStatisticsToCsv(HttpServletResponse response,
                                       Integer days) throws IOException {
        log.info("导出统计数据, days={}", days);

        // 设置响应头
        response.setContentType("text/csv; charset=utf-8");
        response.setCharacterEncoding("utf-8");
        String filename = URLEncoder.encode("统计数据_" + DateUtil.today(), StandardCharsets.UTF_8) + ".csv";
        response.setHeader("Content-Disposition", "attachment; filename=" + filename);

        StatisticsVO overview = statisticsService.getOverviewStatistics();
        UserStatisticsVO userStatistics = statisticsService.getUserStatistics();
        ProductStatisticsVO productStatistics = statisticsService.getProductStatistics();
        OrderStatisticsVO orderStatistics = statisticsService.getOrderStatistics();

        try (OutputStream out = response.getOutputStream();
             PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {

            // 写入 BOM
            out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            writer.println("统计分组,统计项,统计值,统计日期");

            String exportDate = DateUtil.today();
            int statDays = days != null ? days : 7;
            writer.println(row("导出参数", "统计周期(天)", statDays, exportDate));

            writer.println(row("总览", "总用户数", overview.getTotalUsers(), exportDate));
            writer.println(row("总览", "今日新增用户", overview.getTodayNewUsers(), exportDate));
            writer.println(row("总览", "活跃用户数", overview.getActiveUsers(), exportDate));
            writer.println(row("总览", "总商品数", overview.getTotalProducts(), exportDate));
            writer.println(row("总览", "在售商品数", overview.getOnSaleProducts(), exportDate));
            writer.println(row("总览", "今日新增商品", overview.getTodayNewProducts(), exportDate));
            writer.println(row("总览", "总订单数", overview.getTotalOrders(), exportDate));
            writer.println(row("总览", "今日新增订单", overview.getTodayNewOrders(), exportDate));
            writer.println(row("总览", "待处理订单数", overview.getPendingOrders(), exportDate));
            writer.println(row("总览", "已完成订单数", overview.getCompletedOrders(), exportDate));
            writer.println(row("总览", "总交易金额", overview.getTotalAmount(), exportDate));
            writer.println(row("总览", "今日交易金额", overview.getTodayAmount(), exportDate));
            writer.println(row("总览", "本月交易金额", overview.getMonthAmount(), exportDate));
            writer.println(row("总览", "待审核商品数", overview.getPendingProducts(), exportDate));
            writer.println(row("总览", "待审核认证数", overview.getPendingVerifications(), exportDate));
            writer.println(row("总览", "系统通知数", overview.getSystemNotifications(), exportDate));
            writer.println(row("总览", "用户反馈数", overview.getUserFeedbacks(), exportDate));
            writer.println(row("总览", "待处理举报数", overview.getPendingReports(), exportDate));

            writer.println(row("用户", "总用户数", userStatistics.getTotalUsers(), exportDate));
            writer.println(row("用户", "今日新增用户", userStatistics.getTodayNewUsers(), exportDate));
            writer.println(row("用户", "本周新增用户", userStatistics.getWeekNewUsers(), exportDate));
            writer.println(row("用户", "本月新增用户", userStatistics.getMonthNewUsers(), exportDate));
            writer.println(row("用户", "活跃用户数", userStatistics.getActiveUsers(), exportDate));
            writer.println(row("用户", "实名认证用户数", userStatistics.getVerifiedUsers(), exportDate));
            writer.println(row("用户", "封禁用户数", userStatistics.getBannedUsers(), exportDate));

            writer.println(row("商品", "总商品数", productStatistics.getTotalProducts(), exportDate));
            writer.println(row("商品", "在售商品数", productStatistics.getOnSaleProducts(), exportDate));
            writer.println(row("商品", "已售商品数", productStatistics.getSoldProducts(), exportDate));
            writer.println(row("商品", "下架商品数", productStatistics.getOfflineProducts(), exportDate));
            writer.println(row("商品", "今日新增商品", productStatistics.getTodayNewProducts(), exportDate));
            writer.println(row("商品", "本周新增商品", productStatistics.getWeekNewProducts(), exportDate));
            writer.println(row("商品", "本月新增商品", productStatistics.getMonthNewProducts(), exportDate));
            writer.println(row("商品", "待审核商品数", productStatistics.getPendingProducts(), exportDate));
            writer.println(row("商品", "审核通过商品数", productStatistics.getApprovedProducts(), exportDate));
            writer.println(row("商品", "审核拒绝商品数", productStatistics.getRejectedProducts(), exportDate));

            writer.println(row("订单", "总订单数", orderStatistics.getTotalOrders(), exportDate));
            writer.println(row("订单", "待确认订单数", orderStatistics.getPendingOrders(), exportDate));
            writer.println(row("订单", "进行中订单数", orderStatistics.getInProgressOrders(), exportDate));
            writer.println(row("订单", "已完成订单数", orderStatistics.getCompletedOrders(), exportDate));
            writer.println(row("订单", "已取消订单数", orderStatistics.getCancelledOrders(), exportDate));
            writer.println(row("订单", "退款中订单数", orderStatistics.getRefundingOrders(), exportDate));
            writer.println(row("订单", "今日新增订单", orderStatistics.getTodayNewOrders(), exportDate));
            writer.println(row("订单", "本周新增订单", orderStatistics.getWeekNewOrders(), exportDate));
            writer.println(row("订单", "本月新增订单", orderStatistics.getMonthNewOrders(), exportDate));
            writer.println(row("订单", "总交易金额", orderStatistics.getTotalAmount(), exportDate));
            writer.println(row("订单", "今日交易金额", orderStatistics.getTodayAmount(), exportDate));
            writer.println(row("订单", "本周交易金额", orderStatistics.getWeekAmount(), exportDate));
            writer.println(row("订单", "本月交易金额", orderStatistics.getMonthAmount(), exportDate));

            log.info("导出统计数据成功");
        } catch (Exception e) {
            log.error("导出统计数据失败, error={}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 格式化日期时间
     */
    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_FORMATTER);
    }

    /**
     * 转义 CSV 字段（处理包含逗号、引号、换行符的字段）
     */
    private String escapeCsv(String field) {
        if (field == null) {
            return "";
        }
        // 如果字段包含逗号、引号或换行符，需要用引号包裹，并转义内部的引号
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private String row(String group, String item, Object value, String date) {
        return String.format("%s,%s,%s,%s",
                escapeCsv(group),
                escapeCsv(item),
                escapeCsv(value == null ? "" : String.valueOf(value)),
                escapeCsv(date));
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待确认";
            case 1 -> "进行中";
            case 2 -> "已完成";
            case 3 -> "已取消";
            case 4 -> "退款中";
            case 5 -> "已退款";
            default -> "未知";
        };
    }
}
