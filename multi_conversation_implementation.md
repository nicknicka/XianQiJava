# 多会话功能实现总结

## 实现概述

成功实现了基于商品的多会话功能，允许同一对用户根据不同商品创建独立的聊天会话。

---

## 完成的工作

### 1. 数据库修改 ✅

**添加字段**：
```sql
ALTER TABLE conversation
ADD COLUMN related_product_id bigint unsigned NULL AFTER related_order_id,
ADD KEY idx_related_product_id (related_product_id);
```

**字段说明**：
- `related_product_id` - 关联的商品ID
- 支持NULL值，表示普通聊天（不关联商品）

---

### 2. 后端代码修改 ✅

#### 2.1 Service接口

**文件**：`ConversationService.java`

**新增方法**：
```java
/**
 * 创建或获取基于商品的会话
 * 支持同一对用户基于不同商品创建多个独立会话
 */
Long createOrUpdateConversation(Long userId, Long targetUserId, Long relatedProductId);

/**
 * 根据用户ID和商品ID查找会话
 */
ConversationVO findConversationByUserAndProduct(Long userId, Long targetUserId, Long relatedProductId);
```

---

#### 2.2 Service实现

**文件**：`ConversationServiceImpl.java`

**核心逻辑**：

**createOrUpdateConversation**：
```java
@Override
@Transactional(rollbackFor = Exception.class)
public Long createOrUpdateConversation(Long userId, Long targetUserId, Long relatedProductId) {
    // 1. 确保 userId1 < userId2
    Long smallerId = userId < targetUserId ? userId : targetUserId;
    Long largerId = userId < targetUserId ? targetUserId : userId;

    // 2. 查询是否已存在（用户对 + 商品）的会话
    LambdaQueryWrapper<Conversation> queryWrapper = new LambdaQueryWrapper<>();
    queryWrapper.eq(Conversation::getUserId1, smallerId)
            .eq(Conversation::getUserId2, largerId)
            .eq(Conversation::getConversationType, 1);

    if (relatedProductId != null) {
        queryWrapper.eq(Conversation::getRelatedProductId, relatedProductId);
    } else {
        queryWrapper.isNull(Conversation::getRelatedProductId)
                .isNull(Conversation::getRelatedOrderId);
    }

    Conversation conversation = baseMapper.selectOne(queryWrapper);

    // 3. 不存在则创建新会话
    if (conversation == null) {
        conversation = new Conversation();
        conversation.setUserId1(smallerId);
        conversation.setUserId2(largerId);
        conversation.setConversationType(1);
        conversation.setRelatedProductId(relatedProductId);
        // ... 设置其他字段
        baseMapper.insert(conversation);
    }

    return conversation.getConversationId();
}
```

**findConversationByUserAndProduct**：
```java
@Override
public ConversationVO findConversationByUserAndProduct(Long userId, Long targetUserId, Long relatedProductId) {
    // 查询逻辑同上
    // 返回 ConversationVO 或 null
}
```

---

#### 2.3 Controller接口

**文件**：`ConversationController.java`

**新增REST接口**：

**接口1：创建或获取会话**
```
POST /api/conversation/create-or-update

请求参数：
- targetId: Long (必填) - 对方用户ID
- relatedProductId: Long (可选) - 关联商品ID

响应：
{
  "code": 200,
  "message": "成功",
  "data": 123  // 会话ID
}
```

**接口2：查找会话**
```
GET /api/conversation/find

请求参数：
- targetId: Long (必填) - 对方用户ID
- relatedProductId: Long (可选) - 关联商品ID

响应：
{
  "code": 200,
  "message": "成功",
  "data": {
    "conversationId": 123,
    "otherUserId": 2,
    "otherUserNickname": "张三",
    "otherUserAvatar": "http://...",
    "relatedProductId": 100,
    "relatedOrderId": null,
    ...
  }
}

错误响应（会话不存在）：
{
  "code": 404,
  "message": "会话不存在"
}
```

