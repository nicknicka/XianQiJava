# 限时秒杀 - 优化版API接口规范

## 一、API接口列表

| 序号 | 接口路径 | 方法 | 功能说明 | 优先级 |
|-----|---------|------|---------|--------|
| 1 | `/flash-sale/sessions` | GET | 获取秒杀场次列表 | P0 |
| 2 | `/flash-sale/products` | GET | 获取秒杀商品列表（分页） | P0 |
| 3 | `/flash-sale/current/products` | GET | 获取当前秒杀商品（首页用） | P0 |
| 4 | `/flash-sale/current/activity` | GET | 获取当前活动信息 | P0 |
| 5 | `/product/{id}` | GET | 获取商品详情（支持channel参数） | P0 |
| 6 | `/flash-sale/product/{id}` | GET | 获取秒杀商品详情 | P1 |
| 7 | `/flash-sale/{id}/check` | GET | 检查秒杀资格 | P1 |
| 8 | `/flash-sale/{id}/buy` | POST | 秒杀抢购 | P2 |

## 二、接口详细定义

### 1. 获取秒杀场次列表

```http
GET /api/flash-sale/sessions
```

**响应示例：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "sessionId": 1,
      "time": "08:00",
      "status": "ended",
      "progress": 100,
      "startTime": "2026-03-03T08:00:00",
      "endTime": "2026-03-03T10:00:00"
    },
    {
      "sessionId": 2,
      "time": "10:00",
      "status": "ongoing",
      "progress": 45,
      "startTime": "2026-03-03T10:00:00",
      "endTime": "2026-03-03T12:00:00"
    },
    {
      "sessionId": 3,
      "time": "12:00",
      "status": "upcoming",
      "progress": 0,
      "startTime": "2026-03-03T12:00:00",
      "endTime": "2026-03-03T14:00:00"
    }
  ]
}
```

### 2. 获取秒杀商品列表（分页）

```http
GET /api/flash-sale/products?sessionId=2&page=1&pageSize=10
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| sessionId | Long | 否 | 场次ID，为空则查询所有可秒杀商品 |
| activityId | Long | 否 | 活动ID，为空则查询所有活动 |
| page | Integer | 是 | 页码，从1开始 |
| pageSize | Integer | 是 | 每页数量 |

**响应示例：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "list": [
      {
        "id": 123,
        "title": "iPad Air 5 64G 蓝色",
        "description": "9成新 无划痕 配件齐全",
        "image": "https://xxx.jpg",
        "coverImage": "https://xxx.jpg",
        "images": ["https://xxx.jpg"],
        "seckillPrice": 2999,
        "originalPrice": 3599,
        "price": 3599,
        "discount": 8,
        "soldPercent": 75,
        "status": "ongoing",
        "endTime": "2026-03-03T12:00:00",
        "stock": 50,
        "soldCount": 150,
        "categoryId": 1,
        "categoryName": "数码产品",
        "condition": "九成新",
        "location": "南区",
        "userId": 10,
        "userName": "张三",
        "userAvatar": "https://avatar.jpg",
        "creditLevel": "优秀",
        "viewCount": 1000,
        "favoriteCount": 50
      }
    ],
    "total": 100,
    "endTime": "2026-03-03T12:00:00"
  }
}
```

### 3. 获取商品详情（支持渠道参数）

```http
GET /api/product/123?channel=flash
```

**请求参数：**
| 参数 | 类型 | 必填 | 说明 |
|-----|------|-----|------|
| channel | String | 否 | 渠道：normal（默认）或 flash |

**正常渠道响应（channel=normal）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 123,
    "title": "iPad Air 5 64G 蓝色",
    "description": "9成新 无划痕 配件齐全",
    "price": 3599,
    "stock": 10,
    "images": ["https://xxx.jpg"],
    // ... 其他正常商品信息
    "isFlashSale": true  // 提示该商品也参与秒杀
  }
}
```

