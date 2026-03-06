# 聊天模块数据表设计文档

## 数据表概览

聊天模块包含 **5个核心数据表**：

| 表名 | 用途 | 说明 |
|------|------|------|
| `conversation` | 会话表 | 存储单聊和群聊会话信息 |
| `conversation_member` | 会话成员表 | 用于群聊场景，管理群成员 |
| `message` | 消息表 | 存储所有聊天消息 |
| `quick_reply` | 快捷回复表 | 用户常用回复语句 |
| `report` | 举报表 | 聊天举报记录 |

---

## 1. conversation（会话表）

### 表结构
| 字段名 | 类型 | 是否空 | 键 | 默认值 | 说明 |
|--------|------|--------|-----|--------|------|
| conversation_id | bigint unsigned | NO | PRI | AUTO_INCREMENT | 会话ID |
| conversation_type | tinyint unsigned | NO | | 1 | 会话类型：1-单聊 2-群聊 |
| user_id_1 | bigint unsigned | NO | MUL | | 用户1ID（发起方） |
| user_id_2 | bigint unsigned | YES | MUL | | 用户2ID（接收方，单聊时使用） |
| related_order_id | bigint unsigned | YES | MUL | | 关联订单ID |
| last_message_id | bigint unsigned | YES | | | 最后消息ID |
| last_message_content | varchar(500) | YES | | | 最后消息内容（冗余字段，便于列表展示） |
| last_message_time | datetime | YES | MUL | | 最后消息时间 |
| unread_count_user1 | int unsigned | NO | | 0 | 用户1未读数 |
| unread_count_user2 | int unsigned | NO | | 0 | 用户2未读数 |
| is_muted_user1 | tinyint unsigned | NO | | 0 | 用户1是否免打扰：0-否 1-是 |
| is_muted_user2 | tinyint unsigned | NO | | 0 | 用户2是否免打扰：0-否 1-是 |
| remark_user1 | varchar(100) | YES | | | 用户1备注名 |
| remark_user2 | varchar(100) | YES | | | 用户2备注名 |
| is_archived_user1 | tinyint unsigned | NO | | 0 | 用户1是否归档：0-否 1-是 |
| is_archived_user2 | tinyint unsigned | NO | | 0 | 用户2是否归档：0-否 1-是 |
| is_pinned_user1 | tinyint unsigned | NO | MUL | 0 | 用户1是否置顶：0-否 1-是 |
| is_pinned_user2 | tinyint unsigned | NO | MUL | 0 | 用户2是否置顶：0-否 1-是 |
| pin_order_user1 | int unsigned | YES | | | 用户1置顶排序（数值越小越靠前） |
| pin_order_user2 | int unsigned | YES | | | 用户2置顶排序 |
| status | tinyint unsigned | NO | | 0 | 状态：0-正常，1-已删除 |
| create_time | datetime | NO | | CURRENT_TIMESTAMP | 创建时间 |
| update_time | datetime | NO | | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| deleted | tinyint unsigned | NO | | 0 | 逻辑删除：0-未删除 1-已删除 |

### 设计特点

1. **单聊场景优化**：
   - `user_id_1` 和 `user_id_2` 存储单聊的两个用户
   - 两个字段分别存储各自的状态（未读数、免打扰、置顶等）
   - 避免了额外的关联查询

2. **支持订单关联**：
   - `related_order_id` 可以关联订单表
   - 便于在聊天中展示订单信息

3. **冗余字段设计**：
   - `last_message_content` 和 `last_message_time` 冗余存储最后消息
   - 避免每次查询会话列表时都要关联消息表

4. **灵活的会话管理**：
   - 支持置顶、免打扰、归档等功能
   - 每个用户可以独立设置会话属性

### 索引设计
```sql
PRIMARY KEY (conversation_id)
KEY idx_user_id_1 (user_id_1)
KEY idx_user_id_2 (user_id_2)
KEY idx_last_message_time (last_message_time)
KEY idx_related_order_id (related_order_id)
KEY idx_pinned_user1 (is_pinned_user1, pin_order_user1)
KEY idx_pinned_user2 (is_pinned_user2, pin_order_user2)
```