---

#### 2.4 实体类和VO更新

**Conversation.java**：
```java
@Schema(description = "关联商品ID")
@TableField("related_product_id")
private Long relatedProductId;
```

**ConversationVO.java**：
```java
@Schema(description = "关联的商品ID")
private Long relatedProductId;
```

---

## 业务场景示例

### 场景1：用户A向用户B咨询iPhone
```javascript
// 前端调用
const result = await createOrUpdateConversation(2, 100)  // 用户ID=2, 商品ID=100
// 返回：conversationId = 301

// 数据库记录
conversation_id=301, user_id_1=1, user_id_2=2, related_product_id=100
```

### 场景2：用户A向用户B咨询MacBook
```javascript
// 前端调用
const result = await createOrUpdateConversation(2, 200)  // 商品ID=200
// 返回：conversationId = 302

// 数据库记录
conversation_id=302, user_id_1=1, user_id_2=2, related_product_id=200
```

### 场景3：用户A和用户B普通聊天
```javascript
// 前端调用
const result = await createOrUpdateConversation(2, null)  // 无商品ID
// 返回：conversationId = 303

// 数据库记录
conversation_id=303, user_id_1=1, user_id_2=2, related_product_id=null
```

---

## 前端使用示例

### 示例1：商品详情页联系卖家
```javascript
// 商品详情页
async function contactSeller(sellerId, productId) {
  // 1. 先查找是否已有关于该商品的会话
  const conversation = await findConversationByUserAndProduct(sellerId, productId)

  if (conversation) {
    // 2. 存在会话，直接进入
    uni.navigateTo({
      url: `/pages/message/chat?conversationId=${conversation.conversationId}`
    })
  } else {
    // 3. 不存在，创建新会话
    const newConversationId = await createOrUpdateConversation(sellerId, productId)
    uni.navigateTo({
      url: `/pages/message/chat?conversationId=${newConversationId}`
    })
  }
}
```

### 示例2：直接创建会话
```javascript
// 不需要先查找，直接创建或获取
async function startChat(sellerId, productId) {
  const conversationId = await createOrUpdateConversation(sellerId, productId)
  uni.navigateTo({
    url: `/pages/message/chat?conversationId=${conversationId}`
  })
}
```

---

## 数据库查询示例

### 查询用户A和用户B关于iPhone的所有会话
```sql
SELECT
    conversation_id,
    related_product_id,
    related_order_id,
    last_message_content,
    last_message_time
FROM conversation
WHERE (user_id_1 = 1 AND user_id_2 = 2)
   OR (user_id_1 = 2 AND user_id_2 = 1)
ORDER BY last_message_time DESC;
```

### 查询用户A的所有会话（按商品分组）
```sql
SELECT
    c.conversation_id,
    c.related_product_id,
    p.title as product_title,
    u.nickname as other_user_nickname,
    c.last_message_content
FROM conversation c
LEFT JOIN product p ON c.related_product_id = p.product_id
LEFT JOIN user u ON (
    (c.user_id_1 = 1 AND u.user_id = c.user_id_2) OR
    (c.user_id_2 = 1 AND u.user_id = c.user_id_1)
)
WHERE (c.user_id_1 = 1 OR c.user_id_2 = 1)
AND c.status = 0
ORDER BY c.last_message_time DESC;
```

---

## 核心设计要点

### 1. 会话唯一性标识
**三个维度确定唯一会话**：
- 用户1（较小的ID）
- 用户2（较大的ID）
- 关联商品ID（可为NULL）

**规则**：
```
(user1=1, user2=2, product=100) ≠ (user1=1, user2=2, product=200)
(user1=1, user2=2, product=100) ≠ (user1=1, user2=2, product=null)
```

### 2. 数据一致性
- `user_id_1 < user_id_2` 保证查询的一致性
- 无论哪个用户发起会话，数据库存储格式相同
- 避免重复会话的创建

