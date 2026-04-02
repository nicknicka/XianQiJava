package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.ShareItemManageQueryDTO;
import com.xx.xianqijava.dto.admin.ShareItemStatusUpdateDTO;
import com.xx.xianqijava.entity.Category;
import com.xx.xianqijava.entity.ShareItem;
import com.xx.xianqijava.entity.ShareItemBooking;
import com.xx.xianqijava.entity.ShareItemImage;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.mapper.CategoryMapper;
import com.xx.xianqijava.mapper.ShareItemBookingMapper;
import com.xx.xianqijava.mapper.ShareItemImageMapper;
import com.xx.xianqijava.mapper.ShareItemMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.ShareItemManageService;
import com.xx.xianqijava.vo.admin.ShareItemManageStatistics;
import com.xx.xianqijava.vo.admin.ShareItemManageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 共享物品管理服务实现类 - 管理端
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShareItemManageServiceImpl implements ShareItemManageService {

    private final ShareItemMapper shareItemMapper;
    private final ShareItemBookingMapper shareItemBookingMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final ShareItemImageMapper shareItemImageMapper;

    @Override
    public Page<ShareItemManageVO> getShareItemList(ShareItemManageQueryDTO queryDTO) {
        log.info("分页查询共享物品列表，查询条件：{}", queryDTO);

        // 构建查询条件
        LambdaQueryWrapper<ShareItem> queryWrapper = new LambdaQueryWrapper<>();

        // 物品标题模糊搜索
        if (StringUtils.hasText(queryDTO.getTitle())) {
            queryWrapper.like(ShareItem::getTitle, queryDTO.getTitle());
        }

        // 所有者筛选
        if (queryDTO.getOwnerId() != null) {
            queryWrapper.eq(ShareItem::getOwnerId, queryDTO.getOwnerId());
        }

        // 分类筛选
        if (queryDTO.getCategoryId() != null) {
            queryWrapper.eq(ShareItem::getCategoryId, queryDTO.getCategoryId());
        }

        // 状态筛选
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(ShareItem::getStatus, queryDTO.getStatus());
        }

        // 排序
        if ("dailyRent".equals(queryDTO.getSortBy())) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), ShareItem::getDailyRent);
        } else if ("deposit".equals(queryDTO.getSortBy())) {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), ShareItem::getDeposit);
        } else {
            queryWrapper.orderBy(true, "asc".equalsIgnoreCase(queryDTO.getSortOrder()), ShareItem::getCreateTime);
        }

        // 分页查询
        Page<ShareItem> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        Page<ShareItem> shareItemPage = shareItemMapper.selectPage(page, queryWrapper);

        // 转换为VO
        return convertToVOPage(shareItemPage);
    }

    @Override
    public ShareItemManageVO getShareItemDetail(Long shareId) {
        log.info("获取共享物品详情，共享物品ID：{}", shareId);

        ShareItem shareItem = shareItemMapper.selectById(shareId);
        if (shareItem == null) {
            throw new RuntimeException("共享物品不存在");
        }

        return convertToVO(shareItem);
    }

    @Override
    public Boolean updateShareItemStatus(ShareItemStatusUpdateDTO updateDTO) {
        log.info("更新共享物品状态，共享物品ID：{}，状态：{}", updateDTO.getShareId(), updateDTO.getStatus());

        ShareItem shareItem = shareItemMapper.selectById(updateDTO.getShareId());
        if (shareItem == null) {
            throw new RuntimeException("共享物品不存在");
        }

        shareItem.setStatus(updateDTO.getStatus());
        int result = shareItemMapper.updateById(shareItem);

        log.info("更新共享物品状态{}，共享物品ID：{}", result > 0 ? "成功" : "失败", updateDTO.getShareId());
        return result > 0;
    }

    @Override
    public ShareItemManageStatistics getShareItemStatistics() {
        log.info("获取共享物品统计信息");

        ShareItemManageStatistics statistics = new ShareItemManageStatistics();

        // 总共享物品数
        Long totalItems = shareItemMapper.selectCount(null);
        statistics.setTotalItems(totalItems);

        // 可借用物品数
        Long availableItems = shareItemMapper.selectCount(
                new LambdaQueryWrapper<ShareItem>().eq(ShareItem::getStatus, 1)
        );
        statistics.setAvailableItems(availableItems);

        // 借用中物品数
        Long borrowedItems = shareItemMapper.selectCount(
                new LambdaQueryWrapper<ShareItem>().eq(ShareItem::getStatus, 2)
        );
        statistics.setBorrowedItems(borrowedItems);

        // 下架物品数
        Long offlineItems = shareItemMapper.selectCount(
                new LambdaQueryWrapper<ShareItem>().eq(ShareItem::getStatus, 0)
        );
        statistics.setOfflineItems(offlineItems);

        // 今日新增物品数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Long todayNewItems = shareItemMapper.selectCount(
                new LambdaQueryWrapper<ShareItem>().ge(ShareItem::getCreateTime, todayStart)
        );
        statistics.setTodayNewItems(todayNewItems);

        // 本周新增物品数
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        Long weekNewItems = shareItemMapper.selectCount(
                new LambdaQueryWrapper<ShareItem>().ge(ShareItem::getCreateTime, weekStart)
        );
        statistics.setWeekNewItems(weekNewItems);

        // 本月新增物品数
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
        Long monthNewItems = shareItemMapper.selectCount(
                new LambdaQueryWrapper<ShareItem>().ge(ShareItem::getCreateTime, monthStart)
        );
        statistics.setMonthNewItems(monthNewItems);

        // 总借用次数（已完成预约）
        Long totalBorrows = shareItemBookingMapper.selectCount(
                new LambdaQueryWrapper<ShareItemBooking>()
                        .eq(ShareItemBooking::getStatus, 5) // 已完成
        );
        statistics.setTotalBorrows(totalBorrows);

        // 总押金金额和总租金收入
        List<ShareItemBooking> completedBookings = shareItemBookingMapper.selectList(
                new LambdaQueryWrapper<ShareItemBooking>().eq(ShareItemBooking::getStatus, 5)
        );

        BigDecimal totalDepositAmount = completedBookings.stream()
                .map(ShareItemBooking::getDeposit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.setTotalDepositAmount(totalDepositAmount);

        BigDecimal totalRentIncome = completedBookings.stream()
                .map(ShareItemBooking::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        statistics.setTotalRentIncome(totalRentIncome);

        return statistics;
    }

    /**
     * 转换ShareItem分页数据为ShareItemManageVO分页数据
     */
    private Page<ShareItemManageVO> convertToVOPage(Page<ShareItem> shareItemPage) {
        // 批量获取用户信息
        List<Long> ownerIds = shareItemPage.getRecords().stream()
                .map(ShareItem::getOwnerId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, User> userMap = userMapper.selectBatchIds(ownerIds).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        // 批量获取分类信息
        List<Long> categoryIds = shareItemPage.getRecords().stream()
                .map(ShareItem::getCategoryId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Category> categoryMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getCategoryId, c -> c));

        // 批量获取借用记录（性能优化：避免N+1查询）
        List<Long> shareIds = shareItemPage.getRecords().stream()
                .map(ShareItem::getShareId)
                .distinct()
                .collect(Collectors.toList());
        List<ShareItemBooking> allBookings = shareItemBookingMapper.selectList(
                new LambdaQueryWrapper<ShareItemBooking>()
                        .in(ShareItemBooking::getShareId, shareIds)
                        .in(ShareItemBooking::getStatus, 3, 5) // 只查询借用中和已完成的
        );
        // 按shareId分组
        Map<Long, List<ShareItemBooking>> bookingMap = allBookings.stream()
                .collect(Collectors.groupingBy(ShareItemBooking::getShareId));

        // 转换为VO
        Page<ShareItemManageVO> voPage = new Page<>(shareItemPage.getCurrent(), shareItemPage.getSize(), shareItemPage.getTotal());
        List<ShareItemManageVO> voList = shareItemPage.getRecords().stream()
                .map(item -> convertToVO(item, userMap, categoryMap, bookingMap))
                .collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    /**
     * 转换单个ShareItem为ShareItemManageVO
     */
    private ShareItemManageVO convertToVO(ShareItem shareItem) {
        User owner = userMapper.selectById(shareItem.getOwnerId());
        Category category = categoryMapper.selectById(shareItem.getCategoryId());

        Map<Long, User> userMap = new HashMap<>();
        if (owner != null) {
            userMap.put(owner.getUserId(), owner);
        }

        Map<Long, Category> categoryMap = new HashMap<>();
        if (category != null) {
            categoryMap.put(category.getCategoryId(), category);
        }

        // 查询借用记录
        Map<Long, List<ShareItemBooking>> bookingMap = new HashMap<>();
        List<ShareItemBooking> bookings = shareItemBookingMapper.selectList(
                new LambdaQueryWrapper<ShareItemBooking>()
                        .eq(ShareItemBooking::getShareId, shareItem.getShareId())
                        .in(ShareItemBooking::getStatus, 3, 5)
        );
        if (!bookings.isEmpty()) {
            bookingMap.put(shareItem.getShareId(), bookings);
        }

        return convertToVO(shareItem, userMap, categoryMap, bookingMap);
    }

    /**
     * 转换ShareItem为ShareItemManageVO
     */
    private ShareItemManageVO convertToVO(ShareItem shareItem, Map<Long, User> userMap,
                                          Map<Long, Category> categoryMap,
                                          Map<Long, List<ShareItemBooking>> bookingMap) {
        ShareItemManageVO vo = new ShareItemManageVO();
        BeanUtils.copyProperties(shareItem, vo);

        // 设置所有者信息
        User owner = userMap.get(shareItem.getOwnerId());
        if (owner != null) {
            vo.setOwnerNickname(owner.getNickname());
            vo.setOwnerPhone(owner.getPhone());
        }

        // 设置分类信息
        Category category = categoryMap.get(shareItem.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }

        // 从缓存的数据中统计借用次数（性能优化：避免N+1查询）
        List<ShareItemBooking> bookings = bookingMap.getOrDefault(shareItem.getShareId(), List.of());
        long borrowCount = bookings.stream().filter(b -> b.getStatus() == 5).count();
        long currentBorrowCount = bookings.stream().filter(b -> b.getStatus() == 3).count();
        vo.setBorrowCount((int) borrowCount);
        vo.setCurrentBorrowCount((int) currentBorrowCount);

        // 设置封面图片URL
        vo.setCoverImageUrl(getShareItemCoverImage(shareItem.getShareId()));

        return vo;
    }

    /**
     * 获取共享物品封面图
     */
    private String getShareItemCoverImage(Long shareId) {
        // 从 share_item_image 表查询封面图
        LambdaQueryWrapper<ShareItemImage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareItemImage::getShareId, shareId)
               .eq(ShareItemImage::getIsCover, 1)
               .eq(ShareItemImage::getStatus, 0)  // 0=正常，1=删除
               .last("LIMIT 1");

        ShareItemImage coverImage = shareItemImageMapper.selectOne(wrapper);
        return coverImage != null ? coverImage.getImageUrl() : "";
    }
}
