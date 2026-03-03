# 限时秒杀功能 - 优化设计说明

## 一、设计理念

### 核心思想
**商品是主体，活动是场景**

- 商品可以独立存在，有自己的正常价格和库存
- 商品可以配置秒杀属性（秒杀价、秒杀库存），成为"可秒杀商品"
- 活动是组织"可秒杀商品"的时间窗口
- 通过不同入口（URL参数）区分展示正常渠道还是秒杀渠道

### 与原设计的区别

| 对比项 | 原设计 | 优化设计 |
|-------|--------|---------|
| 商品配置 | 商品表只有基础信息 | 商品表可直接配置秒杀价和库存 |
| 创建流程 | 必须先创建活动→添加商品 | 创建商品时即可设置秒杀属性 |
| 使用场景 | 只能在活动期间秒杀 | 商品可随时参与秒杀，活动是可选的 |
| 数据维护 | 活动结束需要删除关联 | 商品配置持久化，可重复参与活动 |
| 前端入口 | 秒杀入口 | 正常+秒杀双入口，URL区分 |

## 二、数据表关系

```
product (商品表)
├── is_flash_sale (是否参与秒杀)
├── flash_price (秒杀价格)
├── flash_sale_stock (秒杀库存)
└── flash_sale_sold (已售数量)
    ↓
flash_sale_product_relation (关联表) - 可选
├── activity_id (关联到活动)
├── session_id (关联到场次)
├── flash_price (可覆盖商品默认价格)
└── stock_count (可覆盖商品默认库存)
    ↓
flash_sale_activity (活动表)
└── flash_sale_session (场次表)
```

## 三、业务场景

### 场景1：简单秒杀（推荐新手使用）
```
卖家发布商品时：
1. 勾选"参与秒杀"
2. 设置秒杀价格：99元
3. 设置秒杀库存：50件
4. 设置每人限购：1件

→ 商品自动出现在"秒杀专区"
→ 前端通过 /pages/market/detail?id=123&flash=1 访问
```

### 场景2：活动秒杀（推荐运营使用）
```
运营创建活动：
1. 创建活动"周末大促"
2. 创建场次"10点场"、"14点场"
3. 从"可秒杀商品"中选择商品添加到场次
4. 可以为该活动设置专属价格（覆盖商品默认价格）

→ 活动页面展示该活动的商品
→ 倒计时以活动/场次时间为准
```

### 场景3：混合模式
```
- 商品默认配置了秒杀属性
- 可以随时被添加到任何活动中
- 活动结束后，商品仍保留秒杀属性
- 可以再次参与其他活动
```

## 四、API接口设计

### 1. 商品详情接口（根据渠道返回不同数据）

```java
/**
 * 获取商品详情
 * @param id 商品ID
 * @param channel 渠道：normal-正常渠道，flash-秒杀渠道
 */
@GetMapping("/product/{id}")
public Result<ProductDetailVO> getProductDetail(
    @PathVariable Long id,
    @RequestParam(defaultValue = "normal") String channel
) {
    if ("flash".equals(channel)) {
        // 返回秒杀信息
        return Result.success(flashSaleService.getProductDetailForFlash(id));
    } else {
        // 返回正常信息
        return Result.success(productService.getProductDetail(id));
    }
}
```

### 2. 获取可秒杀商品列表

```java
/**
 * 获取当前可秒杀商品列表（支持多种查询方式）
 */
@GetMapping("/flash-sale/products")
public Result<PageResult<FlashSaleProductVO>> getFlashSaleProducts(
    @RequestParam(required = false) Long sessionId,  // 场次ID（可选）
    @RequestParam(required = false) Long activityId, // 活动ID（可选）
    @RequestParam(defaultValue = "1") Integer page,
    @RequestParam(defaultValue = "10") Integer pageSize
) {
    // 优先返回场次商品，其次返回活动商品，最后返回所有可秒杀商品
    return Result.success(flashSaleService.getFlashSaleProducts(sessionId, activityId, page, pageSize));
}
```

### 3. 发布/编辑商品接口