### 3. 灵活性
- 有商品ID → 关联商品的会话
- 无商品ID → 普通聊天会话
- 支持未来扩展（如关联订单）

---

## 与现有接口的对比

| 接口 | 支持多会话 | 关联商品 | 使用场景 |
|------|-----------|---------|----------|
| `POST /one-to-one` | ❌ 否 | ❌ 否 | 简单场景，确保一对用户只有一个会话 |
| `POST /create-or-update` | ✅ 是 | ✅ 是 | 电商场景，基于不同商品创建多个会话 |
| `GET /find` | ✅ 是 | ✅ 是 | 查询特定商品的会话是否存在 |

**选择建议**：
- 如果您的业务需要多会话（电商交易） → 使用 `create-or-update`
- 如果您的业务只需要单一会话（社交聊天） → 使用 `one-to-one`

---

## 测试建议

### 测试用例1：创建商品会话
```bash
curl -X POST "http://localhost:8080/api/conversation/create-or-update" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d "targetId=2&relatedProductId=100"
```

### 测试用例2：创建普通会话
```bash
curl -X POST "http://localhost:8080/api/conversation/create-or-update" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d "targetId=2"
```

### 测试用例3：查找会话
```bash
curl -X GET "http://localhost:8080/api/conversation/find?targetId=2&relatedProductId=100" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 测试用例4：重复调用应返回同一会话
```bash
# 第一次调用
curl -X POST "http://localhost:8080/api/conversation/create-or-update?targetId=2&relatedProductId=100"
# 返回：conversationId = 301

# 第二次调用（相同参数）
curl -X POST "http://localhost:8080/api/conversation/create-or-update?targetId=2&relatedProductId=100"
# 返回：conversationId = 301（相同）
```

---

## 注意事项

1. **前后端接口已统一**
   - 前端API定义在 `src/api/message.ts`
   - 后端接口路径已匹配
   - 参数名称一致

2. **数据库字段已添加**
   - `related_product_id` 字段已添加
   - 索引已创建
   - 支持NULL值

3. **向后兼容**
   - 现有的 `one-to-one` 接口仍然可用
   - 不影响现有功能
   - 新旧接口可以共存

4. **事务处理**
   - `createOrUpdateConversation` 使用 `@Transactional`
   - 确保数据一致性

---

## 后续优化建议

1. **性能优化**
   - 为 `(user_id_1, user_id_2, related_product_id)` 添加复合索引
   - 缓存用户的活跃会话列表

2. **功能扩展**
   - 支持会话合并（将多个会话合并为一个）
   - 支持会话转移（更换关联商品）
   - 添加会话搜索（按商品名称搜索）

3. **数据迁移**
   - 为现有数据补充 `related_product_id`
   - 数据清洗和验证

---

## 实现文件清单

### 修改的文件
1. ✅ `/src/main/java/com/xx/xianqijava/service/ConversationService.java`
2. ✅ `/src/main/java/com/xx/xianqijava/service/impl/ConversationServiceImpl.java`
3. ✅ `/src/main/java/com/xx/xianqijava/controller/ConversationController.java`
4. ✅ `/src/main/java/com/xx/xianqijava/entity/Conversation.java`
5. ✅ `/src/main/java/com/xx/xianqijava/vo/ConversationVO.java`

### 数据库变更
1. ✅ 添加字段 `conversation.related_product_id`
2. ✅ 添加索引 `idx_related_product_id`

### 前端文件（已存在，无需修改）
1. ✅ `/src/api/message.ts` - API定义已存在
2. ✅ 前端可以直接使用新接口

---

## 总结

✅ **功能已完整实现**，支持：
- 同一对用户基于不同商品创建多个独立会话
- 查找特定商品相关的会话
- 普通聊天（不关联商品）
- 与现有功能兼容

✅ **后端应用已重启**，新接口已生效

✅ **前端可以直接使用**，无需修改API调用代码
