# 校园二手交易与共享平台 - 开发待办清单

> 更新时间：2026-02-18
> 状态：开发中
> 已完成：数据库初始化、用户注册、用户登录、个人信息管理、文件上传、商品管理、订单管理、评价管理、商品收藏、浏览历史、商品图片管理、用户中心、商品信息更新

---

## 📊 进度统计

- **总任务数**: 48个
- **已完成**: 17个 (35%)
- **进行中**: 0个
- **待开始**: 31个 (65%)

### 优先级分布
- **P0 (核心功能)**: 25个
- **P1 (重要功能)**: 17个
- **P2 (增强功能)**: 3个
- **管理端**: 3个

---

## ✅ 已完成功能

### 1. 数据库初始化
- [x] 创建22张数据库表
- [x] 插入初始数据（分类、系统配置、快捷回复、敏感词）
- [x] 创建测试用户数据

### 2. 用户注册功能 (`POST /api/user/register`)
- [x] UserRegisterDTO - 注册请求参数
- [x] UserRegisterVO - 注册响应数据
- [x] UserMapper - 数据访问层
- [x] UserService - 业务逻辑层
- [x] UserController - 控制器层
- [x] 用户名、手机号、学号唯一性校验
- [x] 密码BCrypt加密
- [x] 事务处理和异常处理

### 3. 用户登录功能 (`POST /api/user/login`)
- [x] UserLoginDTO - 登录请求参数
- [x] UserLoginVO - 登录响应数据
- [x] JWT Token生成（访问Token + 刷新Token）
- [x] 用户名密码验证
- [x] 用户状态校验（封禁用户不可登录）
- [x] 登录失败异常处理

### 4. 个人信息管理 (`GET/PUT /api/user/info`, `PUT /api/user/password`)
- [x] UserUpdateDTO - 更新用户信息DTO
- [x] UserInfoVO - 用户信息详情VO
- [x] UpdatePasswordDTO - 修改密码DTO
- [x] UserService.getUserInfo() - 获取用户信息
- [x] UserService.updateUserInfo() - 更新用户信息
- [x] UserService.updatePassword() - 修改密码
- [x] UserController - 添加相关接口

### 6. 图片上传功能 (`POST /api/upload/image`, `POST /api/upload/images`)
- [x] FileUploadController - 文件上传控制器
- [x] FileUploadService - 文件上传服务
- [x] 单图片上传接口
- [x] 批量图片上传接口
- [x] 文件大小和类型限制
- [x] 返回图片URL

### 7. 发布商品功能 (`POST /api/product`)
- [x] ProductCreateDTO - 创建商品DTO
- [x] ProductVO - 商品视图对象
- [x] ProductMapper - 商品数据访问层
- [x] ProductService - 商品业务逻辑层
- [x] ProductController - 商品控制器
- [x] 商品创建接口

### 8. 商品列表查询 (`GET /api/product`)
- [x] 商品列表分页查询
- [x] 分类筛选
- [x] 搜索关键词

### 9. 商品详情页 (`GET /api/product/{id}`)
- [x] 获取商品详情
- [x] 商品基本信息
- [x] 卖家信息

### 10. 商品搜索功能 (`GET /api/product/search`)
- [x] 关键词搜索（标题+描述）
- [x] 分类筛选
- [x] 价格区间筛选

### 11. 商品编辑功能 (`PUT /api/product/{id}/status`)
- [x] 修改商品状态（上架/下架/已售）
- [x] 权限校验（只能编辑自己的商品）
- [x] DELETE `/api/product/{id}` - 删除商品

### 15. 创建订单功能 (`POST /api/order`)
- [x] OrderCreateDTO - 创建订单DTO
- [x] OrderVO - 订单视图对象
- [x] OrderMapper - 订单数据访问层
- [x] OrderService - 订单业务逻辑层
- [x] OrderController - 订单控制器
- [x] 生成唯一订单号
- [x] 校验商品状态

