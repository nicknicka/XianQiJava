# 订单测试数据说明文档

## 📊 数据概览

已成功插入 **9条** 完整的订单测试数据，包含所有状态和类型：

| 状态 | 数量 | 订单ID |
|------|------|--------|
| 待确认 | 3 | 200, 201, 202 |
| 进行中 | 2 | 203, 204 |
| 已完成 | 2 | 205, 206 |
| 已取消 | 1 | 207 |
| 退款中 | 1 | 208 |

## 🔍 订单详情

### 待确认订单 (status=0)
- **200** - ORD20260310001 - test123购买???的MacBook Pro - ¥4500（普通订单）
- **201** - ORD20260310002 - 小明同学购买test123的商品 - ¥50（普通订单）
- **202** - ORD20260310003 - test123购买小明同学的MacBook Pro - ¥4200（秒杀订单）

### 进行中订单 (status=1)
- **203** - ORD20260310004 - test123购买???的MacBook Pro - ¥4500（已支付）
- **204** - ORD20260310005 - 小明同学购买test123的商品 - ¥50（已支付）

### 已完成订单 (status=2)
- **205** - ORD20260310006 - test123购买???的MacBook Pro - ¥4500（交易完成）
- **206** - ORD20260310007 - 小明同学购买test123的商品 - ¥50（面交完成）

### 已取消订单 (status=3)
- **207** - ORD20260310008 - test123购买???的MacBook Pro - ¥4500（买家取消）

### 退款中订单 (status=4)
- **208** - ORD20260310009 - test123购买???的MacBook Pro - ¥4500（商品与描述不符）

## 🛠️ 使用方法

### 查看所有订单
```bash
mysql -uroot -p123456 Xianqi < query_order_test_data.sql
```

### 快速查询
```sql
-- 查看订单统计
SELECT
    COUNT(*) as total,
    SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as pending,
    SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as processing,
    SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as completed,
    SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END) as cancelled,
    SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END) as refunding
FROM `order` WHERE order_id >= 200;

-- 查看订单列表
SELECT o.order_id, o.order_no,
    CASE o.status WHEN 0 THEN '待确认' WHEN 1 THEN '进行中'
        WHEN 2 THEN '已完成' WHEN 3 THEN '已取消' WHEN 4 THEN '退款中' END as status_name,
    o.amount, u1.nickname as buyer, u2.nickname as seller,
    p.title as product
FROM `order` o
LEFT JOIN user u1 ON o.buyer_id = u1.user_id
LEFT JOIN user u2 ON o.seller_id = u2.user_id
LEFT JOIN product p ON o.product_id = p.product_id
WHERE o.order_id >= 200
ORDER BY o.order_id;
```

## 📱 前端测试

### 测试账号
- **test123** (user_id=9) - 买家，多条订单
- **小明同学** (user_id=18) - 买家，少量订单

### 测试场景
1. **订单列表页** - 查看不同状态订单的展示
2. **订单详情页** - 查看订单ID 200-208的详情
3. **订单操作** - 确认、取消、完成订单
4. **订单统计** - 查看订单数量和金额统计

## 🔧 后端API测试

### 订单相关接口
```bash
# 获取订单列表（买家视角）
curl -X GET "http://localhost:8080/api/order?role=0&page=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 获取订单列表（卖家视角）
curl -X GET "http://localhost:8080/api/order?role=1&page=1&pageSize=10" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 获取订单详情
curl -X GET "http://localhost:8080/api/order/200" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 确认订单（卖家）
curl -X PUT "http://localhost:8080/api/order/200/confirm" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 取消订单（买家）
curl -X PUT "http://localhost:8080/api/order/200/cancel" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "不想要了"}'

# 完成订单（买家）
curl -X PUT "http://localhost:8080/api/order/203/complete" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 📝 注意事项

1. **订单ID范围** - 测试数据从200开始，避免与已有数据冲突
2. **用户关联** - 所有订单都关联到真实用户（test123、小明同学等）
3. **商品关联** - 订单关联到数据库中的真实商品
4. **时间逻辑** - 订单创建时间按逻辑顺序排列，便于测试

## 🗑️ 清理测试数据

如需清理测试数据：
```sql
DELETE FROM `order` WHERE order_id >= 200;
```

## 📚 相关文件

- `insert_complete_order_test_data.sql` - 插入测试数据的SQL脚本
- `query_order_test_data.sql` - 查询测试数据的SQL脚本
- `ORDER_TEST_DATA_README.md` - 本说明文档

---
*生成时间：2026-03-09*
*数据库：Xianqi*
*表名：order*
