# Xianqi 数据库创建脚本说明文档

## 📋 概述

**文件名**: `Xianqi_complete_database_schema.sql`
**项目**: 校园二手交易与共享平台
**作者**: 许佳宜 (2022035123021)
**指导教师**: 温清机
**生成时间**: 2026-03-12
**数据库**: MySQL 8.0+
**字符集**: utf8mb4

## 📊 数据库表结构

本脚本共创建 **49 张表**，分为以下几类：

### 1. User 相关表（10张）- 已重新设计

| 表名 | 说明 | 优化点 |
|------|------|--------|
| `user` | 核心用户表 | 消除字段重复，统一时间字段命名 |
| `user_profile` | 用户资料扩展表 | 分离展示资料，冗余统计字段 |
| `user_real_name_auth` | 实名认证表 | 支持第三方认证，新增身份证图片 |
| `user_student_auth` | 学生认证表 | 优化认证材料存储，添加唯一索引 |
| `user_credit_ext` | 信用扩展表 | 统一信用数据管理，支持第三方信用 |
| `login_device` | 登录设备表 | 新增登录地点，优化索引 |
| `user_preference` | 偏好设置表 | 新增隐私设置，使用JSON扩展 |
| `user_address` | 用户地址表 | 统一时间字段，优化精度 |
| `user_follow` | 关注关系表 | 支持逻辑删除，添加updated_at |
| `blacklist` | 黑名单表 | 支持逻辑删除，优化索引 |

**User相关表主要优化**:
- ✅ 消除字段重复（realName, studentId, college, major, creditScore等）
- ✅ 统一时间字段命名（created_at, updated_at）
- ✅ 分离核心数据和扩展数据
- ✅ 优化索引和外键约束

### 2. 管理员表（3张）

- `admin` - 管理员账号表
- `admin_login_log` - 管理员登录日志
- `admin_session` - 管理员会话管理

### 3. 商品与共享表（9张）

- `product` - 商品表
- `product_image` - 商品图片表
- `product_favorite` - 商品收藏表
- `product_view_history` - 浏览历史表
- `product_statistics` - 商品统计表
- `share_item` - 共享物品表
- `share_item_image` - 共享物品图片表
- `share_item_booking` - 共享物品预约表
- `category` - 分类表

### 4. 订单与交易表（4张）

- `order` - 订单表
- `evaluation` - 评价表
- `refund_record` - 退款记录表
- `transfer_record` - 转账记录表

### 5. 即时通讯表（4张）

- `conversation` - 会话表
- `conversation_member` - 会话成员表
- `message` - 消息表
- `quick_reply` - 快捷回复表

### 6. 营销活动表（7张）

- `banner` - 轮播图表
- `coupon` - 优惠券表
- `flash_sale_product` - 秒杀商品表
- `flash_sale_session` - 秒杀场次表
- `flash_sale_session_template` - 秒杀场次模板表
- `flash_sale_order_ext` - 秒杀订单扩展表
- `hot_tag` - 热门标签表

### 7. 系统配置表（11张）

- `system_config` - 系统配置表
- `system_notification` - 系统通知表
- `notification_read_records` - 通知阅读记录表
- `operation_log` - 操作日志表
- `error_log` - 错误日志表
- `export_log` - 导出日志表
- `report` - 举报记录表
- `user_feedback` - 用户反馈表
- `sensitive_word` - 敏感词表
- `statistics_cache` - 统计缓存表
- `campus_location` - 校园位置表

### 8. 其他表（1张）

- `worker_node` - Worker节点表（用于分布式ID生成）

## 🚀 使用方法

### 方式一：命令行执行

```bash
# 1. 创建数据库
mysql -u root -p -e "CREATE DATABASE Xianqi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. 执行脚本
mysql -u root -p Xianqi < Xianqi_complete_database_schema.sql
```

### 方式二：MySQL 客户端执行

```sql
-- 1. 创建数据库
CREATE DATABASE Xianqi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 选择数据库
USE Xianqi;

-- 3. 执行脚本
source /path/to/Xianqi_complete_database_schema.sql;
```

### 方式三：使用图形化工具

1. 打开 Navicat / MySQL Workbench / DBeaver 等工具
2. 连接到 MySQL 服务器
3. 创建新数据库 `Xianqi`，字符集选择 `utf8mb4`
4. 打开 SQL 文件 `Xianqi_complete_database_schema.sql`
5. 执行脚本

## ⚠️ 注意事项

1. **数据库版本**: 建议使用 MySQL 8.0 或更高版本
2. **字符集**: 务必使用 `utf8mb4` 字符集，支持存储 emoji 等 4 字节字符
3. **备份数据**: 如果数据库中已有数据，执行前请先备份
4. **权限要求**: 需要 CREATE、ALTER、INDEX 权限
5. **外键约束**: 脚本开始时禁用了外键检查，结束后会重新启用

## 🔍 验证安装

执行脚本后，可以使用以下 SQL 验证表是否创建成功：

```sql
-- 查看所有表
USE Xianqi;
SHOW TABLES;

-- 统计表数量
SELECT COUNT(*) AS table_count FROM information_schema.tables WHERE table_schema = 'Xianqi';

-- 应该显示 49 张表
```

## 📝 数据库设计说明

### 命名规范

- **表名**: 使用小写字母和下划线，如 `user_profile`
- **字段名**: 使用小写字母和下划线，如 `created_at`
- **主键**:
  - 主表使用 `{table}_id` 格式，如 `user_id`
  - 关联表使用 `id` 自增主键
- **时间字段**: 统一使用 `created_at` 和 `updated_at`
- **逻辑删除**: 使用 `deleted` 字段（0-正常，1-已删除）

### 字段类型

- **ID字段**: 使用 `BIGINT`，支持大规模数据
- **金额字段**: 使用 `DECIMAL(10,2)`，精确到分
- **状态字段**: 使用 `TINYINT`，节省存储空间
- **JSON字段**: 适当使用 `JSON` 类型存储扩展配置

### 索引设计

- 主键索引：自动创建
- 唯一索引：username, phone, student_id 等
- 普通索引：常用查询字段
- 联合索引：多条件查询优化
- 全文索引：商品搜索（title + description）

## 📚 相关文档

- [数据库设计文档](../docs/database-redesign/user-tables-redesign.md)
- [API 接口文档](../docs/API.md)
- [项目需求文档](../PRD-校园二手交易与共享平台.md)

## 🛠️ 维护说明

### 数据迁移

如果需要从旧版本数据库迁移到新结构，请参考：
- [用户表迁移脚本](./docs/database-redesign/create_user_tables.sql)
- [数据迁移说明](./docs/database-redesign/migration_guide.md)

### 备份建议

建议定期备份数据库：

```bash
# 备份整个数据库
mysqldump -u root -p Xianqi > Xianqi_backup_$(date +%Y%m%d_%H%M%S).sql

# 只备份表结构
mysqldump -u root -p --no-data Xianqi > Xianqi_schema_$(date +%Y%m%d_%H%M%S).sql
```

### 性能优化

1. 定期分析表：`ANALYZE TABLE table_name;`
2. 定期优化表：`OPTIMIZE TABLE table_name;`
3. 监控慢查询日志
4. 根据实际查询情况调整索引

## 📞 联系方式

如有问题，请联系：
- 作者：许佳宜
- 学号：2022035123021
- 指导教师：温清机

## 注意事项
实体类貌似没有完全适应新设计的数据表所以可能需要重新调整下

---

**最后更新**: 2026-03-12
**版本**: v1.0