---

## 2. conversation_member（会话成员表）

### 表结构
| 字段名 | 类型 | 是否空 | 键 | 默认值 | 说明 |
|--------|------|--------|-----|--------|------|
| member_id | bigint unsigned | NO | PRI | AUTO_INCREMENT | 成员ID |
| conversation_id | bigint unsigned | NO | MUL | | 会话ID |
| user_id | bigint unsigned | NO | MUL | | 用户ID |
| nickname | varchar(50) | YES | | | 群昵称 |
| unread_count | int unsigned | NO | | 0 | 未读消息数 |
| is_muted | tinyint unsigned | NO | | 0 | 是否免打扰：0-否 1-是 |
| join_time | datetime | NO | | CURRENT_TIMESTAMP | 加入时间 |
| last_read_message_id | bigint unsigned | YES | | | 最后阅读消息ID |

### 设计特点

1. **群聊场景支持**：
   - 一个会话可以有多个成员
   - 每个成员独立的未读数和设置

2. **已读状态追踪**：
   - `last_read_message_id` 记录每个成员最后阅读的消息
   - 便于计算未读数和显示"已读"回执

### 索引设计
```sql
PRIMARY KEY (member_id)
KEY idx_conversation_id (conversation_id)
KEY idx_user_id (user_id)
```

---

## 3. message（消息表）

### 表结构
| 字段名 | 类型 | 是否空 | 键 | 默认值 | 说明 |
|--------|------|--------|-----|--------|------|
| message_id | bigint unsigned | NO | PRI | AUTO_INCREMENT | 消息ID |
| conversation_id | bigint unsigned | NO | MUL | | 会话ID |
| from_user_id | bigint unsigned | NO | MUL | | 发送者用户ID |
| to_user_id | bigint unsigned | YES | MUL | | 接收者用户ID（单聊时使用） |
| content | text | YES | | | 消息内容 |
| type | tinyint unsigned | NO | | 1 | 消息类型：1-文本 2-图片 3-商品卡片 4-订单卡片 5-引用消息 6-系统通知 |
| parent_message_id | bigint unsigned | YES | MUL | | 父消息ID（引用回复时使用） |
| is_read | tinyint unsigned | NO | | 0 | 是否已读：0-未读 1-已读 |
| read_time | datetime | YES | | | 阅读时间 |
| send_status | tinyint unsigned | NO | | 0 | 发送状态：0-发送中 1-发送成功 2-发送失败 |
| delivered_time | datetime | YES | | | 送达时间 |
| extra_data | json | YES | | | 额外数据（如商品卡片、订单卡片内容） |
| reply_count | int unsigned | NO | | 0 | 回复数量 |
| status | tinyint unsigned | NO | | 0 | 状态：0-正常 1-已撤回 2-已删除 |
| create_time | datetime | NO | MUL | CURRENT_TIMESTAMP | 创建时间 |
| update_time | datetime | NO | | CURRENT_TIMESTAMP ON UPDATE | 更新时间 |
| deleted | tinyint unsigned | NO | | 0 | 逻辑删除：0-未删除 1-已删除 |

### 设计特点

1. **多种消息类型**：
   - 文本消息（type=1）
   - 图片消息（type=2）
   - 商品卡片（type=3）
   - 订单卡片（type=4）
   - 引用消息（type=5）
   - 系统通知（type=6）

2. **引用回复支持**：
   - `parent_message_id` 可以引用其他消息
   - `reply_count` 统计回复数量

3. **消息状态追踪**：
   - `send_status` 追踪发送状态
   - `is_read` 和 `read_time` 追踪已读状态
   - `delivered_time` 记录送达时间

4. **JSON扩展字段**：
   - `extra_data` 存储复杂消息结构
   - 商品卡片、订单卡片等结构化数据

