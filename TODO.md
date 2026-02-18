# 校园二手交易与共享平台 - 开发待办清单

> 更新时间：2026-02-18
> 状态：开发中
> 已完成：数据库初始化、用户注册、用户登录、个人信息管理、文件上传、商品管理、订单管理、评价管理、商品收藏、浏览历史、商品图片管理、用户中心、商品信息更新、即时通讯（WebSocket、会话管理、消息管理、消息已读、发送图片消息）、订单退款、信用积分自动计算、消息撤回、黑名单、举报、快捷回复、敏感词过滤、系统通知、轮播图、用户反馈、系统配置管理、共享物品、地理位置功能

---

## 📊 进度统计

- **总任务数**: 48个
- **已完成**: 37个 (77%)
- **进行中**: 0个
- **待开始**: 11个 (23%)

### 优先级分布
- **P0 (核心功能)**: 25个 ✅ (全部完成！)
- **P1 (重要功能)**: 17个 ✅ (全部完成！)
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

### 20. ✅ 即时通讯模块 (WebSocket + 会话 + 消息)
- [x] WebSocketConfig - WebSocket配置
- [x] WebSocketHandler - WebSocket消息处理器
- [x] WebSocketInterceptor - WebSocket拦截器（JWT认证）
- [x] ConversationMapper - 会话数据访问层
- [x] MessageMapper - 消息数据访问层
- [x] ConversationService - 会话服务接口
- [x] ConversationServiceImpl - 会话服务实现
- [x] ConversationController - 会话控制器（7个接口）
- [x] MessageSendDTO - 发送消息DTO
- [x] MessageVO - 消息视图对象
- [x] ConversationVO - 会话视图对象
- [x] POST `/api/conversation/one-to-one` - 获取或创建单聊会话
- [x] GET `/api/conversation` - 会话列表（分页）
- [x] GET `/api/conversation/{id}` - 会话详情
- [x] DELETE `/api/conversation/{id}` - 删除会话
- [x] PUT `/api/conversation/{id}/read` - 标记会话已读
- [x] POST `/api/conversation/message` - 发送消息
- [x] GET `/api/conversation/{id}/messages` - 历史消息（分页）
- [x] WebSocket实时消息推送
- [x] 未读消息数统计和更新
- [x] 在线状态显示

### 21. ✅ 订单退款功能
- [x] OrderService.requestRefund() - 申请退款（买家）
- [x] OrderService.approveRefund() - 同意退款（卖家）
- [x] OrderService.rejectRefund() - 拒绝退款（卖家）
- [x] PUT `/api/order/{id}/refund-request` - 申请退款接口
- [x] PUT `/api/order/{id}/refund-approve` - 同意退款接口
- [x] PUT `/api/order/{id}/refund-reject` - 拒绝退款接口
- [x] 订单状态流转（进行中 → 退款中 → 已取消/恢复进行中）
- [x] 退款成功后恢复商品状态为在售

### 22. ✅ 信用积分自动计算系统
- [x] 信用积分计算规则：好评+5分、中评+2分、差评-5分
- [x] EvaluationServiceImpl.updateUserCreditScore() - 自动更新信用积分
- [x] UserService.getUserCreditScore() - 获取用户信用积分
- [x] GET `/api/user/{userId}/credit` - 查询用户信用分数接口
- [x] 信用积分范围限制（0-100）
- [x] 创建评价时自动更新被评价人信用积分

### 23. ✅ 消息撤回功能
- [x] ConversationService.recallMessage() - 撤回消息方法
- [x] ConversationServiceImpl.recallMessage() - 撤回消息实现
- [x] PUT `/api/conversation/message/{messageId}/recall` - 撤回消息接口
- [x] 2分钟内可撤回限制
- [x] 只能撤回自己发送的消息
- [x] 撤回后通过WebSocket通知对方
- [x] 消息状态标记（status=1表示已撤回）

### 24. ✅ 发送图片消息功能
- [x] ImageMessageSendDTO - 图片消息发送DTO
- [x] ConversationService.sendImageMessage() - 发送图片消息方法
- [x] ConversationServiceImpl.sendImageMessage() - 发送图片消息实现
- [x] POST `/api/conversation/message/image` - 发送图片消息接口
- [x] 支持图片URL、宽度、高度、大小、缩略图
- [x] 图片信息存储在extraData字段（JSON格式）
- [x] 消息类型为2（图片消息）
- [x] 会话最后消息显示为"[图片]"
- [x] 支持引用消息（parentMessageId）