### 16. 订单列表查询 (`GET /api/order`)
- [x] 订单列表分页查询
- [x] 订单状态筛选
- [x] 角色筛选（我买的/我卖的）

### 17. 订单详情页 (`GET /api/order/{id}`)
- [x] 获取订单详情
- [x] 订单基本信息
- [x] 商品信息
- [x] 买家和卖家信息

### 18. 订单状态管理 (`PUT /api/order/{id}/confirm|cancel|complete`)
- [x] 确认订单（卖家）
- [x] 取消订单（买家）
- [x] 完成订单（买家）

### 19. 交易评价功能 (`POST /api/evaluation`)
- [x] EvaluationCreateDTO - 创建评价DTO
- [x] EvaluationVO - 评价视图对象
- [x] EvaluationMapper - 评价数据访问层
- [x] EvaluationService - 评价业务逻辑层
- [x] EvaluationController - 评价控制器
- [x] 提交评价接口
- [x] 星级评分（1-5星）
- [x] 获取订单评价列表
- [x] 获取用户评价列表
- [x] 获取用户平均评分

### 13. ✅ 商品收藏功能 (`POST /api/favorite`, `DELETE /api/favorite/{id}`, `GET /api/favorite`)
- [x] ProductFavoriteService - 商品收藏服务
- [x] ProductFavoriteServiceImpl - 服务实现
- [x] ProductFavoriteController - 收藏控制器
- [x] 添加收藏接口
- [x] 取消收藏接口
- [x] 我的收藏列表（分页）
- [x] 检查是否已收藏接口

### 14. ✅ 浏览历史功能 (`GET /api/history`, `DELETE /api/history/{id}`, `DELETE /api/history/clear`)
- [x] ProductViewHistoryService - 浏览历史服务
- [x] ProductViewHistoryServiceImpl - 服务实现
- [x] ProductViewController - 浏览历史控制器
- [x] 浏览历史列表（分页）
- [x] 删除浏览记录
- [x] 清空浏览历史
- [x] 自动记录浏览历史（在商品详情接口中调用）

### 12. ✅ 商品图片管理 (`POST /api/product/{id}/images`, `DELETE /api/product/image/{id}`, `PUT /api/product/image/{id}/cover`)
- [x] ProductImageService - 商品图片服务
- [x] ProductImageServiceImpl - 服务实现
- [x] ProductImageController - 商品图片控制器
- [x] ProductImageCreateDTO - 创建图片DTO
- [x] ProductImageVO - 图片视图对象
- [x] 添加商品图片接口
- [x] 获取商品图片列表接口
- [x] 删除商品图片接口
- [x] 设置封面图接口
- [x] 图片数量限制（最多9张）

### 5. ✅ 用户中心 (`GET /api/user/center`)
- [x] UserCenterVO - 用户中心视图对象
- [x] UserService.getUserCenterData() - 获取用户中心数据
- [x] UserController.getUserCenter() - 用户中心接口
- [x] 用户基本信息展示

### 11. ✅ 商品信息更新 (`PUT /api/product/{id}`)
- [x] ProductUpdateDTO - 更新商品DTO
- [x] ProductService.updateProduct() - 更新商品信息
- [x] ProductController.updateProduct() - 更新商品接口
- [x] 权限校验（只能更新自己的商品）
- [x] 已售出商品不能修改

---

## 🚀 P0 优先级 - 核心功能 (25个)

### 用户管理模块

#### 4. ✅ 个人信息管理 (已完成)
**接口**: `GET /api/user/info` - 获取当前用户信息
**接口**: `PUT /api/user/info` - 更新个人信息
**接口**: `PUT /api/user/avatar` - 更新头像 (待实现)
**接口**: `PUT /api/user/password` - 修改密码

**已完成内容**:
- UserUpdateDTO - 更新用户信息DTO
- UserInfoVO - 用户信息详情VO
- UpdatePasswordDTO - 修改密码DTO
- UserService.updateUserInfo() - 更新用户信息
- UserService.updatePassword() - 修改密码
- UserController - 添加相关接口

