package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xx.xianqijava.common.ErrorCode;
import com.xx.xianqijava.entity.User;
import com.xx.xianqijava.entity.UserAddress;
import com.xx.xianqijava.exception.BusinessException;
import com.xx.xianqijava.mapper.UserAddressMapper;
import com.xx.xianqijava.mapper.UserMapper;
import com.xx.xianqijava.service.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 地图服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapServiceImpl implements MapService {

    private static final double EARTH_RADIUS = 6371000;

    private final UserMapper userMapper;
    private final UserAddressMapper userAddressMapper;
    private final ObjectMapper objectMapper;

    @Value("${map.enabled:false}")
    private boolean mapEnabled;

    @Value("${map.provider:amap}")
    private String mapProvider;

    @Value("${map.api-key:}")
    private String apiKey;

    @Value("${map.base-url:https://restapi.amap.com}")
    private String baseUrl;

    @Value("${map.timeout-ms:5000}")
    private long timeoutMs;

    @Value("${map.nearby-radius:1000}")
    private int defaultNearbyRadius;

    @Override
    public Map<String, Object> geocode(String address) {
        log.info("地址解析: address={}", address);
        assertAmapConfigured();

        JsonNode root = executeAmapRequest("/v3/geocode/geo", Map.of(
                "address", address,
                "output", "JSON"
        ));
        JsonNode geocodes = root.path("geocodes");
        if (!geocodes.isArray() || geocodes.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应地址坐标");
        }

        JsonNode first = geocodes.get(0);
        double[] location = parseLocation(first.path("location").asText());

        Map<String, Object> result = new HashMap<>();
        result.put("latitude", location[1]);
        result.put("longitude", location[0]);
        result.put("formattedAddress", textOrNull(first, "formatted_address", address));
        result.put("province", textOrNull(first, "province", null));
        result.put("city", extractCity(first.path("city")));
        result.put("district", textOrNull(first, "district", null));
        return result;
    }

    @Override
    public Map<String, Object> reverseGeocode(double latitude, double longitude) {
        log.info("逆地址解析: lat={}, lon={}", latitude, longitude);
        assertAmapConfigured();

        JsonNode root = executeAmapRequest("/v3/geocode/regeo", Map.of(
                "location", longitude + "," + latitude,
                "extensions", "base",
                "radius", "1000",
                "output", "JSON"
        ));
        JsonNode regeocode = root.path("regeocode");
        if (regeocode.isMissingNode() || regeocode.isNull()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到对应地址信息");
        }

        JsonNode addressComponent = regeocode.path("addressComponent");
        Map<String, Object> result = new HashMap<>();
        result.put("formattedAddress", textOrNull(regeocode, "formatted_address", null));
        result.put("province", textOrNull(addressComponent, "province", null));
        result.put("city", extractCity(addressComponent.path("city")));
        result.put("district", textOrNull(addressComponent, "district", null));
        result.put("street", textOrNull(addressComponent.path("streetNumber"), "street", null));
        result.put("township", textOrNull(addressComponent, "township", null));
        return result;
    }

    @Override
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    @Override
    public Map<String, Object> routePlanning(double startLatitude, double startLongitude,
                                             double endLatitude, double endLongitude,
                                             String mode) {
        log.info("路线规划: start=({}, {}), end=({}, {}), mode={}",
                startLatitude, startLongitude, endLatitude, endLongitude, mode);
        assertAmapConfigured();

        String normalizedMode = normalizeRouteMode(mode);
        JsonNode root;
        if ("bicycling".equals(normalizedMode)) {
            root = executeAmapRequest("/v4/direction/bicycling", Map.of(
                    "origin", startLongitude + "," + startLatitude,
                    "destination", endLongitude + "," + endLatitude
            ));
        } else {
            root = executeAmapRequest("/v3/direction/" + normalizedMode, Map.of(
                    "origin", startLongitude + "," + startLatitude,
                    "destination", endLongitude + "," + endLatitude,
                    "output", "JSON"
            ));
        }

        JsonNode path = findFirstRoutePath(root);
        if (path == null || path.isMissingNode() || path.isNull()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "未找到可用路线");
        }

        double distance = parseDouble(path.path("distance").asText("0"));
        int duration = (int) Math.round(parseDouble(path.path("duration").asText("0")));

        Map<String, Object> result = new HashMap<>();
        result.put("distance", distance);
        result.put("distanceText", formatDistance(distance));
        result.put("duration", duration);
        result.put("durationText", formatDuration(duration));
        result.put("mode", normalizedMode);
        result.put("steps", extractSteps(path.path("steps")));
        result.put("polyline", buildPolyline(path.path("steps")));
        return result;
    }

    @Override
    public List<Map<String, Object>> searchNearby(double latitude, double longitude,
                                                  String keyword, int radius) {
        log.info("搜索附近: lat={}, lon={}, keyword={}, radius={}",
                latitude, longitude, keyword, radius);
        assertAmapConfigured();

        int actualRadius = radius > 0 ? radius : defaultNearbyRadius;
        JsonNode root = executeAmapRequest("/v3/place/around", Map.of(
                "location", longitude + "," + latitude,
                "keywords", keyword,
                "radius", String.valueOf(actualRadius),
                "sortrule", "distance",
                "offset", "20",
                "page", "1",
                "extensions", "base",
                "output", "JSON"
        ));

        JsonNode pois = root.path("pois");
        List<Map<String, Object>> results = new ArrayList<>();
        if (!pois.isArray()) {
            return results;
        }

        for (JsonNode poi : pois) {
            String location = poi.path("location").asText();
            if (location == null || location.isBlank()) {
                continue;
            }

            double[] parsedLocation = parseLocation(location);
            Map<String, Object> item = new HashMap<>();
            item.put("id", textOrNull(poi, "id", null));
            item.put("name", textOrNull(poi, "name", null));
            item.put("latitude", parsedLocation[1]);
            item.put("longitude", parsedLocation[0]);
            item.put("address", textOrNull(poi, "address", null));
            item.put("distance", parseInt(poi.path("distance").asText("0")));
            item.put("type", textOrNull(poi, "type", null));
            results.add(item);
        }
        return results;
    }

    @Override
    public List<Map<String, Object>> getNearbyUsers(double latitude, double longitude,
                                                    int radius, int limit) {
        log.info("查询附近用户: lat={}, lon={}, radius={}, limit={}",
                latitude, longitude, radius, limit);

        try {
            LambdaQueryWrapper<UserAddress> wrapper = new LambdaQueryWrapper<>();
            wrapper.isNotNull(UserAddress::getLatitude)
                    .isNotNull(UserAddress::getLongitude)
                    .eq(UserAddress::getStatus, 0);

            List<UserAddress> addresses = userAddressMapper.selectList(wrapper);
            List<Map<String, Object>> nearbyUsers = addresses.stream()
                    .map(address -> {
                        User user = userMapper.selectById(address.getUserId());
                        if (user == null || !Objects.equals(user.getStatus(), 0)) {
                            return null;
                        }

                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("userId", user.getUserId());
                        userInfo.put("username", user.getUsername());
                        userInfo.put("nickname", user.getNickname());
                        userInfo.put("avatar", user.getAvatar());

                        double distance = calculateDistance(
                                latitude,
                                longitude,
                                address.getLatitude(),
                                address.getLongitude()
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

    private JsonNode executeAmapRequest(String path, Map<String, String> params) {
        HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(baseUrl + path)).newBuilder()
                .addQueryParameter("key", apiKey);
        params.forEach(urlBuilder::addQueryParameter);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(urlBuilder.build())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new BusinessException(ErrorCode.REQUEST_TIMEOUT, "地图服务请求失败");
            }

            JsonNode root = objectMapper.readTree(response.body().string());
            validateAmapResponse(root);
            return root;
        } catch (IOException e) {
            log.error("调用地图服务失败: path={}, params={}", path, params, e);
            throw new BusinessException(ErrorCode.REQUEST_TIMEOUT, "地图服务调用失败");
        }
    }

    private void validateAmapResponse(JsonNode root) {
        if (root == null || root.isNull()) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "地图服务返回空响应");
        }

        if (root.has("status") && !"1".equals(root.path("status").asText())) {
            throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    textOrNull(root, "info", "地图服务调用失败")
            );
        }

        if (root.has("errcode") && root.path("errcode").asInt() != 0) {
            throw new BusinessException(
                    ErrorCode.BAD_REQUEST,
                    textOrNull(root, "errmsg", "地图服务调用失败")
            );
        }
    }

    private void assertAmapConfigured() {
        if (!mapEnabled) {
            throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "地图服务未启用");
        }
        if (!"amap".equalsIgnoreCase(mapProvider)) {
            throw new BusinessException(ErrorCode.NOT_IMPLEMENTED, "当前仅支持高德地图服务");
        }
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "地图服务配置不完整");
        }
    }

    private JsonNode findFirstRoutePath(JsonNode root) {
        JsonNode routePaths = root.path("route").path("paths");
        if (routePaths.isArray() && !routePaths.isEmpty()) {
            return routePaths.get(0);
        }

        JsonNode dataPaths = root.path("data").path("paths");
        if (dataPaths.isArray() && !dataPaths.isEmpty()) {
            return dataPaths.get(0);
        }
        return null;
    }

    private List<Map<String, Object>> extractSteps(JsonNode stepsNode) {
        List<Map<String, Object>> steps = new ArrayList<>();
        if (!stepsNode.isArray()) {
            return steps;
        }

        for (JsonNode step : stepsNode) {
            Map<String, Object> item = new HashMap<>();
            item.put("instruction", firstNonBlank(
                    step.path("instruction").asText(null),
                    step.path("action").asText(null),
                    step.path("assistant_action").asText(null)
            ));
            item.put("road", textOrNull(step, "road", null));
            item.put("distance", parseDouble(step.path("distance").asText("0")));
            item.put("duration", (int) Math.round(parseDouble(step.path("duration").asText("0"))));
            item.put("polyline", textOrNull(step, "polyline", null));
            steps.add(item);
        }
        return steps;
    }

    private String buildPolyline(JsonNode stepsNode) {
        if (!stepsNode.isArray() || stepsNode.isEmpty()) {
            return null;
        }

        StringBuilder polyline = new StringBuilder();
        for (JsonNode step : stepsNode) {
            String stepPolyline = step.path("polyline").asText("");
            if (stepPolyline.isBlank()) {
                continue;
            }
            if (!polyline.isEmpty()) {
                polyline.append(';');
            }
            polyline.append(stepPolyline);
        }
        return polyline.isEmpty() ? null : polyline.toString();
    }

    private double[] parseLocation(String location) {
        String[] parts = location.split(",");
        if (parts.length != 2) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "地图服务返回的坐标格式无效");
        }
        return new double[]{parseDouble(parts[0]), parseDouble(parts[1])};
    }

    private String extractCity(JsonNode cityNode) {
        if (cityNode == null || cityNode.isMissingNode() || cityNode.isNull()) {
            return null;
        }
        if (cityNode.isArray()) {
            return cityNode.isEmpty() ? null : cityNode.get(0).asText(null);
        }
        String city = cityNode.asText(null);
        return city != null && !city.isBlank() ? city : null;
    }

    private String textOrNull(JsonNode node, String field, String defaultValue) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return defaultValue;
        }
        JsonNode fieldNode = node.path(field);
        if (fieldNode.isMissingNode() || fieldNode.isNull()) {
            return defaultValue;
        }
        String text = fieldNode.asText();
        return text == null || text.isBlank() ? defaultValue : text;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0D;
        }
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String normalizeRouteMode(String mode) {
        if (mode == null || mode.isBlank()) {
            return "walking";
        }
        return switch (mode.toLowerCase()) {
            case "driving", "walking", "bicycling" -> mode.toLowerCase();
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST, "不支持的路线规划模式");
        };
    }

    private String formatDistance(double meters) {
        if (meters < 1000) {
            return String.format("%.0f米", meters);
        }
        return String.format("%.2f公里", meters / 1000);
    }

    private String formatDuration(int seconds) {
        if (seconds < 60) {
            return seconds + "秒";
        }
        if (seconds < 3600) {
            return (seconds / 60) + "分钟";
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        return String.format("%d小时%d分钟", hours, minutes);
    }
}