### 25. ✅ 黑名单功能
- [x] BlacklistMapper - 黑名单数据访问层
- [x] BlacklistService - 黑名单服务接口
- [x] BlacklistServiceImpl - 黑名单服务实现
- [x] BlacklistController - 黑名单控制器
- [x] POST `/api/blacklist/{blockedUserId}` - 添加黑名单
- [x] DELETE `/api/blacklist/{blacklistId}` - 移除黑名单
- [x] GET `/api/blacklist` - 黑名单列表（分页）
- [x] GET `/api/blacklist/check/{targetUserId}` - 检查是否在黑名单中
- [x] 不能将自己拉黑校验
- [x] 重复拉黑校验

### 26. ✅ 举报功能
- [x] ReportCreateDTO - 创建举报DTO
- [x] ReportVO - 举报视图对象
- [x] ReportMapper - 举报数据访问层
- [x] ReportService - 举报服务接口
- [x] ReportServiceImpl - 举报服务实现
- [x] ReportController - 举报控制器
- [x] POST `/api/report` - 创建举报
- [x] GET `/api/report` - 我的举报列表（分页）
- [x] 支持举报用户、会话、消息
- [x] 举报状态管理（待处理/已处理/已驳回）

### 27. ✅ 快捷回复模板功能
- [x] QuickReplyDTO - 快捷回复DTO
- [x] QuickReplyVO - 快捷回复视图对象
- [x] QuickReplyMapper - 快捷回复数据访问层
- [x] QuickReplyService - 快捷回复服务接口
- [x] QuickReplyServiceImpl - 快捷回复服务实现
- [x] QuickReplyController - 快捷回复控制器
- [x] POST `/api/quick-reply` - 创建快捷回复
- [x] PUT `/api/quick-reply/{replyId}` - 更新快捷回复
- [x] DELETE `/api/quick-reply/{replyId}` - 删除快捷回复
- [x] GET `/api/quick-reply` - 快捷回复列表（分页）
- [x] GET `/api/quick-reply/system` - 获取系统预设快捷回复
- [x] 个人快捷回复最多20个限制
- [x] 系统预设快捷回复不可修改删除

### 28. ✅ 敏感词过滤功能
- [x] SensitiveWordCheckDTO - 敏感词检测DTO
- [x] SensitiveWordCheckVO - 敏感词检测结果VO
- [x] SensitiveWordMapper - 敏感词数据访问层
- [x] SensitiveWordService - 敏感词服务接口
- [x] SensitiveWordServiceImpl - 敏感词服务实现
- [x] SensitiveWordController - 敏感词控制器
- [x] POST `/api/sensitive-word/check` - 检测敏感词
- [x] POST `/api/sensitive-word/filter` - 过滤敏感词
- [x] 三种敏感词类型：禁止词（拒绝）、敏感词（替换*）、替换词（指定词）
- [x] 缓存启用敏感词列表
- [x] 支持大小写不敏感检测

### 29. ✅ 系统通知功能
- [x] SystemNotificationVO - 系统通知视图对象
- [x] SystemNotificationMapper - 系统通知数据访问层
- [x] SystemNotificationService - 系统通知服务接口
- [x] SystemNotificationServiceImpl - 系统通知服务实现
- [x] SystemNotificationController - 系统通知控制器
- [x] GET `/api/notification` - 通知列表（分页）
- [x] GET `/api/notification/{notificationId}` - 通知详情
- [x] PUT `/api/notification/{notificationId}/read` - 标记已读
- [x] GET `/api/notification/unread-count` - 未读通知数量
- [x] PUT `/api/notification/read-all` - 标记所有通知已读
- [x] 自动标记通知为已读
- [x] 支持全部用户/指定用户/指定等级
- [x] 支持多种通知类型（公告/活动/账户提醒/交易提醒）