---

#### 5. 用户中心
**接口**: `GET /api/user center/products` - 我的发布（商品列表）
**接口**: `GET /api/user center/orders` - 我的订单（订单列表）
**接口**: `GET /api/user center/favorites` - 我的收藏（商品收藏列表）
**接口**: `GET /api/user center/evaluations` - 我的评价（收到的评价）

**依赖**: 商品管理、订单管理、收藏管理
**预估工时**: 6小时

**需要创建的文件**:
- `UserCenterVO.java` - 用户中心数据聚合
- `UserService.getUserCenterData()` - 获取用户中心数据

---

### 文件上传模块

#### 6. ✅ 图片上传功能 (已完成)
**接口**: `POST /api/upload/image` - 上传单个图片
**接口**: `POST /api/upload/images` - 批量上传图片

**已完成内容**:
- FileUploadController - 文件上传控制器
- FileUploadService - 文件上传服务
- FileUploadVO - 文件上传返回对象
- 单图片上传接口
- 批量图片上传接口
- 文件大小和类型限制
- 返回图片URL

---

### 商品管理模块

#### 7. ✅ 发布商品功能 (已完成)
**接口**: `POST /api/product` - 发布商品

**已完成内容**:
- ProductCreateDTO - 创建商品DTO
- ProductVO - 商品视图对象
- ProductMapper - 商品数据访问层
- ProductService - 商品业务逻辑层
- ProductController - 商品控制器
- 商品创建接口

---

#### 8. ✅ 商品列表查询 (已完成)
**接口**: `GET /api/product` - 商品列表（分页）

**已完成内容**:
- 商品列表分页查询
- 分类筛选
- 搜索关键词

**待补充**:
- 价格区间筛选
- 成色筛选
- 排序方式

---

#### 9. ✅ 商品详情页 (已完成)
**接口**: `GET /api/product/{id}` - 获取商品详情

**已完成内容**:
- 获取商品详情接口
- 商品基本信息
- 卖家信息

**待补充**:
- 商品图片列表
- 是否已收藏标记
- 卖家其他商品推荐

---

#### 10. ✅ 商品搜索功能 (已完成)
**接口**: `GET /api/product/search` - 搜索商品

**已完成内容**:
- 关键词搜索（标题+描述）
- 分类筛选
- 价格区间筛选

**待补充**:
- 地理位置筛选（附近）

---

#### 11. ✅ 商品编辑功能 (部分完成)
**接口**: `PUT /api/product/{id}/status` - 修改商品状态（上架/下架/已售）
**接口**: `DELETE /api/product/{id}` - 删除商品

**已完成内容**:
- 修改商品状态
- 权限校验（只能编辑自己的商品）
- 删除商品

**待实现**:
- `PUT /api/product/{id}` - 更新商品信息（标题、描述、价格等）

---

#### 12. 商品图片管理
**接口**: `POST /api/product/{id}/images` - 添加商品图片
**接口**: `DELETE /api/product/image/{id}` - 删除商品图片
**接口**: `PUT /api/product/image/{id}/cover` - 设置封面图

**限制**: 每个商品最多9张图片
**预估工时**: 3小时

---

#### 13. 商品收藏功能
**接口**: `POST /api/favorite` - 收藏商品
**接口**: `DELETE /api/favorite/{id}` - 取消收藏
**接口**: `GET /api/favorite` - 我的收藏列表（分页）
**接口**: `GET /api/favorite/check/{productId}` - 检查是否已收藏

**预估工时**: 3小时

---

#### 14. 浏览历史功能
**接口**: `GET /api/history` - 浏览历史（分页）
**接口**: `DELETE /api/history/{id}` - 删除浏览记录
**接口**: `DELETE /api/history` - 清空浏览历史

