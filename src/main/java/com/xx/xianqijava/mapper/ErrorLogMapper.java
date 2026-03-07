package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.ErrorLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统异常日志Mapper接口
 *
 * @author Claude Code
 * @since 2026-03-07
 */
@Mapper
public interface ErrorLogMapper extends BaseMapper<ErrorLog> {
}