### 30. ✅ 轮播图管理功能
- [x] BannerVO - 轮播图视图对象
- [x] BannerMapper - 轮播图数据访问层
- [x] BannerService - 轮播图服务接口
- [x] BannerServiceImpl - 轮播图服务实现
- [x] BannerController - 轮播图控制器
- [x] GET `/api/banner` - 获取启用的轮播图列表
- [x] POST `/api/banner/{bannerId}/click` - 增加点击次数
- [x] 自动记录曝光次数和点击次数
- [x] 支持时间范围控制
- [x] 支持多种跳转类型（外链/商品详情/功能页面）

### 31. ✅ 用户反馈功能
- [x] UserFeedbackDTO - 用户反馈DTO
- [x] UserFeedbackVO - 用户反馈视图对象
- [x] UserFeedbackMapper - 用户反馈数据访问层
- [x] UserFeedbackService - 用户反馈服务接口
- [x] UserFeedbackServiceImpl - 用户反馈服务实现
- [x] UserFeedbackController - 用户反馈控制器
- [x] POST `/api/feedback` - 创建用户反馈
- [x] GET `/api/feedback` - 我的反馈列表（分页）
- [x] 支持匿名反馈
- [x] 多种反馈类型（功能建议/Bug反馈/投诉/其他）
- [x] 反馈状态管理（待处理/处理中/已处理/已驳回）

### 32. ✅ 系统配置管理功能
- [x] SystemConfigCreateDTO - 系统配置DTO
- [x] SystemConfigVO - 系统配置视图对象
- [x] SystemConfigMapper - 系统配置数据访问层
- [x] SystemConfigService - 系统配置服务接口
- [x] SystemConfigServiceImpl - 系统配置服务实现
- [x] SystemConfigController - 系统配置控制器
- [x] POST `/api/config` - 创建系统配置
- [x] PUT `/api/config/{configId}` - 更新系统配置
- [x] DELETE `/api/config/{configId}` - 删除系统配置
- [x] GET `/api/config` - 配置列表（分页）
- [x] GET `/api/config/value/{configKey}` - 获取配置值
- [x] GET `/api/config/public` - 获取公开配置
- [x] GET `/api/config/group/{groupName}` - 根据分组获取配置
- [x] 支持多种配置类型（string/number/boolean/json）
- [x] 系统配置保护（不允许删除和修改配置键）
- [x] 配置缓存（使用@Cacheable和@CacheEvict）

### 33. ✅ 共享物品功能
- [x] ShareItemCreateDTO - 共享物品DTO
- [x] ShareItemVO - 共享物品视图对象
- [x] ShareItemMapper - 共享物品数据访问层
- [x] ShareItemImageMapper - 共享物品图片数据访问层
- [x] ShareItemService - 共享物品服务接口
- [x] ShareItemServiceImpl - 共享物品服务实现
- [x] ShareItemController - 共享物品控制器
- [x] POST `/api/share-item` - 创建共享物品
- [x] PUT `/api/share-item/{shareId}` - 更新共享物品
- [x] DELETE `/api/share-item/{shareId}` - 删除共享物品
- [x] PUT `/api/share-item/{shareId}/status` - 更新共享物品状态
- [x] GET `/api/share-item/{shareId}` - 获取共享物品详情
- [x] GET `/api/share-item` - 共享物品列表（分页）
- [x] GET `/api/share-item/my` - 我的共享物品列表
- [x] 支持押金和日租金设置
- [x] 支持多图片上传和管理
- [x] 支持可借用时间段配置
- [x] 状态管理（下架/可借用/借用中）

### 34. ✅ 地理位置功能
- [x] UpdateLocationDTO - 更新位置DTO
- [x] UserService.updateUserLocation() - 更新用户位置
- [x] UserService.getNearbyUsers() - 获取附近用户列表
- [x] ProductService.getNearbyProducts() - 获取附近商品
- [x] ShareItemService.getNearbyShareItems() - 获取附近共享物品
- [x] PUT `/api/user/location` - 更新用户位置
- [x] GET `/api/user/nearby` - 附近用户列表
- [x] GET `/api/product/nearby` - 附近商品列表
- [x] GET `/api/share-item/nearby` - 附近共享物品列表
- [x] 基于经纬度的距离计算（Haversin公式）
- [x] 优先推荐同学院/同专业的用户和物品

