# 产品需求文档（PRD）
## 基于SpringBoot与uniapp的校园二手交易与共享平台

---

## 文档信息

| 项目 | 内容 |
|------|------|
| 产品名称 | 校园二手交易与共享平台 |
| 版本号 | v1.0 |
| 文档创建日期 | 2026-02-17 |
| 作者 | 许佳宜 |
| 学号 | 2022035123021 |
| 指导教师 | 温清机 |
| 专业 | 计算机科学与技术(职教师资) |

---

## 1. 产品概述

### 1.1 产品背景
随着高校学生消费水平提升和物品更新迭代加快，校园内产生大量闲置物品。传统的线下交易方式效率低下，信息不透明，而现有的通用二手交易平台缺乏校园特色，无法满足学生对便捷、安全、可信的校内交易需求。

### 1.2 产品定位
面向高校学生的垂直化校园二手交易与物品共享平台，提供"交易+共享"双模式服务，打造安全、便捷、高效的校园闲置资源流转生态系统。

### 1.3 产品目标
- 搭建稳定高效的校园二手交易平台，支持多用户并发操作
- 实现二手商品发布、检索、交易全流程在线化
- 提供物品共享功能，提高校园资源利用率
- 建立信用评价体系，保障交易安全
- 提供数据统计分析功能，辅助平台运营决策

### 1.4 技术栈
- **后端框架**：SpringBoot
- **前端框架**：uniapp（跨端开发）
- **架构模式**：前后端分离
- **数据库**：MySQL（待定）
- **其他技术**：地图定位API、即时通讯

---

## 2. 目标用户

### 2.1 用户画像

| 用户类型 | 描述 | 核心需求 |
|---------|------|---------|
| 买家 | 寻找性价比高的二手商品的学生 | 价格优惠、品质保障、交易便捷 |
| 卖家 | 有闲置物品需要处理的学生 | 快速出售、合理定价、安全交易 |
| 共享者 | 愿意共享物品使用权的用户 | 物品安全保障、共享收益管理 |
| 借用者 | 短期需要使用物品的用户 | 便捷预约、信用记录、按时归还 |
| 管理员 | 平台运营管理人员 | 内容审核、数据监控、用户管理 |

### 2.2 用户规模
- 初期覆盖单所高校，预计用户规模5000-10000人
- 日活跃用户预计1000-2000人
- 并发用户数200-500人

---

## 3. 用户场景

### 3.1 典型使用场景

**场景1：毕业生卖书**
> 小李即将毕业，有大量专业书籍和复习资料需要处理。他通过平台发布书籍信息，设置合理的价格和交易地点。学弟小王搜索到相关书籍，通过平台联系小李，线下完成交易，双方进行互评。

**场景2：共享生活用品**
> 小张有一台闲置的羽毛球拍，平时使用频率不高。他将球拍发布到共享区，设置押金和每日租金。同学小林周末想打球，通过平台预约借用2天，支付押金和租金，按时归还后押金退回。

**场景3：智能推荐**
> 小王经常浏览电子类产品。系统根据他的浏览历史和收藏记录，智能推荐相关的数码配件和二手电子产品，提高交易匹配效率。

---

## 4. 功能需求

### 4.1 核心功能模块

#### 4.1.1 用户管理模块

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 用户注册/登录 | 支持学号认证、手机号注册 | P0 |
| 个人信息管理 | 头像、昵称、联系方式、学院专业 | P0 |
| 实名认证 | 学生证认证，提高交易可信度 | P1 |
| 用户中心 | 我的发布、我的订单、我的收藏、信用评价 | P0 |
| 黑名单管理 | 屏蔽特定用户 | P2 |

#### 4.1.2 商品管理模块

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 发布商品 | 图文描述、价格、成色、分类、位置 | P0 |
| 商品编辑 | 修改价格、描述、状态（上架/下架/已售） | P0 |
| 商品检索 | 关键词搜索、分类筛选、价格区间 | P0 |
| 地理位置定位 | 基于位置推荐附近商品 | P1 |
| 商品收藏 | 收藏感兴趣的商品 | P1 |
| 浏览历史 | 查看最近浏览的商品 | P2 |
| 智能推荐 | 基于用户行为的个性化推荐 | P2 |

#### 4.1.3 物品共享模块

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 发布共享物品 | 物品信息、押金、租金、可借用时间 | P1 |
| 预约借用 | 选择时间段、提交预约申请 | P1 |
| 借用审批 | 出借人审核预约申请 | P1 |
| 押金管理 | 支付/退还押金 | P1 |
| 归还确认 | 确认物品归还，完成共享 | P1 |
| 一键转赠 | 闲置物品免费转让 | P2 |

#### 4.1.4 交易订单模块

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 创建订单 | 确认购买商品/预约共享 | P0 |
| 订单状态管理 | 待确认/进行中/已完成/已取消/已退款 | P0 |
| 订单详情 | 查看订单完整信息和进度 | P0 |
| 订单评价 | 交易完成后对对方进行评价 | P0 |
| 售后处理 | 退款/纠纷处理 | P1 |

#### 4.1.5 即时通讯模块

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 即时聊天 | 买卖双方在线沟通 | P0 |
| 消息推送 | 新消息、订单状态变更通知 | P0 |
| 聊天记录 | 查看历史聊天记录 | P1 |
| 图片发送 | 发送商品实物图片 | P1 |

#### 4.1.6 信用评价模块

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 交易评价 | 星级评分+文字评价+标签 | P0 |
| 信用积分 | 基于评价计算信用分 | P0 |
| 信用等级 | 根据积分划分等级 | P1 |
| 用户主页展示 | 显示用户信用和交易记录 | P0 |
| 好评率统计 | 统计用户好评率 | P1 |

#### 4.1.7 内容审核模块（管理端）

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 商品审核 | 审核新发布的商品 | P0 |
| 违规处理 | 下架违规商品，警告/封禁用户 | P0 |
| 举报处理 | 处理用户举报 | P1 |
| 敏感词过滤 | 自动过滤违规内容 | P1 |

#### 4.1.8 数据统计分析模块（管理端）

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 用户统计 | 注册量、活跃用户、新增用户 | P1 |
| 交易统计 | 交易量、交易额、成功率 | P1 |
| 商品统计 | 热门品类、发布量、成交量 | P1 |
| 数据可视化 | 图表展示各项统计指标 | P1 |
| 报表导出 | 导出Excel/CSV格式报表 | P2 |

---

### 4.2 信息架构

```
校园二手交易与共享平台
├── 首页
│   ├── 轮播图
│   ├── 热门推荐
│   ├── 最新发布
│   ├── 分类导航
│   └── 附近商品
├── 交易市场
│   ├── 二手商品
│   │   ├── 全部商品
│   │   ├── 分类筛选
│   │   └── 搜索结果
│   ├── 共享中心
│   │   ├── 可借用物品
│   │   └── 我的共享
│   └── 一键转赠
├── 发布
│   ├── 发布二手
│   └── 发布共享
├── 消息
│   ├── 聊天列表
│   ├── 系统通知
│   └── 交易提醒
├── 我的
│   ├── 个人信息
│   ├── 我的发布
│   ├── 我的订单
│   ├── 我的收藏
│   ├── 信用评价
│   └── 设置
└── 管理后台
    ├── 用户管理
    ├── 商品管理
    ├── 订单管理
    ├── 数据统计
    └── 系统设置
```

