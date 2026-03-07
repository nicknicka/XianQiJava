# 校园二手交易与共享平台 - 管理员后台系统设计文档

## 目录

- [一、系统概述](#一系统概述)
- [二、功能模块详细设计](#二功能模块详细设计)
- [三、数据库设计](#三数据库设计)
- [四、接口设计](#四接口设计)
- [五、页面设计](#五页面设计)
- [六、权限设计](#六权限设计)
- [七、开发指南](#七开发指南)

---

## 一、系统概述

### 1.1 系统简介

管理员后台系统是校园二手交易与共享平台的管理端，用于管理平台的所有业务数据、用户行为和系统配置。

### 1.2 技术栈

#### 后端技术栈
- **框架**: Spring Boot 4.0.2
- **安全**: Spring Security + JWT
- **持久化**: MyBatis-Plus
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **API文档**: Swagger/Knife4j

#### 前端技术栈
- **框架**: Vue 3 + TypeScript
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router
- **HTTP客户端**: Axios
- **构建工具**: Vite

### 1.3 系统特点

- **权限控制**: 基于角色的访问控制（RBAC）
- **操作审计**: 记录所有管理员操作日志
- **数据统计**: 实时数据统计和可视化
- **批量操作**: 支持批量审核、批量删除等

---

## 二、功能模块详细设计

### 2.1 登录与认证模块

#### 2.1.1 管理员登录

**功能描述**: 管理员使用用户名和密码登录后台系统

**输入参数**:
```json
{
  "username": "admin",
  "password": "123456",
  "captcha": "ABCD",
  "captchaKey": "uuid"
}
```

**返回数据**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "adminInfo": {
      "id": "1",
      "username": "admin",
      "nickname": "超级管理员",
      "avatar": "https://example.com/avatar.jpg",
      "roleId": "1",
      "roleName": "超级管理员",
      "permissions": ["*:*:*"]
    }
  }
}
```

**安全措施**:
- 密码使用 BCrypt 加密存储
- JWT Token 有效期 24 小时
- 验证码防止暴力破解
- 登录失败 5 次锁定账户 30 分钟

---

### 2.2 数据仪表盘模块

#### 2.2.1 核心数据统计

**功能描述**: 展示平台核心运营数据

**统计指标**:

| 指标名称 | 说明 | 数据源 |
|---------|------|--------|
| 总用户数 | 平台注册用户总数 | `SELECT COUNT(*) FROM user` |
| 今日新增用户 | 今日新注册用户数 | `SELECT COUNT(*) FROM user WHERE DATE(create_time) = CURDATE()` |
| 总商品数 | 平台商品总数 | `SELECT COUNT(*) FROM product` |
| 待审核商品 | 状态为待审核的商品数 | `SELECT COUNT(*) FROM product WHERE status = 0` |
| 总订单数 | 平台订单总数 | `SELECT COUNT(*) FROM orders` |
| 今日订单数 | 今日创建的订单数 | `SELECT COUNT(*) FROM orders WHERE DATE(create_time) = CURDATE()` |
| 今日交易额 | 今日订单总金额 | `SELECT SUM(amount) FROM orders WHERE DATE(create_time) = CURDATE()` |
| 总消息数 | 平台消息总数 | `SELECT COUNT(*) FROM message` |

**可视化图表**:

1. **用户增长趋势图** (折线图)
   - X轴: 日期 (最近7天/30天)
   - Y轴: 用户数量
   - 数据源:
   ```sql
   SELECT DATE(create_time) as date, COUNT(*) as count
   FROM user
   WHERE DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
   GROUP BY DATE(create_time)
   ORDER BY date;
   ```

2. **交易额统计图** (柱状图)
   - X轴: 日期
   - Y轴: 交易金额
   - 数据源:
   ```sql
   SELECT DATE(create_time) as date, SUM(total_amount) as amount
   FROM orders
   WHERE DATE(create_time) >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
   GROUP BY DATE(create_time)
   ORDER BY date;
   ```

3. **商品分类占比** (饼图)
   - 数据源:
   ```sql
   SELECT c.name, COUNT(*) as count
   FROM product p
   LEFT JOIN category c ON p.category_id = c.id
   WHERE p.status = 1
   GROUP BY c.id, c.name;
   ```

4. **订单状态分布** (环形图)
   - 待确认、进行中、已完成、已取消、已退款

5. **实时交易流水** (列表)
   - 展示最近 50 笔订单
   - 实时刷新 (每 10 秒)

#### 2.2.2 待办事项提醒

**功能描述**: 顶部导航栏显示待办事项数量角标

**待办类型**:

| 待办类型 | 查询条件 | 跳转页面 |
|---------|---------|---------|
| 待审核商品 | `status = 0` | 商品审核列表 |
| 待审核实名认证 | `status = 1` (审核中) | 实名认证审核 |
| 待审核学生认证 | `status = 1` (审核中) | 学生认证审核 |
| 待处理退款 | `status = 0` (待审核) | 退款列表 |
| 待处理举报 | `status = 0` (待处理) | 举报列表 |

**轮询机制**:
```javascript
// 每 30 秒查询一次待办数量
setInterval(() => {
  fetchPendingCounts()
}, 30000)
```

**接口返回**:
```json
{
  "code": 200,
  "data": {
    "pendingProducts": 15,
    "pendingRealNameAuth": 8,
    "pendingStudentAuth": 5,
    "pendingRefunds": 3,
    "pendingReports": 2
  }
}
```

---

### 2.3 用户管理模块

#### 2.3.1 用户列表

**功能描述**: 查询和管理平台用户

**查询条件**:
- 学号 (模糊查询)
- 手机号 (精确查询)
- 昵称 (模糊查询)
- 注册时间 (时间范围)
- 用户状态 (正常/已封禁)
- 实名认证状态 (未认证/审核中/已认证/认证失败)
- 学生认证状态 (未认证/审核中/已认证/认证失败)
- 信用等级 (优秀/良好/一般/较差)

**列表展示字段**:

| 字段名 | 说明 | 数据来源 |
|-------|------|---------|
| 用户ID | 雪花ID | user.id |
| 学号 | 学号 | user.student_id |
| 昵称 | 昵称 | user.nickname |
| 手机号 | 手机号(脱敏) | user.phone |
| 头像 | 头像URL | user.avatar |
| 实名认证 | 认证状态 | user.real_name_status |
| 学生认证 | 认证状态 | user.student_status |
| 信用等级 | 信用等级 | user.credit_level |
| 状态 | 正常/封禁 | user.is_blacklisted |
| 注册时间 | 注册时间 | user.create_time |
| 操作 | 按钮 | 封禁/解封/查看详情 |

**操作功能**:

1. **封禁用户**
   - 输入封禁原因
   - 确认封禁
   - 后端逻辑:
   ```java
   @PostMapping("/admin/user/{id}/ban")
   public Result banUser(@PathVariable String id, @RequestBody String reason) {
       // 1. 更新用户状态
       user.setIsBlacklisted(true);
       user.setBlacklistReason(reason);
       userService.updateById(user);

       // 2. 记录操作日志
       operationLogService.log("封禁用户", "封禁用户: " + user.getNickname());

       return Result.success();
   }
   ```

2. **解封用户**
   - 确认解封
   - 后端逻辑:
   ```java
   @PostMapping("/admin/user/{id}/unban")
   public Result unbanUser(@PathVariable String id) {
       user.setIsBlacklisted(false);
       user.setBlacklistReason(null);
       userService.updateById(user);
       return Result.success();
   }
   ```

3. **查看详情**
   - 弹窗显示用户详细信息
   - 包括: 基本信息、认证信息、统计数据

**用户详情数据**:
```json
{
  "basicInfo": {
    "id": "288616574015904087",
    "username": "2022035123021",
    "nickname": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "phone": "138****5678",
    "gender": "male",
    "birthday": "2003-05-15",
    "location": "广东省深圳市",
    "bio": "热爱编程的大学生",
    "creditScore": 85,
    "creditLevel": "good",
    "registerTime": "2024-01-15 10:30:00",
    "lastLoginTime": "2024-03-07 18:30:00",
    "isBlacklisted": false
  },
  "authInfo": {
    "realNameStatus": 2,  // 2-已认证
    "realName": "张三",
    "idCard": "442***********1234",
    "authTime": "2024-01-20 14:20:00",
    "studentStatus": 2,  // 2-已认证
    "studentId": "2022035123021",
    "college": "计算机学院",
    "major": "计算机科学与技术",
    "enrollmentYear": "2022"
  },
  "statistics": {
    "productCount": 15,      // 发布商品数
    "soldCount": 8,          // 已售商品数
    "orderCount": 23,        // 订单数
    "buyOrderCount": 15,     // 买订单数
    "sellOrderCount": 8,     // 卖订单数
    "favoriteCount": 45,     // 收藏数
    "followerCount": 12,     // 粉丝数
    "followingCount": 28,    // 关注数
    "evaluationCount": 18,   // 评价数
    "averageRating": 4.8     // 平均评分
  }
}
```

#### 2.3.2 实名认证审核

**功能描述**: 审核用户实名认证信息

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 用户ID | user.id |
| 昵称 | user.nickname |
| 真实姓名 | user_real_name_auth.real_name |
| 身份证号 | 脱敏显示 |
| 身份证照片 | 正反面照片 |
| 申请时间 | user_real_name_auth.create_time |
| 状态 | 0-未认证 1-审核中 2-已认证 3-认证失败 |
| 操作 | 通过/拒绝 |

**审核操作**:

1. **通过认证**
   ```json
   POST /admin/user/real-name-auth/{id}/approve
   ```

2. **拒绝认证**
   ```json
   POST /admin/user/real-name-auth/{id}/reject
   {
     "rejectReason": "身份证照片不清晰"
   }
   ```

#### 2.3.3 学生认证审核

**功能描述**: 审核用户学生认证信息

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 用户ID | user.id |
| 昵称 | user.nickname |
| 学号 | user_student_auth.student_id |
| 学院 | user_student_auth.college |
| 专业 | user_student_auth.major |
| 学生证照片 | 多张照片 |
| 申请时间 | user_student_auth.create_time |
| 状态 | 0-未认证 1-审核中 2-已认证 3-认证失败 |
| 操作 | 通过/拒绝 |

---

### 2.4 商品管理模块

#### 2.4.1 商品审核 ⭐核心功能

**功能描述**: 审核用户发布的商品

**查询条件**:
- 商品标题 (模糊查询)
- 用户昵称 (模糊查询)
- 分类
- 状态 (待审核/已通过/已拒绝)
- 发布时间 (时间范围)

**列表字段**:

| 字段名 | 说明 | 宽度 |
|-------|------|------|
| 选择 | 复选框 | 50px |
| 商品ID | 雪花ID | 120px |
| 封面图 | 缩略图 | 80px |
| 商品标题 | 标题 | 200px |
| 分类 | 分类名称 | 100px |
| 价格 | 价格(元) | 80px |
| 成色 | 成度标签 | 80px |
| 发布者 | 昵称+头像 | 120px |
| 发布时间 | 时间 | 150px |
| 状态 | 徽章 | 80px |
| 操作 | 按钮组 | 150px |

**状态标签**:
- 待审核: 灰色
- 已通过: 绿色
- 已拒绝: 红色

**操作功能**:

1. **查看详情**
   - 弹窗显示完整商品信息
   - 查看所有图片(可轮播)
   - 查看商品描述
   - 查看位置信息

2. **审核通过**
   - 单个通过
   - 批量通过
   - 接口:
   ```java
   @PostMapping("/admin/product/approve")
   public Result approveProduct(@RequestBody List<String> ids) {
       for (String id : ids) {
           Product product = productService.getById(id);
           product.setStatus(1); // 已通过
           productService.updateById(product);
       }
       return Result.success();
   }
   ```

3. **审核拒绝**
   - 单个拒绝
   - 批量拒绝
   - 必须填写拒绝原因
   - 接口:
   ```java
   @PostMapping("/admin/product/reject")
   public Result rejectProduct(
       @RequestBody Map<String, Object> params
   ) {
       List<String> ids = (List<String>) params.get("ids");
       String reason = (String) params.get("reason");

       for (String id : ids) {
           Product product = productService.getById(id);
           product.setStatus(3); // 审核未通过
           product.setRejectReason(reason);
           productService.updateById(product);

           // 发送通知给用户
           notificationService.sendProductRejectNotification(id, reason);
       }
       return Result.success();
   }
   ```

4. **删除商品**
   - 软删除 (逻辑删除)
   - 记录操作日志

**商品详情弹窗**:
```vue
<el-dialog title="商品详情" width="800px">
  <el-descriptions :column="2" border>
    <el-descriptions-item label="商品ID">
      {{ product.id }}
    </el-descriptions-item>
    <el-descriptions-item label="商品标题">
      {{ product.title }}
    </el-descriptions-item>
    <el-descriptions-item label="价格">
      ¥{{ product.price }}
    </el-descriptions-item>
    <el-descriptions-item label="成色">
      {{ product.condition }}
    </el-descriptions-item>
    <el-descriptions-item label="分类" :span="2">
      {{ product.categoryName }}
    </el-descriptions-item>
    <el-descriptions-item label="商品描述" :span="2">
      {{ product.description }}
    </el-descriptions-item>
    <el-descriptions-item label="位置" :span="2">
      {{ product.location }}
    </el-descriptions-item>
    <el-descriptions-item label="发布者">
      {{ product.userName }}
    </el-descriptions-item>
    <el-descriptions-item label="发布时间">
      {{ product.createTime }}
    </el-descriptions-item>
  </el-descriptions>

  <!-- 商品图片轮播 -->
  <el-carousel height="400px" style="margin-top: 20px">
    <el-carousel-item v-for="img in product.images" :key="img">
      <el-image :src="img" fit="contain" />
    </el-carousel-item>
  </el-carousel>
</el-dialog>
```

#### 2.4.2 商品列表

**功能描述**: 查看所有商品(包括已售、已下架)

**查询条件**: 同商品审核

**列表字段**: 同商品审核 + 增加以下字段

| 字段名 | 说明 |
|-------|------|
| 浏览次数 | view_count |
| 收藏次数 | favorite_count |
| 当前状态 | 在售/已售/已下架 |

**操作**:
- 查看详情
- 强制下架
- 删除

#### 2.4.3 分类管理

**功能描述**: 管理商品分类

**树形结构展示**:
```
一级分类
├── 二级分类
├── 二级分类
│   ├── 三级分类
│   └── 三级分类
└── 二级分类
```

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 分类ID | category.id |
| 分类名称 | category.name |
| 图标 | category.icon |
| 父分类 | parent.name |
| 排序 | category.sort_order |
| 商品数量 | 该分类下商品数 |
| 操作 | 编辑/删除 |

**操作功能**:

1. **添加分类**
   - 选择父分类
   - 输入分类名称
   - 上传分类图标
   - 设置排序值

2. **编辑分类**
   - 修改名称、图标、排序

3. **删除分类**
   - 检查是否有子分类
   - 检查是否有商品
   - 有商品则不允许删除

---

### 2.5 订单管理模块

#### 2.5.1 订单列表

**功能描述**: 查看全平台订单

**查询条件**:
- 订单号 (精确/模糊)
- 商品标题
- 买家昵称
- 卖家昵称
- 订单状态
- 创建时间 (时间范围)
- 交易金额 (区间)

**列表字段**:

| 字段名 | 说明 | 宽度 |
|-------|------|------|
| 订单ID | 雪花ID | 120px |
| 订单号 | order_no | 180px |
| 商品信息 | 图片+标题 | 250px |
| 买家 | 昵称+头像 | 100px |
| 卖家 | 昵称+头像 | 100px |
| 交易金额 | 金额(元) | 80px |
| 订单状态 | 状态标签 | 80px |
| 创建时间 | 时间 | 150px |
| 操作 | 按钮 | 120px |

**订单状态**:
- 待确认
- 待付款
- 进行中
- 已完成
- 已取消
- 退款中
- 已退款

**颜色标识**:
- 待确认: 橙色
- 进行中: 蓝色
- 已完成: 绿色
- 已取消: 灰色
- 退款中: 红色

**操作功能**:
1. 查看详情
2. 查看物流 (如果有)

#### 2.5.2 订单详情

**详情弹窗内容**:

```vue
<el-dialog title="订单详情" width="900px">
  <!-- 基本信息 -->
  <el-card header="基本信息">
    <el-descriptions :column="3" border>
      <el-descriptions-item label="订单号">
        {{ order.orderNo }}
      </el-descriptions-item>
      <el-descriptions-item label="订单状态">
        <el-tag :type="getStatusType(order.status)">
          {{ getStatusText(order.status) }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">
        {{ order.createTime }}
      </el-descriptions-item>
    </el-descriptions>
  </el-card>

  <!-- 商品信息 -->
  <el-card header="商品信息" style="margin-top: 10px">
    <div class="product-info">
      <el-image :src="order.productImage" width="80px" />
      <div class="details">
        <div class="title">{{ order.productName }}</div>
        <div class="price">¥{{ order.productPrice }}</div>
      </div>
    </div>
  </el-card>

  <!-- 买卖双方信息 -->
  <el-card header="买卖双方" style="margin-top: 10px">
    <el-row :gutter="20">
      <el-col :span="12">
        <div class="user-info">
          <el-avatar :src="order.buyerAvatar" />
          <span>{{ order.buyerName }}</span>
          <el-tag size="small">买家</el-tag>
        </div>
      </el-col>
      <el-col :span="12">
        <div class="user-info">
          <el-avatar :src="order.sellerAvatar" />
          <span>{{ order.sellerName }}</span>
          <el-tag size="small">卖家</el-tag>
        </div>
      </el-col>
    </el-row>
  </el-card>

  <!-- 收货地址 -->
  <el-card header="收货地址" style="margin-top: 10px">
    <el-descriptions :column="1" border>
      <el-descriptions-item label="联系人">
        {{ order.address.contactName }}
      </el-descriptions-item>
      <el-descriptions-item label="联系电话">
        {{ order.address.contactPhone }}
      </el-descriptions-item>
      <el-descriptions-item label="详细地址">
        {{ order.address.province }} {{ order.address.city }}
        {{ order.address.district }} {{ order.address.detail }}
      </el-descriptions-item>
    </el-descriptions>
  </el-card>

  <!-- 交易金额 -->
  <el-card header="交易金额" style="margin-top: 10px">
    <el-descriptions :column="2" border>
      <el-descriptions-item label="商品金额">
        ¥{{ order.productPrice }}
      </el-descriptions-item>
      <el-descriptions-item label="总金额">
        <span style="color: red; font-weight: bold;">
          ¥{{ order.totalAmount }}
        </span>
      </el-descriptions-item>
    </el-descriptions>
  </el-card>

  <!-- 订单备注 -->
  <el-card header="订单备注" style="margin-top: 10px" v-if="order.remark">
    <p>{{ order.remark }}</p>
  </el-card>
</el-dialog>
```

---

### 2.6 退款管理模块

#### 2.6.1 退款列表

**功能描述**: 处理用户退款申请

**查询条件**:
- 退款单号
- 订单号
- 买家昵称
- 退款状态
- 申请时间 (时间范围)

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 退款ID | refund_record.id |
| 退款单号 | refund_record.refund_no |
| 订单号 | order.order_no |
| 商品信息 | 图片+标题 |
| 买家 | 昵称+头像 |
| 卖家 | 昵称+头像 |
| 退款金额 | 金额(元) |
| 退款原因 | reason |
| 退款类型 | 仅退款/退货退款 |
| 申请时间 | apply_time |
| 状态 | 状态标签 |
| 操作 | 按钮 |

**退款状态**:
- 待审核 (橙色)
- 已同意 (蓝色)
- 已拒绝 (红色)
- 退货中 (橙色)
- 已完成 (绿色)
- 已取消 (灰色)

**操作功能**:

1. **查看详情**
   - 查看退款原因
   - 查看凭证图片
   - 查看订单信息
   - 查看买卖双方信息

2. **同意退款**
   ```java
   @PostMapping("/admin/refund/{id}/approve")
   public Result approveRefund(
       @PathVariable String id,
       @RequestBody(required = false) String remark
   ) {
       RefundRecord refund = refundService.getById(id);
       refund.setStatus(1); // 已同意
       refund.setAuditTime(new Date());
       refund.setRemark(remark);
       refundService.updateById(refund);

       // 通知买卖双方
       notificationService.sendRefundApprovedNotification(refund);

       return Result.success();
   }
   ```

3. **拒绝退款**
   - 必须填写拒绝原因
   ```java
   @PostMapping("/admin/refund/{id}/reject")
   public Result rejectRefund(
       @PathVariable String id,
       @RequestBody String rejectReason
   ) {
       RefundRecord refund = refundService.getById(id);
       refund.setStatus(2); // 已拒绝
       refund.setRejectReason(rejectReason);
       refund.setAuditTime(new Date());
       refundService.updateById(refund);

       // 通知买家
       notificationService.sendRefundRejectedNotification(refund);

       return Result.success();
   }
   ```

---

### 2.7 内容管理模块

#### 2.7.1 轮播图管理

**功能描述**: 管理首页轮播图

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 轮播图ID | banner.id |
| 标题 | banner.title |
| 图片 | banner.image (预览) |
| 链接类型 | none/product/url/page |
| 链接地址 | banner.link_url |
| 排序 | banner.sort_order |
| 状态 | 启用/禁用 |
| 创建时间 | banner.create_time |
| 操作 | 编辑/删除 |

**操作功能**:

1. **添加轮播图**
   - 上传图片 (推荐尺寸: 750x400)
   - 输入标题
   - 选择链接类型
   - 设置链接地址
   - 设置排序值

2. **编辑轮播图**
   - 修改图片、标题、链接等

3. **删除轮播图**
   - 确认删除

4. **启用/禁用**
   - 切换状态

**表单结构**:
```vue
<el-form>
  <el-form-item label="标题" required>
    <el-input v-model="form.title" />
  </el-form-item>

  <el-form-item label="图片" required>
    <el-upload
      :action="uploadUrl"
      :limit="1"
      list-type="picture-card"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>
  </el-form-item>

  <el-form-item label="链接类型" required>
    <el-radio-group v-model="form.linkType">
      <el-radio label="none">无链接</el-radio>
      <el-radio label="product">商品详情</el-radio>
      <el-radio label="url">外部链接</el-radio>
      <el-radio label="page">内部页面</el-radio>
    </el-radio-group>
  </el-form-item>

  <el-form-item
    label="链接地址"
    v-if="form.linkType !== 'none'"
    required
  >
    <el-input
      v-if="form.linkType === 'url'"
      v-model="form.linkUrl"
      placeholder="请输入完整URL，如: https://www.example.com"
    />
    <el-input
      v-else-if="form.linkType === 'product'"
      v-model="form.linkUrl"
      placeholder="请输入商品ID"
    />
    <el-select
      v-else-if="form.linkType === 'page'"
      v-model="form.linkUrl"
    >
      <el-option label="商品列表" value="/pages/market/list" />
      <el-option label="共享物品" value="/pages/share/list" />
      <el-option label="秒杀活动" value="/pages/flash-sale" />
    </el-select>
  </el-form-item>

  <el-form-item label="排序" required>
    <el-input-number
      v-model="form.sortOrder"
      :min="0"
      :max="999"
    />
    <span class="tip">数值越小越靠前</span>
  </el-form-item>

  <el-form-item label="状态">
    <el-switch v-model="form.isActive" />
  </el-form-item>
</el-form>
```

#### 2.7.2 系统通知管理

**功能描述**: 发布系统通知给用户

**通知列表字段**:

| 字段名 | 说明 |
|-------|------|
| 通知ID | notification.id |
| 标题 | notification.title |
| 内容 | notification.content (预览) |
| 类型 | system/order/message/activity |
| 发送时间 | notification.create_time |
| 已读数 | 已读用户数 |
| 总用户数 | 全部用户数 |
| 阅读率 | 已读数/总用户数 |
| 操作 | 查看/删除 |

**发布通知**:

1. **表单字段**:
   ```json
   {
     "title": "系统维护通知",
     "content": "系统将于今晚 22:00-24:00 进行维护...",
     "type": "system"
   }
   ```

2. **通知类型**:
   - system: 系统通知
   - order: 订单通知
   - message: 消息通知
   - activity: 活动通知

3. **发送逻辑**:
   ```java
   @PostMapping("/admin/notification")
   public Result sendNotification(@RequestBody SystemNotification notification) {
       // 1. 保存通知记录
       notification.setCreateTime(new Date());
       notificationService.save(notification);

       // 2. 推送给所有在线用户 (WebSocket)
       webSocketHandler.broadcastNotification(notification);

       // 3. 为所有离线用户创建未读记录
       List<User> allUsers = userService.list();
       for (User user : allUsers) {
           NotificationReadRecord record = new NotificationReadRecord();
           record.setNotificationId(notification.getId());
           record.setUserId(user.getId());
           record.setIsRead(false);
           notificationReadRecordService.save(record);
       }

       return Result.success();
   }
   ```

---

### 2.8 秒杀活动管理模块

#### 2.8.1 场次管理

**功能描述**: 创建和管理秒杀场次

**场次列表字段**:

| 字段名 | 说明 |
|-------|------|
| 场次ID | session.id |
| 场次名称 | session.name |
| 日期 | session.date |
| 开始时间 | session.start_time (HH:mm) |
| 结束时间 | session.end_time (HH:mm) |
| 状态 | 未开始/进行中/已结束 |
| 商品数 | 该场次商品数量 |
| 操作 | 编辑/删除/查看商品 |

**状态判断**:
```java
// 当前时间
LocalDateTime now = LocalDateTime.now();

// 场次开始时间
LocalDateTime startTime = LocalDateTime.of(
    session.getDate(),
    session.getStartTime()
);

// 场次结束时间
LocalDateTime endTime = LocalDateTime.of(
    session.getDate(),
    session.getEndTime()
);

// 判断状态
if (now.isBefore(startTime)) {
    status = "未开始";
} else if (now.isAfter(endTime)) {
    status = "已结束";
} else {
    status = "进行中";
}
```

**添加场次表单**:
```vue
<el-form>
  <el-form-item label="场次名称" required>
    <el-input v-model="form.name" placeholder="如: 早场、午场、晚场" />
  </el-form-item>

  <el-form-item label="日期" required>
    <el-date-picker
      v-model="form.date"
      type="date"
      placeholder="选择日期"
    />
  </el-form-item>

  <el-form-item label="开始时间" required>
    <el-time-picker
      v-model="form.startTime"
      format="HH:mm"
      placeholder="选择开始时间"
    />
  </el-form-item>

  <el-form-item label="结束时间" required>
    <el-time-picker
      v-model="form.endTime"
      format="HH:mm"
      placeholder="选择结束时间"
    />
  </el-form-item>

  <el-form-item label="状态">
    <el-switch
      v-model="form.isActive"
      active-text="启用"
      inactive-text="禁用"
    />
  </el-form-item>
</el-form>
```

#### 2.8.2 秒杀商品管理

**功能描述**: 为场次添加秒杀商品

**商品列表字段**:

| 字段名 | 说明 |
|-------|------|
| 商品ID | product.id |
| 商品图片 | product.cover_image |
| 商品标题 | product.title |
| 原价 | product.price |
| 秒杀价 | flash_sale_price |
| 库存 | flash_sale_stock |
| 已售数量 | sold_count |
| 限购数量 | limit_per_user |
| 折扣 | (秒杀价/原价)*100 |
| 折扣标签 | 8折/7折等 |
| 操作 | 编辑/删除 |

**添加秒杀商品**:

1. **选择商品**: 从已审核通过的商品中选择

2. **设置秒杀参数**:
   ```vue
   <el-form>
     <el-form-item label="选择商品" required>
       <el-select
         v-model="form.productId"
         filterable
         remote
         :remote-method="searchProducts"
         placeholder="搜索商品标题"
       >
         <el-option
           v-for="item in productOptions"
           :key="item.id"
           :label="item.title"
           :value="item.id"
         >
           <div>{{ item.title }}</div>
           <div style="color: #999; font-size: 12px">
             原价: ¥{{ item.price }}
           </div>
         </el-option>
       </el-select>
     </el-form-item>

     <el-form-item label="秒杀价格" required>
       <el-input-number
         v-model="form.flashPrice"
         :precision="2"
         :min="0.01"
         :max="form.originalPrice"
       />
       <span class="tip">
         原价: ¥{{ form.originalPrice }}
         折扣: {{ calculateDiscount(form.flashPrice, form.originalPrice) }}折
       </span>
     </el-form-item>

     <el-form-item label="秒杀库存" required>
       <el-input-number
         v-model="form.flashSaleStock"
         :min="1"
         :max="form.originalStock"
       />
       <span class="tip">原库存: {{ form.originalStock }}</span>
     </el-form-item>

     <el-form-item label="每人限购" required>
       <el-input-number
         v-model="form.limitPerUser"
         :min="1"
         :max="form.flashSaleStock"
       />
     </el-form-item>

     <el-form-item label="排序">
       <el-input-number v-model="form.sortOrder" :min="0" />
     </el-form-item>
   </el-form>
   ```

---

### 2.9 举报与反馈模块

#### 2.9.1 举报处理

**功能描述**: 处理用户举报

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 举报ID | report.id |
| 举报人 | 昵称+头像 |
| 被举报对象 | 类型+ID+名称 |
| 举报原因 | report.reason |
| 详细描述 | report.description |
| 凭证图片 | report.images |
| 举报时间 | report.create_time |
| 状态 | 待处理/已通过/已驳回 |
| 操作 | 查看/处理 |

**举报类型**:
- product: 商品
- user: 用户
- message: 消息
- comment: 评论

**处理操作**:

1. **查看详情**
   - 查看举报内容
   - 查看被举报对象详情
   - 查看举报人信息

2. **处理举报**
   ```java
   @PostMapping("/admin/report/{id}/handle")
   public Result handleReport(
       @PathVariable String id,
       @RequestBody HandleReportDTO dto
   ) {
       Report report = reportService.getById(id);

       // 1. 更新举报状态
       report.setStatus(dto.getStatus()); // 1-已通过 2-已驳回
       report.setHandleResult(dto.getResult());
       report.setHandleTime(new Date());
       reportService.updateById(report);

       // 2. 如果通过，执行相应操作
       if (dto.getStatus() == 1) {
           String targetType = report.getTargetType();
           String targetId = report.getTargetId();

           switch (targetType) {
               case "product":
                   // 下架商品
                   productService.offlineProduct(targetId);
                   break;
               case "user":
                   // 封禁用户
                   userService.banUser(targetId, "举报处理: " + report.getReason());
                   break;
               case "message":
                   // 删除消息
                   messageService.deleteMessage(targetId);
                   break;
           }
       }

       // 3. 通知举报人处理结果
       notificationService.sendReportHandleNotification(report);

       return Result.success();
   }
   ```

#### 2.9.2 用户反馈

**功能描述**: 查看和回复用户反馈

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 反馈ID | feedback.id |
| 用户 | 昵称+头像 |
| 反馈类型 | feedback.type |
| 反馈内容 | feedback.content |
| 截图 | feedback.images |
| 联系方式 | feedback.contact |
| 状态 | 待处理/处理中/已解决/已关闭 |
| 回复内容 | feedback.reply |
| 反馈时间 | feedback.create_time |
| 操作 | 查看/回复 |

**回复反馈**:
```java
@PostMapping("/admin/feedback/{id}/reply")
public Result replyFeedback(
    @PathVariable String id,
    @RequestBody String reply
) {
    UserFeedback feedback = feedbackService.getById(id);
    feedback.setReply(reply);
    feedback.setStatus(2); // 已解决
    feedback.setReplyTime(new Date());
    feedbackService.updateById(feedback);

    // 通知用户
    notificationService.sendFeedbackReplyNotification(feedback);

    return Result.success();
}
```

---

### 2.10 操作日志模块

#### 2.10.1 操作日志列表

**功能描述**: 查看所有管理员操作记录

**列表字段**:

| 字段名 | 说明 | 宽度 |
|-------|------|------|
| 日志ID | operation_log.id | 120px |
| 管理员 | admin.username | 100px |
| 操作模块 | operation_log.module | 100px |
| 操作类型 | operation_log.operation | 100px |
| 操作描述 | operation_log.description | 300px |
| IP地址 | operation_log.ip | 120px |
| 操作时间 | operation_log.create_time | 150px |
| 耗时 | operation_log.duration | 80px |

**查询条件**:
- 管理员用户名
- 操作模块 (用户/商品/订单等)
- 操作类型 (创建/更新/删除/审核等)
- 操作时间 (时间范围)
- 关键字 (操作描述)

**操作类型枚举**:
```java
public enum OperationType {
    CREATE("创建"),
    UPDATE("更新"),
    DELETE("删除"),
    APPROVE("审核通过"),
    REJECT("审核拒绝"),
    BAN("封禁"),
    UNBAN("解封"),
    LOGIN("登录"),
    LOGOUT("登出");
}
```

**日志记录方式**:
```java
@Aspect
@Component
public class OperationLogAspect {

    @Autowired
    private OperationLogService operationLogService;

    @Around("@annotation(operationLog)")
    public Object recordLog(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取当前管理员
        AdminInfo admin = SecurityContextHolder.getAdmin();

        // 执行方法
        Object result = point.proceed();

        long duration = System.currentTimeMillis() - startTime;

        // 记录日志
        OperationLog log = new OperationLog();
        log.setAdminId(admin.getId());
        log.setAdminName(admin.getUsername());
        log.setModule(operationLog.module());
        log.setOperation(operationLog.operation());
        log.setDescription(buildDescription(point, operationLog));
        log.setIp(RequestUtils.getIp());
        log.setDuration(duration);
        log.setCreateTime(new Date());

        operationLogService.save(log);

        return result;
    }
}

// 使用注解
@PostMapping("/product/approve")
@OperationLog(module = "商品管理", operation = "审核通过")
public Result approveProduct(@RequestBody List<String> ids) {
    // 业务逻辑
}
```

---

### 2.11 系统配置模块

#### 2.11.1 基础配置

**功能描述**: 配置系统基础信息

**配置项**:

| 配置键 | 说明 | 类型 | 示例值 |
|-------|------|------|--------|
| app.name | 应用名称 | string | 校园二手 |
| app.logo | 应用Logo | string | https://example.com/logo.png |
| app.icp | ICP备案号 | string | 粤ICP备12345678号 |
| service.hotline | 客服热线 | string | 400-123-4567 |
| service.email | 客服邮箱 | string | service@example.com |
| service.qq | 客服QQ | string | 123456789 |
| service.wechat | 客服微信 | string | service_wechat |

#### 2.11.2 业务配置

**功能描述**: 配置业务规则

| 配置键 | 说明 | 类型 | 默认值 |
|-------|------|------|--------|
| product.max_images | 商品最大图片数 | int | 9 |
| product.need_audit | 商品是否需要审核 | boolean | true |
| order.auto_confirm_days | 订单自动确认天数 | int | 7 |
| order.auto_complete_days | 订单自动完成天数 | int | 15 |
| order.cancel_minutes | 订单自动取消分钟数 | int | 30 |
| refund.audit_hours | 退款审核小时数 | int | 48 |

#### 2.11.3 安全配置

**功能描述**: 配置安全策略

| 配置键 | 说明 | 类型 | 默认值 |
|-------|------|------|--------|
| password.min_length | 密码最小长度 | int | 6 |
| password.max_length | 密码最大长度 | int | 20 |
| token.expire_hours | Token过期小时数 | int | 24 |
| login.max_attempts | 最大登录尝试次数 | int | 5 |
| login.lock_minutes | 登录锁定分钟数 | int | 30 |
| sensitive.enabled | 敏感词过滤开关 | boolean | true |

**配置接口**:
```java
@RestController
@RequestMapping("/admin/config")
public class AdminConfigController {

    @GetMapping("/group/{group}")
    public Result getConfigGroup(@PathVariable String group) {
        List<SystemConfig> configs = systemConfigService.list(
            new QueryWrapper<SystemConfig>()
                .eq("config_group", group)
        );

        Map<String, Object> result = new HashMap<>();
        for (SystemConfig config : configs) {
            result.put(config.getConfigKey(), parseValue(config));
        }

        return Result.success(result);
    }

    @PostMapping("/update")
    @OperationLog(module = "系统配置", operation = "更新配置")
    public Result updateConfig(@RequestBody Map<String, Object> params) {
        params.forEach((key, value) -> {
            SystemConfig config = systemConfigService.getByKey(key);
            config.setConfigValue(String.valueOf(value));
            systemConfigService.updateById(config);
        });

        // 刷新Redis缓存
        systemConfigService.refreshCache();

        return Result.success();
    }
}
```

---

### 2.12 管理员管理模块

#### 2.12.1 管理员账号管理

**功能描述**: 管理管理员账号（简化版，只有管理员一个角色）

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 管理员ID | admin.id |
| 用户名 | admin.username |
| 昵称 | admin.nickname |
| 头像 | admin.avatar |
| 状态 | 启用/禁用 |
| 最后登录时间 | admin.last_login_time |
| 最后登录IP | admin.last_login_ip |
| 创建时间 | admin.create_time |
| 操作 | 编辑/删除/重置密码 |

**添加/编辑管理员**:
```vue
<el-form>
  <el-form-item label="用户名" required>
    <el-input
      v-model="form.username"
      placeholder="请输入用户名"
      :disabled="isEdit"
    />
  </el-form-item>

  <el-form-item label="密码" :required="!isEdit">
    <el-input
      v-model="form.password"
      type="password"
      :placeholder="isEdit ? '不填写则不修改密码' : '请输入密码(6-20位)'"
      show-password
    />
  </el-form-item>

  <el-form-item label="昵称" required>
    <el-input v-model="form.nickname" />
  </el-form-item>

  <el-form-item label="头像">
    <el-upload
      :action="uploadUrl"
      :show-file-list="false"
      :on-success="handleAvatarSuccess"
    >
      <img v-if="form.avatar" :src="form.avatar" class="avatar" />
      <el-icon v-else><Plus /></el-icon>
    </el-upload>
  </el-form-item>

  <el-form-item label="状态">
    <el-switch
      v-model="form.isActive"
      active-text="启用"
      inactive-text="禁用"
    />
  </el-form-item>
</el-form>
```

**操作功能**:

1. **创建管理员**
   ```java
   @PostMapping("/admin/admin/create")
   public Result createAdmin(@RequestBody CreateAdminDTO dto) {
       // 1. 检查用户名是否重复
       if (adminService.getByUsername(dto.getUsername()) != null) {
           return Result.error("用户名已存在");
       }

       // 2. 密码加密
       String encodedPassword = passwordEncoder.encode(dto.getPassword());

       // 3. 创建管理员
       Admin admin = new Admin();
       admin.setUsername(dto.getUsername());
       admin.setPassword(encodedPassword);
       admin.setNickname(dto.getNickname());
       admin.setAvatar(dto.getAvatar());
       admin.setIsActive(true);
       adminService.save(admin);

       return Result.success();
   }
   ```

2. **更新管理员**
   ```java
   @PutMapping("/admin/admin/{id}")
   public Result updateAdmin(
       @PathVariable String id,
       @RequestBody UpdateAdminDTO dto
   ) {
       Admin admin = adminService.getById(id);

       if (StringUtils.isNotBlank(dto.getPassword())) {
           admin.setPassword(passwordEncoder.encode(dto.getPassword()));
       }
       admin.setNickname(dto.getNickname());
       admin.setAvatar(dto.getAvatar());
       admin.setIsActive(dto.getIsActive());

       adminService.updateById(admin);
       return Result.success();
   }
   ```

3. **重置密码**
   ```java
   @PostMapping("/admin/admin/{id}/reset-password")
   public Result resetPassword(
       @PathVariable String id,
       @RequestBody String newPassword
   ) {
       Admin admin = adminService.getById(id);
       admin.setPassword(passwordEncoder.encode(newPassword));
       adminService.updateById(admin);
       return Result.success();
   }
   ```

---

### 2.13 共享物品管理模块

#### 2.13.1 共享物品审核

**功能描述**: 审核用户发布的共享物品

**查询条件**:
- 物品标题 (模糊查询)
- 发布者昵称 (模糊查询)
- 分类
- 状态 (待审核/已通过/已拒绝)
- 发布时间 (时间范围)

**列表字段**:

| 字段名 | 说明 | 宽度 |
|-------|------|------|
| 选择 | 复选框 | 50px |
| 物品ID | share_item.id | 120px |
| 封面图 | 缩略图 | 80px |
| 物品标题 | 标题 | 200px |
| 分类 | 分类名称 | 100px |
| 日租金 | 日租金(元) | 80px |
| 押金 | 押金(元) | 80px |
| 发布者 | 昵称+头像 | 120px |
| 发布时间 | 时间 | 150px |
| 状态 | 徽章 | 80px |
| 操作 | 按钮组 | 150px |

**状态标签**:
- 待审核: 灰色
- 已通过: 绿色
- 已拒绝: 红色

**操作功能**:

1. **查看详情**
   - 弹窗显示完整信息
   - 查看所有图片
   - 查看可借用时间
   - 查看位置信息

2. **审核通过**
   ```java
   @PostMapping("/admin/share-item/approve")
   public Result approveShareItem(@RequestBody List<String> ids) {
       for (String id : ids) {
           ShareItem item = shareItemService.getById(id);
           item.setStatus(1); // 已通过
           shareItemService.updateById(item);

           // 通知用户
           notificationService.sendShareItemApprovedNotification(id);
       }
       return Result.success();
   }
   ```

3. **审核拒绝**
   ```java
   @PostMapping("/admin/share-item/reject")
   public Result rejectShareItem(
       @RequestBody Map<String, Object> params
   ) {
       List<String> ids = (List<String>) params.get("ids");
       String reason = (String) params.get("reason");

       for (String id : ids) {
           ShareItem item = shareItemService.getById(id);
           item.setStatus(3); // 审核未通过
           item.setRejectReason(reason);
           shareItemService.updateById(item);

           // 通知用户
           notificationService.sendShareItemRejectedNotification(id, reason);
       }
       return Result.success();
   }
   ```

#### 2.13.2 借用预约管理

**功能描述**: 查看和管理共享物品借用预约

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 预约ID | booking.id |
| 共享物品 | 物品标题+图片 |
| 借用者 | 昵称+头像 |
| 开始时间 | start_date |
| 结束时间 | end_date |
| 借用天数 | 天数 |
| 总金额 | 押金+租金 |
| 状态 | 待审批/已批准/已拒绝/使用中/已完成/已取消 |
| 申请时间 | create_time |
| 操作 | 查看/处理 |

**状态说明**:
- 待批准: 用户刚提交申请
- 已批准: 管理员同意借用
- 已拒绝: 管理员拒绝借用
- 使用中: 物品正在借用
- 已完成: 已归还物品
- 已取消: 用户取消申请

**处理预约**:
```java
@PostMapping("/admin/share-item/booking/{id}/approve")
public Result approveBooking(@PathVariable String id) {
    ShareItemBooking booking = bookingService.getById(id);
    booking.setStatus(2); // 已批准
    bookingService.updateById(booking);

    // 通知借用者
    notificationService.sendBookingApprovedNotification(booking);

    return Result.success();
}

@PostMapping("/admin/share-item/booking/{id}/reject")
public Result rejectBooking(
    @PathVariable String id,
    @RequestBody String reason
) {
    ShareItemBooking booking = bookingService.getById(id);
    booking.setStatus(3); // 已拒绝
    booking.setRejectReason(reason);
    bookingService.updateById(booking);

    // 通知借用者
    notificationService.sendBookingRejectedNotification(booking, reason);

    return Result.success();
}
```

#### 2.13.3 转赠记录管理

**功能描述**: 查看共享物品转赠记录

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 转赠ID | transfer.id |
| 共享物品 | 物品标题+图片 |
| 转赠者 | 原持有者 |
| 接收者 | 新持有者 |
| 转赠时间 | transfer_time |
| 备注 | remark |

---

### 2.14 黑名单管理模块

#### 2.14.1 黑名单列表

**功能描述**: 查看和管理用户黑名单

**查询条件**:
- 拉黑用户昵称 (模糊查询)
- 被拉黑用户昵称 (模糊查询)
- 拉黑时间 (时间范围)

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 记录ID | blacklist.id |
| 拉黑用户 | 昵称+头像 |
| 被拉黑用户 | 昵称+头像 |
| 拉黑原因 | blacklist.reason |
| 拉黑时间 | blacklist.create_time |
| 操作 | 解除黑名单 |

**解除黑名单**:
```java
@DeleteMapping("/admin/blacklist/{id}")
public Result removeFromBlacklist(@PathVariable String id) {
    Blacklist blacklist = blacklistService.getById(id);
    blacklistService.removeById(id);

    // 通知被拉黑用户
    notificationService.sendRemoveFromBlacklistNotification(
        blacklist.getBlockedUserId()
    );

    return Result.success();
}
```

#### 2.14.2 黑名单统计

**功能描述**: 统计黑名单数据

**统计指标**:
- 黑名单总数
- 本月新增黑名单
- 被拉黑次数最多的用户 TOP10
- 拉黑原因分布

---

### 2.15 数据报表导出模块

#### 2.15.1 用户数据导出

**功能描述**: 导出用户数据报表

**导出字段**:

| 字段名 | 说明 |
|-------|------|
| 用户ID | user.id |
| 学号 | user.student_id |
| 昵称 | user.nickname |
| 手机号 | user.phone |
| 性别 | user.gender |
| 位置 | user.location |
| 信用分 | user.credit_score |
| 实名认证状态 | user.real_name_status |
| 学生认证状态 | user.student_status |
| 商品数 | 发布商品数 |
| 订单数 | 交易订单数 |
| 注册时间 | user.create_time |

**导出接口**:
```java
@GetMapping("/admin/report/export/users")
public void exportUsers(HttpServletResponse response) throws IOException {
    // 1. 查询数据
    List<UserExportVO> users = adminReportService.getAllUsersForExport();

    // 2. 创建Excel工作簿
    Workbook workbook = ExcelExportUtil.exportUsers(users);

    // 3. 设置响应头
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setCharacterEncoding("utf-8");

    String fileName = URLEncoder.encode("用户数据_" + DateUtil.format(new Date(), "yyyyMMdd"), "UTF-8");
    response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

    // 4. 写入响应
    ServletOutputStream out = response.getOutputStream();
    workbook.write(out);
    out.flush();
    out.close();
}
```

#### 2.15.2 交易数据导出

**功能描述**: 导出交易订单报表

**导出字段**:

| 字段名 | 说明 |
|-------|------|
| 订单ID | order.id |
| 订单号 | order.order_no |
| 商品标题 | product.title |
| 商品价格 | product.price |
| 买家昵称 | buyer.nickname |
| 买家学号 | buyer.student_id |
| 卖家昵称 | seller.nickname |
| 卖家学号 | seller.student_id |
| 交易金额 | order.total_amount |
| 订单状态 | order.status |
| 创建时间 | order.create_time |
| 完成时间 | order.complete_time |

**查询条件**:
- 时间范围 (必选)
- 订单状态 (可选)
- 交易金额区间 (可选)

#### 2.15.3 商品数据导出

**功能描述**: 导出商品数据报表

**导出字段**:

| 字段名 | 说明 |
|-------|------|
| 商品ID | product.id |
| 商品标题 | product.title |
| 分类 | category.name |
| 价格 | product.price |
| 成色 | product.condition |
| 发布者 | user.nickname |
| 浏览次数 | product.view_count |
| 收藏次数 | product.favorite_count |
| 状态 | product.status |
| 发布时间 | product.create_time |
| 是否售出 | is_sold |

#### 2.15.4 导出记录

**功能描述**: 查看数据导出记录

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 导出ID | export_log.id |
| 报表类型 | 用户/交易/商品 |
| 导出人 | admin.username |
| 导出时间 | export_time |
| 数据行数 | row_count |
| 文件大小 | file_size |
| 操作 | 下载 |

---

### 2.16 消息与评价管理模块

#### 2.16.1 消息管理

**功能描述**: 管理平台消息

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 消息ID | message.id |
| 会话ID | conversation.id |
| 发送者 | 发送者昵称 |
| 接收者 | 接收者昵称 |
| 消息类型 | 文本/图片/商品卡片/订单卡片 |
| 消息内容 | 内容预览 |
| 发送时间 | send_time |
| 状态 | 正常/已撤回 |
| 操作 | 查看/删除 |

**操作功能**:
- 查看消息详情
- 删除违规消息
- 撤回敏感消息

#### 2.16.2 评价管理

**功能描述**: 管理用户评价

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 评价ID | evaluation.id |
| 订单号 | order.order_no |
| 商品标题 | product.title |
| 评价者 | from_user.nickname |
| 被评价者 | to_user.nickname |
| 评分 | 1-5星 |
| 评价内容 | content |
| 图片 | images |
| 评价时间 | create_time |
| 状态 | 正常/已删除 |
| 操作 | 查看/删除 |

**删除评价**:
```java
@DeleteMapping("/admin/evaluation/{id}")
public Result deleteEvaluation(@PathVariable String id) {
    Evaluation evaluation = evaluationService.getById(id);

    // 1. 逻辑删除
    evaluation.setIsDeleted(true);
    evaluationService.updateById(evaluation);

    // 2. 重新计算被评价者的信用分
    userService.recalculateCredit(evaluation.getToUserId());

    // 3. 记录操作日志
    operationLogService.log("删除评价", "删除评价ID: " + id);

    return Result.success();
}
```

---

### 2.17 内容辅助管理模块

#### 2.17.1 热门标签管理

**功能描述**: 管理首页热门搜索标签

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 标签ID | hot_tag.id |
| 关键词 | hot_tag.keyword |
| 点击次数 | hot_tag.click_count |
| 排序 | hot_tag.sort_order |
| 状态 | 启用/禁用 |
| 操作 | 编辑/删除 |

**操作功能**:

1. **添加标签**
   ```java
   @PostMapping("/admin/hot-tag")
   public Result createHotTag(@RequestBody HotTag tag) {
       tag.setCreateTime(new Date());
       hotTagService.save(tag);
       return Result.success();
   }
   ```

2. **更新点击次数**
   ```java
   @PostMapping("/hot-tag/{id}/click")
   public Result incrementClick(@PathVariable String id) {
       HotTag tag = hotTagService.getById(id);
       tag.setClickCount(tag.getClickCount() + 1);
       hotTagService.updateById(tag);
       return Result.success();
   }
   ```

#### 2.17.2 快捷回复管理

**功能描述**: 管理系统快捷回复模板

**列表字段**:

| 字段名 | 说明 |
|-------|------|
| 回复ID | quick_reply.id |
| 回复内容 | quick_reply.content |
| 分类 | category |
| 类型 | 系统/个人 |
| 排序 | sort_order |
| 使用次数 | use_count |
| 创建时间 | create_time |
| 操作 | 编辑/删除 |

**操作功能**:

1. **添加快捷回复**
   ```java
   @PostMapping("/admin/quick-reply")
   public Result createQuickReply(@RequestBody QuickReply reply) {
       reply.setCreateTime(new Date());
       reply.setIsSystem(true);
       quickReplyService.save(reply);
       return Result.success();
   }
   ```

2. **更新快捷回复**
   ```java
   @PutMapping("/admin/quick-reply/{id}")
   public Result updateQuickReply(
       @PathVariable String id,
       @RequestBody QuickReply reply
   ) {
       reply.setId(id);
       quickReplyService.updateById(reply);
       return Result.success();
   }
   ```

---

### 2.18 系统监控模块

#### 2.18.1 在线用户监控

**功能描述**: 实时监控在线用户

**统计指标**:
- 当前在线用户数
- 今日活跃用户数
- WebSocket连接数

**在线用户列表**:

| 字段名 | 说明 |
|-------|------|
| 用户ID | user.id |
| 昵称 | user.nickname |
| 头像 | user.avatar |
| 在线时长 | online_duration |
| 最后活动时间 | last_active_time |
| 当前页面 | current_page |

#### 2.18.2 系统异常监控

**功能描述**: 监控系统异常和错误

**异常列表**:

| 字段名 | 说明 |
|-------|------|
| 异常ID | error_log.id |
| 异常类型 | ERROR/WARN |
| 异常信息 | error_message |
| 发生时间 | occur_time |
| 请求路径 | request_url |
| 请求参数 | request_params |
| 操作 | 查看详情 |

**异常统计**:
- 今日异常总数
- 异常类型分布
- 异常趋势图

---

## 三、数据库设计

### 3.1 管理员表 (admin)

**简化版设计 - 只需要一个管理员表**

```sql
CREATE TABLE `admin` (
  `id` BIGINT NOT NULL COMMENT '管理员ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `avatar` VARCHAR(255) COMMENT '头像',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用 0-禁用 1-启用',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录IP',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';
```

**权限控制逻辑**:
```java
// 简单的权限判断 - 只需要判断是否是管理员
public class AdminPermissionService {

    public boolean isAdmin(String token) {
        // 1. 从token中获取管理员ID
        String adminId = JwtUtil.getAdminIdFromToken(token);

        // 2. 查询管理员是否存在且启用
        Admin admin = adminService.getById(adminId);
        return admin != null && admin.getIsActive();
    }
}
```

**拦截器**:
```java
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) {
        // 1. 获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token)) {
            throw new UnauthorizedException("未登录");
        }

        // 2. 验证token
        if (!JwtUtil.validateToken(token)) {
            throw new UnauthorizedException("Token无效");
        }

        // 3. 验证管理员身份
        String adminId = JwtUtil.getAdminIdFromToken(token);
        Admin admin = adminService.getById(adminId);
        if (admin == null || !admin.getIsActive()) {
            throw new UnauthorizedException("管理员身份无效");
        }

        // 4. 设置到上下文
        SecurityContextHolder.setAdmin(admin);

        return true;
    }
}
```

### 3.2 操作日志表 (operation_log)

```sql
CREATE TABLE `operation_log` (
  `id` BIGINT NOT NULL COMMENT '日志ID',
  `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
  `admin_name` VARCHAR(50) NOT NULL COMMENT '管理员用户名',
  `module` VARCHAR(50) NOT NULL COMMENT '操作模块',
  `operation` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `description` VARCHAR(500) NOT NULL COMMENT '操作描述',
  `method` VARCHAR(200) COMMENT '执行方法',
  `params` TEXT COMMENT '请求参数',
  `ip` VARCHAR(50) COMMENT 'IP地址',
  `location` VARCHAR(100) COMMENT 'IP归属地',
  `browser` VARCHAR(100) COMMENT '浏览器',
  `os` VARCHAR(100) COMMENT '操作系统',
  `status` TINYINT DEFAULT 1 COMMENT '状态 0-失败 1-成功',
  `error_msg` VARCHAR(500) COMMENT '错误信息',
  `duration` BIGINT COMMENT '执行耗时(毫秒)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_module` (`module`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
```

### 3.3 登录日志表 (admin_login_log)

```sql
CREATE TABLE `admin_login_log` (
  `id` BIGINT NOT NULL COMMENT '日志ID',
  `admin_id` BIGINT COMMENT '管理员ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `login_time` DATETIME NOT NULL COMMENT '登录时间',
  `ip` VARCHAR(50) COMMENT 'IP地址',
  `location` VARCHAR(100) COMMENT 'IP归属地',
  `browser` VARCHAR(100) COMMENT '浏览器',
  `os` VARCHAR(100) COMMENT '操作系统',
  `status` TINYINT DEFAULT 1 COMMENT '登录状态 0-失败 1-成功',
  `message` VARCHAR(255) COMMENT '提示信息',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员登录日志表';
```

### 3.4 数据导出记录表 (export_log)

```sql
CREATE TABLE `export_log` (
  `id` BIGINT NOT NULL COMMENT '导出ID',
  `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
  `admin_name` VARCHAR(50) NOT NULL COMMENT '管理员用户名',
  `report_type` VARCHAR(50) NOT NULL COMMENT '报表类型: user/order/product',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件路径',
  `row_count` INT NOT NULL COMMENT '数据行数',
  `file_size` BIGINT NOT NULL COMMENT '文件大小(字节)',
  `start_time` DATETIME NOT NULL COMMENT '开始时间',
  `end_time` DATETIME NOT NULL COMMENT '结束时间',
  `duration` BIGINT COMMENT '导出耗时(毫秒)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据导出记录表';
```

---

## 四、接口设计

### 4.1 认证接口

#### 4.1.1 管理员登录

```
POST /admin/auth/login
```

**请求参数**:
```json
{
  "username": "admin",
  "password": "123456",
  "captcha": "ABCD",
  "captchaKey": "uuid"
}
```

**返回数据**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "adminInfo": {
      "id": "1",
      "username": "admin",
      "nickname": "超级管理员",
      "avatar": "https://example.com/avatar.jpg"
    }
  }
}
```

#### 4.1.2 获取管理员信息

```
GET /admin/auth/info
```

**请求头**:
```
Authorization: Bearer {token}
```

**返回数据**:
```json
{
  "code": 200,
  "data": {
    "id": "1",
    "username": "admin",
    "nickname": "超级管理员",
    "avatar": "https://example.com/avatar.jpg",
    "lastLoginTime": "2024-03-07 18:30:00",
    "lastLoginIp": "192.168.1.100"
  }
}
```

#### 4.1.3 退出登录

```
POST /admin/auth/logout
```

**返回数据**:
```json
{
  "code": 200,
  "message": "退出成功"
}
```

---

### 4.2 共享物品管理接口

#### 4.2.1 共享物品审核列表

```
GET /admin/share-item/pending-list
```

**请求参数**:
```
page: 1
pageSize: 10
title: 手机       // 物品标题(模糊)
nickname: 张      // 发布者(模糊)
categoryId: 1     // 分类
status: 0         // 状态
startTime: 2024-01-01
endTime: 2024-03-07
```

**返回数据**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": "288616574015904087",
        "title": "MacBook Pro 2021",
        "coverImage": "https://example.com/image.jpg",
        "dailyRent": 50.00,
        "deposit": 2000.00,
        "categoryName": "电脑",
        "userName": "张三",
        "userAvatar": "https://example.com/avatar.jpg",
        "userId": "288616574015904088",
        "status": 0,
        "createTime": "2024-03-07 10:30:00"
      }
    ],
    "total": 45
  }
}
```

#### 4.2.2 审核通过

```
POST /admin/share-item/approve
```

**请求参数**:
```json
{
  "ids": ["288616574015904087", "288616574015904088"]
}
```

#### 4.2.3 审核拒绝

```
POST /admin/share-item/reject
```

**请求参数**:
```json
{
  "ids": ["288616574015904087"],
  "reason": "物品描述与实物不符"
}
```

---

### 4.3 黑名单管理接口

#### 4.3.1 黑名单列表

```
GET /admin/blacklist/list
```

**请求参数**:
```
page: 1
pageSize: 10
userName: 张        // 拉黑用户(模糊)
blockedUserName: 李 // 被拉黑用户(模糊)
startTime: 2024-01-01
endTime: 2024-03-07
```

**返回数据**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": "1",
        "userId": "288616574015904087",
        "userName": "张三",
        "userAvatar": "https://example.com/user1.jpg",
        "blockedUserId": "288616574015904088",
        "blockedUserName": "李四",
        "blockedUserAvatar": "https://example.com/user2.jpg",
        "reason": "发布违规商品",
        "createTime": "2024-03-07 10:30:00"
      }
    ],
    "total": 123
  }
}
```

#### 4.3.2 解除黑名单

```
DELETE /admin/blacklist/{id}
```

---

### 4.4 数据报表导出接口

#### 4.4.1 导出用户数据

```
GET /admin/report/export/users
```

**请求参数**:
```
startTime: 2024-01-01  // 必填
endTime: 2024-03-07     // 必填
realNameStatus: 2       // 可选: 实名认证状态
studentStatus: 2        // 可选: 学生认证状态
```

**返回**: Excel文件流

#### 4.4.2 导出交易数据

```
GET /admin/report/export/orders
```

**请求参数**:
```
startTime: 2024-01-01  // 必填
endTime: 2024-03-07     // 必填
status: 1              // 可选: 订单状态
minAmount: 100          // 可选: 最小金额
maxAmount: 10000        // 可选: 最大金额
```

**返回**: Excel文件流

#### 4.4.3 导出商品数据

```
GET /admin/report/export/products
```

**请求参数**:
```
startTime: 2024-01-01  // 必填
endTime: 2024-03-07     // 必填
categoryId: 1          // 可选: 分类
status: 1              // 可选: 状态
```

**返回**: Excel文件流

#### 4.4.4 导出记录列表

```
GET /admin/report/export-logs
```

**返回数据**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": "1",
        "reportType": "user",
        "fileName": "用户数据_20240307.xlsx",
        "filePath": "/exports/2024/03/07/user_data_20240307.xlsx",
        "rowCount": 1523,
        "fileSize": 2048576,
        "adminName": "admin",
        "duration": 2536,
        "createTime": "2024-03-07 10:30:00"
      }
    ],
    "total": 89
  }
}
```

---

### 4.5 评价管理接口

#### 4.5.1 评价列表

```
GET /admin/evaluation/list
```

**请求参数**:
```
page: 1
pageSize: 10
orderId: 2024        // 订单号(模糊)
productTitle: 手机    // 商品标题(模糊)
fromUserName: 张      // 评价者(模糊)
toUserName: 李         // 被评价者(模糊)
rating: 5             // 评分
startTime: 2024-01-01
endTime: 2024-03-07
```

**返回数据**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": "288616574015904087",
        "orderId": "288616574015904088",
        "orderNo": "202403071030001234",
        "productId": "288616574015904089",
        "productTitle": "iPhone 13 Pro",
        "productImage": "https://example.com/image.jpg",
        "fromUserId": "288616574015904090",
        "fromUserName": "张三",
        "fromUserAvatar": "https://example.com/user1.jpg",
        "toUserId": "288616574015904091",
        "toUserName": "李四",
        "toUserAvatar": "https://example.com/user2.jpg",
        "rating": 5,
        "content": "商品很新，卖家态度很好",
        "tags": ["发货快", "包装好"],
        "images": ["https://example.com/eval1.jpg"],
        "createTime": "2024-03-07 10:30:00"
      }
    ],
    "total": 1523
  }
}
```

#### 4.5.2 删除评价

```
DELETE /admin/evaluation/{id}
```

---

### 4.6 热门标签接口

#### 4.6.1 热门标签列表

```
GET /admin/hot-tag/list
```

**返回数据**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": "1",
        "keyword": "iPhone",
        "clickCount": 1523,
        "sortOrder": 1,
        "isActive": true,
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 20
  }
}
```