### 35. ✅ 共享物品预约借用功能
- [x] ShareItemBooking - 预约借用实体
- [x] ShareItemBookingCreateDTO - 创建预约DTO
- [x] BookingApproveDTO - 审批预约DTO
- [x] BookingReturnDTO - 归还确认DTO
- [x] ShareItemBookingVO - 预约借用VO
- [x] ShareItemBookingMapper - 数据访问层
- [x] ShareItemBookingService - 服务接口
- [x] ShareItemBookingServiceImpl - 服务实现
- [x] ShareItemBookingController - 控制器
- [x] POST `/api/share-item-booking` - 创建预约借用
- [x] PUT `/api/share-item-booking/approve` - 审批预约
- [x] PUT `/api/share-item-booking/{bookingId}/cancel` - 取消预约
- [x] PUT `/api/share-item-booking/return` - 确认归还
- [x] PUT `/api/share-item-booking/{bookingId}/deposit-return` - 退还押金
- [x] GET `/api/share-item-booking/{bookingId}` - 预约详情
- [x] GET `/api/share-item-booking/my` - 我的预约列表
- [x] GET `/api/share-item-booking/received` - 收到的预约列表
- [x] 预约状态管理（待审批/已批准/已拒绝/已取消/借用中/已完成）
- [x] 自动计算借用天数、租金、总金额
- [x] 日期冲突检测
- [x] 押金退还管理

### 36. ✅ 押金管理功能
- [x] DepositRecord - 押金记录实体
- [x] DepositPayDTO - 支付押金DTO
- [x] DepositRefundDTO - 退还押金DTO
- [x] DepositRecordVO - 押金记录VO
- [x] DepositRecordMapper - 数据访问层
- [x] DepositRecordService - 服务接口
- [x] DepositRecordServiceImpl - 服务实现
- [x] DepositRecordController - 控制器
- [x] POST `/api/deposit/pay` - 支付押金
- [x] PUT `/api/deposit/refund` - 退还押金
- [x] PUT `/api/deposit/{recordId}/deduct` - 扣除押金
- [x] GET `/api/deposit/{recordId}` - 押金记录详情
- [x] GET `/api/deposit/booking/{bookingId}` - 根据预约ID获取押金记录
- [x] GET `/api/deposit/my` - 我的押金记录列表
- [x] 支付方式管理（余额/支付宝/微信）
- [x] 交易流水号生成
- [x] 押金状态管理（待支付/已支付/已退还/已扣除）
- [x] 押金扣除和退还功能

---

## 🚀 P0 优先级 - 核心功能 (25个) ✅ 全部完成！

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

#### 18. ✅ 订单状态管理 (已完成)
**接口**: `PUT /api/order/{id}/confirm` - 确认订单（卖家）
**接口**: `PUT /api/order/{id}/cancel` - 取消订单（买家或卖家）
**接口**: `PUT /api/order/{id}/complete` - 完成订单（买家）
**接口**: `PUT /api/order/{id}/refund-request` - 申请退款（买家）
**接口**: `PUT /api/order/{id}/refund-approve` - 同意退款（卖家）
**接口**: `PUT /api/order/{id}/refund-reject` - 拒绝退款（卖家）

**已完成内容**:
- 确认订单
- 取消订单
- 完成订单
- 申请退款（买家发起，订单状态变为"退款中"）
- 同意退款（卖家同意，订单变为"已取消"，商品恢复在售）
- 拒绝退款（卖家拒绝，订单恢复"进行中"状态）

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

#### 20. ✅ 信用积分系统 (已完成)
**已完成内容**:
- ✅ 信用积分自动计算逻辑（好评+5、中评+2、差评-5）
- ✅ 创建评价时自动更新被评价人信用积分
- ✅ `GET /api/user/{userId}/credit` - 查询用户信用分数
- ✅ 信用积分范围限制（0-100分）
- ✅ UserService.getUserCreditScore() - 获取用户信用积分
- ✅ EvaluationServiceImpl.updateUserCreditScore() - 更新信用积分

---

### 即时通讯模块

#### 21. ✅ 即时通讯基础 (已完成)
**WebSocket连接**: `ws://localhost:8080/api/ws?token={jwt_token}`

