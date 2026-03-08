package com.xx.xianqijava.service;

import java.util.List;
import java.util.Map;

/**
 * 地图服务接口
 * 提供地理位置相关功能
 */
public interface MapService {

    /**
     * 地址解析（地址 -> 坐标）
     *
     * @param address 地址
     * @return 坐标信息 {latitude, longitude, formattedAddress}
     */
    Map<String, Object> geocode(String address);

    /**
     * 逆地址解析（坐标 -> 地址）
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @return 地址信息 {formattedAddress, province, city, district, street}
     */
    Map<String, Object> reverseGeocode(double latitude, double longitude);

    /**
     * 计算两点之间的距离
     *
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @return 距离（米）
     */
    double calculateDistance(double lat1, double lon1, double lat2, double lon2);

    /**
     * 路线规划
     *
     * @param startLatitude  起点纬度
     * @param startLongitude 起点经度
     * @param endLatitude    终点纬度
     * @param endLongitude   终点经度
     * @param mode           出行方式：driving（驾车）、walking（步行）、bicycling（骑行）
     * @return 路线信息 {distance, duration, steps}
     */
    Map<String, Object> routePlanning(double startLatitude, double startLongitude,
                                      double endLatitude, double endLongitude,
                                      String mode);

    /**
     * 搜索附近的关键词
     *
     * @param latitude  纬度
     * @param longitude 经度
     * @param keyword   关键词
     * @param radius    搜索半径（米）
     * @return 搜索结果列表
     */
    List<Map<String, Object>> searchNearby(double latitude, double longitude,
                                           String keyword, int radius);

    /**
     * 获取附近用户
     *
     * @param latitude  当前纬度
     * @param longitude 当前经度
     * @param radius    搜索半径（米）
     * @param limit     返回数量限制
     * @return 附近用户列表
     */
    List<Map<String, Object>> getNearbyUsers(double latitude, double longitude,
                                             int radius, int limit);

    /**
     * 批量计算距离
     *
     * @param latitude  当前纬度
     * @param longitude 当前经度
     * @param locations 位置列表 [{userId, latitude, longitude}]
     * @return 距离列表 [{userId, distance}]
     */
    List<Map<String, Object>> batchCalculateDistance(double latitude, double longitude,
                                                      List<Map<String, Object>> locations);
}
