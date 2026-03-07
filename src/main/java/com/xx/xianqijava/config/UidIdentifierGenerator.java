package com.xx.xianqijava.config;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * MyBatis-Plus ID生成器适配器
 *
 * <p>将雪花算法生成器适配到MyBatis-Plus的IdentifierGenerator接口
 * <p>这样在实体类中使用 @TableId(type = IdType.ASSIGN_ID) 时，会自动使用雪花算法生成ID
 *
 * @author Claude
 * @since 2025-03-07
 * @see com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator
 * @see SnowflakeIdGenerator
 */
@Component
public class UidIdentifierGenerator implements IdentifierGenerator {

    @Resource
    private SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 生成实体ID
     *
     * <p>当实体类使用 @TableId(type = IdType.ASSIGN_ID) 时，MyBatis-Plus会调用此方法生成ID
     *
     * @param entity 实体对象
     * @return 雪花算法生成的ID
     */
    @Override
    public Number nextId(Object entity) {
        return snowflakeIdGenerator.nextId();
    }
}