**已完成内容**:
- WebSocketConfig - WebSocket配置
- WebSocketHandler - WebSocket消息处理器
- WebSocketInterceptor - WebSocket拦截器（JWT认证）
- 在线用户管理
- 发送文本消息
- 接收实时消息
- 心跳检测
- 断线重连

---

#### 22. ✅ 会话管理 (已完成)
**接口**: `GET /api/conversation` - 会话列表（分页）
**接口**: `POST /api/conversation/one-to-one` - 获取或创建单聊会话
**接口**: `GET /api/conversation/{id}` - 会话详情
**接口**: `DELETE /api/conversation/{id}` - 删除会话
**接口**: `PUT /api/conversation/{id}/read` - 标记已读

**已完成内容**:
- ConversationMapper - 会话数据访问层
- ConversationService - 会话服务接口
- ConversationServiceImpl - 会话服务实现
- ConversationController - 会话控制器
- ConversationVO - 会话视图对象
- 单聊会话创建（两个用户之间只有一个会话）
- 会话列表查询（分页、按最后消息时间排序）
- 会话详情（包含对方用户信息、在线状态、未读数）
- 删除会话（逻辑删除）
- 标记会话已读（清空未读数、标记所有消息为已读）

---

#### 23. ✅ 消息管理 (已完成)
**接口**: `POST /api/conversation/message` - 发送消息
**接口**: `GET /api/conversation/{id}/messages` - 历史消息（分页）

**已完成内容**:
- MessageMapper - 消息数据访问层
- MessageSendDTO - 发送消息DTO
- MessageVO - 消息视图对象
- ConversationService.sendMessage() - 发送消息
- ConversationService.getMessages() - 获取历史消息
- 消息类型支持：文本、图片、商品卡片、订单卡片、引用消息、系统通知
- 发送消息时更新会话的最后消息信息
- 自动增加接收者的未读数
- 通过WebSocket实时推送新消息给接收者
- 历史消息分页查询（按创建时间倒序）
- 引用消息支持（parentMessageId）

---

#### 24. ✅ 消息已读机制 (已完成)
**接口**: `PUT /api/conversation/{id}/read` - 标记会话为已读

**已完成内容**:
- 未读消息数统计（unreadCountUser1、unreadCountUser2）
- 消息已读状态更新（isRead、readTime）
- ConversationServiceImpl.markConversationAsRead() - 清空用户未读数
- 标记该会话中所有发给该用户的未读消息为已读
- ConversationVO中展示未读数和对方在线状态

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

### 33-37. ✅ 消息增强功能 (全部完成)
**功能**:
- ✅ 发送图片消息 - 已完成
- ✅ 消息撤回（2分钟内）- 已完成
- ✅ 快捷回复模板 - 已完成
- ✅ 黑名单功能 - 已完成
- ✅ 举报功能 - 已完成

**已完成内容**:
- 发送图片消息（支持图片URL、尺寸、大小、缩略图，支持引用）
- 消息撤回功能（2分钟限制、WebSocket通知）
- 快捷回复模板（系统预设+用户自定义，个人最多20个）
- 黑名单功能（添加/移除/查询/检查）
- 举报功能（创建举报、查询我的举报）

---

### 38. ✅ 敏感词过滤 (已完成)
**功能**: 发布商品、发送消息前检测敏感词
**接口**:
- `POST /api/sensitive-word/check` - 检测敏感词
- `POST /api/sensitive-word/filter` - 过滤敏感词

**已完成内容**:
- 三种敏感词类型：禁止词（拒绝）、敏感词（替换*）、替换词（自定义替换）
- 支持大小写不敏感检测
- 缓存启用敏感词列表
- 返回检测详情和过滤后的内容

---

### 39-42. ✅ 系统功能 (全部已完成)
**功能**:
- ✅ 系统通知（公告、活动）- 已完成
- ✅ 轮播图管理 - 已完成
- ✅ 系统配置管理 - 已完成
- ✅ 用户反馈 - 已完成

**已完成内容**:
- 系统通知（支持全部用户/指定用户/指定等级，自动标记已读）
- 轮播图管理（自动记录曝光和点击次数）
- 系统配置管理（动态配置项、支持多类型、缓存）
- 用户反馈（支持匿名反馈，多种反馈类型）

