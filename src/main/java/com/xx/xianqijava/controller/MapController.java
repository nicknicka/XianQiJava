package com.xx.xianqijava.controller;

import com.xx.xianqijava.common.Result;
import com.xx.xianqijava.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 地图服务控制器
 */
@Slf4j
@Tag(name = "地图服务", description = "地理位置相关接口")
@RestController
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    /**
     * 地址解析（地址 -> 坐标）
     */
    @Operation(summary = "地址解析", description = "将地址转换为经纬度坐标")
    @GetMapping("/geocode")
    public Result<Map<String, Object>> geocode(
            @Parameter(description = "地址") @RequestParam String address) {
        log.info("地址解析请求: address={}", address);
        Map<String, Object> result = mapService.geocode(address);
        return Result.success(result);
    }

    /**
     * 逆地址解析（坐标 -> 地址）
     */
    @Operation(summary = "逆地址解析", description = "将经纬度坐标转换为地址")
    @GetMapping("/reverse-geocode")
    public Result<Map<String, Object>> reverseGeocode(
            @Parameter(description = "纬度") @RequestParam Double latitude,
            @Parameter(description = "经度") @RequestParam Double longitude) {
        log.info("逆地址解析请求: lat={}, lon={}", latitude, longitude);
        Map<String, Object> result = mapService.reverseGeocode(latitude, longitude);
        return Result.success(result);
    }

    /**
     * 计算两点距离
     */
    @Operation(summary = "计算两点距离", description = "计算两个坐标点之间的距离")
    @GetMapping("/distance")
    public Result<Double> calculateDistance(
            @Parameter(description = "起点纬度") @RequestParam Double lat1,
            @Parameter(description = "起点经度") @RequestParam Double lon1,
            @Parameter(description = "终点纬度") @RequestParam Double lat2,
            @Parameter(description = "终点经度") @RequestParam Double lon2) {
        log.info("计算距离请求: ({}, {}) -> ({}, {})", lat1, lon1, lat2, lon2);
        double distance = mapService.calculateDistance(lat1, lon1, lat2, lon2);
        return Result.success(distance);
    }

    /**
     * 路线规划
     */
    @Operation(summary = "路线规划", description = "规划两点之间的路线")
    @GetMapping("/route")
    public Result<Map<String, Object>> routePlanning(
            @Parameter(description = "起点纬度") @RequestParam Double startLatitude,
            @Parameter(description = "起点经度") @RequestParam Double startLongitude,
            @Parameter(description = "终点纬度") @RequestParam Double endLatitude,
            @Parameter(description = "终点经度") @RequestParam Double endLongitude,
            @Parameter(description = "出行方式: driving/walking/bicycling")
            @RequestParam(defaultValue = "walking") String mode) {
        log.info("路线规划请求: ({}, {}) -> ({}, {}), mode={}",
                startLatitude, startLongitude, endLatitude, endLongitude, mode);
        Map<String, Object> result = mapService.routePlanning(
                startLatitude, startLongitude,
                endLatitude, endLongitude,
                mode
        );
        return Result.success(result);
    }

    /**
     * 搜索附近
     */
    @Operation(summary = "搜索附近", description = "搜索附近的POI")
    @GetMapping("/search/nearby")
    public Result<List<Map<String, Object>>> searchNearby(
            @Parameter(description = "纬度") @RequestParam Double latitude,
            @Parameter(description = "经度") @RequestParam Double longitude,
            @Parameter(description = "关键词") @RequestParam String keyword,
            @Parameter(description = "搜索半径（米）") @RequestParam(defaultValue = "1000") Integer radius) {
        log.info("搜索附近请求: lat={}, lon={}, keyword={}, radius={}",
                latitude, longitude, keyword, radius);
        List<Map<String, Object>> result = mapService.searchNearby(
                latitude, longitude, keyword, radius
        );
        return Result.success(result);
    }

    /**
     * 获取附近用户
     */
    @Operation(summary = "获取附近用户", description = "查询附近的其他用户")
    @GetMapping("/users/nearby")
    public Result<List<Map<String, Object>>> getNearbyUsers(
            @Parameter(description = "纬度") @RequestParam Double latitude,
            @Parameter(description = "经度") @RequestParam Double longitude,
            @Parameter(description = "搜索半径（米）") @RequestParam(defaultValue = "5000") Integer radius,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "20") Integer limit) {
        log.info("查询附近用户请求: lat={}, lon={}, radius={}, limit={}",
                latitude, longitude, radius, limit);
        List<Map<String, Object>> result = mapService.getNearbyUsers(
                latitude, longitude, radius, limit
        );
        return Result.success(result);
    }
}
