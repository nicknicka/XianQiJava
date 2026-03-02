package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.dto.UserAddressCreateDTO;
import com.xx.xianqijava.entity.UserAddress;
import com.xx.xianqijava.vo.UserAddressVO;

/**
 * 用户地址服务接口
 */
public interface UserAddressService extends IService<UserAddress> {

    /**
     * 创建用户地址
     *
     * @param userId  用户ID
     * @param createDTO 创建DTO
     * @return 地址VO
     */
    UserAddressVO createAddress(Long userId, UserAddressCreateDTO createDTO);

    /**
     * 更新用户地址
     *
     * @param addressId 地址ID
     * @param userId    用户ID
     * @param createDTO 更新DTO
     * @return 地址VO
     */
    UserAddressVO updateAddress(Long addressId, Long userId, UserAddressCreateDTO createDTO);

    /**
     * 删除用户地址
     *
     * @param addressId 地址ID
     * @param userId    用户ID
     */
    void deleteAddress(Long addressId, Long userId);

    /**
     * 设置默认地址
     *
     * @param addressId 地址ID
     * @param userId    用户ID
     */
    void setDefaultAddress(Long addressId, Long userId);

    /**
     * 获取用户地址列表
     *
     * @param userId 用户ID
     * @return 地址列表
     */
    IPage<UserAddressVO> getUserAddresses(Long userId, Page<UserAddress> page);

    /**
     * 获取用户默认地址
     *
     * @param userId 用户ID
     * @return 地址VO
     */
    UserAddressVO getDefaultAddress(Long userId);

    /**
     * 获取地址详情
     *
     * @param addressId 地址ID
     * @param userId    用户ID
     * @return 地址VO
     */
    UserAddressVO getAddressDetail(Long addressId, Long userId);
}
