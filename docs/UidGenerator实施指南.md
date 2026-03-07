# 百度UidGenerator实施指南

> 项目：XianQiJava - 校园二手交易平台
> 日期：2025-03-07

---

## 目录

- [一、部署前准备](#一部署前准备)
- [二、部署步骤](#二部署步骤)
- [三、使用说明](#三使用说明)
- [四、验证测试](#四验证测试)
- [五、常见问题](#五常见问题)

---

## 一、部署前准备

### 1.1 环境要求

- **JDK**: 17+
- **Spring Boot**: 3.2.5+
- **MySQL**: 5.7+ / 8.0+
- **MyBatis-Plus**: 3.5.5+

### 1.2 检查清单

- [ ] Maven依赖已添加
- [ ] 数据库连接正常
- [ ] 有CREATE TABLE权限

---

## 二、部署步骤

### 步骤1：创建数据库表

连接到数据库，执行以下SQL：

```bash
mysql -u root -p Xianqi
```

```sql
source /path/to/XianQiJava/docs/uid_generator_worker_node.sql
```

或者直接执行：

```sql
CREATE TABLE IF NOT EXISTS worker_node (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    host_name VARCHAR(64) NOT NULL COMMENT '主机名',
    port VARCHAR(64) NOT NULL COMMENT '端口',
    type INT NOT NULL COMMENT '节点类型: ACTUAL(1) CONTAINER(2)',
    launch_date DATE NOT NULL COMMENT '上线日期',
    PRIMARY KEY (id),
    UNIQUE KEY uk_host_port (host_name, port)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='DB WorkerID分配';
```

**验证：**
```sql
SELECT TABLE_NAME, TABLE_COMMENT
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'Xianqi' AND TABLE_NAME = 'worker_node';
```

---

### 步骤2：编译项目

```bash
cd /path/to/XianQiJava
./mvnw clean compile
```

---

### 步骤3：运行测试

```bash
./mvnw test -Dtest=IdGeneratorServiceTest
```

**预期输出：**
```
生成的UID1: 1234567890123456789
生成的UID2: 1234567890123456790
批量生成100个ID成功
...
[INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
```

---

### 步骤4：启动应用

```bash
./mvnw spring-boot:run
```

**查看启动日志，确认UidGenerator初始化成功：**
```
[UidGeneratorConfig] - DefaultUidGenerator initialized successfully
[UidGeneratorConfig] - CachedUidGenerator initialized successfully
```

---

## 三、使用说明

### 3.1 自动生成（推荐）

**场景：** 插入新记录时自动生成ID

**实体类配置（已完成）：**
```java
@Data
@TableName("user")
public class User extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)  // ✅ 已配置
    private Long userId;

    private String username;
    // ...
}
```

**Service使用：**
```java
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    public void createUser(User user) {
        // ❌ 不需要手动设置ID
        // user.setUserId(123L);

        // ✅ MyBatis-Plus会自动调用UidIdentifierGenerator生成ID
        userMapper.insert(user);

        // user.getUserId() 已经自动赋值
        log.info("用户创建成功，ID: {}", user.getUserId());
    }
}
```

---

### 3.2 手动生成业务编号

**场景：** 生成订单号、退款单号等对外展示的编号

**注入IdGeneratorService：**
```java
@Service
public class OrderService {

    @Resource
    private IdGeneratorService idGeneratorService;

    @Resource
    private OrderMapper orderMapper;

    public void createOrder(Order order) {
        // 生成订单号
        String orderNo = idGeneratorService.generateOrderNo();
        order.setOrderNo(orderNo);

        // orderId会自动生成（@TableId(type = IdType.ASSIGN_ID)）
        orderMapper.insert(order);

        log.info("订单创建成功，订单号: {}", orderNo);
    }
}
```

---

### 3.3 可用方法列表

| 方法 | 说明 | 示例输出 |
|-----|------|---------|
| `generateUid()` | 生成雪花ID | `1234567890123456789` |
| `generateCachedUid()` | 生成缓存雪花ID（高并发） | `1234567890123456790` |
| `generateOrderNo()` | 生成订单号 | `XD1234567890123456789` |
| `generateRefundNo()` | 生成退款单号 | `RF250307123456` |
| `generatePaymentNo()` | 生成支付流水号 | `PAY1234567890123456789` |
| `generateDepositNo()` | 生成押金流水号 | `DEP1234567890123456789` |
| `generateProductNo()` | 生成商品编号 | `PROD1234567890123456789` |
| `generateCouponCode()` | 生成优惠券码 | `AB12CD34` |
| `generateShareCode()` | 生成分享码 | `123456` |
| `generateImageUuid()` | 生成图片UUID | `a1b2c3d4e5f6...` |
| `generateConversationId()` | 生成会话ID | `CONV1234567890123456789` |

---

## 四、验证测试

### 4.1 运行测试套件

```bash
./mvnw test -Dtest=IdGeneratorServiceTest
```

### 4.2 手动验证

**测试类：**
```java
@SpringBootTest
public class ManualTest {

    @Resource
    private IdGeneratorService idGeneratorService;

    @Test
    public void testManual() {
        // 生成10个ID
        for (int i = 0; i < 10; i++) {
            long uid = idGeneratorService.generateUid();
            System.out.println("UID[" + i + "]: " + uid);
        }
    }
}
```

---

## 五、常见问题

### Q1: 启动报错：Table 'Xianqi.worker_node' doesn't exist

**原因：** 没有创建worker_node表

**解决：** 执行步骤1中的SQL脚本

---

### Q2: ID生成失败：Cannot get WorkerID

**原因：** 数据库连接失败或权限不足

**解决：**
1. 检查数据库连接配置
2. 确保有SELECT/INSERT权限

---

### Q3: 生成的ID不是递增的

**原因：** 使用了`CachedUidGenerator`，ID有少量跳跃

**说明：** 这是正常现象，缓存模式会在号段切换时跳跃

**解决：** 如果需要严格递增，使用`DefaultUidGenerator`

---

### Q4: 性能不达标

**检查项：**
1. 使用`CachedUidGenerator`而不是`DefaultUidGenerator`
2. 检查数据库连接池配置
3. 增大`boostPower`参数（RingBuffer大小）

**优化示例：**
```java
@Bean("cachedUidGenerator")
public UidGenerator cachedUidGenerator() {
    CachedUidGenerator generator = new CachedUidGenerator();
    generator.setWorkerIdAssigner(workerIdAssigner);
    generator.setBoostPower(3);  // 默认3，可改为5或7
    generator.setPaddingFactor(50);  // 默认50
    return generator;
}
```

---

### Q5: 时钟回拨问题

**现象：** 报错"Clock moved backwards"

**原因：** 系统时间被回调

**解决：**
1. 不要修改系统时间
2. 使用NTP同步时间
3. 如果必须回调，重启应用

---

## 六、已修改的实体类清单

| 序号 | 表名 | 实体类 | ID字段 |
|-----|------|-------|--------|
| 1 | user | User | userId |
| 2 | product | Product | productId |
| 3 | share_item | ShareItem | shareId |
| 4 | `order` | Order | orderId |
| 5 | message | Message | messageId |
| 6 | conversation | Conversation | conversationId |
| 7 | conversation_member | ConversationMember | memberId |
| 8 | evaluation | Evaluation | evalId |
| 9 | product_image | ProductImage | imageId |
| 10 | share_item_image | ShareItemImage | imageId |
| 11 | category | Category | categoryId |
| 12 | share_item_booking | ShareItemBooking | bookingId |
| 13 | flash_sale_session | FlashSaleSession | sessionId |
| 14 | flash_sale_product | FlashSaleProduct | id |
| 15 | flash_sale_order_ext | FlashSaleOrderExt | id |
| 16 | deposit_record | DepositRecord | recordId |
| 17 | transfer_record | TransferRecord | transferId |
| 18 | refund_record | RefundRecord | refundId |
| 19 | coupon | Coupon | couponId |

**总计：19张表**

---

## 七、数据迁移说明

### 7.1 新建数据库

**直接部署即可，无需迁移**

---

### 7.2 已有数据

**策略：** 渐进式迁移

```
旧数据：保持AUTO（1, 2, 3...）
新数据：使用UidGenerator（1234567890123456789...）

由于UidGenerator生成的ID远大于AUTO的ID，不会冲突
```

**验证：**
```sql
-- 查看现有最大ID
SELECT MAX(user_id) FROM user;  -- 假设返回1000

-- 插入新数据后，ID会是雪花算法生成的大数值
SELECT user_id FROM user ORDER BY user_id DESC LIMIT 10;
```

---

## 八、项目结构

```
XianQiJava/
├── docs/
│   ├── ID生成策略分析.md          # ID策略分析文档
│   ├── init_auto_increment.sql   # AUTO_INCREMENT起始值脚本
│   ├── uid_generator_worker_node.sql  # worker_node表脚本
│   └── UidGenerator实施指南.md    # 本文档
│
├── src/main/java/com/xx/xianqijava/
│   ├── config/
│   │   ├── UidGeneratorConfig.java       # UidGenerator配置
│   │   └── UidIdentifierGenerator.java   # MyBatis-Plus适配器
│   │
│   ├── service/
│   │   └── IdGeneratorService.java       # ID生成服务
│   │
│   └── entity/
│       ├── User.java                      # ✅ 已修改
│       ├── Product.java                   # ✅ 已修改
│       ├── Order.java                     # ✅ 已修改
│       ├── ... (共19张表)
│
└── src/test/java/com/xx/xianqijava/
    └── service/
        └── IdGeneratorServiceTest.java    # 测试类
```

---

## 九、参考资源

- [百度UidGenerator GitHub](https://github.com/baidu/uid-generator)
- [MyBatis-Plus ID生成策略](https://baomidou.com/pages/568eb2/)
- [Snowflake算法原理](https://developer.twitter.com/en/docs/twitter-ids)

---

## 十、更新日志

| 版本 | 日期 | 说明 |
|-----|------|------|
| v1.0 | 2025-03-07 | 初始版本，完成UidGenerator集成 |

---

**文档维护者：** Claude
**最后更新：** 2025-03-07