---

## 5. 非功能性需求

### 5.1 性能要求

| 指标 | 要求 |
|------|------|
| 响应时间 | 页面加载时间 < 2秒 |
| 并发支持 | 支持500+用户同时在线操作 |
| 系统可用性 | 99.9%以上 |
| 数据准确性 | 交易数据准确率100% |

### 5.2 安全要求

- 用户密码加密存储
- 学号实名认证，防止虚假账户
- 交易纠纷仲裁机制
- 敏感信息脱敏处理
- 防SQL注入、XSS攻击

### 5.3 兼容性要求

| 平台 | 支持版本 |
|------|---------|
| iOS | iOS 12.0+ |
| Android | Android 6.0+ |
| 微信小程序 | 基础库 2.0+ |
| H5 | 主流现代浏览器 |

### 5.4 易用性要求

- 界面简洁美观，操作流程清晰
- 核心功能步骤不超过3步
- 提供新手引导和帮助文档
- 支持夜间模式

### 5.5 可维护性要求

- 代码规范，注释完整
- 模块化设计，降低耦合
- 完善的日志记录
- 定期数据备份

---

## 6. 技术架构

### 6.1 整体架构

```
┌─────────────────────────────────────────────┐
│                用户端 (uniapp)               │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  │
│  │  iOS/    │  │ Android/ │  │  微信    │  │
│  │  Android │  │   H5     │  │  小程序  │  │
│  └──────────┘  └──────────┘  └──────────┘  │
└───────────────────┬─────────────────────────┘
                    │ HTTP/HTTPS
                    ▼
┌─────────────────────────────────────────────┐
│              后端服务 (SpringBoot)           │
│  ┌──────────────────────────────────────┐  │
│  │         Controller Layer             │  │
│  ├──────────────────────────────────────┤  │
│  │         Service Layer                │  │
│  ├──────────────────────────────────────┤  │
│  │         DAO Layer                    │  │
│  └──────────────────────────────────────┘  │
└───────────────────┬─────────────────────────┘
                    │
        ┌───────────┼───────────┐
        ▼           ▼           ▼
  ┌──────────┐ ┌──────────┐ ┌──────────┐
  │  MySQL   │ │   Redis  │ │  OSS/    │
  │ Database │ │  Cache   │ │  Local   │
  └──────────┘ └──────────┘ └──────────┘
```

### 6.2 核心技术选型

| 层次 | 技术选型 | 说明 |
|------|---------|------|
| 前端框架 | uniapp | 跨端开发，一套代码多端运行 |
| 后端框架 | SpringBoot | 快速开发，约定优于配置 |
| 数据库 | MySQL | 关系型数据库，支持事务 |
| 缓存 | Redis | 提升性能，减轻数据库压力 |
| 持久化 | MyBatis/MyBatis-Plus | ORM框架，简化数据库操作 |
| API文档 | Swagger/Knife4j | 接口文档自动生成 |
| 地图服务 | 高德地图/腾讯地图 | 位置服务和地图展示 |
| 即时通讯 | WebSocket | 实时消息推送 |

---

## 7. 数据模型

### 7.1 核心实体

#### 用户表 (user)
```
- user_id (主键)
- username (用户名)
- password (密码)
- nickname (昵称)
- avatar (头像)
- phone (手机号)
- student_id (学号)
- real_name (真实姓名)
- college (学院)
- major (专业)
- credit_score (信用分)
- status (状态: 0-正常 1-封禁)
- create_time
- update_time
```

#### 商品表 (product)
```
- product_id (主键)
- seller_id (卖家ID, 外键)
- title (商品标题)
- description (商品描述)
- category_id (分类ID)
- price (价格)
- original_price (原价)
- condition_level (成色: 1-10全新)
- cover_image_id (封面图片ID, 外键, 可为空)
- image_count (图片数量)
- location (交易地点)
- latitude (纬度)
- longitude (经度)
- status (状态: 0-下架 1-在售 2-已售 3-预订)
- view_count (浏览次数)
- favorite_count (收藏次数)
- create_time
- update_time
```

#### 商品图片表 (product_image)
```
- image_id (主键)
- product_id (商品ID, 外键)
- image_url (图片URL)
- image_thumbnail_url (缩略图URL)
- image_medium_url (中等尺寸图片URL)
- sort_order (排序顺序, 数字越小越靠前)
- file_size (文件大小, 字节)
- width (图片宽度)
- height (图片高度)
- is_cover (是否为封面: 0-否 1-是)
- status (状态: 0-正常 1-已删除)
- create_time
```

#### 共享物品表 (share_item)
```
- share_id (主键)
- owner_id (所有者ID, 外键)
- title (物品标题)
- description (描述)
- category_id (分类ID)
- deposit (押金)
- daily_rent (日租金)
- cover_image_id (封面图片ID, 外键, 可为空)
- image_count (图片数量)
- status (状态: 0-下架 1-可借用 2-借用中)
- available_times (可借用时间段, JSON)
- create_time
- update_time
```

#### 共享物品图片表 (share_item_image)
```
- image_id (主键)
- share_id (共享物品ID, 外键)
- image_url (图片URL)
- image_thumbnail_url (缩略图URL)
- image_medium_url (中等尺寸图片URL)
- sort_order (排序顺序)
- file_size (文件大小, 字节)
- width (图片宽度)
- height (图片高度)
- is_cover (是否为封面: 0-否 1-是)
- status (状态: 0-正常 1-已删除)
- create_time
```

#### 订单表 (order)
```
- order_id (主键)
- order_no (订单号)
- buyer_id (买家ID, 外键)
- seller_id (卖家ID, 外键)
- product_id (商品ID)
- share_id (共享物品ID)
- type (类型: 1-购买 2-共享)
- amount (交易金额)
- status (状态: 0-待确认 1-进行中 2-已完成 3-已取消 4-退款中)
- remark (备注)
- create_time
- update_time
- finish_time
```

#### 评价表 (evaluation)
```
- eval_id (主键)
- order_id (订单ID, 外键)
- from_user_id (评价人ID)
- to_user_id (被评价人ID)
- score (评分: 1-5星)
- content (评价内容)
- tags (标签, JSON: 如"发货快"、"描述准确")
- create_time
```

#### 会话表 (conversation)
```
- conversation_id (主键)
- conversation_type (会话类型: 1-单聊 2-群聊)
- user_id_1 (用户1ID, 单聊时使用)
- user_id_2 (用户2ID, 单聊时使用)
- related_order_id (关联订单ID, 可为空)
- last_message_id (最后一条消息ID)
- last_message_content (最后一条消息内容, 冗余字段便于列表展示)
- last_message_time (最后消息时间, 用于排序)
- unread_count_user1 (用户1的未读消息数)
- unread_count_user2 (用户2的未读消息数)
- is_muted_user1 (用户1是否免打扰: 0-否 1-是)
- is_muted_user2 (用户2是否免打扰: 0-否 1-是)
- remark_user1 (用户1对会话的备注名)
- remark_user2 (用户2对会话的备注名)
- is_archived_user1 (用户1是否归档: 0-否 1-是)
- is_archived_user2 (用户2是否归档: 0-否 1-是)
- status (状态: 0-正常 1-删除 2-置顶)
- create_time
- update_time
```

