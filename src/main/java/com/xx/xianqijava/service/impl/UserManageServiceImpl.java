package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.UserQueryDTO;
import com.xx.xianqijava.dto.admin.UserUpdateStatusDTO;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.mapper.EvaluationMapper;
import com.xx.xianqijava.mapper.OrderMapper;
import com.xx.xianqijava.mapper.ProductMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.UserManageService;
import com.xx.xianqijava.vo.admin.UserManageVO;
import com.xx.xianqijava.vo.admin.UserStatisticsInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现类 - 管理端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserManageServiceImpl implements UserManageService {

    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final EvaluationMapper evaluationMapper;

    @Override
    public Page<UserManageVO> getUserList(UserQueryDTO queryDTO) {
        log.info("分页查询用户列表，查询条件：{}", queryDTO);

        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（用户名/昵称/手机号/学号）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword();
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getPhone, keyword)
                    .or()
                    .like(User::getStudentId, keyword)
            );
        }
        // 用户名模糊搜索
        else if (StringUtils.hasText(queryDTO.getUsername())) {
            queryWrapper.like(User::getUsername, queryDTO.getUsername());
        }

        // 手机号精确匹配
        if (StringUtils.hasText(queryDTO.getPhone())) {
            queryWrapper.eq(User::getPhone, queryDTO.getPhone());
        }

        // 学号模糊搜索
        if (StringUtils.hasText(queryDTO.getStudentId())) {
            queryWrapper.like(User::getStudentId, queryDTO.getStudentId());
        }

        // 学院模糊搜索
        if (StringUtils.hasText(queryDTO.getCollege())) {
            queryWrapper.like(User::getCollege, queryDTO.getCollege());
        }

        // 专业模糊搜索
        if (StringUtils.hasText(queryDTO.getMajor())) {
            queryWrapper.like(User::getMajor, queryDTO.getMajor());
        }

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(User::getStatus, queryDTO.getStatus());
        }

        // 实名认证筛选
        if (queryDTO.getIsVerified() != null) {
            queryWrapper.eq(User::getIsVerified, queryDTO.getIsVerified());
        }

        // 排序
        if ("creditScore".equals(queryDTO.getSortBy())) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), User::getCreditScore);
        } else {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), User::getCreateTime);
        }

        // 分页查询
        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<User> userPage = userMapper.selectPage(page, queryWrapper);

        // 转换为VO
        Page<UserManageVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserManageVO> voList = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public UserManageVO getUserDetail(Long userId) {
        log.info("获取用户详情，用户ID：{}", userId);

        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        return convertToVO(user);
    }

    @Override
    public Boolean updateUserStatus(UserUpdateStatusDTO updateDTO) {
        log.info("更新用户状态，用户ID：{}，状态：{}", updateDTO.getUserId(), updateDTO.getStatus());

        User user = userMapper.selectById(updateDTO.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 封禁时需要填写原因
        if (updateDTO.getStatus() == 1 && !StringUtils.hasText(updateDTO.getReason())) {
            throw new RuntimeException("封禁用户必须填写原因");
        }

        user.setStatus(updateDTO.getStatus());
        int result = userMapper.updateById(user);

        log.info("更新用户状态{}，用户ID：{}", result > 0 ? "成功" : "失败", updateDTO.getUserId());
        return result > 0;
    }

    @Override
    public UserStatisticsInfo getUserStatistics() {
        log.info("获取用户统计信息");

        UserStatisticsInfo statistics = new UserStatisticsInfo();

        // 总用户数
        Long totalUsers = userMapper.selectCount(null);
        statistics.setTotalUsers(totalUsers);

        // 正常用户数
        Long normalUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 0)
        );
        statistics.setNormalUsers(normalUsers);

        // 封禁用户数
        Long bannedUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 1)
        );
        statistics.setBannedUsers(bannedUsers);

        // 实名认证用户数
        Long verifiedUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getIsVerified, 1)
        );
        statistics.setVerifiedUsers(verifiedUsers);

        // 今日新增用户数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Long todayNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)
        );
        statistics.setTodayNewUsers(todayNewUsers);

        // 本周新增用户数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        Long weekNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, weekStart)
        );
        statistics.setWeekNewUsers(weekNewUsers);

        // 本月新增用户数
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        Long monthNewUsers = userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, monthStart)
        );
        statistics.setMonthNewUsers(monthNewUsers);

        // 活跃用户数（7天内登录）
        // TODO: User实体暂无lastLoginTime字段，暂时统计7天内活跃用户
        // 建议在User实体中添加lastLoginTime字段或在LoginLog表中统计
        LocalDateTime activeTime = LocalDateTime.now().minusDays(7);
        Long activeUsers = 0L;
        // 暂时返回0，待添加lastLoginTime字段后启用以下代码：
        // Long activeUsers = userMapper.selectCount(
        //         new LambdaQueryWrapper<User>().ge(User::getLastLoginTime, activeTime)
        // );
        statistics.setActiveUsers(activeUsers);

        return statistics;
    }

    /**
     * 转换User实体为UserManageVO
     */
    private UserManageVO convertToVO(User user) {
        UserManageVO vo = new UserManageVO();
        BeanUtils.copyProperties(user, vo);

        // 统计商品数量
        Long productCount = productMapper.selectCount(
                new LambdaQueryWrapper<com.xx.xianqijava.entity.Product>()
                        .eq(com.xx.xianqijava.entity.Product::getSellerId, user.getUserId())
        );
        vo.setProductCount(productCount.intValue());

        // 统计订单数量
        Long orderCount = orderMapper.selectCount(
                new LambdaQueryWrapper<com.xx.xianqijava.entity.Order>()
                        .eq(com.xx.xianqijava.entity.Order::getBuyerId, user.getUserId())
                        .or()
                        .eq(com.xx.xianqijava.entity.Order::getSellerId, user.getUserId())
        );
        vo.setOrderCount(orderCount.intValue());

        // 统计评价数量
        Long evaluationCount = evaluationMapper.selectCount(
                new LambdaQueryWrapper<com.xx.xianqijava.entity.Evaluation>()
                        .eq(com.xx.xianqijava.entity.Evaluation::getToUserId, user.getUserId())
        );
        vo.setEvaluationCount(evaluationCount.intValue());

        return vo;
    }
}
