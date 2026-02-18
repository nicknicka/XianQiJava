package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.UserVerification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户实名认证Mapper
 */
@Mapper
public interface UserVerificationMapper extends BaseMapper<UserVerification> {
}