### 索引设计
```sql
PRIMARY KEY (message_id)
KEY idx_conversation_id (conversation_id)
KEY idx_from_user_id (from_user_id)
KEY idx_to_user_id (to_user_id)
KEY idx_parent_message_id (parent_message_id)
KEY idx_create_time (create_time)
```

### 消息类型说明

```java
public enum MessageType {
    TEXT(1, "文本"),
    IMAGE(2, "图片"),
    PRODUCT_CARD(3, "商品卡片"),
    ORDER_CARD(4, "订单卡片"),
    REFERENCE(5, "引用消息"),
    SYSTEM(6, "系统通知");
}
```

### extra_data JSON格式示例

**商品卡片消息**：
```json
{
  "type": "product",
  "productId": 100,
  "title": "iPhone 15 Pro",
  "price": 5999.00,
  "image": "https://xxx.com/product.jpg",
  "status": "在售"
}
```

**订单卡片消息**：
```json
{
  "type": "order",
  "orderId": 50,
  "orderNo": "ORD20260306001",
  "amount": 4300.00,
  "status": "待确认",
  "productName": "MacBook Pro"
}
```

---

## 4. quick_reply（快捷回复表）

### 表结构
| 字段名 | 类型 | 是否空 | 键 | 默认值 | 说明 |
|--------|------|--------|-----|--------|------|
| reply_id | bigint unsigned | NO | PRI | AUTO_INCREMENT | 回复ID |
| user_id | bigint unsigned | NO | MUL | 0 | 用户ID（0表示系统预设） |
| content | varchar(500) | NO | | | 回复内容 |
| category | varchar(50) | YES | MUL | | 分类标签 |
| sort_order | int unsigned | NO | | 0 | 排序 |
| is_system | tinyint unsigned | NO | | 0 | 是否系统预设：0-否 1-是 |
| create_time | datetime | NO | | CURRENT_TIMESTAMP | 创建时间 |

### 设计特点

1. **系统预设 + 用户自定义**：
   - `is_system=1` 表示系统预设的快捷回复
   - `user_id=0` 且 `is_system=1` 表示全局系统预设

2. **分类管理**：
   - `category` 字段支持分类标签
   - 例如："问候"、"交易"、"感谢"等

3. **自定义排序**：
   - `sort_order` 控制显示顺序

### 索引设计
```sql
PRIMARY KEY (reply_id)
KEY idx_user_id (user_id)
KEY idx_category (category)
```

---

## 5. report（举报表）

### 表结构
| 字段名 | 类型 | 是否空 | 键 | 默认值 | 说明 |
|--------|------|--------|-----|--------|------|
| report_id | bigint unsigned | NO | PRI | AUTO_INCREMENT | 举报ID |
| reporter_id | bigint unsigned | NO | MUL | | 举报人ID |
| reported_user_id | bigint unsigned | NO | MUL | | 被举报人ID |
| conversation_id | bigint unsigned | YES | MUL | | 会话ID |
| message_id | bigint unsigned | YES | MUL | | 消息ID |
| reason | varchar(50) | NO | | | 举报原因 |
| description | varchar(500) | YES | | | 详细描述 |
| evidence_images | json | YES | | | 证据图片（JSON数组） |
| status | tinyint unsigned | NO | MUL | 0 | 处理状态：0-待处理 1-已处理 2-已驳回 |
| admin_note | varchar(500) | YES | | | 管理员备注 |
| create_time | datetime | NO | | CURRENT_TIMESTAMP | 举报时间 |
| handle_time | datetime | YES | | | 处理时间 |

### 设计特点

1. **多种举报类型**：
   - 可以举报用户
   - 可以举报会话
   - 可以举报具体消息

2. **证据收集**：
   - `evidence_images` 存储截图证据
   - JSON数组格式：`["url1", "url2", "url3"]`

3. **处理流程**：
   - 待处理 → 已处理/已驳回
   - 管理员可以添加备注

