package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.entity.UserAddress;
import com.xx.xianqijava.mapper.UserAddressMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 地图服务实现类
 * 使用 Haversine 公式计算距离，支持对接高德/腾讯地图 API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapServiceImpl implements MapService {

    private final UserMapper userMapper;
    private final UserAddressMapper userAddressMapper;

    @Value("${map.enabled:false}")
    private boolean mapEnabled;

    @Value("${map.provider:amap}")
    private String mapProvider;

    @Value("${map.api-key:}")
    private String apiKey;

    // 地球平均半径（米）
    private static final double EARTH_RADIUS = 6371000;

    @Override
    public Map<String, Object> geocode(String address) {
        log.info("地址解析: address={}", address);

        Map<String, Object> result = new HashMap<>();

        if (!mapEnabled || apiKey.isEmpty()) {
            // 模拟实现
            result.put("latitude", 23.1291);  // 广州市中心
            result.put("longitude", 113.2644);
            result.put("formattedAddress", address);
            result.put("level", "模拟数据");
            log.warn("地图服务未启用，返回模拟数据");
        } else {
            // TODO: 对接高德/腾讯地图 API
            // 高德地图地理编码 API: https://restapi.amap.com/v3/geocode/geo
            // 腾讯地图地理编码 API: https://apis.map.qq.com/ws/geocoder/v1/
            result.put("latitude", 0);
            result.put("longitude", 0);
            result.put("message", "待实现");
        }

        return result;
    }

    @Override
    public Map<String, Object> reverseGeocode(double latitude, double longitude) {
        log.info("逆地址解析: lat={}, lon={}", latitude, longitude);

        Map<String, Object> result = new HashMap<>();

        if (!mapEnabled || apiKey.isEmpty()) {
            // 模拟实现
            result.put("formattedAddress", "广东省广州市天河区");
            result.put("province", "广东省");
            result.put("city", "广州市");
            result.put("district", "天河区");
            result.put("street", "五山路");
            result.put("level", "模拟数据");
            log.warn("地图服务未启用，返回模拟数据");
        } else {
            // TODO: 对接高德/腾讯地图 API
            // 高德地图逆地理编码 API: https://restapi.amap.com/v3/geocode/regeo
            // 腾讯地图逆地理编码 API: https://apis.map.qq.com/ws/geocoder/v1/
            result.put("formattedAddress", "待实现");
            result.put("message", "待实现");
        }

        return result;
    }

    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 使用 Haversine 公式计算两点之间的球面距离
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    @Override
    public Map<String, Object> routePlanning(double startLatitude, double startLongitude,
                                             double endLatitude, double endLongitude,
                                             String mode) {
        log.info("路线规划: start=({}, {}), end=({}, {}), mode={}",
                startLatitude, startLongitude, endLatitude, endLongitude, mode);

        Map<String, Object> result = new HashMap<>();

        if (!mapEnabled || apiKey.isEmpty()) {
            // 模拟实现：计算直线距离和时间
            double distance = calculateDistance(startLatitude, startLongitude,
                    endLatitude, endLongitude);

            // 根据出行方式估算时间
            int speed;
            switch (mode.toLowerCase()) {
                case "walking":
                    speed = 5;      // 步行 5 km/h
                    break;
                case "bicycling":
                    speed = 15;     // 骑行 15 km/h
                    break;
                case "driving":
                default:
                    speed = 40;     // 驾车 40 km/h（城市道路）
                    break;
            }

            int duration = (int) ((distance / 1000) / speed * 3600); // 秒

            result.put("distance", distance);
            result.put("distanceText", formatDistance(distance));
            result.put("duration", duration);
            result.put("durationText", formatDuration(duration));
            result.put("mode", mode);
            result.put("level", "模拟数据（直线距离）");
            log.warn("地图服务未启用，返回模拟数据");
        } else {
            // TODO: 对接高德/腾讯地图 API
            // 高德地图路径规划 API: https://restapi.amap.com/v3/direction/driving
            // 腾讯地图路径规划 API: https://apis.map.qq.com/ws/direction/v1/
            result.put("distance", 0);
            result.put("duration", 0);
            result.put("message", "待实现");
        }

        return result;
    }

    @Override
    public List<Map<String, Object>> searchNearby(double latitude, double longitude,
                                                  String keyword, int radius) {
        log.info("搜索附近: lat={}, lon={}, keyword={}, radius={}",
                latitude, longitude, keyword, radius);

        List<Map<String, Object>> results = new ArrayList<>();

        if (!mapEnabled || apiKey.isEmpty()) {
            // 模拟实现：返回一些模拟的 POI 数据
            for (int i = 1; i <= 5; i++) {
                Map<String, Object> poi = new HashMap<>();
                poi.put("name", keyword + " " + i);
                poi.put("latitude", latitude + (Math.random() - 0.5) * 0.01);
                poi.put("longitude", longitude + (Math.random() - 0.5) * 0.01);
                poi.put("address", "模拟地址 " + i);
                poi.put("distance", (int) (Math.random() * radius));
                results.add(poi);
            }
            log.warn("地图服务未启用，返回模拟数据");
        } else {
            // TODO: 对接高德/腾讯地图 API
            // 高德地图周边搜索 API: https://restapi.amap.com/v3/place/around
            // 腾讯地图周边搜索 API: https://apis.map.qq.com/ws/place/v1/search/
            log.warn("地图服务 API 对接待实现");
        }

        return results;
    }

    @Override
    public List<Map<String, Object>> getNearbyUsers(double latitude, double longitude,
                                                    int radius, int limit) {
        log.info("查询附近用户: lat={}, lon={}, radius={}, limit={}",
                latitude, longitude, radius, limit);

        try {
            // 查询所有有位置信息的用户地址
            LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNotNull(UserAddress::getLatitude)
                    .isNotNull(UserAddress::getLongitude)
                    .eq(UserAddress::getStatus, 0); // 只查询正常状态的地址

            List<UserAddress> addresses = userAddressMapper.selectList(wrapper);

            // 计算每个用户的距离
            List<Map<String, Object>> nearbyUsers = addresses.stream()
                    .map(address -> {
                        // 根据userId查询用户信息
                        User user = userMapper.selectById(address.getUserId());
                        if (user == null || user.getStatus() != 0) {
                            return null;
                        }

                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("userId", user.getUserId());
                        userInfo.put("username", user.getUsername());
                        userInfo.put("nickname", user.getNickname());
                        userInfo.put("avatar", user.getAvatar());

                        double distance = calculateDistance(
                                latitude, longitude,
                                address.getLatitude(), address.getLongitude()
                        );
                        userInfo.put("distance", distance);
                        userInfo.put("latitude", address.getLatitude());
                        userInfo.put("longitude", address.getLongitude());

                        return userInfo;
                    })
                    .filter(Objects::nonNull)
                    .filter(userInfo -> (Double) userInfo.get("distance") <= radius)
                    .sorted(Comparator.comparingDouble(userInfo -> (Double) userInfo.get("distance")))
                    .limit(limit)
                    .collect(Collectors.toList());

            log.info("找到{}个附近用户", nearbyUsers.size());
            return nearbyUsers;

        } catch (Exception e) {
            log.error("查询附近用户失败", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> batchCalculateDistance(double latitude, double longitude,
                                                             List<Map<String, Object>> locations) {
        log.info("批量计算距离: center=({}, {}), count={}",
                latitude, longitude, locations.size());

        return locations.stream()
                .map(location -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("userId", location.get("userId"));

                    Double lat = (Double) location.get("latitude");
                    Double lon = (Double) location.get("longitude");

                    if (lat != null && lon != null) {
                        double distance = calculateDistance(latitude, longitude, lat, lon);
                        result.put("distance", distance);
                    } else {
                        result.put("distance", Double.MAX_VALUE);
                    }

                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * 格式化距离
     */
    private String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format("%.0f米", meters);
        } else {
            return String.format("%.2f公里", meters / 1000);
        }
    }

    /**
     * 格式化时长
     */
    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            int minutes = seconds / 60;
            return minutes + "分钟";
        } else {
            int hours = seconds / 3600;
            int minutes = (seconds % 3600) / 60;
            return String.format("%d小时%d分钟", hours, minutes);
        }
    }
}