**自动记录**: 用户访问商品详情时自动记录
**预估工时**: 3小时

---

### 订单管理模块

#### 15. ✅ 创建订单功能 (已完成)
**接口**: `POST /api/order` - 创建订单

**已完成内容**:
- OrderCreateDTO - 创建订单DTO
- OrderVO - 订单视图对象
- OrderMapper - 订单数据访问层
- OrderService - 订单业务逻辑层
- OrderController - 订单控制器
- 生成唯一订单号
- 校验商品状态

---

#### 16. ✅ 订单列表查询 (已完成)
**接口**: `GET /api/order` - 订单列表（分页）

**已完成内容**:
- 订单列表分页查询
- 订单状态筛选
- 角色筛选（我买的/我卖的）

---

#### 17. ✅ 订单详情页 (已完成)
**接口**: `GET /api/order/{id}` - 订单详情

**已完成内容**:
- 获取订单详情接口
- 订单基本信息
- 商品信息
- 买家和卖家信息

**待补充**:
- 订单状态流转记录
- 关联的会话ID

---

#### 18. ✅ 订单状态管理 (部分完成)
**接口**: `PUT /api/order/{id}/confirm` - 确认订单（卖家）
**接口**: `PUT /api/order/{id}/cancel` - 取消订单（买家）
**接口**: `PUT /api/order/{id}/complete` - 完成订单（买家）

**已完成内容**:
- 确认订单
- 取消订单
- 完成订单

**待实现**:
- `PUT /api/order/{id}/refund` - 申请退款（买家）

---

### 评价管理模块

#### 19. ✅ 交易评价功能 (已完成)
**接口**: `POST /api/evaluation` - 提交评价

**已完成内容**:
- EvaluationCreateDTO - 创建评价DTO
- EvaluationVO - 评价视图对象
- EvaluationMapper - 评价数据访问层
- EvaluationService - 评价业务逻辑层
- EvaluationController - 评价控制器
- 提交评价接口
- 星级评分（1-5星）
- 获取订单评价列表
- 获取用户评价列表
- 获取用户平均评分

---

#### 20. ⚠️ 信用积分系统 (部分完成)
**接口**:
- ✅ `GET /api/evaluation/user/{userId}` - 查询用户收到的评价
- ✅ `GET /api/evaluation/user/{userId}/average-rating` - 获取用户平均评分

**待实现**:
- ❌ 信用积分自动计算逻辑（好评+5、中评+2、差评-5）
- ❌ `GET /api/user/{id}/credit` - 查询用户信用分数
- ❌ 用户信用等级展示

---

### 即时通讯模块

#### 21. 即时通讯基础
**WebSocket连接**: `ws://localhost:8080/api/ws?token={jwt_token}`

**功能要点**:
- WebSocket连接管理
- 发送文本消息
- 接收实时消息
- 心跳检测
- 断线重连

**预估工时**: 8小时

---

#### 22. 会话管理
**接口**: `GET /api/conversation` - 会话列表（分页）
**接口**: `GET /api/conversation/{id}` - 会话详情
**接口**: `DELETE /api/conversation/{id}` - 删除会话
**接口**: `PUT /api/conversation/{id}/read` - 标记已读

**会话类型**:
- 单聊：两个用户之间只有一个会话
- 群聊：多个用户（暂不实现）

**预估工时**: 5小时

---

#### 23. 消息管理
**接口**: `GET /api/conversation/{id}/messages` - 历史消息（分页）
**WebSocket事件**:
- 发送消息
- 接收消息
- 消息已送达回执
- 消息已读回执

**预估工时**: 6小时

---

#### 24. 消息已读机制
**接口**: `PUT /api/message/read-all` - 批量标记已读
**WebSocket事件**: 消息已读推送

**功能要点**:
- 未读消息数统计
- 消息已读状态更新
- 实时推送未读数

**预估工时**: 3小时

---

## 🎯 P1 优先级 - 重要功能 (17个)

