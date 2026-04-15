package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.ShareItemCreateDTO;
import com.xx.xianqijava.dto.ShareItemDraftSaveDTO;
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
import com.xx.xianqijava.util.IdConverter;
import com.xx.xianqijava.vo.ShareItemDraftVO;
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
    private final ObjectMapper objectMapper;

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
        shareItem.setAvailableTimes(normalizeAvailableTimes(createDTO.getAvailableTimes()));

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
        shareItem.setAvailableTimes(normalizeAvailableTimes(createDTO.getAvailableTimes()));
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
            case 4:
                return "草稿";
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

    // ==================== 草稿相关方法实现 ====================

    private static final int MAX_DRAFT_COUNT = 10;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareItemDraftVO saveDraft(ShareItemDraftSaveDTO draftDTO, Long ownerId) {
        log.info("保存共享物品草稿, ownerId={}, draftId={}", ownerId, draftDTO.getDraftId());

        // 1. 如果是新草稿，检查数量限制
        if (draftDTO.getDraftId() == null) {
            int currentDraftCount = countUserDrafts(ownerId);
            if (currentDraftCount >= MAX_DRAFT_COUNT) {
                throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "草稿数量已达上限（" + MAX_DRAFT_COUNT + "个），请先发布或删除部分草稿");
            }
        }

        // 2. 验证分类是否存在（如果提供了分类ID）
        if (draftDTO.getCategoryId() != null) {
            Category category = categoryMapper.selectById(draftDTO.getCategoryId());
            if (category == null || category.getDeleted() == 1) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        ShareItem shareItem;
        boolean isUpdate = draftDTO.getDraftId() != null;

        if (isUpdate) {
            // 更新现有草稿
            shareItem = getById(draftDTO.getDraftId());
            if (shareItem == null) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "共享物品不存在");
            }
            if (!shareItem.getOwnerId().equals(ownerId)) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            if (!shareItem.isDraft()) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "只能修改草稿状态的记录");
            }
        } else {
            // 创建新草稿
            shareItem = new ShareItem();
            shareItem.setOwnerId(ownerId);
            shareItem.setAsDraft();
        }

        // 3. 更新字段（只更新非空字段）
        if (draftDTO.getTitle() != null) {
            shareItem.setTitle(draftDTO.getTitle());
        }
        if (draftDTO.getDescription() != null) {
            shareItem.setDescription(draftDTO.getDescription());
        }
        if (draftDTO.getCategoryId() != null) {
            shareItem.setCategoryId(IdConverter.toLong(draftDTO.getCategoryId()));
        }
        if (draftDTO.getDeposit() != null) {
            shareItem.setDeposit(draftDTO.getDeposit());
        }
        if (draftDTO.getDailyRent() != null) {
            shareItem.setDailyRent(draftDTO.getDailyRent());
        }
        if (draftDTO.getAvailableTimes() != null) {
            shareItem.setAvailableTimes(normalizeAvailableTimes(draftDTO.getAvailableTimes()));
        }

        // 4. 保存共享物品
        boolean saved = isUpdate ? updateById(shareItem) : save(shareItem);
        if (!saved) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "草稿保存失败");
        }

        // 5. 处理图片（草稿也支持图片上传）
        if (draftDTO.getImageUrls() != null && !draftDTO.getImageUrls().isEmpty()) {
            saveShareItemImages(shareItem.getShareId(), draftDTO.getImageUrls());
        }

        log.info("草稿保存成功, shareId={}", shareItem.getShareId());
        return convertToDraftVO(shareItem);
    }

    @Override
    public IPage<ShareItemDraftVO> getDraftList(Page<ShareItem> page, Long ownerId) {
        log.info("获取用户草稿列表, ownerId={}, page={}", ownerId, page.getCurrent());

        LambdaQueryWrapper<ShareItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareItem::getOwnerId, ownerId);
        wrapper.eq(ShareItem::getStatus, ShareItem.STATUS_DRAFT);
        wrapper.eq(ShareItem::getDeleted, 0);
        wrapper.orderByDesc(ShareItem::getUpdateTime);

        IPage<ShareItem> draftPage = page(page, wrapper);
        return draftPage.convert(this::convertToDraftVO);
    }

    @Override
    public ShareItemDraftVO getDraftDetail(Long draftId, Long ownerId) {
        log.info("获取草稿详情, draftId={}, ownerId={}", draftId, ownerId);

        ShareItem shareItem = getById(draftId);
        if (shareItem == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "共享物品不存在");
        }
        if (!shareItem.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!shareItem.isDraft()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该记录不是草稿");
        }

        return convertToDraftVO(shareItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ShareItemVO publishFromDraft(Long draftId, Long ownerId) {
        log.info("从草稿发布共享物品, draftId={}, ownerId={}", draftId, ownerId);

        ShareItem shareItem = getById(draftId);
        if (shareItem == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "共享物品不存在");
        }
        if (!shareItem.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!shareItem.isDraft()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该记录不是草稿");
        }

        // 验证必填字段
        validateRequiredFields(shareItem);
        shareItem.setAvailableTimes(normalizeAvailableTimes(shareItem.getAvailableTimes()));

        // 更新状态为可借用
        shareItem.setStatus(ShareItem.STATUS_AVAILABLE);

        boolean updated = updateById(shareItem);
        if (!updated) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "发布失败");
        }

        log.info("草稿发布成功, shareId={}", draftId);
        return convertToVO(shareItem);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDraft(Long draftId, Long ownerId) {
        log.info("删除草稿, draftId={}, ownerId={}", draftId, ownerId);

        ShareItem shareItem = getById(draftId);
        if (shareItem == null) {
            throw new BusinessException(ErrorCode.DRAFT_NOT_FOUND);
        }
        if (!shareItem.getOwnerId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (!shareItem.isDraft()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "只能删除草稿状态的记录");
        }

        // 逻辑删除
        boolean deleted = removeById(draftId);
        if (!deleted) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "草稿删除失败");
        }

        // 删除关联图片
        LambdaQueryWrapper<ShareItemImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ShareItemImage::getShareId, draftId);
        shareItemImageMapper.delete(imageWrapper);

        log.info("草稿删除成功");
    }

    @Override
    public int countUserDrafts(Long ownerId) {
        return Math.toIntExact(lambdaQuery()
                .eq(ShareItem::getOwnerId, ownerId)
                .eq(ShareItem::getStatus, ShareItem.STATUS_DRAFT)
                .eq(ShareItem::getDeleted, 0)
                .count());
    }

    /**
     * 验证发布时的必填字段
     */
    private void validateRequiredFields(ShareItem shareItem) {
        List<String> missingFields = new ArrayList<>();

        if (shareItem.getTitle() == null || shareItem.getTitle().trim().isEmpty()) {
            missingFields.add("物品标题");
        }
        if (shareItem.getCategoryId() == null) {
            missingFields.add("分类");
        }
        if (shareItem.getDeposit() == null) {
            missingFields.add("押金");
        }
        if (shareItem.getDailyRent() == null) {
            missingFields.add("日租金");
        }

        if (!missingFields.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                "发布前请完善以下必填信息：" + String.join("、", missingFields));
        }
    }

    /**
     * 规范化可借用时间段 JSON，避免非法字符串直接写入 JSON 列。
     */
    private String normalizeAvailableTimes(String availableTimes) {
        if (StrUtil.isBlank(availableTimes)) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(availableTimes));
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "availableTimes 必须为合法JSON格式");
        }
    }

    /**
     * 保存共享物品图片
     */
    private void saveShareItemImages(Long shareId, List<String> imageUrls) {
        // 删除旧图片
        LambdaQueryWrapper<ShareItemImage> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ShareItemImage::getShareId, shareId);
        shareItemImageMapper.delete(deleteWrapper);

        // 保存新图片
        for (int i = 0; i < imageUrls.size() && i < 9; i++) {
            ShareItemImage image = new ShareItemImage();
            image.setShareId(shareId);
            image.setImageUrl(imageUrls.get(i));
            image.setSortOrder(i);
            image.setIsCover(i == 0 ? 1 : 0); // 第一张为封面
            image.setStatus(0);
            shareItemImageMapper.insert(image);
        }
    }

    /**
     * 转换为草稿VO
     */
    private ShareItemDraftVO convertToDraftVO(ShareItem shareItem) {
        ShareItemDraftVO vo = new ShareItemDraftVO();
        BeanUtil.copyProperties(shareItem, vo);
        vo.setDraftId(String.valueOf(shareItem.getShareId()));
        vo.setShareId(String.valueOf(shareItem.getShareId()));

        // 格式化时间
        if (shareItem.getCreateTime() != null) {
            vo.setCreateTime(shareItem.getCreateTime().toString());
        }
        if (shareItem.getUpdateTime() != null) {
            vo.setUpdateTime(shareItem.getUpdateTime().toString());
        }

        // 获取分类名称
        if (shareItem.getCategoryId() != null) {
            Category category = categoryMapper.selectById(shareItem.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 获取图片
        LambdaQueryWrapper<ShareItemImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ShareItemImage::getShareId, shareItem.getShareId())
                .eq(ShareItemImage::getStatus, 0)
                .orderByAsc(ShareItemImage::getSortOrder);
        List<ShareItemImage> images = shareItemImageMapper.selectList(imageWrapper);

        if (!images.isEmpty()) {
            String[] imageUrls = images.stream()
                    .map(ShareItemImage::getImageUrl)
                    .toArray(String[]::new);
            vo.setImages(imageUrls);
            vo.setImageCount(images.size());
            vo.setCoverImage(images.get(0).getImageUrl());
        }

        // 计算完成度和缺失字段
        vo.setCompletion(calculateCompletion(shareItem));
        vo.setMissingFields(getMissingFields(shareItem));

        return vo;
    }

    /**
     * 计算草稿完成度（0-100）
     */
    private Integer calculateCompletion(ShareItem shareItem) {
        int totalFields = 5; // 标题、描述、分类、押金、日租金、图片
        int completedFields = 0;

        if (shareItem.getTitle() != null && !shareItem.getTitle().trim().isEmpty()) {
            completedFields++;
        }
        if (shareItem.getDescription() != null && !shareItem.getDescription().trim().isEmpty()) {
            completedFields++;
        }
        if (shareItem.getCategoryId() != null) {
            completedFields++;
        }
        if (shareItem.getDeposit() != null) {
            completedFields++;
        }
        if (shareItem.getDailyRent() != null) {
            completedFields++;
        }

        // 检查是否有图片
        LambdaQueryWrapper<ShareItemImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ShareItemImage::getShareId, shareItem.getShareId())
                .eq(ShareItemImage::getStatus, 0);
        long imageCount = shareItemImageMapper.selectCount(imageWrapper);
        if (imageCount > 0) {
            completedFields++;
        }

        return (completedFields * 100) / totalFields;
    }

    /**
     * 获取缺失的必填字段列表
     */
    private String[] getMissingFields(ShareItem shareItem) {
        List<String> missingFields = new ArrayList<>();

        if (shareItem.getTitle() == null || shareItem.getTitle().trim().isEmpty()) {
            missingFields.add("物品标题");
        }
        if (shareItem.getDescription() == null || shareItem.getDescription().trim().isEmpty()) {
            missingFields.add("物品描述");
        }
        if (shareItem.getCategoryId() == null) {
            missingFields.add("分类");
        }
        if (shareItem.getDeposit() == null) {
            missingFields.add("押金");
        }
        if (shareItem.getDailyRent() == null) {
            missingFields.add("日租金");
        }

        // 检查是否有图片
        LambdaQueryWrapper<ShareItemImage> imageWrapper = new LambdaQueryWrapper<>();
        imageWrapper.eq(ShareItemImage::getShareId, shareItem.getShareId())
                .eq(ShareItemImage::getStatus, 0);
        long imageCount = shareItemImageMapper.selectCount(imageWrapper);
        if (imageCount == 0) {
            missingFields.add("物品图片");
        }

        return missingFields.toArray(new String[0]);
    }
}
