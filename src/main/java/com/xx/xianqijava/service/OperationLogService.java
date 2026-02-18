package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.OperationLog;
import com.xx.xianqijava.vo.OperationLogVO;

/**
 * 操作日志服务接口
 */
public interface OperationLogService extends IService<OperationLog> {

    /**
     * 记录操作日志
     *
     * @param userId          用户ID
     * @param username        用户名
     * @param module          操作模块
     * @param action          操作类型
     * @param description     操作描述
     * @param requestMethod   请求方法
     * @param requestUrl      请求URL
     * @param requestParams   请求参数
     * @param ipAddress       IP地址
     * @param userAgent       用户代理
     * @param executeTime     执行时长
     * @param status          执行状态
     * @param errorMessage    错误信息
     */
    void recordLog(Long userId, String username, String module, String action,
                   String description, String requestMethod, String requestUrl,
                   String requestParams, String ipAddress, String userAgent,
                   Long executeTime, Integer status, String errorMessage);

    /**
     * 获取操作日志列表
     *
     * @param page        分页参数
     * @param userId      用户ID（可选）
     * @param module      模块（可选）
     * @param action      操作类型（可选）
     * @param status      状态（可选）
     * @param startTime   开始时间（可选）
     * @param endTime     结束时间（可选）
     * @return 操作日志列表
     */
    IPage<OperationLogVO> getLogList(Page<OperationLog> page, Long userId, String module,
                                     String action, Integer status, String startTime, String endTime);

    /**
     * 获取我的操作日志
     *
     * @param page   分页参数
     * @param userId 用户ID
     * @return 操作日志列表
     */
    IPage<OperationLogVO> getMyLogs(Page<OperationLog> page, Long userId);

    /**
     * 清理过期日志
     *
     * @param days 保留天数
     * @return 清理的记录数
     */
    int cleanExpiredLogs(int days);
}