### 25. 地理位置功能
**功能**: 商品附近推荐
**接口**: `GET /api/product/nearby` - 附近的商品

**依赖**: 高德地图/腾讯地图API
**预估工时**: 6小时

---

### 26-32. 共享物品模块
**接口**:
- `POST /api/share-item` - 发布共享物品
- `GET /api/share-item` - 共享物品列表
- `POST /api/share-item/{id}/booking` - 预约借用
- `PUT /api/share-item-booking/{id}/approve` - 审批借用
- `PUT /api/share-item-booking/{id}/return` - 确认归还
- `POST /api/deposit` - 支付押金
- `POST /api/deposit/refund` - 退还押金

**预估工时**: 20小时

---

### 33-37. 消息增强功能
**功能**:
- 发送图片消息
- 消息撤回（2分钟内）
- 快捷回复模板
- 黑名单功能
- 举报功能

**预估工时**: 10小时

---

### 38. 敏感词过滤
**功能**: 发布商品、发送消息前检测敏感词
**接口**: `POST /api/sensitive-word/check` - 检测敏感词
**预估工时**: 4小时

---

### 39-42. 系统功能
**功能**:
- 系统通知（公告、活动）
- 轮播图管理
- 系统配置管理
- 用户反馈

**预估工时**: 12小时

---

## 🎨 P2 优先级 - 增强功能 (3个)

### 43-45. 增强功能
**功能**:
- 智能推荐（基于浏览历史）
- 一键转赠（免费转让）
- 实名认证（学生证认证）

**预估工时**: 15小时

---

## 🔧 管理端功能 (3个)

### 46-48. 后台管理
**功能**:
- 操作日志
- 商品审核
- 数据统计（用户/交易/商品）

**预估工时**: 12小时

---

## 📝 开发注意事项

### 技术规范
1. 所有接口使用RESTful风格
2. 统一返回格式 `Result<T>`
3. 统一异常处理 `BusinessException`
4. 使用Lombok简化代码
5. 所有DTO添加 `@Valid` 注解进行参数校验
6. Service层使用 `@Transactional` 保证事务

### 安全要求
1. 所有需要认证的接口添加JWT Token验证
2. 密码使用BCrypt加密
3. 敏感信息脱敏处理
4. 防止SQL注入、XSS攻击

### 性能要求
1. 分页查询默认页大小20
2. 使用Redis缓存热点数据
3. 图片使用OSS CDN加速
4. 数据库查询添加索引

### 测试要求
1. 每个功能编写单元测试
2. 接口使用Postman/Apifox测试
3. 边界条件和异常情况测试

---

## 📅 开发计划

### 第一阶段（✅ 已完成）
- ✅ 数据库设计和初始化
- ✅ 用户注册和登录
- ✅ 个人信息管理
- ✅ 图片上传功能
- ✅ 商品管理（发布、列表、详情、搜索）
- ✅ 订单管理
- ✅ 评价管理

### 第二阶段（✅ 已完成）
- ✅ 商品图片管理（添加/删除图片、设置封面）
- ✅ 商品信息更新
- ✅ 商品收藏功能
- ✅ 浏览历史功能
- ✅ 用户中心

**待完善**:
- 订单退款功能
- 用户信用积分系统

### 第三阶段（⬜ 待开始）
- 即时通讯基础（WebSocket连接管理、发送消息、接收消息）
- 会话管理
- 消息管理

### 第四阶段（⬜ 待开始）
- 共享物品功能
- 消息增强功能（图片消息、消息撤回、快捷回复）
- 系统功能（通知、轮播图、反馈）
- 敏感词过滤

### 第五阶段（⬜ 待开始）
- 智能推荐
- 管理端功能
- 性能优化

---

**下一步建议**:
1. 实现即时通讯基础功能（WebSocket连接管理）
2. 实现会话管理和消息管理
3. 完善订单退款功能
4. 实现信用积分自动计算系统
