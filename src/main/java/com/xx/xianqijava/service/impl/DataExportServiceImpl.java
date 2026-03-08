package com.xx.xianqijava.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.xianqijava.entity.Order;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.DataExportService;
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

        // TODO: 实现统计数据导出
        // 这里需要查询统计数据，包括用户统计、订单统计、商品统计等

        try (OutputStream out = response.getOutputStream();
             PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {

            // 写入 BOM
            out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            // 写入表头
            writer.println("统计项,统计值,日期");

            // 写入示例数据
            writer.println(String.format("统计天数,%s,%s", days != null ? days : 7, DateUtil.date()));
            writer.println("更多统计数据待实现...,");

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
