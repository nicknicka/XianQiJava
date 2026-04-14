package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.entity.CampusLocation;
import com.xx.xianqijava.mapper.CampusLocationMapper;
import com.xx.xianqijava.vo.CampusLocationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 校园位置控制器
 */
@Slf4j
@Tag(name = "校园位置管理")
@RestController
@RequestMapping("/location")
@RequiredArgsConstructor
public class CampusLocationController {

    private final CampusLocationMapper campusLocationMapper;

    /**
     * 获取校园位置列表
     */
    @Operation(summary = "获取校园位置列表")
    @GetMapping
    public Result<List<CampusLocationVO>> getLocationList() {
        log.info("获取校园位置列表");

        List<CampusLocation> locations = campusLocationMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CampusLocation>()
                .eq(CampusLocation::getStatus, 1)
                .eq(CampusLocation::getDeleted, 0)
                .orderByAsc(CampusLocation::getSortOrder)
        );

        List<CampusLocationVO> locationVOList = new ArrayList<>();
        for (CampusLocation location : locations) {
            CampusLocationVO vo = convertToVO(location);
            locationVOList.add(vo);
        }

        return Result.success(locationVOList);
    }

    /**
     * 获取位置详情
     */
    @Operation(summary = "获取位置详情")
    @GetMapping("/{id}")
    public Result<CampusLocationVO> getLocationDetail(
            @PathVariable("id") Long id) {
        log.info("获取位置详情, locationId={}", id);

        CampusLocation location = campusLocationMapper.selectById(id);
        if (location == null || location.getDeleted() == 1) {
            return Result.error("位置不存在");
        }

        CampusLocationVO vo = convertToVO(location);
        return Result.success(vo);
    }

    /**
     * 根据代码获取位置
     */
    @Operation(summary = "根据代码获取位置")
    @GetMapping("/code/{code}")
    public Result<CampusLocationVO> getLocationByCode(
            @PathVariable("code") String code) {
        log.info("根据代码获取位置, code={}", code);

        CampusLocation location = campusLocationMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<CampusLocation>()
                .eq(CampusLocation::getCode, code)
                .eq(CampusLocation::getDeleted, 0)
        );

        if (location == null) {
            return Result.error("位置不存在");
        }

        CampusLocationVO vo = convertToVO(location);
        return Result.success(vo);
    }

    /**
     * 转换为 VO
     */
    private CampusLocationVO convertToVO(CampusLocation location) {
        CampusLocationVO vo = new CampusLocationVO();
        vo.setLocationId(String.valueOf(location.getLocationId()));
        vo.setName(location.getName());
        vo.setCode(location.getCode());
        vo.setDescription(location.getDescription());
        vo.setSortOrder(location.getSortOrder());
        vo.setStatus(location.getStatus());
        return vo;
    }
}
