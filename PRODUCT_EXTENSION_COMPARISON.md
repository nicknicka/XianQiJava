# 商品扩展设计对比：修改原表 vs 扩展表

## 两种方案对比

### 方案A：修改商品表（之前的设计）

```sql
-- 直接在商品表增加字段
ALTER TABLE `product`
ADD COLUMN `is_flash_sale` TINYINT DEFAULT 0,
ADD COLUMN `flash_price` DECIMAL(10,2),
ADD COLUMN `flash_sale_stock` INT DEFAULT 0,
ADD COLUMN `flash_sale_sold` INT DEFAULT 0,
ADD COLUMN `limit_per_user` INT DEFAULT 1;
```

**优点：**
- ✅ 查询简单，单表查询
- ✅ 不需要JOIN

**缺点：**
- ❌ 修改现有表结构
- ❌ 普通商品也会有NULL字段
- ❌ 字段越来越多，表结构臃肿
- ❌ 如果秒杀业务逻辑变化，需要再次修改表

---

### 方案B：扩展表（最终方案）✅

```sql
-- 商品表保持不变
-- 创建秒杀商品扩展表
CREATE TABLE `flash_sale_product_ext` (
  `product_id` BIGINT PRIMARY KEY,
  `flash_price` DECIMAL(10,2),
  `stock_count` INT,
  `sold_count` INT,
  `limit_per_user` INT,
  ...
);
```

**优点：**
- ✅ **不修改原表** - 商品表结构保持不变
- ✅ **职责分离** - 秒杀配置独立管理
- ✅ **扩展性强** - 可以有其他扩展表（拍卖、团购等）
- ✅ **性能优秀** - 扩展表有独立索引
- ✅ **易于维护** - 删除扩展表不影响原表
- ✅ **支持多版本** - 同一商品可以有多个秒杀配置（不同时间段）

**缺点：**
- ⚠️ 查询时需要JOIN（但可以创建视图优化）

---

## 详细对比

| 对比项 | 方案A：修改表 | 方案B：扩展表 |
|-------|-----------|-----------|
| **修改现有表** | 是（5个字段） | 否 |
| **表结构清晰度** | 混合（基础+扩展字段） | 清晰（职责分离） |
| **NULL字段** | 普通商品有4个NULL | 无 |
| **查询复杂度** | 低（单表） | 中（需JOIN或视图） |
| **扩展性** | 差（字段越来越多） | 好（独立扩展表） |
| **维护成本** | 中 | 低 |
| **业务隔离** | 差 | 好 |
| **影响范围** | 可能影响现有业务 | 不影响现有业务 |

---

## 实际使用对比

### 场景1：查询商品列表

```sql
-- 方案A：单表查询（简单）
SELECT * FROM product WHERE status = 1;

-- 方案B：需要JOIN（稍微复杂）
SELECT p.* FROM product p
LEFT JOIN flash_sale_product_ext f ON p.product_id = f.product_id
WHERE p.status = 1;
```

**但实际场景中，我们通常需要分场景查询：**

```sql
-- 查询普通商品（不需要秒杀信息）
SELECT * FROM product WHERE status = 1;

-- 查询秒杀商品（需要秒杀信息）
SELECT p.*, f.flash_price, f.stock_count
FROM product p
INNER JOIN flash_sale_product_ext f ON p.product_id = f.product_id
WHERE p.status = 1 AND f.status = 1;
```

→ 方案B实际使用中更清晰！

---

### 场景2：发布/编辑商品

```java
// 方案A：所有字段在一起
Product product = new Product();
product.setTitle("iPad");
product.setPrice(3599);
product.setFlashPrice(2999);  // 秒杀相关
product.setFlashSaleStock(50);
productMapper.insert(product);

// 方案B：分离存储
@Transactional
public void publishProductWithFlash(ProductDTO dto) {
    // 1. 保存商品基本信息
    Product product = new Product();
    product.setTitle("iPad");
    product.setPrice(3599);
    productMapper.insert(product);

    // 2. 如果配置了秒杀，保存秒杀配置
    if (dto.getIsFlashSale()) {
        FlashSaleProductExt ext = new FlashSaleProductExt();
        ext.setProductId(product.getProductId());
        ext.setFlashPrice(2999);
        ext.setStockCount(50);
        flashSaleExtMapper.insert(ext);
    }
}
```