#### 4.6.2 添加热门标签

```
POST /admin/hot-tag
```

**请求参数**:
```json
{
  "keyword": "iPhone",
  "sortOrder": 1,
  "isActive": true
}
```

#### 4.6.3 更新热门标签

```
PUT /admin/hot-tag/{id}
```

#### 4.6.4 删除热门标签

```
DELETE /admin/hot-tag/{id}
```

---

### 4.7 快捷回复接口

#### 4.7.1 快捷回复列表

```
GET /admin/quick-reply/list
```

**返回数据**:
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": "1",
        "content": "您好，请问商品还在吗？",
        "category": "咨询",
        "isSystem": true,
        "sortOrder": 1,
        "useCount": 523,
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 15
  }
}
```

#### 4.7.2 添加快捷回复

```
POST /admin/quick-reply
```

**请求参数**:
```json
{
  "content": "好的，收到",
  "category": "确认",
  "sortOrder": 2
}
```

#### 4.7.3 更新快捷回复

```
PUT /admin/quick-reply/{id}
```

#### 4.7.4 删除快捷回复

```
DELETE /admin/quick-reply/{id}
```

---

## 五、页面设计

### 5.1 整体布局

```
┌─────────────────────────────────────────────────────┐
│  Logo    顶部导航   [管理员]     [退出]              │
├──────┬──────────────────────────────────────────────┤
│      │                                              │
│ 侧边 │              主内容区域                      │
│导航  │                                              │
│      │                                              │
│      │                                              │
│      │                                              │
└──────┴──────────────────────────────────────────────┘
```

### 5.2 侧边导航菜单（简化版）

```
系统管理
  └── 管理员管理