```java
/**
 * 发布商品（支持设置秒杀属性）
 */
@PostMapping("/product")
public Result<Product> publishProduct(@RequestBody ProductPublishDTO dto) {
    // dto包含：
    // - 基础信息：标题、描述、正常价格、库存等
    // - 秒杀配置：isFlashSale、flashPrice、flashSaleStock、limitPerUser
    return Result.success(productService.publish(dto));
}
```

### 4. 秒杀下单接口

```java
/**
 * 秒杀抢购
 */
@PostMapping("/flash-sale/{productId}/buy")
public Result<Order> seckillBuy(
    @PathVariable Long productId,
    @RequestBody SeckillBuyDTO dto
) {
    // 1. 检查商品是否可秒杀
    // 2. 检查用户是否达到限购
    // 3. 扣减秒杀库存（Redis原子操作）
    // 4. 创建订单
    // 5. 记录秒杀订单
    return Result.success(flashSaleService.seckillBuy(productId, dto));
}
```

## 五、前端使用示例

### 1. 商品详情页根据渠道展示

```vue
<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isFlashChannel = route.query.flash === '1'
const productId = route.query.id

onMounted(async () => {
  const channel = isFlashChannel ? 'flash' : 'normal'
  const product = await api.getProductDetail(productId, { channel })

  if (isFlashChannel) {
    // 显示秒杀相关信息
    displayPrice = product.flashPrice
    displayStock = product.flashSaleStock - product.flashSaleSold
    showCountdown = true
    showSeckillButton = true
  } else {
    // 显示正常信息
    displayPrice = product.price
    displayStock = product.stock
    showCountdown = false
    showBuyButton = true
  }
})
</script>

<template>
  <view class="product-detail">
    <view class="price-section">
      <text v-if="isFlashChannel" class="flash-price">
        ¥{{ displayPrice }} <text class="flash-tag">秒杀价</text>
      </text>
      <text v-else class="normal-price">¥{{ displayPrice }}</text>
    </view>

    <!-- 秒杀专属UI -->
    <view v-if="isFlashChannel" class="flash-info">
      <view class="countdown">距离结束：02:30:45</view>
      <view class="stock-info">已抢 {{ soldPercent }}%</view>
      <progress-bar :percent="soldPercent" />
    </view>

    <button v-if="isFlashChannel" @click="seckillBuy">立即抢</button>
    <button v-else @click="buyNow">立即购买</button>
  </view>
</template>
```

### 2. 发布商品时选择参与秒杀

```vue
<template>
  <view class="publish-page">
    <!-- 基础信息 -->
    <input v-model="form.title" placeholder="商品标题" />
    <input v-model="form.price" type="number" placeholder="正常价格" />
    <input v-model="form.stock" type="number" placeholder="库存数量" />

    <!-- 秒杀配置 -->
    <view class="flash-sale-config">
      <checkbox v-model="form.isFlashSale">参与秒杀</checkbox>

      <view v-if="form.isFlashSale" class="flash-fields">
        <input v-model="form.flashPrice" type="number" placeholder="秒杀价格" />
        <input v-model="form.flashSaleStock" type="number" placeholder="秒杀库存" />
        <input v-model="form.limitPerUser" type="number" placeholder="每人限购数量" />
      </view>
    </view>

    <button @click="submit">发布商品</button>
  </view>
</template>
```

## 六、优势总结

### 对卖家
- ✅ 发布商品时即可设置秒杀，无需等待活动
- ✅ 商品配置持久化，可重复参与活动
- ✅ 支持灵活的价格和库存策略

### 对买家
- ✅ 正常渠道和秒杀渠道明确区分
- ✅ 秒杀渠道有专属UI（倒计时、进度条等）
- ✅ 避免误操作（秒杀商品有限购）

### 对运营
- ✅ 可以灵活创建各种活动
- ✅ 支持多场次管理
- ✅ 可以选择性地组织商品参与活动

### 对开发
- ✅ 数据模型清晰，易于维护
- ✅ 支持从简单到复杂的多种场景
- ✅ 向后兼容，不影响现有功能
