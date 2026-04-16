package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.UserCreditExt;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * 用户信用分扩展 Mapper
 */
@Mapper
public interface UserCreditExtMapper extends BaseMapper<UserCreditExt> {

    /**
     * 根据用户ID查询信用扩展信息
     */
    @Select("SELECT * FROM user_credit_ext WHERE user_id = #{userId}")
    UserCreditExt getByUserId(@Param("userId") Long userId);

    /**
     * 更新用户最后活跃时间
     */
    @Insert("INSERT INTO user_credit_ext (user_id, last_active_time, updated_at) " +
            "VALUES (#{userId}, #{activeTime}, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "  last_active_time = #{activeTime}, " +
            "  updated_at = NOW()")
    int updateLastActiveTime(@Param("userId") Long userId, @Param("activeTime") LocalDateTime activeTime);

    /**
     * 更新信用分
     */
    @Insert("INSERT INTO user_credit_ext (user_id, credit_level, updated_at) " +
            "VALUES (#{userId}, #{creditLevel}, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "  credit_level = #{creditLevel}, " +
            "  updated_at = NOW()")
    int updateCreditLevel(@Param("userId") Long userId, @Param("creditLevel") String creditLevel);

    /**
     * 更新评价统计
     */
    @Insert("INSERT INTO user_credit_ext (user_id, total_positive_evaluations, total_neutral_evaluations, total_negative_evaluations, updated_at) " +
            "VALUES (#{userId}, #{positive}, #{neutral}, #{negative}, NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "  total_positive_evaluations = #{positive}, " +
            "  total_neutral_evaluations = #{neutral}, " +
            "  total_negative_evaluations = #{negative}, " +
            "  updated_at = NOW()")
    int updateEvaluationStats(
        @Param("userId") Long userId,
        @Param("positive") Integer positive,
        @Param("neutral") Integer neutral,
        @Param("negative") Integer negative
    );

    /**
     * 更新信用分衰减时间
     */
    @Update("UPDATE user_credit_ext SET last_credit_decay_time = #{decayTime}, updated_at = NOW() " +
            "WHERE user_id = #{userId}")
    int updateDecayTime(@Param("userId") Long userId, @Param("decayTime") LocalDateTime decayTime);

    /**
     * 查询需要衰减的用户（90天不活跃且信用分>60）
     */
    @Select("SELECT uce.*, u.credit_score " +
            "FROM user_credit_ext uce " +
            "INNER JOIN user u ON uce.user_id = u.user_id " +
            "WHERE uce.last_active_time < DATE_SUB(NOW(), INTERVAL 90 DAY) " +
            "  AND u.credit_score > 60 " +
            "  AND (uce.last_credit_decay_time IS NULL " +
            "       OR uce.last_credit_decay_time < DATE_SUB(NOW(), INTERVAL 30 DAY))")
    java.util.List<UserCreditExt> getUsersForDecay();
}
