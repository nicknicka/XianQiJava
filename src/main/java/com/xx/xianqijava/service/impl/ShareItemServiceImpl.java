package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.ShareItemCreateDTO;
import com.xx.xianqijava.entity.Category;
import com.xx.xianqijava.entity.ShareItem;
import com.xx.xianqijava.entity.ShareItemImage;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.CategoryMapper;
import com.xx.xianqijava.mapper.ShareItemImageMapper;
import com.xx.xianqijava.mapper.ShareItemMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.ShareItemService;
import com.xx.xianqijava.vo.ShareItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 共享物品服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShareItemServiceImpl extends ServiceImpl<ShareItemMapper, ShareItem>
        implements ShareItemService {

    private final ShareItemImageMapper shareItemImageMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareItemVO createShareItem(ShareItemCreateDTO createDTO, Long ownerId) {
        log.info("创建共享物品, ownerId={}, title={}", ownerId, createDTO.getTitle());

        // 验证分类是否存在
        Category category = categoryMapper.selectById(createDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "分类不存在");
        }

        // 创建共享物品
        ShareItem shareItem = new ShareItem();
        BeanUtil.copyProperties(createDTO, shareItem);
        shareItem.setOwnerId(ownerId);
        shareItem.setStatus(1); // 默认可借用
        shareItem.setImageCount(createDTO.getImageUrls() != null ? createDTO.getImageUrls().size() : 0);

        save(shareItem);

        // 处理图片
        if (createDTO.getImageUrls() != null && !createDTO.getImageUrls().isEmpty()) {
            List<ShareItemImage> images = new ArrayList<>();
            for (int i = 0; i < createDTO.getImageUrls().size(); i++) {
                ShareItemImage image = new ShareItemImage();
                image.setShareId(shareItem.getShareId());
                image.setImageUrl(createDTO.getImageUrls().get(i));
                image.setSortOrder(i);
                image.setIsCover(i == 0 ? 1 : 0); // 第一张作为封面
                image.setStatus(0);
                images.add(image);
            }
            if (!images.isEmpty()) {
                images.forEach(shareItemImageMapper::insert);
            }
            // 更新封面图片ID
            if (!images.isEmpty()) {
                shareItem.setCoverImageId(images.get(0).getImageId());
                updateById(shareItem);
            }
        }

        log.info("共享物品创建成功, shareId={}", shareItem.getShareId());
        return convertToVO(shareItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareItemVO updateShareItem(Long shareId, ShareItemCreateDTO createDTO, Long ownerId) {
        log.info("更新共享物品, shareId={}, ownerId={}", shareId, ownerId);

        ShareItem shareItem = getById(shareId);
        if (shareItem == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "共享物品不存在");
        }

        // 验证权限
        if (!shareItem.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限修改此共享物品");
        }

        // 验证分类是否存在
        Category category = categoryMapper.selectById(createDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "分类不存在");
        }

        // 更新基本信息
        BeanUtil.copyProperties(createDTO, shareItem);
        if (createDTO.getImageUrls() != null) {
            shareItem.setImageCount(createDTO.getImageUrls().size());
        }
        updateById(shareItem);

        // 先添加新图片，成功后再删除旧图片（避免数据丢失）
        List<ShareItemImage> newImages = new ArrayList<>();
        if (createDTO.getImageUrls() != null && !createDTO.getImageUrls().isEmpty()) {
            for (int i = 0; i < createDTO.getImageUrls().size(); i++) {
                ShareItemImage image = new ShareItemImage();
                image.setShareId(shareId);
                image.setImageUrl(createDTO.getImageUrls().get(i));
                image.setSortOrder(i);
                image.setIsCover(i == 0 ? 1 : 0);
                image.setStatus(0);
                newImages.add(image);
            }
            // 批量插入新图片
            if (!newImages.isEmpty()) {
                newImages.forEach(shareItemImageMapper::insert);
                shareItem.setCoverImageId(newImages.get(0).getImageId());
                updateById(shareItem);
            }
        }

        // 新图片插入成功后，删除旧图片
        // 注意：整个方法在@Transactional中，如果新图片插入失败，删除操作会回滚
        LambdaQueryWrapper<ShareItemImage> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ShareItemImage::getShareId, shareId);
        // 排除刚插入的新图片
        if (!newImages.isEmpty()) {
            List<Long> newImageIds = newImages.stream()
                    .map(ShareItemImage::getImageId)
                    .collect(java.util.stream.Collectors.toList());
            deleteWrapper.notIn(ShareItemImage::getImageId, newImageIds);
        }
        shareItemImageMapper.delete(deleteWrapper);

        log.info("共享物品更新成功, shareId={}", shareId);
        return convertToVO(shareItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteShareItem(Long shareId, Long ownerId) {
        log.info("删除共享物品, shareId={}, ownerId={}", shareId, ownerId);

        ShareItem shareItem = getById(shareId);
        if (shareItem == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "共享物品不存在");
        }

        // 验证权限
        if (!shareItem.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限删除此共享物品");
        }

        // 借用中的物品不能删除
        if (shareItem.getStatus() == 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "借用中的物品不能删除");
        }

        // 删除图片
        LambdaQueryWrapper<ShareItemImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ShareItemImage::getShareId, shareId);
        shareItemImageMapper.delete(imageWrapper);

        // 删除共享物品
        removeById(shareId);

        log.info("共享物品删除成功, shareId={}", shareId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShareItemStatus(Long shareId, Integer status, Long ownerId) {
        log.info("更新共享物品状态, shareId={}, status={}, ownerId={}", shareId, status, ownerId);

        ShareItem shareItem = getById(shareId);
        if (shareItem == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "共享物品不存在");
        }

        // 验证权限
        if (!shareItem.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限修改此共享物品");
        }

        shareItem.setStatus(status);
        updateById(shareItem);

        log.info("共享物品状态更新成功, shareId={}, status={}", shareId, status);
    }

    @Override
    public ShareItemVO getShareItemDetail(Long shareId) {
        log.info("查询共享物品详情, shareId={}", shareId);

        ShareItem shareItem = getById(shareId);
        if (shareItem == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "共享物品不存在");
        }

        return convertToVO(shareItem);
    }

    @Override
    public IPage<ShareItemVO> getShareItemList(Page<ShareItem> page, Long categoryId, Integer status, String keyword) {
        log.info("查询共享物品列表, page={}, categoryId={}, status={}, keyword={}",
                page.getCurrent(), categoryId, status, keyword);

        LambdaQueryWrapper<ShareItem> queryWrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            queryWrapper.eq(ShareItem::getCategoryId, categoryId);
        }
        if (status != null) {
            queryWrapper.eq(ShareItem::getStatus, status);
        }
        if (StrUtil.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(ShareItem::getTitle, keyword)
                    .or()
                    .like(ShareItem::getDescription, keyword)
            );
        }
        queryWrapper.orderByDesc(ShareItem::getCreateTime);

        IPage<ShareItem> shareItemPage = page(page, queryWrapper);
        return shareItemPage.convert(this::convertToVO);
    }

    @Override
    public IPage<ShareItemVO> getMyShareItems(Page<ShareItem> page, Long ownerId) {
        log.info("查询我的共享物品列表, ownerId={}, page={}", ownerId, page.getCurrent());

        LambdaQueryWrapper<ShareItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShareItem::getOwnerId, ownerId)
                .orderByDesc(ShareItem::getCreateTime);

        IPage<ShareItem> shareItemPage = page(page, queryWrapper);
        return shareItemPage.convert(this::convertToVO);
    }

    /**
     * 转换为VO
     */
    private ShareItemVO convertToVO(ShareItem shareItem) {
        ShareItemVO vo = new ShareItemVO();
        BeanUtil.copyProperties(shareItem, vo);

        // 设置状态描述
        vo.setStatusDesc(getStatusDesc(shareItem.getStatus()));
        vo.setCanBorrow(shareItem.getStatus() == 1);

        // 设置时间
        if (shareItem.getCreateTime() != null) {
            vo.setCreateTime(shareItem.getCreateTime().toString());
        }
        if (shareItem.getUpdateTime() != null) {
            vo.setUpdateTime(shareItem.getUpdateTime().toString());
        }

        // 获取所有者信息
        User owner = userMapper.selectById(shareItem.getOwnerId());
        if (owner != null) {
            vo.setOwnerNickname(owner.getNickname());
            vo.setOwnerAvatar(owner.getAvatar());
            vo.setOwnerCreditScore(owner.getCreditScore());
        }

        // 获取分类信息
        if (shareItem.getCategoryId() != null) {
            Category category = categoryMapper.selectById(shareItem.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 获取图片列表
        LambdaQueryWrapper<ShareItemImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ShareItemImage::getShareId, shareItem.getShareId())
                .eq(ShareItemImage::getStatus, 0)
                .orderByAsc(ShareItemImage::getSortOrder);
        List<ShareItemImage> images = shareItemImageMapper.selectList(imageWrapper);

        List<String> imageUrls = new ArrayList<>();
        String coverImageUrl = null;
        for (ShareItemImage image : images) {
            imageUrls.add(image.getImageUrl());
            if (image.getIsCover() == 1 || coverImageUrl == null) {
                coverImageUrl = image.getImageUrl();
            }
        }
        vo.setImageUrls(imageUrls);
        vo.setCoverImageUrl(coverImageUrl);

        return vo;
    }

    /**
     * 获取状态描述
     */
    private String getStatusDesc(Integer status) {
        switch (status) {
            case 0:
                return "已下架";
            case 1:
                return "可借用";
            case 2:
                return "借用中";
            default:
                return "未知状态";
        }
    }

    @Override
    public IPage<ShareItemVO> getNearbyShareItems(Page<ShareItem> page, Long userId) {
        log.info("获取附近共享物品, userId={}", userId);

        // 获取当前用户信息
        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        LambdaQueryWrapper<ShareItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShareItem::getStatus, 1); // 只查询可借用的物品

        // 优先推荐同学院/同专业的用户的共享物品
        if (currentUser.getCollege() != null && !currentUser.getCollege().isEmpty()) {
            // 获取同学院用户列表
            LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.eq(User::getCollege, currentUser.getCollege())
                    .select(User::getUserId);
            List<Long> collegeUserIds = userMapper.selectList(userWrapper).stream()
                    .map(User::getUserId)
                    .collect(java.util.stream.Collectors.toList());

            if (!collegeUserIds.isEmpty()) {
                queryWrapper.and(wrapper -> wrapper.in(ShareItem::getOwnerId, collegeUserIds)
                        .or()
                        .ne(ShareItem::getOwnerId, userId)); // 或者其他用户的物品
            } else {
                queryWrapper.ne(ShareItem::getOwnerId, userId); // 排除自己的物品
            }
        } else {
            queryWrapper.ne(ShareItem::getOwnerId, userId); // 排除自己的物品
        }

        queryWrapper.orderByDesc(ShareItem::getCreateTime);

        IPage<ShareItem> shareItemPage = page(page, queryWrapper);
        return shareItemPage.convert(this::convertToVO);
    }
}
