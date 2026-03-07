# 校园二手交易平台 - ID生成策略分析文档

> 项目：XianQiJava
> 作者：Claude
> 日期：2025-03-07

---

## 目录

- [一、ID生成策略分类](#一id生成策略分类)
- [二、各表ID策略详细说明](#二各表id策略详细说明)
- [三、为什么AUTO表不推荐更换策略](#三为什么auto表不推荐更换策略)
- [四、实施建议](#四实施建议)
- [五、百度UidGenerator介绍](#五百度uidgenerator介绍)

---

## 一、ID生成策略分类

### 1.1 策略总览

| 策略类型 | 表数量 | 占比 | 说明 |
|---------|-------|-----|------|
| **百度UidGenerator（雪花算法）** | 19张 | 42% | 核心业务表 |
| **数据库自增（AUTO）** | 22张 | 49% | 关联/日志/用户扩展/配置表 |
| **业务编号（自定义格式）** | 3张 | 7% | 订单/退款/支付流水 |
| **INPUT（手动指定）** | 1张 | 2% | 特殊统计表 |

**总计：45张表**

---

### 1.2 策略定义

#### 🔴 百度UidGenerator（推荐用于核心业务）

**特点：**
- 分布式唯一
- 趋势递增（按时间有序）
- 高性能（本地生成，QPS > 1000万）
- 包含时间戳信息
- 支持时钟回拨处理

**ID结构：**
```
0 - 0000000000 0000000000 0000000000 0000000000 0 - 00000 - 00000 - 000000000000
↑   ↑                    ↑                                    ↑     ↑     ↑
│   └───── 28位时间戳 ─────┘                                    │     │     └─ 12位序列号
│                                                              │     └───── 5位机器ID
固定值                                                        22位数据中心ID

总长：64位（Long类型）
```

**适用场景：**
- 需要全局唯一的实体
- 高并发插入场景
- 需要按时间排序的数据
- 可能需要分库分表的表

---

#### 🟢 数据库自增AUTO

**特点：**
- 简单可靠
- 性能良好
- 严格递增
- 无需额外依赖

**局限性：**
- 分库分表时ID冲突
- ID可预测（安全性问题）
- 高并发有锁竞争

**适用场景：**
- 关联表（中间表）
- 日志表
- 用户附属表
- 系统配置表
- 数据量小且单机部署的表

---

#### 🟡 业务编号（自定义格式）

**特点：**
- 可读性好
- 包含业务信息
- 对外展示友好
- 需要额外生成逻辑

**常见格式：**
- `XD{雪花ID}` - 订单号
- `RF{yyMMdd}{6位随机}` - 退款单号
- `PAY{雪花ID}` - 支付流水号

---

## 二、各表ID策略详细说明

### 2.1 🔴 核心业务表（19张）- 使用UidGenerator

| 表名 | 主键字段 | 策略 | 理由 |
|-----|---------|------|------|
| **user** | userId | UidGenerator | 用户ID需全局唯一，长期稳定，可能分库分表 |
| **product** | productId | UidGenerator | 商品ID需全局唯一，便于排序和扩展 |
| **share_item** | shareId | UidGenerator | 共享物品ID需全局唯一 |
| **order** | orderId | UidGenerator | 订单ID需全局唯一，高并发写入 |
| **message** | messageId | UidGenerator | 消息ID需分布式唯一，高频插入 |
| **conversation** | conversationId | UidGenerator | 会话ID需全局唯一 |
| **conversation_member** | memberId | UidGenerator | 会话成员ID需唯一 |
| **evaluation** | evalId | UidGenerator | 评价ID需全局唯一 |
| **product_image** | imageId | UidGenerator | 图片ID需全局唯一，可能分布式存储 |
| **share_item_image** | imageId | UidGenerator | 图片ID需全局唯一 |
| **category** | categoryId | UidGenerator | 分类ID需全局唯一，便于扩展 |
| **share_item_booking** | bookingId | UidGenerator | 预约ID需全局唯一 |
| **flash_sale_session** | sessionId | UidGenerator | 秒杀场次ID需唯一 |
| **flash_sale_product** | id | UidGenerator | 秒杀商品需唯一 |
| **flash_sale_order_ext** | id | UidGenerator | 订单扩展需唯一 |
| **deposit_record** | recordId | UidGenerator | 押金记录需全局唯一 |
| **transfer_record** | transferId | UidGenerator | 转赠记录需全局唯一 |
| **refund_record** | refundId | UidGenerator | 退款记录需全局唯一 |
| **coupon** | couponId | UidGenerator | 优惠券ID需全局唯一 |

**为什么这些表用UidGenerator？**

1. **全局唯一性需求**：用户、商品、订单等是核心业务实体，需要确保在整个系统中唯一
2. **扩展性考虑**：未来可能需要分库分表，自增ID会导致冲突
3. **高并发场景**：订单、消息等表会有高并发写入，雪花算法性能更好
4. **时间有序性**：这些表通常需要按时间排序查询，雪花算法生成的ID按时间递增

---

### 2.2 🟢 关联/日志表（9张）- 保持AUTO

| 表名 | 主键字段 | 策略 | 理由 |
|-----|---------|------|------|
| **product_favorite** | favoriteId | AUTO | 简单关联表，通过(user_id, product_id)唯一索引保证数据完整性 |
| **product_view_history** | historyId | AUTO | 日志表，数据量大但无全局唯一需求 |
| **blacklist** | blacklistId | AUTO | 关联表，通过(user_id, blocked_user_id)唯一索引 |
| **user_follow** | followId | AUTO | 关注关系表，通过(follower_id, following_id)唯一索引 |
| **quick_reply** | replyId | AUTO | 用户级数据，无需全局唯一 |
| **report** | reportId | AUTO | 举报记录，无需全局唯一 |
| **operation_log** | logId | AUTO | 操作日志，高频写入，简单即可 |
| **login_device** | deviceId | AUTO | 设备记录，用户级别，无需全局唯一 |
| **notification_read_records** | id | AUTO | 通知阅读记录表 |

---

### 2.3 🔵 用户扩展表（7张）- 保持AUTO

| 表名 | 主键字段 | 策略 | 理由 |
|-----|---------|------|------|
| **user_real_name_auth** | id | AUTO | 用户附属表，一个用户一条记录 |
| **user_student_auth** | id | AUTO | 用户附属表，一个用户一条记录 |
| **user_verification** | verificationId | AUTO | 用户附属表，一个用户一条记录 |
| **user_coupon** | userCouponId | AUTO | 用户优惠券，用户级别数据 |
| **user_address** | addressId | AUTO | 用户地址，用户级别数据 |
| **user_preference** | preferenceId | AUTO | 用户偏好，一个用户一条记录 |
| **user_feedback** | feedbackId | AUTO | 用户反馈，无需全局唯一 |

---

### 2.4 🟣 系统配置表（6张）- 保持AUTO

| 表名 | 主键字段 | 策略 | 理由 |
|-----|---------|------|------|
| **system_config** | configId | AUTO | 系统表，数据量小，由管理员配置 |
| **system_notification** | notificationId | AUTO | 系统通知，量小，管理员发布 |
| **banner** | bannerId | AUTO | 轮播图，量小（通常<100条） |
| **sensitive_word** | wordId | AUTO | 敏感词表，量小，管理员维护 |
| **campus_location** | locationId | AUTO | 校园位置配置，量小 |
| **hot_tag** | tagId | AUTO | 热门标签，量小 |

---

### 2.5 🟠 其他表（4张）

| 表名 | 主键字段 | 策略 | 理由 |
|-----|---------|------|------|
| **flash_sale_session_template** | templateId | AUTO | 模板表，数据量小 |
| **product_statistics** | productId | INPUT | 特殊：使用商品ID作为主键 |
| **order** | orderNo | 自定义 | 业务编号：`XD{雪花ID}` |
| **deposit_record** | transactionNo | 自定义 | 业务编号：`PAY{雪花ID}` |

---

## 三、为什么AUTO表不推荐更换策略

### 3.1 关联表（如 product_favorite）

**当前情况：**
```sql
CREATE TABLE product_favorite (
    favorite_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    create_time DATETIME,
    UNIQUE KEY uk_user_product (user_id, product_id)
);
```

**为什么不需要换？**

1. **唯一性已保证**：通过 `(user_id, product_id)` 唯一索引已经保证了数据唯一性
2. **主键仅用于标识**：favorite_id 只是一个技术主键，没有业务含义
3. **无需跨表关联**：很少会通过 favorite_id 去关联其他表
4. **查询模式固定**：查询总是通过 `WHERE user_id = ? AND product_id = ?` 来查找

**如果换成雪花算法：**
- ✅ 好处：无
- ❌ 坏处：ID变长（从1-1000变成1234567890123456789），占用更多存储空间

---

### 3.2 日志表（如 operation_log）

**为什么不需要换？**

1. **写入量大**：日志表可能每天写入数十万条记录，自增ID性能足够
2. **查询模式简单**：通常是按时间范围查询，不使用主键
3. **数据归档**：日志表会定期归档/删除，自增ID便于管理
4. **无业务含义**：日志ID只是技术主键，没有业务价值

**如果换成雪花算法：**
- ✅ 好处：无（日志表通常不分库分表）
- ❌ 坏处：
  - ID不连续，难以判断数据量
  - 占用更多存储空间
  - 排序时无法直观看出时间顺序

---

### 3.3 用户附属表（如 user_address）

**为什么不需要换？**

1. **数据归属明确**：用户地址通过 `user_id` 关联，不需要全局唯一
2. **数据量小**：每个用户最多10个地址，总量可控
3. **查询模式**：总是 `WHERE user_id = ?` 查询
4. **无需跨库**：用户附属表总是和用户表在同一个数据库

**如果换成雪花算法：**
- ✅ 好处：无
- ❌ 坏处：存储空间增加

---

### 3.4 系统配置表（如 system_config）

**为什么不需要换？**

1. **数据量极小**：系统配置通常<1000条
2. **管理员操作**：由管理员在后台配置，不需要高并发
3. **不分库分表**：配置表永远是单表
4. **主键仅用于标识**：config_id 只是技术主键

**如果换成雪花算法：**
- ✅ 好处：无
- ❌ 坏处：ID变长，不便记忆和管理

---

## 四、AUTO表可以换成其他策略吗？

### 4.1 技术上可以，但意义不大

**换成的条件（满足2条以上才考虑）：**

1. ✅ **需要分库分表**：AUTO会导致ID冲突
2. ✅ **需要全局唯一**：其他表需要通过这个ID关联
3. ✅ **高并发写入**：单库AUTO成为性能瓶颈
4. ✅ **ID暴露有风险**：通过ID规律推测业务量

### 4.2 各类AUTO表分析

| 表类型 | 是否可以换 | 换的收益 | 换的成本 | 建议 |
|-------|----------|---------|---------|------|
| **关联表** | ❌ 不建议 | 无 | 存储增加 | **保持AUTO** |
| **日志表** | ⚠️ 可选 | 避免单库瓶颈 | 归档复杂化 | **保持AUTO** |
| **用户附属** | ❌ 不建议 | 无 | 存储增加 | **保持AUTO** |
| **系统配置** | ❌ 不建议 | 无 | 管理复杂 | **保持AUTO** |

---

### 4.3 特殊情况：什么时候需要换？

#### 场景1：日志表数据量巨大（每天1000万+）

**解决方案：**
```java
// 按月分表，每个表使用AUTO
operation_log_202501
operation_log_202502
operation_log_202503
```

**策略：** 使用AUTO + 分表，而不是雪花算法

---

#### 场景2：关联表需要跨库关联

**示例：** `product_favorite` 和 `product` 在不同库

**解决方案：**
```java
// 方案1：使用雪花ID
@TableId(type = IdType.ASSIGN_ID)
private Long favoriteId;

// 方案2：使用复合主键
@EmbeddedId
private ProductFavoriteKey id; // (userId + productId)
```

**策略：** 只有在真正分库的情况下才需要换

---

## 五、实施建议

### 5.1 分阶段实施

#### 第一阶段：核心表切换（推荐立即实施）

**切换的表（19张）：**
```
✅ user
✅ product
✅ share_item
✅ order
✅ message
✅ conversation
✅ conversation_member
✅ evaluation
✅ product_image
✅ share_item_image
✅ category
✅ share_item_booking
✅ flash_sale_session
✅ flash_sale_product
✅ flash_sale_order_ext
✅ deposit_record
✅ transfer_record
✅ refund_record
✅ coupon
```

**影响：** 中等
**收益：** 为未来扩展打下基础

---

#### 第二阶段：保持现状（AUTO表）

**保持AUTO的表（22张）：**
```
🟢 关联表：product_favorite, product_view_history, blacklist, user_follow
🟢 日志表：operation_log, login_device
🟢 用户附属：user_real_name_auth, user_student_auth, user_coupon, user_address
🟢 系统配置：system_config, system_notification, banner, sensitive_word
```

**影响：** 无
**收益：** 保持简单

---

### 5.2 实施步骤

#### 步骤1：添加依赖

```xml
<dependency>
    <groupId>com.baidu.fsg</groupId>
    <artifactId>uid-generator</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### 步骤2：创建数据库表

```sql
CREATE TABLE worker_node (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  host_name VARCHAR(64) NOT NULL COMMENT '主机名',
  port VARCHAR(64) NOT NULL COMMENT '端口',
  type INT NOT NULL COMMENT '节点类型: ACTUAL(1) CONTAINER(2)',
  launch_date DATE NOT NULL COMMENT '上线日期',
  PRIMARY KEY (id),
  UNIQUE KEY uk_host_port (host_name, port)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DB WorkerID分配';
```

#### 步骤3：配置UidGenerator

```java
@Configuration
public class UidGeneratorConfig {

    @Bean("defaultUidGenerator")
    public UidGenerator defaultUidGenerator() {
        DefaultUidGenerator defaultUidGenerator = new DefaultUidGenerator();
        // 时间位数
        defaultUidGenerator.setTimeBits(29);
        // 机器位数
        defaultUidGenerator.setWorkerBits(21);
        // 序列位数
        defaultUidGenerator.setSeqBits(13);
        // 日期格式化
        defaultUidGenerator.setEpochStr("2024-01-01");
        return defaultUidGenerator;
    }
}
```

#### 步骤4：创建ID生成服务

```java
@Service
public class IdGeneratorService {

    @Resource
    private UidGenerator uidGenerator;

    /**
     * 生成雪花ID
     */
    public long generateUid() {
        return uidGenerator.getUID();
    }

    /**
     * 生成订单号
     */
    public String generateOrderNo() {
        return "XD" + generateUid();
    }

    /**
     * 生成退款单号
     */
    public String generateRefundNo() {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyMMdd"));
        String random = String.format("%06d", new Random().nextInt(1000000));
        return "RF" + timestamp + random;
    }
}
```

#### 步骤5：修改实体类

```java
// 修改前
@TableId(type = IdType.AUTO)
private Long userId;

// 修改后
@TableId(type = IdType.ASSIGN_ID)
private Long userId;
```

---

### 5.3 数据迁移

#### 方案1：渐进式迁移（推荐）

```sql
-- 旧数据保持不变
-- 新数据使用雪花ID
-- 由于雪花ID > 自增ID，不会冲突
```

**优点：** 简单，无需修改现有数据
**缺点：** ID不连续

---

#### 方案2：一次性迁移

```sql
-- 1. 备份现有数据
CREATE TABLE user_backup AS SELECT * FROM user;

-- 2. 修改表结构
ALTER TABLE user MODIFY COLUMN user_id BIGINT;

-- 3. 重新生成ID（需要程序处理）
```

**优点：** ID统一
**缺点：** 需要更新所有关联数据

---

## 六、百度UidGenerator介绍

### 6.1 与普通雪花算法的区别

| 特性 | 普通雪花算法 | 百度UidGenerator |
|-----|------------|-----------------|
| **时钟回拨** | 直接报错 | 支持容忍 |
| **性能** | 高 | 更高（Cached模式） |
| **配置** | 硬编码机器ID | 数据库动态分配 |
| **实现复杂度** | 简单 | 中等 |
| **生产验证** | 广泛使用 | 百度大规模验证 |

---

### 6.2 UidGenerator两种模式

#### DefaultUidGenerator

**特点：**
- 实时计算生成
- 严格按时间递增
- 性能：单机 > 100万/秒

**适用：** 普通业务场景

---

#### CachedUidGenerator

**特点：**
- 预缓存ID段
- 性能更高：单机 > 1000万/秒
- ID有少量跳跃

**适用：** 超高并发场景（如秒杀）

**缓存策略：**
```
1. 启动时缓存一个号段
2. 号段用完80%时异步加载下一个号段
3. 双Buffer保证平滑切换
```

---

### 6.3 推荐配置

```java
// 核心业务（订单、消息）- 使用CachedUidGenerator
@Bean
public UidGenerator cachedUidGenerator() {
    CachedUidGenerator cachedUidGenerator = new CachedUidGenerator();
    cachedUidGenerator.setEpochStr("2024-01-01");
    cachedUidGenerator.setBoostPower(3);  // 增益因子
    cachedUidGenerator.setPaddingFactor(50); // 填充因子
    return cachedUidGenerator;
}

// 普通业务（用户、商品）- 使用DefaultUidGenerator
@Bean
public UidGenerator defaultUidGenerator() {
    DefaultUidGenerator defaultUidGenerator = new DefaultUidGenerator();
    defaultUidGenerator.setEpochStr("2024-01-01");
    return defaultUidGenerator;
}
```

---

## 七、总结

### 7.1 核心观点

1. **不是所有表都需要全局唯一ID**
   - 关联表、日志表、用户附属表使用AUTO完全足够

2. **核心业务表优先使用UidGenerator**
   - 用户、商品、订单等核心实体应该使用雪花算法

3. **根据实际需求选择**
   - 不要为了技术而技术
   - 考虑未来扩展性，但不过度设计

4. **渐进式迁移**
   - 旧数据保持AUTO
   - 新数据使用UidGenerator
   - 两者可以共存

---

### 7.2 最终策略汇总表

| 分类 | 表数量 | 策略 | 实施优先级 |
|-----|-------|------|----------|
| 核心业务 | 19张 | UidGenerator | **P0（立即）** |
| 关联/日志 | 9张 | AUTO | 保持现状 |
| 用户扩展 | 7张 | AUTO | 保持现状 |
| 系统配置 | 6张 | AUTO | 保持现状 |
| 业务编号 | 3张 | 自定义 | **P1（近期）** |
| 特殊表 | 1张 | INPUT | 保持现状 |

---

## 附录A：AUTO_INCREMENT 起始值设置

### A.1 设置原则

| 表类型 | 起始值 | 理由 |
|-------|-------|------|
| **关联表** | 1,000,000（100万） | 预留100万空间，避免业务量被轻易推测 |
| **日志表** | 10,000,000（1000万） | 日志量大，预留更多空间 |
| **用户附属表** | 100,000（10万） | 用户级数据，适中起始值 |
| **系统配置表** | 10,000（1万） | 配置表量小，较小起始值即可 |

### A.2 实施方法

#### 方法1：直接执行SQL（推荐）

```sql
-- 关联表
ALTER TABLE product_favorite AUTO_INCREMENT = 1000000;
ALTER TABLE blacklist AUTO_INCREMENT = 1000000;
ALTER TABLE user_follow AUTO_INCREMENT = 1000000;

-- 日志表
ALTER TABLE operation_log AUTO_INCREMENT = 10000000;
ALTER TABLE login_device AUTO_INCREMENT = 10000000;

-- 用户附属表
ALTER TABLE user_address AUTO_INCREMENT = 100000;
ALTER TABLE user_coupon AUTO_INCREMENT = 100000;

-- 系统配置表
ALTER TABLE system_config AUTO_INCREMENT = 10000;
ALTER TABLE banner AUTO_INCREMENT = 10000;
```

#### 方法2：Flyway/Liquibase迁移脚本

```sql
-- V1.0.3__set_auto_increment_start.sql
ALTER TABLE product_favorite AUTO_INCREMENT = 1000000;
```

### A.3 注意事项

1. **必须在空表上执行**：如果表已有数据，起始值会设置为 `MAX(id) + 1`
2. **备份数据**：执行前建议备份数据库
3. **测试环境先验证**：在测试环境验证无误后再在生产环境执行

### A.4 完整脚本

详见：`docs/init_auto_increment.sql`

---

## 附录B：参考资料

- [百度UidGenerator GitHub](https://github.com/baidu/uid-generator)
- [Snowflake算法原理](https://developer.twitter.com/en/docs/twitter-ids)
- [MyBatis-Plus ID生成策略](https://baomidou.com/pages/568eb2/)

### B. 实施检查清单

- [ ] 添加依赖
- [ ] 创建worker_node表
- [ ] 配置UidGenerator
- [ ] 创建ID生成服务
- [ ] 修改19张核心表实体类
- [ ] 添加业务编号生成方法
- [ ] 测试ID生成
- [ ] 数据迁移（如需要）
- [ ] 性能测试
- [ ] 上线部署

---

**文档版本：** v1.0
**最后更新：** 2025-03-07