#### 消息表 (message)
```
- message_id (主键)
- conversation_id (会话ID, 外键)
- from_user_id (发送者ID)
- to_user_id (接收者ID)
- content (消息内容)
- type (类型: 1-文本 2-图片 3-商品卡片 4-订单卡片 5-系统通知 6-引用消息)
- parent_message_id (引用的父消息ID, 引用回复时使用, 可为空)
- is_read (是否已读: 0-未读 1-已读)
- read_time (阅读时间)
- send_status (发送状态: 0-发送中 1-成功 2-失败)
- delivered_time (送达时间)
- extra_data (扩展数据JSON, 存储卡片详细信息)
- reply_count (被回复次数, 用于热门消息统计)
- status (状态: 0-正常 1-撤回 2-删除)
- create_time
- update_time
```

#### 会话成员表 (conversation_member) - 群聊场景使用
```
- member_id (主键)
- conversation_id (会话ID, 外键)
- user_id (用户ID, 外键)
- nickname (群昵称)
- unread_count (未读消息数)
- is_muted (是否免打扰: 0-否 1-是)
- join_time (加入时间)
- last_read_message_id (最后阅读的消息ID)
```

#### 分类表 (category)
```
- category_id (主键)
- name (分类名称)
- parent_id (父分类ID)
- icon (图标)
- sort_order (排序)
- status (状态: 0-禁用 1-启用)
```

#### 黑名单表 (blacklist)
```
- blacklist_id (主键)
- user_id (用户ID, 外键)
- blocked_user_id (被拉黑的用户ID, 外键)
- reason (拉黑原因)
- create_time
```

#### 快捷回复模板表 (quick_reply)
```
- reply_id (主键)
- user_id (用户ID, 0表示系统预设)
- title (模板标题)
- content (回复内容)
- category (分类: 交易-询问/交易-确认/其他)
- sort_order (排序)
- is_system (是否系统预设: 0-否 1-是)
- create_time
```

#### 举报记录表 (report)
```
- report_id (主键)
- reporter_id (举报人ID, 外键)
- reported_user_id (被举报人ID, 外键)
- conversation_id (会话ID, 外键)
- message_id (消息ID, 外键, 可为空)
- reason (举报原因: 欺诈/骚扰/虚假信息/其他)
- description (描述)
- evidence_images (证据图片, JSON数组)
- status (处理状态: 0-待处理 1-已处理 2-已驳回)
- admin_note (管理员备注)
- create_time
- handle_time
```

#### 商品收藏表 (product_favorite)
```
- favorite_id (主键)
- user_id (用户ID, 外键)
- product_id (商品ID, 外键)
- create_time (收藏时间)
- 备注：用户可收藏感兴趣的商品，便于后续查看
```

#### 浏览历史表 (product_view_history)
```
- history_id (主键)
- user_id (用户ID, 外键)
- product_id (商品ID, 外键)
- view_time (浏览时间)
- view_duration (浏览时长, 秒, 可为空)
- 备注：用于记录用户浏览历史，支持智能推荐
```

#### 系统通知表 (system_notification)
```
- notification_id (主键)
- title (通知标题)
- content (通知内容, 支持富文本)
- type (通知类型: 1-系统公告 2-活动通知 3-账户提醒 4-交易提醒)
- target_type (目标类型: 1-全部用户 2-指定用户 3-指定等级)
- target_users (指定用户ID列表, JSON数组, target_type=2时使用)
- target_level (目标用户等级, target_type=3时使用)
- is_read (已读用户ID列表, JSON数组, 追踪已读用户)
- link_type (跳转链接类型: 1-无 2-网页 3-商品详情 4-订单详情)
- link_url (跳转URL)
- link_product_id (关联商品ID)
- link_order_id (关联订单ID)
- publish_time (发布时间)
- status (状态: 0-草稿 1-已发布 2-已撤回)
- priority (优先级: 1-低 2-中 3-高, 高优先级置顶显示)
- create_time
- update_time
```

#### 操作日志表 (operation_log)
```
- log_id (主键)
- user_id (操作用户ID, 外键, 0表示系统操作)
- username (用户名, 冗余字段)
- module (操作模块: user/product/order/share_item/system等)
- action (操作类型: login/create/update/delete/query/export等)
- description (操作描述)
- request_method (请求方法: GET/POST/PUT/DELETE)
- request_url (请求URL)
- request_params (请求参数, JSON)
- response_result (响应结果, JSON)
- ip_address (IP地址)
- user_agent (用户代理)
- execute_time (执行时长, 毫秒)
- status (执行状态: 1-成功 0-失败)
- error_message (错误信息, 失败时记录)
- create_time (操作时间)
- 备注：用于系统审计、问题排查、数据分析
```

#### 系统配置表 (system_config)
```
- config_id (主键)
- config_key (配置键, 唯一)
- config_value (配置值)
- config_type (配置类型: string/number/boolean/json)
- value_options (可选值列表, JSON, 如枚举类型的所有选项)
- description (配置说明)
- group_name (分组名称: basic/upload/payment/email/sms等)
- is_public (是否公开: 0-否 1-是, 公开的配置前端可访问)
- is_system (是否系统配置: 0-否 1-是, 系统配置不可删除)
- sort_order (排序)
- create_time
- update_time
- 备注：存储平台各种配置参数，无需修改代码即可调整系统行为
```

#### 敏感词表 (sensitive_word)
```
- word_id (主键)
- word (敏感词)
- type (类型: 1-禁止词 2-敏感词 3-替换词)
- replace_word (替换词, type=3时使用)
- level (等级: 1-一般 2-严重, 严重等级直接拦截)
- status (状态: 0-禁用 1-启用)
- create_time
- update_time
- 备注：用于内容过滤，维护平台内容安全
```

#### 轮播图表 (banner)
```
- banner_id (主键)
- title (轮播图标题)
- image_url (图片URL)
- image_thumbnail_url (缩略图URL)
- link_type (链接类型: 1-无 2-外链 3-商品详情 4-功能页面)
- link_url (跳转URL, 外链时使用)
- link_product_id (关联商品ID, link_type=3时使用)
- link_page_path (功能页面路径, link_type=4时使用, 如/pages/product/list)
- sort_order (排序, 数字越小越靠前)
- status (状态: 0-禁用 1-启用)
- start_time (开始展示时间)
- end_time (结束展示时间)
- click_count (点击次数, 统计用)
- exposure_count (曝光次数, 统计用)
- create_time
- update_time
- 备注：首页轮播图，支持营销活动、热门商品推广
```

#### 用户反馈表 (user_feedback)
```
- feedback_id (主键)
- user_id (用户ID, 外键, 可为空, 匿名反馈)
- contact (联系方式, 手机/邮箱)
- type (反馈类型: 1-功能建议 2-Bug反馈 3-投诉 4-其他)
- title (反馈标题)
- content (反馈内容)
- images (图片列表, JSON数组)
- status (处理状态: 0-待处理 1-处理中 2-已处理 3-已驳回)
- handler_id (处理人ID, 外键, 管理员)
- handle_note (处理备注)
- handle_time (处理时间)
- create_time
- update_time
- 备注：收集用户反馈，提升平台服务质量
```

