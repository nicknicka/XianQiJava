package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.LoginDevice;
import com.xx.xianqijava.vo.LoginDeviceVO;

import java.util.List;

/**
 * 登录设备服务接口
 */
public interface LoginDeviceService extends IService<LoginDevice> {

    /**
     * 记录或更新登录设备
     *
     * @param userId           用户ID
     * @param deviceIdentifier 设备唯一标识
     * @param deviceName       设备名称
     * @param deviceType       设备类型
     * @param platform         平台
     * @param ip               IP地址
     * @return 设备ID
     */
    Long recordOrUpdateLoginDevice(Long userId, String deviceIdentifier, String deviceName,
                                    String deviceType, String platform, String ip);

    /**
     * 获取用户登录设备列表
     *
     * @param userId 用户ID
     * @param page   分页参数
     * @return 设备列表
     */
    IPage<LoginDeviceVO> getUserLoginDevices(Long userId, Page<LoginDevice> page);

    /**
     * 移除登录设备
     *
     * @param userId   用户ID
     * @param deviceId 设备ID
     */
    void removeLoginDevice(Long userId, Long deviceId);

    /**
     * 移除所有其他设备（只保留当前设备）
     *
     * @param userId           用户ID
     * @param currentDeviceId 当前设备ID
     */
    void removeAllOtherDevices(Long userId, Long currentDeviceId);

    /**
     * 标记当前设备
     *
     * @param userId           用户ID
     * @param deviceIdentifier 设备标识
     */
    void markAsCurrentDevice(Long userId, String deviceIdentifier);

    /**
     * 获取用户当前设备
     *
     * @param userId           用户ID
     * @param deviceIdentifier 设备标识
     * @return 设备信息
     */
    LoginDevice getCurrentDevice(Long userId, String deviceIdentifier);
}
