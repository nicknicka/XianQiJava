package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.entity.OperationLog;
import com.xx.xianqijava.mapper.OperationLogMapper;
import com.xx.xianqijava.service.OperationLogService;
import com.xx.xianqijava.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 操作日志服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Async
    public void recordLog(Long userId, String username, String module, String action,
                          String description, String requestMethod, String requestUrl,
                          String requestParams, String ipAddress, String userAgent,
                          Long executeTime, Integer status, String errorMessage) {
        try {
            OperationLog log = new OperationLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setModule(module);
            log.setAction(action);
            log.setDescription(description);
            log.setRequestMethod(requestMethod);
            log.setRequestUrl(requestUrl);
            log.setRequestParams(requestParams);
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);
            log.setExecuteTime(executeTime);
            log.setStatus(status);
            log.setErrorMessage(errorMessage);
            log.setCreateTime(LocalDateTime.now());

            save(log);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    @Override
    public IPage<OperationLogVO> getLogList(Page<OperationLog> page, Long userId, String module,
                                            String action, Integer status, String startTime, String endTime) {
        log.info("查询操作日志列表, userId={}, module={}, action={}, status={}",
                userId, module, action, status);

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();

        if (userId != null) {
            wrapper.eq(OperationLog::getUserId, userId);
        }
        if (StrUtil.isNotBlank(module)) {
            wrapper.eq(OperationLog::getModule, module);
        }
        if (StrUtil.isNotBlank(action)) {
            wrapper.eq(OperationLog::getAction, action);
        }
        if (status != null) {
            wrapper.eq(OperationLog::getStatus, status);
        }
        if (StrUtil.isNotBlank(startTime)) {
            LocalDateTime start = LocalDateTime.parse(startTime, DATE_FORMATTER);
            wrapper.ge(OperationLog::getCreateTime, start);
        }
        if (StrUtil.isNotBlank(endTime)) {
            LocalDateTime end = LocalDateTime.parse(endTime, DATE_FORMATTER);
            wrapper.le(OperationLog::getCreateTime, end);
        }

        wrapper.orderByDesc(OperationLog::getCreateTime);

        IPage<OperationLog> logPage = page(page, wrapper);
        return logPage.convert(this::convertToVO);
    }

    @Override
    public IPage<OperationLogVO> getMyLogs(Page<OperationLog> page, Long userId) {
        log.info("查询我的操作日志, userId={}, page={}", userId, page.getCurrent());

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OperationLog::getUserId, userId)
                .orderByDesc(OperationLog::getCreateTime);

        IPage<OperationLog> logPage = page(page, wrapper);
        return logPage.convert(this::convertToVO);
    }

    @Override
    public int cleanExpiredLogs(int days) {
        log.info("清理{}天前的操作日志", days);

        LocalDateTime expireTime = LocalDateTime.now().minusDays(days);

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(OperationLog::getCreateTime, expireTime);

        int count = Math.toIntExact(count(wrapper));
        remove(wrapper);

        log.info("清理完成，共清理{}条记录", count);
        return count;
    }

    /**
     * 转换为VO
     */
    private OperationLogVO convertToVO(OperationLog log) {
        OperationLogVO vo = new OperationLogVO();
        BeanUtil.copyProperties(log, vo);

        // 设置状态描述
        vo.setStatusDesc(log.getStatus() == 1 ? "成功" : "失败");

        // 设置时间
        if (log.getCreateTime() != null) {
            vo.setCreateTime(log.getCreateTime().toString());
        }

        return vo;
    }
}