### 7.2 ER关系图

```
用户 1 ---- N 商品
用户 1 ---- N 商品收藏
用户 1 ---- N 浏览历史
用户 1 ---- N 共享物品
用户 1 ---- N 订单 (作为买家)
用户 1 ---- N 订单 (作为卖家)
用户 1 ---- N 系统通知 (接收者)
用户 1 ---- N 操作日志
用户 1 ---- N 用户反馈
商品 1 ---- N 商品图片
商品 1 ---- 1 封面图片 (通过product_image表, is_cover=1)
商品 1 ---- N 商品收藏
商品 1 ---- N 浏览历史
商品 1 ---- N 订单
共享物品 1 ---- N 共享物品图片
共享物品 1 ---- 1 封面图片 (通过share_item_image表, is_cover=1)
共享物品 1 ---- N 订单
订单 1 ---- 1 评价
用户 1 ---- N 评价 (发出)
用户 1 ---- N 评价 (接收)
用户 1 ---- N 会话 (作为用户1)
用户 1 ---- N 会话 (作为用户2)
会话 1 ---- N 消息
会话 1 ---- 1 订单 (可选关联)
订单 1 ---- 1 会话 (可选关联)
用户 1 ---- N 消息 (发送)
用户 1 ---- N 消息 (接收)
会话 1 ---- N 会话成员 (群聊场景)
用户 1 ---- N 会话成员
用户 1 ---- N 黑名单 (拉黑他人)
用户 1 ---- N 黑名单 (被拉黑)
用户 1 ---- N 快捷回复模板
用户 1 ---- N 举报记录 (作为举报人)
用户 1 ---- N 举报记录 (作为被举报人)
分类 1 ---- N 商品
轮播图 N ---- 1 商品 (可选关联)
系统通知 N ---- 1 商品 (可选关联)
系统通知 N ---- 1 订单 (可选关联)
```

### 7.3 商品图片管理设计说明

#### 7.3.1 图片多尺寸存储
为提升加载速度和用户体验，图片上传后自动生成3种尺寸：
- **原图**：完整尺寸，用于查看大图
- **中等图**：800x800，用于商品详情页展示
- **缩略图**：200x200，用于列表页展示

**技术实现：**
```java
// 使用阿里云OSS图片处理服务
原图URL: https://oss.example.com/images/abc123.jpg
中等图: https://oss.example.com/images/abc123.jpg?x-oss-process=image/resize,w_800
缩略图: https://oss.example.com/images/abc123.jpg?x-oss-process=image/resize,w_200
```

#### 7.3.2 图片上传限制
| 限制项 | 限制值 | 说明 |
|--------|--------|------|
| 图片数量 | 1-9张 | 每个商品最多9张图片 |
| 单张大小 | ≤5MB | 限制单张图片大小 |
| 支持格式 | JPG/PNG/WebP | 仅支持常见图片格式 |
| 图片尺寸 | 建议800x800 | 保持正方形比例 |

#### 7.3.3 封面图机制
- **封面图标识**：`is_cover = 1` 的图片为封面
- **默认封面**：用户上传的第一张图片默认为封面
- **更换封面**：用户可随时更换任意图片为封面
- **封面同步**：`product.cover_image_id` 字段同步更新

#### 7.3.4 图片排序
- 使用 `sort_order` 字段控制显示顺序
- 上传时按上传顺序递增（1, 2, 3...）
- 用户可拖拽调整顺序，前端调用接口更新 `sort_order`

#### 7.3.5 图片删除逻辑
```
删除图片流程：
1. 用户点击删除某张图片
   ↓
2. 检查是否为封面图
   ├─ 是封面 → 提示"是否删除封面？" → 确认后删除并自动设置下一张为封面
   └─ 不是封面 → 直接删除
   ↓
3. 更新 product.image_count
   ↓
4. 如果最后一张图片被删除，商品显示默认占位图
   ↓
5. 图片记录 status = 1（已删除），不物理删除，便于恢复
```

#### 7.3.6 图片缓存策略
```
CDN缓存规则：
├─ 静态图片URL：永久缓存（URL包含文件hash）
├─ OSS处理URL：缓存1小时（带参数的URL）
└─ 图片更新后：通过URL参数版本号控制缓存（如 ?v=2）
```

#### 7.3.7 图片安全性
- **文件类型验证**：后端验证文件真实类型（非仅扩展名）
- **内容审核**：接入阿里云/腾讯云内容安全API，识别违规图片
- **防盗链**：OSS配置Referer白名单
- **水印**：可选择添加平台水印（用户可选）

---

### 7.4 聊天模块设计说明

#### 7.4.1 单聊场景
- 两个用户之间只存在一个会话（conversation）
- 会话表中 `user_id_1` 和 `user_id_2` 存储对话双方用户ID
- `unread_count_user1` 和 `unread_count_user2` 分别记录双方的未读消息数
- 支持会话与订单关联（`related_order_id`），便于查看订单相关聊天
- 支持会话备注（`remark_user1/remark_user2`），如"买羽毛球拍的人"
- 支持会话归档（`is_archived_user1/2`），不活跃会话移到归档区
- 支持免打扰（`is_muted_user1/2`），不接收通知但保留消息

#### 7.4.2 群聊场景
- 会话类型 `conversation_type = 2` 表示群聊
- 使用 `conversation_member` 表管理群成员
- 每个成员独立维护未读消息数和最后阅读位置
- 支持群昵称、免打扰等功能

#### 7.4.3 消息类型
1. **文本消息**：普通文字聊天
2. **图片消息**：发送商品图片等
3. **商品卡片**：从商品详情分享商品信息卡片，包含图片、标题、价格
4. **订单卡片**：发送订单信息卡片，点击可直接跳转到订单详情
5. **引用消息**：引用某条消息进行回复，显示被引用内容
6. **系统通知**：如"订单已创建"、"交易已完成"等系统自动发送的消息

**商品卡片数据结构（extra_data）：**
```json
{
  "type": "product_card",
  "product_id": "123",
  "title": "iPhone 13 128G",
  "price": 3500,
  "image": "https://...",
  "status": "在售"
}
```

**订单卡片数据结构（extra_data）：**
```json
{
  "type": "order_card",
  "order_id": "456",
  "order_no": "20260217001",
  "status": "待付款",
  "amount": 3500,
  "action_url": "订单详情页"
}
```

#### 7.4.4 未读消息机制
- 在会话表中维护未读计数（单聊）
- 或在会话成员表中维护未读计数（群聊）
- 用户查看会话时，批量更新消息已读状态
- 使用 `read_time` 记录消息阅读时间
- 支持消息已读回执，发送方可见对方是否已读

#### 7.4.5 消息状态追踪
消息发送状态流转：
```
发送中 (send_status=0)
  ↓
发送成功 (send_status=1) → 已送达 (delivered_time) → 已读 (is_read=1, read_time)
  ↓
发送失败 (send_status=2) → 可重发
```

#### 7.4.6 消息引用回复
- 通过 `parent_message_id` 关联被引用的消息
- 引用消息展示格式：显示被引用消息的摘要内容
- 支持嵌套引用，但UI上只显示一级引用

