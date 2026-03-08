package com.xx.xianqijava.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 数据导出服务接口
 */
public interface DataExportService {

    /**
     * 导出订单数据为 CSV
     *
     * @param response HTTP 响应
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param status 订单状态（可选）
     * @throws IOException IO 异常
     */
    void exportOrdersToCsv(HttpServletResponse response,
                           String startTime,
                           String endTime,
                           Integer status) throws IOException;

    /**
     * 导出用户数据为 CSV
     *
     * @param response HTTP 响应
     * @param keyword 关键词（可选）
     * @param status 用户状态（可选）
     * @throws IOException IO 异常
     */
    void exportUsersToCsv(HttpServletResponse response,
                          String keyword,
                          Integer status) throws IOException;

    /**
     * 导出统计数据为 CSV
     *
     * @param response HTTP 响应
     * @param days 统计天数
     * @throws IOException IO 异常
     */
    void exportStatisticsToCsv(HttpServletResponse response,
                               Integer days) throws IOException;
}