数据统计
  ├── 数据仪表盘
  └── 数据报表

用户管理
  ├── 用户列表
  ├── 实名认证
  └── 学生认证

商品管理
  ├── 商品审核 ⭐
  ├── 商品列表
  └── 分类管理

共享物品
  ├── 物品审核 ⭐
  ├── 借用预约
  └── 转赠记录

订单管理
  ├── 订单列表
  └── 退款管理

内容管理
  ├── 轮播图管理
  ├── 系统通知
  ├── 热门标签
  └── 快捷回复

消息评价
  ├── 消息管理
  └── 评价管理

举报反馈
  ├── 举报处理
  └── 用户反馈

用户关系
  └── 黑名单管理

系统监控
  ├── 在线用户
  └── 系统异常

系统配置
  ├── 基础配置
  ├── 业务配置
  └── 安全配置

日志管理
  ├── 操作日志
  └── 登录日志
```

### 5.3 核心页面设计

#### 5.3.1 共享物品审核页面

```
┌─────────────────────────────────────────────────────┐
│  共享物品审核          [批量通过] [批量拒绝]        │
├─────────────────────────────────────────────────────┤
│  筛选: [物品标题] [分类▼] [状态▼] [时间范围] [搜索]│
├─────────────────────────────────────────────────────┤
│☐ │ 封面 │ 标题        │ 日租金│ 押金 │ 发布者│ 操作│
│☐ │ 图片 │ MacBook Pro │ 50   │ 2000 │ 张三  │[详情]│
│  │      │ 2021款      │      │      │        │[通过]│
│  │      │             │      │      │        │[拒绝]│
├─────────────────────────────────────────────────────┤
│  共 45 条   [<] 1 / 5 [>]                          │
└─────────────────────────────────────────────────────┘
```

#### 5.3.2 数据报表导出页面

```
┌─────────────────────────────────────────────────────┐
│  数据报表导出                                        │
├─────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  │
│  │  用户数据   │  │  交易数据   │  │  商品数据   │  │
│  │  [导出Excel]│  │  [导出Excel]│  │  [导出Excel]│  │
│  └─────────────┘  └─────────────┘  └─────────────┘  │
├─────────────────────────────────────────────────────┤
│  导出记录                                            │
│  ┌───────────────────────────────────────────────┐  │
│  │ 文件名          │ 导出人 │ 导出时间  │ 下载    │  │
│  │ 用户数据_0307   │ admin  │ 03-07 10:30│[下载]  │  │
│  │ 交易数据_0306   │ admin  │ 03-06 15:20│[下载]  │  │
│  └───────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────┘
```

---

## 六、权限设计（简化版）

### 6.1 权限控制原则

**简化方案**:
- 只有两个角色：普通用户、管理员
- 所有 `/admin/*` 接口都需要管理员权限
- 通过JWT Token验证管理员身份
- 不需要复杂的RBAC系统

### 6.2 后端权限实现

#### 6.2.1 自定义注解（可选，简化版）

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminAuth {
    String value() default "";  // 描述
}
```

#### 6.2.2 权限拦截器

```java
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {

        // 1. 检查是否是管理员接口
        String uri = request.getRequestURI();
        if (!uri.startsWith("/admin/")) {
            return true;  // 非管理员接口，放行
        }

        // 2. 获取token
        String token = request.getHeader("Authorization");
        if (StringUtils.isBlank(token) || !token.startsWith("Bearer ")) {
            throw new UnauthorizedException("未登录");
        }

        token = token.substring(7);  // 去掉 "Bearer " 前缀

        // 3. 验证token
        try {
            Claims claims = JwtUtil.parseToken(token);
            String adminId = claims.getSubject();

            // 4. 查询管理员
            Admin admin = adminService.getById(adminId);
            if (admin == null || !admin.getIsActive()) {
                throw new UnauthorizedException("管理员身份无效");
            }

            // 5. 设置到上下文
            SecurityContextHolder.setAdmin(admin);

            return true;

        } catch (Exception e) {
            throw new UnauthorizedException("Token无效或已过期");
        }
    }
}
```

#### 6.2.3 配置拦截器

```java
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")  // 管理员接口都需要认证
                .excludePathPatterns("/admin/auth/login")  // 排除登录接口
                .excludePathPatterns("/admin/captcha/**"); // 排除验证码接口
    }
}
```

### 6.3 前端权限控制

#### 6.3.1 路由守卫

```typescript
// router/index.ts
router.beforeEach((to, from, next) => {
  const adminStore = useAdminStore()
  const token = adminStore.token

  // 访问管理员页面，需要登录
  if (to.path.startsWith('/admin')) {
    if (!token) {
      next('/admin/login')
      return
    }
  }

  next()
})
```

#### 6.3.2 Axios拦截器

```typescript
// utils/request.ts
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useAdminStore } from '@/store/admin'

const service = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const adminStore = useAdminStore()
    if (adminStore.token) {
      config.headers.Authorization = `Bearer ${adminStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')

      // Token过期，跳转登录
      if (res.code === 401) {
        const adminStore = useAdminStore()
        adminStore.logout()
        window.location.href = '/admin/login'
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }

    return res.data
  },
  (error) => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service
```

---

## 七、开发指南

### 7.1 后端项目结构（简化版）

```
XianQiJava/
├── src/main/java/com/xx/xianqijava/
│   ├── controller/
│   │   └── admin/                 # 管理员控制器
│   │       ├── AdminAuthController.java        # 认证
│   │       ├── AdminDashboardController.java   # 仪表盘
│   │       ├── AdminUserController.java         # 用户管理
│   │       ├── AdminProductController.java      # 商品管理
│   │       ├── AdminOrderController.java        # 订单管理
│   │       ├── AdminRefundController.java       # 退款管理
│   │       ├── AdminShareItemController.java    # 共享物品
│   │       ├── AdminContentController.java      # 内容管理
│   │       ├── AdminFlashSaleController.java    # 秒杀管理
│   │       ├── AdminReportController.java       # 举报管理
│   │       ├── AdminBlacklistController.java    # 黑名单管理
│   │       ├── AdminReportExportController.java # 数据导出
│   │       ├── AdminEvaluationController.java   # 评价管理
│   │       ├── AdminHotTagController.java       # 热门标签
│   │       ├── AdminQuickReplyController.java    # 快捷回复
│   │       ├── AdminLogController.java          # 日志管理
│   │       ├── AdminConfigController.java       # 系统配置
│   │       └── AdminManageController.java       # 管理员管理
│   ├── service/
│   │   └── impl/                  # 服务实现
│   ├── mapper/                     # 数据访问层
│   ├── entity/                     # 实体类
│   │   └── Admin.java             # 管理员实体
│   ├── dto/                        # 数据传输对象
│   │   └── admin/                  # 管理员DTO
│   ├── vo/                         # 视图对象
│   │   └── admin/                  # 管理员VO
│   ├── security/                   # 安全相关
│   │   ├── AdminAuthInterceptor.java
│   │   ├── SecurityContextHolder.java
│   │   └── JwtUtil.java
│   ├── aspect/                     # 切面
│   │   └── OperationLogAspect.java
│   ├── annotation/                 # 自定义注解
│   │   └── OperationLog.java
│   └── util/                       # 工具类
│       └── ExcelExportUtil.java    # Excel导出工具
```

### 7.2 前端项目结构

```
XianQiAdmin/
├── src/
│   ├── api/                        # API接口
│   │   ├── auth.ts
│   │   ├── dashboard.ts
│   │   ├── user.ts
│   │   ├── product.ts
│   │   ├── order.ts
│   │   ├── refund.ts
│   │   ├── share-item.ts           # 共享物品
│   │   ├── content.ts
│   │   ├── flash-sale.ts
│   │   ├── report.ts
│   │   ├── blacklist.ts            # 黑名单
│   │   ├── export.ts               # 数据导出
│   │   ├── evaluation.ts           # 评价
│   │   ├── hot-tag.ts              # 热门标签
│   │   ├── quick-reply.ts          # 快捷回复
│   │   ├── log.ts
│   │   ├── config.ts
│   │   └── admin.ts                # 管理员管理
│   ├── views/                      # 页面组件
│   │   ├── layout/                 # 布局组件
│   │   │   ├── Header.vue
│   │   │   ├── Sidebar.vue
│   │   │   └── index.vue
│   │   ├── dashboard/              # 仪表盘
│   │   ├── user/                   # 用户管理
│   │   ├── product/                # 商品管理
│   │   ├── share-item/             # 共享物品
│   │   ├── order/                  # 订单管理
│   │   ├── refund/                 # 退款管理
│   │   ├── content/                # 内容管理
│   │   ├── flash-sale/             # 秒杀管理
│   │   ├── report/                 # 举报管理
│   │   ├── blacklist/              # 黑名单
│   │   ├── export/                 # 数据导出
│   │   ├── evaluation/             # 评价管理
│   │   ├── system/                 # 系统管理
│   │   │   ├── hot-tag.vue
│   │   │   ├── quick-reply.vue
│   │   │   └── admin.vue
│   │   ├── config/                 # 系统配置
│   │   └── login/                  # 登录页
│   ├── components/                 # 公共组件
│   │   ├── Pagination.vue
│   │   ├── ImageUpload.vue
│   │   ├── ImagePreview.vue
│   │   └── ExportButton.vue        # 导出按钮
│   ├── router/                     # 路由配置
│   │   └── index.ts
│   ├── store/                      # 状态管理
│   │   ├── modules/
│   │   │   ├── admin.ts
│   │   │   └── app.ts
│   │   └── index.ts
│   ├── utils/                      # 工具函数
│   │   ├── request.ts
│   │   ├── auth.ts
│   │   ├── date.ts
│   │   └── export.ts               # Excel导出
│   ├── styles/                     # 样式文件
│   ├── types/                      # TypeScript类型定义
│   ├── App.vue
│   └── main.ts
├── package.json
├── vite.config.ts
└── tsconfig.json
```

### 7.3 开发步骤（简化版）

#### 第一阶段：基础框架（1周）

1. **后端基础**
   - ✅ 创建管理员表
   - ✅ 实现JWT认证
   - ✅ 创建拦截器
   - ✅ 创建基础Controller和Service

2. **前端基础**
   - ✅ 初始化Vue3项目
   - ✅ 安装Element Plus
   - ✅ 配置路由
   - ✅ 实现登录功能
   - ✅ 搭建页面布局

#### 第二阶段：核心功能（2周）

1. **数据统计**
   - ✅ 核心数据统计
   - ✅ 数据可视化图表

2. **用户管理**
   - ✅ 用户列表
   - ✅ 封禁/解封
   - ✅ 认证审核

3. **商品管理**
   - ✅ 商品审核
   - ✅ 商品列表
   - ✅ 分类管理

4. **共享物品**
   - ✅ 物品审核
   - ✅ 借用预约管理

5. **订单退款**
   - ✅ 订单列表
   - ✅ 退款处理

#### 第三阶段：辅助功能（1周）

1. **内容管理**
   - ✅ 轮播图
   - ✅ 系统通知
   - ✅ 热门标签
   - ✅ 快捷回复

2. **用户关系**
   - ✅ 黑名单管理

3. **举报反馈**
   - ✅ 举报处理
   - ✅ 用户反馈

4. **消息评价**
   - ✅ 消息管理
   - ✅ 评价管理

#### 第四阶段：系统功能（1周）

1. **数据导出**
   - ✅ 用户数据导出
   - ✅ 交易数据导出
   - ✅ 商品数据导出

2. **系统配置**
   - ✅ 基础配置
   - ✅ 业务配置
   - ✅ 安全配置

3. **日志监控**
   - ✅ 操作日志
   - ✅ 登录日志

4. **管理员管理**
   - ✅ 管理员CRUD
   - ✅ 密码重置

---

## 附录

### A. 开发环境要求

#### 后端
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- IDE: IntelliJ IDEA

#### 前端
- Node.js 16+
- npm 或 yarn
- 浏览器: Chrome 90+
- IDE: VS Code

### B. 核心依赖

#### 后端依赖
```xml
<!-- Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- MyBatis-Plus -->
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.3</version>
</dependency>

<!-- Excel导出 -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
```

#### 前端依赖
```json
{
  "dependencies": {
    "vue": "^3.3.0",
    "element-plus": "^2.4.0",
    "pinia": "^2.1.0",
    "vue-router": "^4.2.0",
    "axios": "^1.4.0",
    "echarts": "^5.4.0",
    "xlsx": "^0.18.5"
  }
}
```

### C. 更新日志

| 版本 | 日期 | 说明 |
|-----|------|------|
| v1.0 | 2024-03-07 | 初始版本 |
| v1.1 | 2024-03-07 | 简化权限设计，补充缺失功能模块 |

---

**文档结束**