**秒杀渠道响应（channel=flash）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 123,
    "title": "iPad Air 5 64G 蓝色",
    "description": "9成新 无划痕 配件齐全",
    "price": 3599,
    "originalPrice": 3599,
    "seckillPrice": 2999,
    "flashSaleStock": 50,
    "flashSaleSold": 150,
    "soldPercent": 75,
    "limitPerUser": 1,
    "images": ["https://xxx.jpg"],
    "endTime": "2026-03-03T12:00:00",
    // ... 其他商品信息
    "isFlashSale": true
  }
}
```

### 4. 发布商品（支持秒杀配置）

```http
POST /api/product
```

**请求体：**
```json
{
  "title": "iPad Air 5 64G 蓝色",
  "description": "9成新 无划痕 配件齐全",
  "price": 3599,
  "stock": 10,
  "categoryId": 1,
  "images": ["https://xxx.jpg"],
  "condition": 9,
  "location": "南区",
  "latitude": 23.123,
  "longitude": 113.123,
  // 秒杀配置
  "isFlashSale": true,
  "flashPrice": 2999,
  "flashSaleStock": 50,
  "limitPerUser": 1
}
```

### 5. 秒杀抢购

```http
POST /api/flash-sale/123/buy
```

**请求体：**
```json
{
  "quantity": 1,
  "addressId": 5,
  "remark": "请尽快发货"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "抢购成功",
  "data": {
    "orderId": 456,
    "totalAmount": 2999,
    "seckillPrice": 2999
  }
}
```

**错误响应：**
```json
{
  "code": 40001,
  "message": "库存不足"
}
```

```json
{
  "code": 40002,
  "message": "已达到限购数量"
}
```

### 6. 检查秒杀资格

```http
GET /api/flash-sale/123/check
```

**响应示例：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "canBuy": true,
    "remainingStock": 50,
    "userBuyLimit": 1,
    "userBoughtCount": 0
  }
}
```

**不能购买时：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "canBuy": false,
    "reason": "已达到限购数量",
    "remainingStock": 30,
    "userBuyLimit": 1,
    "userBoughtCount": 1
  }
}
```

## 三、VO类定义

### FlashSaleSessionVO.java
```java
@Data
@Schema(description = "秒杀场次VO")
public class FlashSaleSessionVO {
    @Schema(description = "场次ID")
    private Long sessionId;

    @Schema(description = "场次时间（HH:mm格式）")
    private String time;

    @Schema(description = "状态：upcoming-即将开始，ongoing-进行中，ended-已结束")
    private String status;

    @Schema(description = "进度百分比（0-100）")
    private Integer progress;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;
}
```

### FlashSaleProductVO.java（扩展版）
```java
@Data
@Schema(description = "秒杀商品VO")
public class FlashSaleProductVO {
    // 商品基础信息
    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "商品标题")
    private String title;

    @Schema(description = "商品描述")
    private String description;

    @Schema(description = "商品图片")
    private String image;

    @Schema(description = "商品封面图")
    private String coverImage;

    @Schema(description = "商品图片列表")
    private List<String> images;

    // 价格信息
    @Schema(description = "秒杀价格")
    private BigDecimal seckillPrice;

    @Schema(description = "原价")
    private BigDecimal originalPrice;

    @Schema(description = "正常价格")
    private BigDecimal price;

    @Schema(description = "折扣（如8表示8折）")
    private Integer discount;

    // 库存信息
    @Schema(description = "已抢百分比")
    private Integer soldPercent;

    @Schema(description = "秒杀库存")
    private Integer stock;

    @Schema(description = "已售数量")
    private Integer soldCount;

    // 状态信息
    @Schema(description = "状态：ongoing-进行中，upcoming-即将开始，ended-已结束")
    private String status;

    @Schema(description = "结束时间")
    private String endTime;

    // 商品详细信息
    @Schema(description = "分类ID")
    private Integer categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "成色")
    private String condition;

    @Schema(description = "位置")
    private String location;

    // 卖家信息
    @Schema(description = "卖家ID")
    private Long userId;

    @Schema(description = "卖家昵称")
    private String userName;

    @Schema(description = "卖家头像")
    private String userAvatar;

    @Schema(description = "信用等级")
    private String creditLevel;

    // 统计信息
    @Schema(description = "浏览次数")
    private Integer viewCount;

    @Schema(description = "收藏次数")
    private Integer favoriteCount;
}
```

### FlashSaleProductPageVO.java
```java
@Data
@Schema(description = "秒杀商品分页结果")
public class FlashSaleProductPageVO {
    @Schema(description = "商品列表")
    private List<FlashSaleProductVO> list;

    @Schema(description = "总数")
    private Integer total;

    @Schema(description = "当前场次结束时间")
    private String endTime;
}
```

### ProductDetailVO.java（扩展版）
```java
@Data
@Schema(description = "商品详情VO")
public class ProductDetailVO {
    // 基础字段
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;

    // 库存字段
    private Integer stock;  // 正常库存

    // 秒杀字段（仅在秒杀渠道返回）
    private Boolean isFlashSale;  // 是否参与秒杀
    private BigDecimal seckillPrice;  // 秒杀价格
    private Integer flashSaleStock;  // 秒杀库存
    private Integer flashSaleSold;  // 已售数量
    private Integer soldPercent;  // 已抢百分比
    private Integer limitPerUser;  // 每人限购
    private String endTime;  // 结束时间

    // ... 其他字段
}
```

## 四、错误码定义

| 错误码 | 说明 |
|-------|------|
| 40001 | 秒杀商品不存在 |
| 40002 | 秒杀活动未开始 |
| 40003 | 秒杀活动已结束 |
| 40004 | 库存不足 |
| 40005 | 已达到限购数量 |
| 40006 | 商品未参与秒杀 |