---

### 43. ✅ 共享物品功能 (部分完成)
**功能**: 物品共享、押金和租金管理
**接口**:
- `POST /api/share-item` - 创建共享物品
- `PUT /api/share-item/{shareId}` - 更新共享物品
- `DELETE /api/share-item/{shareId}` - 删除共享物品
- `PUT /api/share-item/{shareId}/status` - 更新状态
- `GET /api/share-item/{shareId}` - 物品详情
- `GET /api/share-item` - 物品列表（分页）
- `GET /api/share-item/my` - 我的共享物品

**已完成内容**:
- 共享物品创建、更新、删除
- 支持押金和日租金设置
- 多图片管理（封面图、排序）
- 可借用时间段配置
- 状态管理（下架/可借用/借用中）
- 分类筛选和搜索

**待实现**:
- 预约借用功能
- 借用审批流程
- 押金支付和退还
- 归还确认

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

### 第三阶段（✅ 已完成）
- ✅ 即时通讯基础（WebSocket连接管理、发送消息、接收消息）
- ✅ 会话管理（获取或创建会话、会话列表、会话详情、删除会话、标记已读）
- ✅ 消息管理（发送消息、历史消息、WebSocket实时推送）
- ✅ 消息已读机制（未读数统计、标记已读）

### 第四阶段（✅ 已完成 - P0核心功能全部完成！）
- ✅ 订单退款功能（申请退款、同意退款、拒绝退款）
- ✅ 信用积分自动计算系统（好评+5、中评+2、差评-5）

**🎉 恭喜！所有P0核心功能已经全部完成！**

### 第五阶段（✅ 全部完成 - P1重要功能全部完成！）
- ✅ 消息撤回功能（2分钟内可撤回）
- ✅ 黑名单功能（添加/移除/查询）
- ✅ 举报功能（创建举报、查询我的举报）
- ✅ 发送图片消息（支持图片URL、尺寸、缩略图）
- ✅ 快捷回复模板（系统预设+用户自定义，最多20个）
- ✅ 敏感词过滤（禁止词、敏感词、替换词三种类型）
- ✅ 系统通知（公告、活动、支持全部/指定用户）
- ✅ 轮播图管理（曝光和点击次数统计）
- ✅ 用户反馈（支持匿名反馈）
- ✅ 系统配置管理（动态配置、多类型支持、缓存）
- ✅ 共享物品（创建、更新、删除、状态管理）
- ✅ 地理位置功能（附近商品、附近共享物品、附近用户）
- ✅ 共享物品预约借用（创建、审批、取消、归还、押金退还）
- ✅ 押金管理（支付、退还、扣除、记录查询）

**🎉 恭喜！所有P0和P1功能已经全部完成！**

### 第六阶段（⬜ 待开始 - P2增强功能）
- 智能推荐
- 管理端功能
- 性能优化

---

**✨ 已完成功能总结**:

**P0核心功能** (全部完成 ✅):
1. ✅ 用户管理（注册、登录、个人信息、用户中心）
2. ✅ 商品管理（发布、编辑、搜索、图片管理）
3. ✅ 订单管理（创建、状态管理、退款）
4. ✅ 评价管理（提交评价、信用积分自动计算）
5. ✅ 即时通讯（WebSocket、会话、消息、消息已读、消息撤回、发送图片消息）
6. ✅ 商品收藏和浏览历史
7. ✅ 文件上传功能

**P1重要功能** (17/17完成 ✅):
1. ✅ 消息撤回
2. ✅ 黑名单
3. ✅ 举报
4. ✅ 发送图片消息
5. ✅ 快捷回复模板
6. ✅ 敏感词过滤
7. ✅ 系统通知
8. ✅ 轮播图管理
9. ✅ 用户反馈
10. ✅ 系统配置管理
11. ✅ 共享物品（基础功能）
12. ✅ 地理位置功能（附近推荐）
13. ✅ 共享物品预约借用（创建、审批、归还、押金退还）
14. ✅ 押金管理（支付、退还、扣除、记录查询）

**下一步建议**:
1. 开始P2增强功能（智能推荐、管理端、性能优化）
2. 进行系统测试和Bug修复
3. 编写API文档和部署文档