#### 7.4.7 消息撤回机制
- 消息发送后2分钟内可撤回
- 撤回后消息状态改为 `status = 1`（撤回）
- 客户端显示"XXX撤回了一条消息"
- 对方已读的消息无法撤回（前端判断）

#### 7.4.8 会话列表排序
- 使用 `last_message_time` 字段进行倒序排列
- 最新消息的会话排在最前面
- 置顶会话固定在顶部（`status = 2`）
- 归档会话单独显示在"归档"区域

#### 7.4.9 快捷回复功能
- 系统预设常用回复模板（`is_system = 1`）：
  - 交易询问类："还在吗？"、"能便宜点吗？"、"可以面交吗？"
  - 交易确认类："好的，没问题"、"已下单，请尽快发货"、"收到货了"
  - 其他类："不好意思，不需要了"
- 用户可自定义快捷回复（`user_id > 0`）
- 支持分类管理，方便快速选择

#### 7.4.10 黑名单与举报机制
- **黑名单**：拉黑后对方无法发送消息，会话列表隐藏
- **举报功能**：支持举报欺诈、骚扰、虚假信息等行为
- **证据提交**：可上传聊天记录截图作为证据
- **管理员处理**：后台可查看举报记录并处理

---

## 8. 核心接口设计

### 8.1 用户相关接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/user/register | POST | 用户注册 |
| /api/user/login | POST | 用户登录 |
| /api/user/profile | GET/PUT | 获取/更新个人信息 |
| /api/user/{id} | GET | 获取用户公开信息 |

### 8.2 商品相关接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/product/list | GET | 获取商品列表 |
| /api/product/{id} | GET | 获取商品详情 |
| /api/product/publish | POST | 发布商品 |
| /api/product/{id} | PUT | 更新商品信息 |
| /api/product/{id} | DELETE | 删除商品 |
| /api/product/search | GET | 搜索商品 |

#### 8.2.1 商品图片管理接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/product/{id}/images | GET | 获取商品所有图片 |
| /api/product-image/upload | POST | 上传商品图片 |
| /api/product-image/{id}/delete | DELETE | 删除商品图片 |
| /api/product-image/{id}/set-cover | PUT | 设置为封面图 |
| /api/product-image/reorder | PUT | 调整图片排序 |
| /api/image/upload | POST | 通用图片上传（返回OSS URL） |

**上传商品图片示例：**
```
POST /api/product-image/upload
Body (FormData):
- productId: "123"
- image: (File)
- sortOrder: 1

Response:
{
  "code": 200,
  "data": {
    "image_id": "456",
    "image_url": "https://oss.example.com/images/abc123.jpg",
    "thumbnail_url": "https://oss.example.com/images/abc123.jpg?x-oss-process=image/resize,w_200",
    "medium_url": "https://oss.example.com/images/abc123.jpg?x-oss-process=image/resize,w_800",
    "sort_order": 1,
    "is_cover": true
  }
}
```

**设置封面图示例：**
```
PUT /api/product-image/{id}/set-cover
Response:
{
  "code": 200,
  "message": "封面设置成功"
}
```

**调整图片排序示例：**
```
PUT /api/product-image/reorder
Body:
{
  "productId": "123",
  "imageOrders": [
    {"image_id": "456", "sort_order": 1},
    {"image_id": "457", "sort_order": 2},
    {"image_id": "458", "sort_order": 3}
  ]
}
```

#### 8.2.2 共享物品图片管理接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/share-item/{id}/images | GET | 获取共享物品所有图片 |
| /api/share-item-image/upload | POST | 上传共享物品图片 |
| /api/share-item-image/{id}/delete | DELETE | 删除共享物品图片 |
| /api/share-item-image/{id}/set-cover | PUT | 设置为封面图 |
| /api/share-item-image/reorder | PUT | 调整图片排序 |

**图片上传流程：**
```
1. 前端选择图片
   ↓
2. 客户端压缩（可选，限制最大5MB）
   ↓
3. 调用 /api/image/upload 获取OSS上传凭证
   ↓
4. 直接上传到阿里云OSS
   ↓
5. OSS返回图片URL
   ↓
6. 调用 /api/product-image/upload 保存图片记录
   ↓
7. 返回图片信息（包含多尺寸URL）
```

### 8.3 订单相关接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/order/create | POST | 创建订单 |
| /api/order/list | GET | 获取订单列表 |
| /api/order/{id} | GET | 获取订单详情 |
| /api/order/{id}/cancel | PUT | 取消订单 |
| /api/order/{id}/complete | PUT | 完成订单 |

### 8.4 评价相关接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/evaluation/submit | POST | 提交评价 |
| /api/evaluation/user/{id} | GET | 获取用户评价列表 |

### 8.5 聊天相关接口

#### 8.5.1 会话管理接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/conversation/list | GET | 获取会话列表（排除归档） |
| /api/conversation/archived | GET | 获取归档会话列表 |
| /api/conversation/{id} | GET | 获取会话详情 |
| /api/conversation/create | POST | 创建会话 |
| /api/conversation/{id}/delete | DELETE | 删除会话 |
| /api/conversation/{id}/top | PUT | 置顶/取消置顶会话 |
| /api/conversation/{id}/mute | PUT | 开启/关闭免打扰 |
| /api/conversation/{id}/archive | PUT | 归档/取消归档 |
| /api/conversation/{id}/remark | PUT | 设置会话备注 |
| /api/conversation/get-or-create | POST | 获取或创建与指定用户的会话 |
| /api/conversation/by-order/{orderId} | GET | 根据订单ID获取关联会话 |

**请求示例 - 获取会话列表：**
```
GET /api/conversation/list
Response:
{
  "code": 200,
  "data": [
    {
      "conversation_id": "1",
      "user": {
        "user_id": "2",
        "nickname": "张三",
        "avatar": "https://..."
      },
      "remark": "买羽毛球拍的", // 备注
      "last_message": "还在吗？",
      "last_message_time": "2026-02-17 20:30:00",
      "unread_count": 2,
      "is_muted": false,
      "is_pinned": true,
      "related_order": {
        "order_id": "123",
        "status": "进行中"
      }
    }
  ]
}
```

#### 8.5.2 消息管理接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/message/history | GET | 获取历史消息（分页） |
| /api/message/send | POST | 发送消息（文本/图片/卡片） |
| /api/message/send-with-quote | POST | 引用回复发送消息 |
| /api/message/{id}/read | PUT | 标记消息已读 |
| /api/message/batch-read | PUT | 批量标记会话消息已读 |
| /api/message/{id}/recall | DELETE | 撤回消息（2分钟内） |
| /api/message/{id}/resend | POST | 重新发送失败的消息 |
| /api/message/unread-count | GET | 获取总未读消息数 |
| /api/message/search | GET | 搜索历史消息 |

**请求示例 - 发送商品卡片：**
```
POST /api/message/send
Body:
{
  "conversation_id": "1",
  "type": 3, // 商品卡片
  "extra_data": {
    "type": "product_card",
    "product_id": "123",
    "title": "iPhone 13 128G",
    "price": 3500,
    "image": "https://...",
    "status": "在售"
  }
}
```

