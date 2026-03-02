package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.entity.LoginDevice;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.LoginDeviceMapper;
import com.xx.xianqijava.service.LoginDeviceService;
import com.xx.xianqijava.vo.LoginDeviceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 登录设备服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginDeviceServiceImpl extends ServiceImpl<LoginDeviceMapper, LoginDevice>
        implements LoginDeviceService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long recordOrUpdateLoginDevice(Long userId, String deviceIdentifier, String deviceName,
                                           String deviceType, String platform, String ip) {
        // 查询是否已存在该设备记录
        LambdaQueryWrapper<LoginDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginDevice::getUserId, userId)
                .eq(LoginDevice::getDeviceIdentifier, deviceIdentifier)
                .eq(LoginDevice::getStatus, 0);

        LoginDevice device = baseMapper.selectOne(queryWrapper);

        if (device == null) {
            // 创建新设备记录
            device = new LoginDevice();
            device.setUserId(userId);
            device.setDeviceIdentifier(deviceIdentifier);
            device.setDeviceName(deviceName);
            device.setDeviceType(deviceType);
            device.setPlatform(platform);
            device.setIsCurrent(1);
            device.setLastLoginIp(ip);
            device.setLastLoginTime(LocalDateTime.now());
            device.setStatus(0);

            // 将该用户的其他设备标记为非当前
            LambdaUpdateWrapper<LoginDevice> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(LoginDevice::getUserId, userId)
                    .set(LoginDevice::getIsCurrent, 0);
            baseMapper.update(null, updateWrapper);

            baseMapper.insert(device);
            log.info("记录新登录设备, deviceId={}, userId={}, deviceType={}",
                    device.getDeviceId(), userId, deviceType);
        } else {
            // 更新最后登录信息
            device.setLastLoginIp(ip);
            device.setLastLoginTime(LocalDateTime.now());

            // 标记为当前设备
            LambdaUpdateWrapper<LoginDevice> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(LoginDevice::getUserId, userId)
                    .set(LoginDevice::getIsCurrent, 0);
            baseMapper.update(null, updateWrapper);

            device.setIsCurrent(1);
            baseMapper.updateById(device);

            log.info("更新登录设备信息, deviceId={}, userId={}", device.getDeviceId(), userId);
        }

        return device.getDeviceId();
    }

    @Override
    public IPage<LoginDeviceVO> getUserLoginDevices(Long userId, Page<LoginDevice> page) {
        LambdaQueryWrapper<LoginDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginDevice::getUserId, userId)
                .eq(LoginDevice::getStatus, 0)
                .orderByDesc(LoginDevice::getIsCurrent)
                .orderByDesc(LoginDevice::getLastLoginTime);

        IPage<LoginDevice> devicePage = baseMapper.selectPage(page, queryWrapper);

        return devicePage.convert(this::convertToVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeLoginDevice(Long userId, Long deviceId) {
        LoginDevice device = baseMapper.selectById(deviceId);
        if (device == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "设备不存在");
        }

        if (!device.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此设备");
        }

        if (device.getIsCurrent() != null && device.getIsCurrent() == 1) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "不能移除当前登录设备");
        }

        device.setStatus(1);
        baseMapper.updateById(device);

        log.info("移除登录设备, deviceId={}, userId={}", deviceId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAllOtherDevices(Long userId, Long currentDeviceId) {
        // 获取当前设备
        LoginDevice currentDevice = baseMapper.selectById(currentDeviceId);
        if (currentDevice == null || !currentDevice.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "设备不存在");
        }

        // 移除所有其他设备
        LambdaUpdateWrapper<LoginDevice> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LoginDevice::getUserId, userId)
                .ne(LoginDevice::getDeviceId, currentDeviceId)
                .set(LoginDevice::getStatus, 1);

        baseMapper.update(null, updateWrapper);

        log.info("移除所有其他登录设备, userId={}, currentDeviceId={}", userId, currentDeviceId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsCurrentDevice(Long userId, String deviceIdentifier) {
        // 先将所有设备标记为非当前
        LambdaUpdateWrapper<LoginDevice> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LoginDevice::getUserId, userId)
                .set(LoginDevice::getIsCurrent, 0);
        baseMapper.update(null, updateWrapper);

        // 标记指定设备为当前
        LambdaQueryWrapper<LoginDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginDevice::getUserId, userId)
                .eq(LoginDevice::getDeviceIdentifier, deviceIdentifier)
                .eq(LoginDevice::getStatus, 0);

        LoginDevice device = baseMapper.selectOne(queryWrapper);
        if (device != null) {
            device.setIsCurrent(1);
            baseMapper.updateById(device);
        }

        log.info("标记当前设备, userId={}, deviceIdentifier={}", userId, deviceIdentifier);
    }

    @Override
    public LoginDevice getCurrentDevice(Long userId, String deviceIdentifier) {
        LambdaQueryWrapper<LoginDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(LoginDevice::getUserId, userId)
                .eq(LoginDevice::getDeviceIdentifier, deviceIdentifier)
                .eq(LoginDevice::getStatus, 0);

        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 转换为VO
     */
    private LoginDeviceVO convertToVO(LoginDevice device) {
        LoginDeviceVO vo = new LoginDeviceVO();
        vo.setDeviceId(device.getDeviceId().toString());
        vo.setDeviceName(device.getDeviceName());
        vo.setDeviceType(device.getDeviceType());
        vo.setPlatform(device.getPlatform());
        vo.setLastLoginTime(device.getLastLoginTime());
        vo.setLastLoginIp(device.getLastLoginIp());
        vo.setIsCurrent(device.getIsCurrent() != null && device.getIsCurrent() == 1);
        return vo;
    }
}
