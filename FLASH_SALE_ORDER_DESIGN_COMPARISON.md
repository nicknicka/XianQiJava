# 秒杀订单存储方案对比

## 三种方案对比

### 方案1：修改订单表，增加字段

```sql
ALTER TABLE `order`
ADD COLUMN `order_type` TINYINT,
ADD COLUMN `activity_id` BIGINT,
ADD COLUMN `session_id` BIGINT,
ADD COLUMN `flash_price` DECIMAL(10,2);
```

**优点：**
- ✅ 查询简单，不需要JOIN
- ✅ 所有信息在一个表中

**缺点：**
- ❌ 修改现有表结构，可能影响现有业务
- ❌ 订单表字段越来越多，不够清晰
- ❌ 普通订单也会有无用的字段（NULL）

---

### 方案2：独立秒杀订单表（数据冗余）

```sql
CREATE TABLE `flash_sale_order` (
  `id` BIGINT,
  `product_id` BIGINT,
  `user_id` BIGINT,
  `order_id` BIGINT,
  `flash_price` DECIMAL(10,2),
  ...
);
```

**优点：**
- ✅ 不修改现有表
- ✅ 秒杀订单独立管理

**缺点：**
- ❌ 数据冗余（order_id 关联，但product_id、user_id重复）
- ❌ 需要同步两个表
- ❌ 数据一致性风险

---

### 方案3：扩展表 + 最小化修改原表（推荐）✅

```sql
-- 1. 订单表只增加类型字段
ALTER TABLE `order`
ADD COLUMN `order_type` TINYINT DEFAULT 0;

-- 2. 创建秒杀订单扩展表
CREATE TABLE `flash_sale_order` (
  `order_id` BIGINT PRIMARY KEY,
  `activity_id` BIGINT,
  `session_id` BIGINT,
  `flash_price` DECIMAL(10,2),
  `seckill_time` DATETIME,
  ...
);
```

**优点：**
- ✅ 最小化修改现有表（只增加一个字段）
- ✅ 秒杀特有信息独立管理
- ✅ 通过 order_id 唯一关联，无数据冗余
- ✅ 符合数据库设计的开闭原则
- ✅ 扩展性强，未来可以有其他订单类型的扩展表

**缺点：**
- ⚠️ 查询时需要JOIN（但可以通过视图或缓存优化）

---

## 详细对比表

| 对比项 | 方案1：修改表 | 方案2：独立表 | 方案3：扩展表 |
|-------|------------|------------|------------|
| **修改现有表** | 是（增加4个字段） | 否 | 是（增加1个字段） |
| **数据冗余** | 无 | 高 | 无 |
| **查询复杂度** | 低（单表查询） | 中（需JOIN或单独查询） | 中（需JOIN） |
| **维护成本** | 低 | 高（需同步两个表） | 低 |
| **扩展性** | 中（字段越来越多） | 差（数据分散） | 好（职责清晰） |
| **数据一致性** | 好 | 差 | 好 |
| **对现有业务影响** | 中 | 小 | 小 |

---

## 实际使用对比

### 场景1：查询订单详情

```sql
-- 方案1：单表查询
SELECT * FROM `order` WHERE order_id = 123;

-- 方案2：需要查两个表
SELECT * FROM `order` WHERE order_id = 123;
SELECT * FROM flash_sale_order WHERE order_id = 123;

-- 方案3：连接查询（或创建视图）
SELECT o.*, f.activity_id, f.flash_price
FROM `order` o
LEFT JOIN flash_sale_order f ON o.order_id = f.order_id
WHERE o.order_id = 123;
```

**优化：方案3可以创建视图**
```sql
CREATE VIEW v_order_detail AS
SELECT o.*,
       f.activity_id, f.session_id, f.flash_price, f.seckill_time
FROM `order` o
LEFT JOIN flash_sale_order f ON o.order_id = f.order_id;

-- 使用视图查询（像单表一样简单）
SELECT * FROM v_order_detail WHERE order_id = 123;
```

---

### 场景2：检查用户限购

```sql
-- 方案1：
SELECT COUNT(*) FROM `order`
WHERE user_id = 123
  AND activity_id = 1
  AND order_type = 1
  AND order_status != 4;  -- 排除已取消

-- 方案2：
SELECT COUNT(*) FROM flash_sale_order
WHERE user_id = 123 AND activity_id = 1;

-- 方案3：⭐ 性能最好（独立索引）
SELECT COUNT(*) FROM flash_sale_order
WHERE user_id = 123 AND activity_id = 1;
```

