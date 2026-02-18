package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.Banner;
import com.xx.xianqijava.vo.BannerVO;

import java.util.List;

/**
 * 轮播图服务接口
 */
public interface BannerService extends IService<Banner> {

    /**
     * 获取启用的轮播图列表
     *
     * @return 轮播图列表
     */
    List<BannerVO> getActiveBanners();

    /**
     * 增加轮播图点击次数
     *
     * @param bannerId 轮播图ID
     */
    void incrementClickCount(Long bannerId);
}