→ 方案B职责更清晰！

---

### 场景3：支持多轮秒杀

假设同一个商品要参与多次秒杀活动：

**方案A的问题：**
```sql
-- 只能存储一个秒杀配置
UPDATE product SET flash_price = 2999, start_time = '2026-03-03 10:00:00';
-- 下次活动又需要更新，覆盖了之前的配置
UPDATE product SET flash_price = 2799, start_time = '2026-03-04 14:00:00';
-- 无法保留历史配置
```

**方案B的优势：**
```sql
-- 可以为同一商品创建多条秒杀配置
INSERT INTO flash_sale_product_ext (product_id, flash_price, start_time, end_time)
VALUES (123, 2999, '2026-03-03 10:00:00', '2026-03-03 12:00:00');

INSERT INTO flash_sale_product_ext (product_id, flash_price, start_time, end_time)
VALUES (123, 2799, '2026-03-04 14:00:00', '2026-03-04 16:00:00');

-- 查询某个时间段的配置
SELECT * FROM flash_sale_product_ext
WHERE product_id = 123
  AND start_time <= '2026-03-03 10:30:00'
  AND end_time >= '2026-03-03 10:30:00';
```

→ 方案B支持多轮秒杀！

---

## 视图优化（简化查询）

为了解决JOIN查询的复杂度，可以创建视图：

```sql
-- 秒杀商品详情视图
CREATE VIEW v_flash_sale_product AS
SELECT
    p.*,
    f.flash_price,
    f.stock_count,
    f.sold_count
FROM product p
INNER JOIN flash_sale_product_ext f ON p.product_id = f.product_id;

-- 使用视图查询（像单表一样简单）
SELECT * FROM v_flash_sale_product WHERE status = 1;
```

---

## 最终推荐方案

### ✅ 全扩展表方案

```
product 表（保持不变）
├── 商品基础信息
└── 不关心业务类型

flash_sale_product_ext 表（扩展表）
├── 秒杀价格配置
├── 库存配置
└── 只存储秒杀商品

auction_product_ext 表（未来扩展）
├── 拍卖价格配置
├── 起拍价
└── 只存储拍卖商品
```

### 为什么选择扩展表？

1. **符合数据库设计原则**
   - 单一职责原则：一个表只负责一类数据
   - 开闭原则：对扩展开放，对修改关闭

2. **不破坏现有系统**
   - 商品表结构不变，不影响现有业务
   - 降低风险

3. **支持业务演进**
   - 秒杀业务逻辑变化只影响扩展表
   - 可以轻松支持其他业务类型

4. **性能优化**
   - 扩展表有独立索引，查询更快
   - 可以分别缓存不同数据

5. **易于维护**
   - 删除扩展表不影响商品表
   - 数据清理更简单

---

## 扩展示例：支持多种业务类型

使用扩展表设计，可以轻松支持多种业务：

```sql
-- 秒杀商品扩展表
CREATE TABLE flash_sale_product_ext (...);

-- 拍卖商品扩展表
CREATE TABLE auction_product_ext (
  product_id BIGINT PRIMARY KEY,
  start_price DECIMAL(10,2),  -- 起拍价
  current_price DECIMAL(10,2), -- 当前价
  auction_end_time DATETIME,   -- 拍卖结束时间
  ...
);

-- 团购商品扩展表
CREATE TABLE group_buy_product_ext (
  product_id BIGINT PRIMARY KEY,
  group_price DECIMAL(10,2),   -- 团购价
  min_people INT,               -- 成团人数
  current_people INT,           -- 当前参团人数
  ...
);

-- 租赁商品扩展表
CREATE TABLE rental_product_ext (
  product_id BIGINT PRIMARY KEY,
  daily_price DECIMAL(10,2),   -- 日租金
  deposit_price DECIMAL(10,2),  -- 押金
  ...
);

-- 订单表只需要一个类型字段区分
ALTER TABLE `order` ADD COLUMN `order_type` TINYINT;
-- 0-普通, 1-秒杀, 2-拍卖, 3-团购, 4-租赁
```

这样设计，系统扩展性极强！