**请求示例 - 引用回复：**
```
POST /api/message/send-with-quote
Body:
{
  "conversation_id": "1",
  "parent_message_id": "456", // 被引用的消息ID
  "type": 1, // 文本消息
  "content": "好的，没问题"
}
```

#### 8.5.3 快捷回复接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/quick-reply/list | GET | 获取快捷回复列表 |
| /api/quick-reply/{id} | GET | 获取快捷回复详情 |
| /api/quick-reply/create | POST | 创建自定义快捷回复 |
| /api/quick-reply/{id} | PUT | 更新快捷回复 |
| /api/quick-reply/{id} | DELETE | 删除快捷回复 |
| /api/quick-reply/category/{category} | GET | 按分类获取快捷回复 |

**请求示例 - 获取快捷回复：**
```
GET /api/quick-reply/list
Response:
{
  "code": 200,
  "data": [
    {
      "reply_id": "1",
      "title": "询问商品",
      "content": "还在吗？",
      "category": "交易-询问",
      "is_system": true
    },
    {
      "reply_id": "2",
      "title": "确认交易",
      "content": "好的，没问题",
      "category": "交易-确认",
      "is_system": true
    }
  ]
}
```

#### 8.5.4 黑名单接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/blacklist/list | GET | 获取黑名单列表 |
| /api/blacklist/add | POST | 添加用户到黑名单 |
| /api/blacklist/{id}/remove | DELETE | 移出黑名单 |
| /api/blacklist/check/{userId} | GET | 检查用户是否在黑名单中 |

#### 8.5.5 举报接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/report/submit | POST | 提交举报 |
| /api/report/{id} | GET | 获取举报详情 |
| /api/report/my-reports | GET | 获取我的举报记录 |

**请求示例 - 提交举报：**
```
POST /api/report/submit
Body:
{
  "reported_user_id": "2",
  "conversation_id": "1",
  "message_id": "456",
  "reason": "欺诈",
  "description": "对方虚假发货",
  "evidence_images": ["https://...", "https://..."]
}
```

#### 8.5.6 WebSocket实时通信

| 事件名 | 方向 | 描述 | 数据格式 |
|--------|------|------|---------|
| message | 服务端→客户端 | 接收新消息 | {message_id, conversation_id, content, type, from_user} |
| message_read | 服务端→客户端 | 消息已读回执 | {message_id, read_time} |
| message_delivered | 服务端→客户端 | 消息已送达 | {message_id, delivered_time} |
| message_recalled | 服务端→客户端 | 消息已撤回 | {message_id, conversation_id} |
| typing | 双向 | 输入状态提示 | {conversation_id, is_typing: true/false} |
| online | 双向 | 在线状态通知 | {user_id, online: true/false} |
| unread_count | 服务端→客户端 | 未读数更新 | {total_count, conversation_unread: [{conv_id, count}]} |

**WebSocket连接：**
```
URL: wss://api.example.com/ws?token={jwt_token}

客户端发送：
{
  "event": "typing",
  "data": {
    "conversation_id": "1",
    "is_typing": true
  }
}

服务端推送：
{
  "event": "message",
  "data": {
    "message_id": "789",
    "conversation_id": "1",
    "content": "你好",
    "type": 1,
    "from_user": {
      "user_id": "2",
      "nickname": "张三"
    },
    "create_time": "2026-02-17 20:35:00"
  }
}
```

---

### 8.6 商品收藏接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/favorite/list | GET | 获取我的收藏列表 |
| /api/favorite/add | POST | 添加收藏 |
| /api/favorite/{id}/remove | DELETE | 取消收藏 |
| /api/favorite/check/{productId} | GET | 检查是否已收藏 |

**添加收藏示例：**
```
POST /api/favorite/add
Body:
{
  "product_id": "123"
}

Response:
{
  "code": 200,
  "message": "收藏成功"
}
```

---

### 8.7 浏览历史接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/history/list | GET | 获取浏览历史列表 |
| /api/history/add | POST | 记录浏览行为 |
| /api/history/clear | DELETE | 清空浏览历史 |
| /api/history/{id}/delete | DELETE | 删除单条浏览记录 |

**获取浏览历史示例：**
```
GET /api/history/list?page=1&size=20
Response:
{
  "code": 200,
  "data": {
    "total": 50,
    "list": [
      {
        "history_id": "1",
        "product": {
          "product_id": "123",
          "title": "iPhone 13 128G",
          "price": 3500,
          "cover_image": "https://..."
        },
        "view_time": "2026-02-17 20:30:00",
        "view_duration": 120
      }
    ]
  }
}
```

---

### 8.8 系统通知接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/notification/list | GET | 获取通知列表 |
| /api/notification/{id} | GET | 获取通知详情 |
| /api/notification/{id}/read | PUT | 标记通知已读 |
| /api/notification/read-all | PUT | 全部标记已读 |
| /api/notification/unread-count | GET | 获取未读通知数 |

**获取通知列表示例：**
```
GET /api/notification/list
Response:
{
  "code": 200,
  "data": [
    {
      "notification_id": "1",
      "title": "系统维护通知",
      "content": "<p>平台将于今晚22:00-24:00进行系统维护...</p>",
      "type": 1,
      "priority": 3,
      "is_read": false,
      "publish_time": "2026-02-17 18:00:00",
      "link": {
        "type": 1,
        "url": null
      }
    }
  ]
}
```

---

### 8.9 轮播图管理接口

#### 用户端接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/banner/list | GET | 获取轮播图列表 |

#### 管理端接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/admin/banner/list | GET | 获取所有轮播图 |
| /api/admin/banner/create | POST | 创建轮播图 |
| /api/admin/banner/{id} | PUT | 更新轮播图 |
| /api/admin/banner/{id} | DELETE | 删除轮播图 |
| /api/admin/banner/{id}/stats | GET | 获取轮播图统计数据 |

**创建轮播图示例：**
```
POST /api/admin/banner/create
Body:
{
  "title": "开学季大促",
  "image_url": "https://oss.example.com/banners/promo.jpg",
  "link_type": 3,
  "link_product_id": "123",
  "sort_order": 1,
  "start_time": "2026-02-20 00:00:00",
  "end_time": "2026-02-28 23:59:59",
  "status": 1
}
```

---

### 8.10 用户反馈接口

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/feedback/submit | POST | 提交反馈 |
| /api/feedback/my-list | GET | 获取我的反馈列表 |
| /api/feedback/{id} | GET | 获取反馈详情 |

**提交反馈示例：**
```
POST /api/feedback/submit
Body:
{
  "type": 1,
  "title": "建议增加夜间模式",
  "content": "希望平台能增加夜间模式，晚上使用更舒适",
  "images": ["https://...", "https://..."],
  "contact": "13800138000"
}
```

---

### 8.11 系统配置接口（管理端）

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/admin/config/list | GET | 获取配置列表 |
| /api/admin/config/{key} | GET | 获取单个配置 |
| /api/admin/config/{key} | PUT | 更新配置 |
| /api/admin/config/create | POST | 创建配置 |
| /api/admin/config/{key}/delete | DELETE | 删除配置 |
| /api/public/config | GET | 获取公开配置（前端可调用） |

