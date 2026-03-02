package com.xx.xianqijava.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.dto.UserAddressCreateDTO;
import com.xx.xianqijava.entity.UserAddress;
import com.xx.xianqijava.service.UserAddressService;
import com.xx.xianqijava.util.SecurityUtil;
import com.xx.xianqijava.vo.UserAddressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户地址控制器
 */
@Slf4j
@Tag(name = "用户地址管理")
@RestController
@RequestMapping("/user/address")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    /**
     * 创建用户地址
     */
    @Operation(summary = "创建用户地址")
    @PostMapping
    public Result<UserAddressVO> createAddress(@Valid @RequestBody UserAddressCreateDTO createDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("创建用户地址, userId={}", userId);
        UserAddressVO addressVO = userAddressService.createAddress(userId, createDTO);
        return Result.success("地址创建成功", addressVO);
    }

    /**
     * 更新用户地址
     */
    @Operation(summary = "更新用户地址")
    @PutMapping("/{addressId}")
    public Result<UserAddressVO> updateAddress(
            @Parameter(description = "地址ID") @PathVariable("addressId") Long addressId,
            @Valid @RequestBody UserAddressCreateDTO createDTO) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("更新用户地址, addressId={}, userId={}", addressId, userId);
        UserAddressVO addressVO = userAddressService.updateAddress(addressId, userId, createDTO);
        return Result.success("地址更新成功", addressVO);
    }

    /**
     * 删除用户地址
     */
    @Operation(summary = "删除用户地址")
    @DeleteMapping("/{addressId}")
    public Result<Void> deleteAddress(
            @Parameter(description = "地址ID") @PathVariable("addressId") Long addressId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("删除用户地址, addressId={}, userId={}", addressId, userId);
        userAddressService.deleteAddress(addressId, userId);
        return Result.success("地址删除成功");
    }

    /**
     * 设置默认地址
     */
    @Operation(summary = "设置默认地址")
    @PutMapping("/{addressId}/default")
    public Result<Void> setDefaultAddress(
            @Parameter(description = "地址ID") @PathVariable("addressId") Long addressId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("设置默认地址, addressId={}, userId={}", addressId, userId);
        userAddressService.setDefaultAddress(addressId, userId);
        return Result.success("默认地址设置成功");
    }

    /**
     * 获取用户地址列表
     */
    @Operation(summary = "获取用户地址列表")
    @GetMapping
    public Result<IPage<UserAddressVO>> getAddressList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页大小") @RequestParam(defaultValue = "20") Integer size) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取用户地址列表, userId={}, page={}, size={}", userId, page, size);

        Page<UserAddress> pageParam = new Page<>(page, size);
        IPage<UserAddressVO> addressPage = userAddressService.getUserAddresses(userId, pageParam);

        return Result.success(addressPage);
    }

    /**
     * 获取默认地址
     */
    @Operation(summary = "获取默认地址")
    @GetMapping("/default")
    public Result<UserAddressVO> getDefaultAddress() {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取默认地址, userId={}", userId);
        UserAddressVO addressVO = userAddressService.getDefaultAddress(userId);
        return Result.success(addressVO);
    }

    /**
     * 获取地址详情
     */
    @Operation(summary = "获取地址详情")
    @GetMapping("/{addressId}")
    public Result<UserAddressVO> getAddressDetail(
            @Parameter(description = "地址ID") @PathVariable("addressId") Long addressId) {
        Long userId = SecurityUtil.getCurrentUserIdRequired();
        log.info("获取地址详情, addressId={}, userId={}", addressId, userId);
        UserAddressVO addressVO = userAddressService.getAddressDetail(addressId, userId);
        return Result.success(addressVO);
    }
}