### 索引设计
```sql
PRIMARY KEY (report_id)
KEY idx_reporter_id (reporter_id)
KEY idx_reported_user_id (reported_user_id)
KEY idx_conversation_id (conversation_id)
KEY idx_message_id (message_id)
KEY idx_status (status)
```

---

## 数据表关系图

```
┌─────────────────┐       ┌──────────────────┐       ┌─────────────────┐
│  conversation   │───────│conversation_member│       │     user        │
└─────────────────┘       └──────────────────┘       └─────────────────┘
         │                           │                         │
         │                           │                         │
         ▼                           │                         │
┌─────────────────┐                  │                         │
│     message     │                  │                         │
└─────────────────┘                  │                         │
         │                           │                         │
         │                           │                         │
         ▼                           │                         │
┌─────────────────┐                  │                         │
│     report      │                  │                         │
└─────────────────┘                  │                         │
                                      │                         │
┌─────────────────┐                  │                         │
│  quick_reply    │◄─────────────────┘                         │
└─────────────────┘                                            │
         │                                                      │
         └──────────────────────────────────────────────────────┘
```

## 核心业务流程

### 1. 发送消息流程
```
1. 前端调用发送消息API
2. 后端创建message记录（send_status=0发送中）
3. 通过WebSocket推送消息给接收方
4. 更新message的send_status=1（发送成功）
5. 更新conversation的last_message_*字段
6. 增加接收方的unread_count
```

### 2. 消息已读流程
```
1. 用户进入会话页面
2. 前端调用标记已读API，传入last_read_message_id
3. 后端更新message.is_read=1, read_time=now()
4. 更新conversation或conversation_member的未读数
5. 通过WebSocket发送"已读"回执给发送方
```

### 3. 引用回复流程
```
1. 用户长按消息，选择"引用回复"
2. 创建新消息，设置parent_message_id
3. 增加原消息的reply_count
4. WebSocket推送消息，包含被引用消息的内容
```

### 4. 消息撤回流程
```
1. 用户点击撤回按钮（2分钟内）
2. 检查消息创建时间，超过2分钟不允许撤回
3. 更新message.status=1（已撤回）
4. 通过WebSocket推送"消息撤回"通知
5. 前端将消息内容替换为"消息已撤回"
```

## 性能优化建议

1. **分页查询**：
   - 消息列表使用分页查询
   - 每次加载20-50条消息

2. **索引优化**：
   - 为高频查询字段建立索引
   - 复合索引优化排序查询

3. **冗余字段**：
   - conversation表的last_message_*字段避免频繁关联查询

4. **缓存策略**：
   - 会话列表缓存5分钟
   - 快捷回复缓存1小时

5. **消息归档**：
   - 超过6个月的旧消息可以考虑归档
   - 减少主表数据量，提高查询性能

## 统计查询示例

```sql
-- 查询用户的所有会话（按最后消息时间排序）
SELECT c.*, u.avatar, u.nickname
FROM conversation c
LEFT JOIN user u ON (
    (c.user_id_1 = {userId} AND u.user_id = c.user_id_2) OR
    (c.user_id_2 = {userId} AND u.user_id = c.user_id_1)
)
WHERE c.user_id_1 = {userId} OR c.user_id_2 = {userId}
ORDER BY c.last_message_time DESC;

-- 查询会话的消息列表（分页）
SELECT m.*, u.avatar, u.nickname
FROM message m
LEFT JOIN user u ON m.from_user_id = u.user_id
WHERE m.conversation_id = {conversationId}
AND m.status != 1  -- 排除已撤回消息
ORDER BY m.create_time DESC
LIMIT 20 OFFSET 0;

-- 统计未读消息数
SELECT COUNT(*) as unread_count
FROM message m
JOIN conversation c ON m.conversation_id = c.conversation_id
WHERE (c.user_id_1 = {userId} AND c.unread_count_user1 > 0)
   OR (c.user_id_2 = {userId} AND c.unread_count_user2 > 0)
AND m.is_read = 0;
```