**获取公开配置示例：**
```
GET /api/public/config
Response:
{
  "code": 200,
  "data": {
    "app_name": "校园易购",
    "upload_max_size": 5242880,
    "upload_allowed_types": ["jpg", "jpeg", "png", "webp"],
    "customer_service_tel": "400-123-4567",
    "version": "1.0.0"
  }
}
```

---

### 8.12 敏感词管理接口（管理端）

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/admin/sensitive-word/list | GET | 获取敏感词列表 |
| /api/admin/sensitive-word/add | POST | 添加敏感词 |
| /api/admin/sensitive-word/{id} | PUT | 更新敏感词 |
| /api/admin/sensitive-word/{id} | DELETE | 删除敏感词 |
| /api/admin/sensitive-word/check | POST | 检测文本是否包含敏感词 |

**检测敏感词示例：**
```
POST /api/admin/sensitive-word/check
Body:
{
  "text": "这是一个测试文本"
}

Response:
{
  "code": 200,
  "data": {
    "has_sensitive": false,
    "filtered_text": "这是一个测试文本",
    "detected_words": []
  }
}
```

---

### 8.13 操作日志接口（管理端）

| 接口 | 方法 | 描述 |
|------|------|------|
| /api/admin/log/list | GET | 获取操作日志列表（分页） |
| /api/admin/log/{id} | GET | 获取日志详情 |
| /api/admin/log/stats | GET | 获取日志统计数据 |
| /api/admin/log/export | POST | 导出日志 |

**查询日志示例：**
```
GET /api/admin/log/list?module=product&action=create&page=1&size=20
Response:
{
  "code": 200,
  "data": {
    "total": 156,
    "list": [
      {
        "log_id": "1",
        "user_id": "2",
        "username": "张三",
        "module": "product",
        "action": "create",
        "description": "发布商品：iPhone 13 128G",
        "ip_address": "192.168.1.100",
        "status": 1,
        "create_time": "2026-02-17 20:30:00"
      }
    ]
  }
}
```

---

## 9. 页面原型

### 9.1 主要页面列表

| 页面名称 | 功能描述 |
|---------|---------|
| 首页 | 展示轮播图、分类导航、热门商品、最新发布 |
| 商品列表 | 商品筛选、搜索、列表展示 |
| 商品详情 | 商品图文信息、卖家信息、立即购买/收藏 |
| 发布商品 | 表单填写，上传图片，设置价格和位置 |
| 订单详情 | 订单状态、商品信息、交易进度 |
| 聊天页面 | 即时通讯界面 |
| 个人中心 | 用户信息、功能入口 |
| 我的发布 | 管理已发布的商品 |
| 我的订单 | 订单列表和状态筛选 |
| 搜索页面 | 搜索历史、热门搜索、搜索结果 |

---

## 10. 项目进度规划

| 阶段 | 时间 | 主要工作内容 |
|------|------|-------------|
| 需求分析 | 2025.12.12-2026.1.10 | 查阅文献，调研平台方案，分析功能需求 |
| 方案设计 | 2026.1.11-2026.02.20 | 撰写开题报告，完成技术方案设计 |
| 架构设计 | 2026.02.21-2026.03.20 | 前后端架构设计、数据库设计、原型设计 |
| 开发实现 | 2026.03.21-2026.04.15 | 代码编写、接口集成、单元测试、中期检查 |
| 测试优化 | 2026.04.16-2026.04.29 | 集成测试、性能优化、bug修复 |
| 论文撰写 | 2026.04.30-2026.05.06 | 撰写论文、修改完善 |
| 答辩准备 | 2026.05.07-2026.05.20 | 制作PPT、预答辩、正式答辩 |

---

## 11. 风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|---------|
| 用户活跃度低 | 平台无法正常运行 | 加强推广，与学生会、社团合作 |
| 交易纠纷 | 用户体验差，信誉受损 | 建立完善的评价和仲裁机制 |
| 并发性能问题 | 系统卡顿崩溃 | 数据库优化，Redis缓存，负载均衡 |
| 数据安全 | 用户隐私泄露 | 加密存储，权限控制，定期安全审计 |

---

## 12. 成功指标

### 12.1 功能完成度
- ✅ 所有P0级功能100%实现
- ✅ 所有P1级功能80%以上实现
- ✅ 核心流程可正常演示

### 12.2 性能指标
- ✅ 页面响应时间 < 2秒
- ✅ 支持500+并发用户
- ✅ 系统稳定性 > 99%

### 12.3 论文质量
- ✅ 论文格式符合学校规范
- ✅ 语句通顺，无错别字
- ✅ 内容完整，逻辑清晰

---

## 附录

### A. 参考文献列表
1. 赵壮.推荐算法在校园二手交易平台中的研究与应用[D].武汉轻工大学,2021.
2. 饶锎月,鲍懿喜.服务主导逻辑下垂直二手交易平台设计研究——以"多抓鱼"为例[J].设计,2024,37(11):118-121.
3. 赵俊杰,葛敬军,朱文婷.基于微信小程序的校园二手书交易平台的设计与实现[J].科技与创新,2024,(09):7-11+15.
4. 蒋瑞霞,王莉.基于地理位置的校园二手交易平台设计与实现[J].物联网技术,2024,14(04):73-76.
5. 梅忠.基于UniApp与Spring Boot的校友平台设计与实现[J].信息记录材料,2025,26(11):113-116.

### B. 术语表
| 术语 | 解释 |
|------|------|
| SpringBoot | 基于Spring框架的快速开发框架 |
| uniapp | 基于Vue.js的跨端应用开发框架 |
| 前后端分离 | 前端和后端独立开发，通过API交互 |
| ORM | 对象关系映射，将数据库表映射为对象 |
| Redis | 内存数据库，用作缓存 |
| WebSocket | 全双工通信协议，用于即时通讯 |

### D. 系统配置清单

#### D.1 基础配置 (basic)
| 配置键 | 配置值 | 类型 | 说明 | 是否公开 |
|--------|--------|------|------|---------|
| app_name | 校园易购 | string | 应用名称 | 是 |
| app_version | 1.0.0 | string | 应用版本号 | 是 |
| icp_license | 粤ICP备xxxxxxxx号 | string | ICP备案号 | 是 |
| copyright | © 2026 校园易购 | string | 版权信息 | 是 |

#### D.2 上传配置 (upload)
| 配置键 | 配置值 | 类型 | 说明 | 是否公开 |
|--------|--------|------|------|---------|
| upload_max_size | 5242880 | number | 最大上传文件大小(5MB) | 是 |
| upload_max_count | 9 | number | 单次最多上传数量 | 是 |
| upload_allowed_types | jpg,jpeg,png,webp | string | 允许的文件类型 | 是 |
| oss_bucket_name | campus-trade | string | OSS存储桶名称 | 否 |
| oss_access_key | ******** | string | OSS访问密钥 | 否 |
| oss_secret_key | ******** | string | OSS密钥 | 否 |
| oss_endpoint | oss-cn-guangzhou.aliyuncs.com | string | OSS端点 | 否 |

#### D.3 支付配置 (payment)
| 配置键 | 配置值 | 类型 | 说明 | 是否公开 |
|--------|--------|------|------|---------|
| payment_enabled | true | boolean | 是否启用支付功能 | 是 |
| alipay_app_id | ******** | string | 支付宝应用ID | 否 |
| wechat_pay_mch_id | ******** | string | 微信支付商户号 | 否 |
| min_order_amount | 0.01 | number | 最小订单金额 | 是 |
| max_order_amount | 50000 | number | 最大订单金额 | 是 |