---

### 场景3：统计活动数据

```sql
-- 方案1：
SELECT COUNT(*) as total, SUM(flash_price) as amount
FROM `order`
WHERE activity_id = 1 AND order_type = 1;

-- 方案2：
SELECT COUNT(*) as total, SUM(flash_price) as amount
FROM flash_sale_order
WHERE activity_id = 1;

-- 方案3：⭐ 性能最好（独立索引）
SELECT COUNT(*) as total, SUM(flash_price) as amount
FROM flash_sale_order
WHERE activity_id = 1;
```

---

## 性能对比

假设有100万条订单，其中10万条是秒杀订单：

| 操作 | 方案1 | 方案2 | 方案3 |
|-----|------|------|------|
| 查询秒杀订单列表 | 扫描100万行 | 直接查询10万行 | JOIN后查询10万行 |
| 检查用户限购 | 扫描全表+过滤 | 独立索引查询 | 独立索引查询 ⭐ |
| 统计活动数据 | 扫描全表+过滤 | 独立索引查询 | 独立索引查询 ⭐ |

**结论：** 方案3在频繁的秒杀相关查询中性能最好。

---

## 数据一致性

### 方案2的风险

```java
// 创建订单时需要同时操作两个表
@Transactional
public Order createSeckillOrder(...) {
    // 1. 插入订单表
    Order order = orderMapper.insert(order);

    // 2. 插入秒杀订单表
    FlashSaleOrder flashOrder = new FlashSaleOrder();
    flashOrder.setOrderId(order.getOrderId());
    flashOrder.setUserId(order.getUserId());
    flashOrder.setProductId(order.getProductId());  // 数据冗余！
    flashSaleOrderMapper.insert(flashOrder);

    return order;
}

// 风险：如果第二步失败，数据不一致
// 风险：更新用户ID时需要同步两个表
```

### 方案3的优势

```java
// 创建订单时
@Transactional
public Order createSeckillOrder(...) {
    // 1. 设置订单类型
    order.setOrderType(1);  // 秒杀订单
    orderMapper.insert(order);

    // 2. 只插入秒杀特有信息
    FlashSaleOrderExt ext = new FlashSaleOrderExt();
    ext.setOrderId(order.getOrderId());  // 唯一关联
    ext.setActivityId(activityId);
    ext.setFlashPrice(flashPrice);
    flashSaleOrderMapper.insert(ext);

    return order;
}

// 优势：职责清晰，无数据冗余
// 优势：更新用户信息不需要同步秒杀表
```

---

## 扩展性示例

未来可能有其他订单类型，方案3可以轻松扩展：

```sql
-- 订单表只有一个类型字段
ALTER TABLE `order` ADD COLUMN `order_type` TINYINT DEFAULT 0;
-- 0: 普通订单
-- 1: 秒杀订单
-- 2: 共享订单
-- 3: 拍卖订单
-- 4: 团购订单
-- ...

-- 每种类型可以有独立的扩展表
CREATE TABLE `flash_sale_order` (
  `order_id` BIGINT PRIMARY KEY,
  `activity_id` BIGINT,
  `flash_price` DECIMAL(10,2),
  ...
);

CREATE TABLE `auction_order` (
  `order_id` BIGINT PRIMARY KEY,
  `auction_id` BIGINT,
  `bid_price` DECIMAL(10,2),
  ...
);

CREATE TABLE `group_buy_order` (
  `order_id` BIGINT PRIMARY KEY,
  `group_id` BIGINT,
  `group_price` DECIMAL(10,2),
  ...
);
```

---

## 推荐方案

**✅ 方案3：扩展表 + 最小化修改原表**

**理由：**
1. 最小化对现有系统的影响
2. 职责清晰，易于维护
3. 性能优秀（独立索引）
4. 扩展性强（支持多种订单类型）
5. 无数据冗余
6. 数据一致性好

**适用场景：**
- ✅ 现有系统已经运行，不想大改
- ✅ 秒杀业务逻辑复杂，特有字段多
- ✅ 未来可能支持其他订单类型
- ✅ 需要频繁查询秒杀相关数据（限购检查、统计等）
