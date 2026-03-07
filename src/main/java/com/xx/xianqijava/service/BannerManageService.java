package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.dto.admin.BannerCreateDTO;
import com.xx.xianqijava.dto.admin.BannerManageQueryDTO;
import com.xx.xianqijava.dto.admin.BannerUpdateDTO;
import com.xx.xianqijava.vo.admin.BannerManageVO;

/**
 * 轮播图管理服务接口 - 管理端
 */
public interface BannerManageService {

    /**
     * 分页查询轮播图列表
     *
     * @param queryDTO 查询条件
     * @return 轮播图分页数据
     */
    Page<BannerManageVO> getBannerList(BannerManageQueryDTO queryDTO);

    /**
     * 获取轮播图详情
     *
     * @param bannerId 轮播图ID
     * @return 轮播图详情
     */
    BannerManageVO getBannerDetail(Long bannerId);

    /**
     * 创建轮播图
     *
     * @param createDTO 创建DTO
     * @return 是否成功
     */
    Boolean createBanner(BannerCreateDTO createDTO);

    /**
     * 更新轮播图
     *
     * @param updateDTO 更新DTO
     * @return 是否成功
     */
    Boolean updateBanner(BannerUpdateDTO updateDTO);

    /**
     * 删除轮播图
     *
     * @param bannerId 轮播图ID
     * @return 是否成功
     */
    Boolean deleteBanner(Long bannerId);

    /**
     * 启用/禁用轮播图
     *
     * @param bannerId 轮播图ID
     * @param status 状态：0-禁用，1-启用
     * @return 是否成功
     */
    Boolean updateBannerStatus(Long bannerId, Integer status);
}