#### D.4 邮件配置 (email)
| 配置键 | 配置值 | 类型 | 说明 | 是否公开 |
|--------|--------|------|------|---------|
| email_host | smtp.qq.com | string | SMTP服务器地址 | 否 |
| email_port | 587 | number | SMTP端口 | 否 |
| email_username | noreply@example.com | string | 发件邮箱 | 否 |
| email_password | ******** | string | 邮箱密码/授权码 | 否 |
| email_from_name | 校园易购 | string | 发件人名称 | 否 |

#### D.5 短信配置 (sms)
| 配置键 | 配置值 | 类型 | 说明 | 是否公开 |
|--------|--------|------|------|---------|
| sms_enabled | true | boolean | 是否启用短信功能 | 是 |
| sms_provider | aliyun | string | 短信服务商(aliyun/tencent) | 否 |
| sms_access_key | ******** | string | 短信服务AccessKey | 否 |
| sms_secret_key | ******** | string | 短信服务SecretKey | 否 |
| sms_sign_name | 校园易购 | string | 短信签名 | 否 |
| sms_template_code | SMS_123456789 | string | 验证码模板ID | 否 |

#### D.6 安全配置 (security)
| 配置键 | 配置值 | 类型 | 说明 | 是否公开 |
|--------|--------|------|------|---------|
| password_min_length | 6 | number | 密码最小长度 | 是 |
| password_max_length | 20 | number | 密码最大长度 | 是 |
| token_expire_time | 604800 | number | Token过期时间(秒,7天) | 是 |
| refresh_token_expire_time | 2592000 | number | 刷新Token过期时间(秒,30天) | 是 |
| max_login_attempts | 5 | number | 最大登录尝试次数 | 是 |
| login_lock_time | 1800 | number | 登录锁定时长(秒,30分钟) | 是 |

#### D.7 业务配置 (business)
| 配置键 | 配置值 | 类型 | 说明 | 是否公开 |
|--------|--------|------|------|---------|
| product_max_images | 9 | number | 商品最大图片数 | 是 |
| product_title_max_length | 50 | number | 商品标题最大长度 | 是 |
| product_desc_max_length | 2000 | number | 商品描述最大长度 | 是 |
| order_auto_close_time | 1800 | number | 订单自动关闭时间(秒,30分钟) | 是 |
| order_auto_finish_time | 604800 | number | 订单自动完成时间(秒,7天) | 是 |
| share_item_max_days | 30 | number | 共享物品最长借用天数 | 是 |
| credit_score_initial | 100 | number | 初始信用分数 | 是 |

#### D.8 内容审核配置 (content)
| 配置键 | 配置值 | 类型 | 说明 | 是否公开 |
|--------|--------|------|------|---------|
| sensitive_word_enabled | true | boolean | 是否启用敏感词过滤 | 是 |
| sensitive_word_replace | *** | string | 敏感词替换字符 | 是 |
| image_audit_enabled | true | boolean | 是否启用图片审核 | 是 |
| user_realname_required | false | boolean | 是否要求实名认证 | 是 |
| product_audit_required | false | boolean | 商品是否需要审核后发布 | 是 |

### E. 数据库索引设计建议

#### E.1 必须创建的索引
```sql
-- 用户表
CREATE UNIQUE INDEX idx_username ON user(username);
CREATE INDEX idx_phone ON user(phone);
CREATE INDEX idx_student_id ON user(student_id);

-- 商品表
CREATE INDEX idx_seller_id ON product(seller_id);
CREATE INDEX idx_category_id ON product(category_id);
CREATE INDEX idx_status ON product(status);
CREATE INDEX idx_create_time ON product(create_time);
CREATE INDEX idx_price ON product(price);
CREATE FULLTEXT INDEX idx_title_desc ON product(title, description);

-- 商品图片表
CREATE INDEX idx_product_id ON product_image(product_id);
CREATE INDEX idx_is_cover ON product_image(is_cover);

-- 订单表
CREATE UNIQUE INDEX idx_order_no ON order(order_no);
CREATE INDEX idx_buyer_id ON order(buyer_id);
CREATE INDEX idx_seller_id ON order(seller_id);
CREATE INDEX idx_status ON order(status);
CREATE INDEX idx_create_time ON order(create_time);

-- 会话表
CREATE INDEX idx_user_id_1 ON conversation(user_id_1);
CREATE INDEX idx_user_id_2 ON conversation(user_id_2);
CREATE INDEX idx_last_message_time ON conversation(last_message_time);

-- 消息表
CREATE INDEX idx_conversation_id ON message(conversation_id);
CREATE INDEX idx_from_user_id ON message(from_user_id);
CREATE INDEX idx_create_time ON message(create_time);

-- 商品收藏表
CREATE UNIQUE INDEX idx_user_product ON product_favorite(user_id, product_id);
CREATE INDEX idx_user_id ON product_favorite(user_id);

-- 浏览历史表
CREATE INDEX idx_user_product_view ON product_view_history(user_id, product_id);
CREATE INDEX idx_view_time ON product_view_history(view_time);

-- 操作日志表
CREATE INDEX idx_user_id ON operation_log(user_id);
CREATE INDEX idx_module ON operation_log(module);
CREATE INDEX idx_action ON operation_log(action);
CREATE INDEX idx_create_time ON operation_log(create_time);
```

### F. 接口响应码规范

#### F.1 成功响应
| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 201 | 创建成功 |

#### F.2 客户端错误 (4xx)
| 状态码 | 说明 |
|--------|------|
| 400 | 请求参数错误 |
| 401 | 未登录/Token无效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 409 | 资源冲突(如重复收藏) |
| 422 | 参数验证失败 |
| 429 | 请求过于频繁 |

#### F.3 服务端错误 (5xx)
| 状态码 | 说明 |
|--------|------|
| 500 | 服务器内部错误 |
| 501 | 功能未实现 |
| 503 | 服务暂时不可用 |

#### F.4 业务错误码
| 错误码 | 说明 |
|--------|------|
| 10001 | 用户名已存在 |
| 10002 | 手机号已注册 |
| 10003 | 密码错误 |
| 10004 | 用户已被封禁 |
| 20001 | 商品不存在 |
| 20002 | 商品已下架 |
| 20003 | 商品已售出 |
| 30001 | 订单不存在 |
| 30002 | 订单状态错误 |
| 30003 | 订单已超时 |
| 40001 | 会话不存在 |
| 40002 | 消息已撤回 |
| 50001 | 敏感词违规 |
| 50002 | 图片审核不通过 |

### C. 版本历史

| 版本 | 日期 | 修改内容 | 修改人 |
|------|------|---------|--------|
| v1.0 | 2026-02-17 | 初始版本创建 | 许佳宜 |
| v1.1 | 2026-02-17 | 补充8张数据库表：商品收藏、浏览历史、系统通知、操作日志、系统配置、敏感词、轮播图、用户反馈；补充商品图片独立表；补充聊天功能完善设计；补充7个接口模块；补充系统配置清单；补充索引设计和响应码规范 | 许佳宜 |

---

**文档结束**
