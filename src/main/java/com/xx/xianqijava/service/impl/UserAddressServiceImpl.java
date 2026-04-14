package com.xx.xianqijava.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.StrFormatter;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.dto.UserAddressCreateDTO;
import com.xx.xianqijava.entity.UserAddress;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.UserAddressMapper;
import com.xx.xianqijava.service.UserAddressService;
import com.xx.xianqijava.vo.UserAddressVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户地址服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAddressServiceImpl extends ServiceImpl<UserAddressMapper, UserAddress>
        implements UserAddressService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAddressVO createAddress(Long userId, UserAddressCreateDTO createDTO) {
        // 如果设置为默认地址，先取消其他默认地址
        if (createDTO.getIsDefault() != null && createDTO.getIsDefault()) {
            LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, userId)
                    .set(UserAddress::getIsDefault, 0);
            baseMapper.update(null, updateWrapper);
        }

        // 创建新地址
        UserAddress address = new UserAddress();
        address.setUserId(userId);
        address.setContactName(createDTO.getContactName());
        address.setContactPhone(createDTO.getContactPhone());
        address.setProvince(createDTO.getProvince());
        address.setCity(createDTO.getCity());
        address.setDistrict(createDTO.getDistrict());
        address.setDetailAddress(createDTO.getDetailAddress());
        address.setPostalCode(createDTO.getPostalCode());
        address.setTag(createDTO.getTag());
        address.setIsDefault(createDTO.getIsDefault() != null && createDTO.getIsDefault() ? 1 : 0);
        address.setLongitude(createDTO.getLongitude());
        address.setLatitude(createDTO.getLatitude());
        address.setStatus(0);

        baseMapper.insert(address);
        log.info("创建用户地址成功, addressId={}, userId={}", address.getAddressId(), userId);

        return convertToVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAddressVO updateAddress(Long addressId, Long userId, UserAddressCreateDTO createDTO) {
        UserAddress address = baseMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "地址不存在");
        }

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此地址");
        }

        // 如果设置为默认地址，先取消其他默认地址
        if (createDTO.getIsDefault() != null && createDTO.getIsDefault()) {
            LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(UserAddress::getUserId, userId)
                    .ne(UserAddress::getAddressId, addressId)
                    .set(UserAddress::getIsDefault, 0);
            baseMapper.update(null, updateWrapper);
        }

        // 更新地址信息
        address.setContactName(createDTO.getContactName());
        address.setContactPhone(createDTO.getContactPhone());
        address.setProvince(createDTO.getProvince());
        address.setCity(createDTO.getCity());
        address.setDistrict(createDTO.getDistrict());
        address.setDetailAddress(createDTO.getDetailAddress());
        address.setPostalCode(createDTO.getPostalCode());
        address.setTag(createDTO.getTag());
        if (createDTO.getIsDefault() != null) {
            address.setIsDefault(createDTO.getIsDefault() ? 1 : 0);
        }
        address.setLongitude(createDTO.getLongitude());
        address.setLatitude(createDTO.getLatitude());

        baseMapper.updateById(address);
        log.info("更新用户地址成功, addressId={}, userId={}", addressId, userId);

        return convertToVO(address);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAddress(Long addressId, Long userId) {
        UserAddress address = baseMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "地址不存在");
        }

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此地址");
        }

        address.setStatus(1);
        baseMapper.updateById(address);

        log.info("删除用户地址成功, addressId={}, userId={}", addressId, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefaultAddress(Long addressId, Long userId) {
        UserAddress address = baseMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "地址不存在");
        }

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限操作此地址");
        }

        // 取消所有默认地址
        LambdaUpdateWrapper<UserAddress> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, 0);
        baseMapper.update(null, updateWrapper);

        // 设置为默认地址
        address.setIsDefault(1);
        baseMapper.updateById(address);

        log.info("设置默认地址成功, addressId={}, userId={}", addressId, userId);
    }

    @Override
    public IPage<UserAddressVO> getUserAddresses(Long userId, Page<UserAddress> page) {
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getStatus, 0)
                .orderByDesc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime);

        IPage<UserAddress> addressPage = baseMapper.selectPage(page, queryWrapper);

        return addressPage.convert(this::convertToVO);
    }

    @Override
    public UserAddressVO getDefaultAddress(Long userId) {
        LambdaQueryWrapper<UserAddress> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getStatus, 0)
                .eq(UserAddress::getIsDefault, 1)
                .orderByDesc(UserAddress::getCreateTime)
                .last("LIMIT 1");

        UserAddress address = baseMapper.selectOne(queryWrapper);
        if (address != null) {
            return convertToVO(address);
        }

        // 如果没有默认地址，返回第一个地址
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddress::getUserId, userId)
                .eq(UserAddress::getStatus, 0)
                .orderByDesc(UserAddress::getCreateTime)
                .last("LIMIT 1");

        address = baseMapper.selectOne(queryWrapper);
        return address != null ? convertToVO(address) : null;
    }

    @Override
    public UserAddressVO getAddressDetail(Long addressId, Long userId) {
        UserAddress address = baseMapper.selectById(addressId);
        if (address == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "地址不存在");
        }

        if (!address.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权限访问此地址");
        }

        return convertToVO(address);
    }

    /**
     * 转换为VO
     */
    private UserAddressVO convertToVO(UserAddress address) {
        UserAddressVO vo = new UserAddressVO();
        vo.setAddressId(String.valueOf(address.getAddressId()));
        vo.setContactName(address.getContactName());
        vo.setContactPhone(address.getContactPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setDistrict(address.getDistrict());
        vo.setDetailAddress(address.getDetailAddress());
        vo.setPostalCode(address.getPostalCode());
        vo.setTag(address.getTag());
        vo.setIsDefault(address.getIsDefault() != null && address.getIsDefault() == 1);
        vo.setLongitude(address.getLongitude());
        vo.setLatitude(address.getLatitude());

        // 生成完整地址
        String fullAddress = StrFormatter.format("{}{}{}{}",
                address.getProvince(),
                address.getCity(),
                address.getDistrict(),
                address.getDetailAddress());
        vo.setFullAddress(fullAddress);

        return vo;
    }
}
