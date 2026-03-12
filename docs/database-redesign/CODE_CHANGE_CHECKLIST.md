# 代码修改检查清单

**项目**：校园二手交易与共享平台 - User 表重构
**创建时间**：2026-03-12
**使用说明**：按顺序完成每一项修改，勾选 ✅ 表示已完成

---

## Part 1: 数据库层修改

### Step 1.1: 执行 SQL 迁移脚本

- [ ] 1. 在测试环境执行 `user_tables_redesign.sql`
- [ ] 2. 验证所有表已创建：`SHOW TABLES LIKE 'user%';`
- [ ] 3. 验证表结构：`SHOW CREATE TABLE user;`
- [ ] 4. 验证数据迁移：`SELECT COUNT(*) FROM user;`

### Step 1.2: 验证索引

```sql
-- 执行以下 SQL 验证索引
SHOW INDEX FROM user_follow;
SHOW INDEX FROM blacklist;
SHOW INDEX FROM user;
```

- [ ] user_follow 的唯一索引包含 deleted 字段
- [ ] blacklist 的唯一索引包含 deleted 字段
- [ ] 所有表的索引已正确创建

---

## Part 2: Entity 层修改

### Step 2.1: 修改 User.java

**文件**：`src/main/java/com/xx/xianqijava/entity/User.java`

#### 需要移除的字段

```java
// ❌ 删除以下字段
- private String realName;
- private String studentId;
- private String college;
- private String major;
- private Integer creditScore;
- private Integer isVerified;
- private LocalDateTime lastLoginTime;
- private Integer phoneSearchEnabled;
- private Integer locationEnabled;
```

- [ ] 移除 `realName` 字段
- [ ] 移除 `studentId` 字段
- [ ] 移除 `college` 字段
- [ ] 移除 `major` 字段
- [ ] 移除 `creditScore` 字段
- [ ] 移除 `isVerified` 字段
- [ ] 移除 `lastLoginTime` 字段
- [ ] 移除 `phoneSearchEnabled` 字段
- [ ] 移除 `locationEnabled` 字段

#### 需要添加的字段

```java
// ✅ 新增字段
+ private Integer phoneVerified;  // 手机号是否已验证：0-否，1-是
+ private String statusReason;     // 账号状态原因
```

- [ ] 添加 `phoneVerified` 字段
- [ ] 添加 `statusReason` 字段

#### 需要调整的字段映射

```java
// ⚙️ 调整字段映射
@TableField(value = "created_at", fill = FieldFill.INSERT)
private LocalDateTime createTime;  // 映射到 created_at

// 注意：user 表不再有 updated_at，移除 updateTime 字段
```

- [ ] 调整 `createTime` 字段映射到 `created_at`
- [ ] 移除 `updateTime` 字段（user 表已删除 `updated_at`）

---

### Step 2.2: 新建 UserProfile.java

**文件**：`src/main/java/com/xx/xianqijava/entity/UserProfile.java`

```java
package com.xx.xianqijava.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户资料扩展表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_profile")
public class UserProfile extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId
    private Long userId;

    private String realName;
    private Integer gender;
    private LocalDate birthday;
    private String bio;
    private String location;
    private String homepage;
    private String tags;

    // 统计数据
    private Integer followersCount;
    private Integer followingCount;
    private Integer productsCount;
    private Integer favoritesCount;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] 创建 `UserProfile.java` 文件
- [ ] 添加所有字段
- [ ] 继承 `BaseEntity`

---

### Step 2.3: 修改 UserRealNameAuth.java

**文件**：`src/main/java/com/xx/xianqijava/entity/UserRealNameAuth.java`

#### 需要添加的字段

```java
// ✅ 新增字段
+ private String idCardFront;        // 身份证正面图片
+ private String idCardBack;         // 身份证背面图片
+ private String authProvider;       // 认证提供商
+ private String authTransactionId;  // 第三方认证流水号
```

- [ ] 添加 `idCardFront` 字段
- [ ] 添加 `idCardBack` 字段
- [ ] 添加 `authProvider` 字段
- [ ] 添加 `authTransactionId` 字段

#### 需要调整的字段映射

```java
// ⚙️ 调整字段映射
@TableField(value = "created_at", fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updateTime;
```

- [ ] 调整 `createTime` 字段映射到 `created_at`
- [ ] 调整 `updateTime` 字段映射到 `updated_at`

---

### Step 2.4: 修改 UserStudentAuth.java

**文件**：`src/main/java/com/xx/xianqijava/entity/UserStudentAuth.java`

#### 需要调整的字段类型

```java
// ⚙️ 调整字段类型
- private String educationLevel;
+ private Integer educationLevel;  // 1-专科，2-本科，3-硕士，4-博士
```

- [ ] 将 `educationLevel` 类型从 `String` 改为 `Integer`

#### 需要添加的字段

```java
// ✅ 新增字段
+ private String enrollmentCertificate;  // 录取通知书/学信网截图
```

- [ ] 添加 `enrollmentCertificate` 字段

#### 需要调整的字段映射

```java
// ⚙️ 调整字段映射
@TableField(value = "created_at", fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updateTime;
```

- [ ] 调整 `createTime` 字段映射到 `created_at`
- [ ] 调整 `updateTime` 字段映射到 `updated_at`

---

### Step 2.5: 修改 UserCreditExt.java

**文件**：`src/main/java/com/xx/xianqijava/entity/UserCreditExt.java`

#### 需要移除的字段

```java
// ❌ 删除不存在的字段（表中已移除 credit_level）
- private String creditLevel;
```

- [ ] 移除 `creditLevel` 字段（注意：表中已删除此字段）

#### 需要添加的字段

```java
// ✅ 新增字段
+ private LocalDateTime createdAt;
+ private LocalDateTime updatedAt;
```

- [ ] 添加 `createdAt` 字段
- [ ] 添加 `updatedAt` 字段

---

### Step 2.6: 修改 LoginDevice.java

**文件**：`src/main/java/com/xx/xianqijava/entity/LoginDevice.java`

#### 需要添加的字段

```java
// ✅ 新增字段
+ private String loginLocation;  // 登录地点
+ private LocalDateTime createdAt;
+ private LocalDateTime updatedAt;
```

- [ ] 添加 `loginLocation` 字段
- [ ] 添加 `createdAt` 字段
- [ ] 添加 `updatedAt` 字段

#### 需要重命名的字段

```java
// ⚙️ 重命名字段
- private String lastLoginIp;
+ private String loginIp;  // 统一命名风格
```

- [ ] 将 `lastLoginIp` 重命名为 `loginIp`

---

### Step 2.7: 修改 UserPreference.java

**文件**：`src/main/java/com/xx/xianqijava/entity/UserPreference.java`

#### 需要添加的字段

```java
// ✅ 新增隐私设置字段
+ private Integer showOnlineStatus;       // 显示在线状态
+ private Integer allowStrangerMessage;   // 允许陌生人私信
+ private Integer phoneSearchEnabled;     // 允许手机号搜索（从 user 表移过来）
+ private Integer locationEnabled;        // 显示位置信息（从 user 表移过来）
+ private String extraSettings;           // JSON 格式的扩展设置

// ✅ 新增时间字段
+ private LocalDateTime createdAt;
+ private LocalDateTime updatedAt;
```

- [ ] 添加 `showOnlineStatus` 字段
- [ ] 添加 `allowStrangerMessage` 字段
- [ ] 添加 `phoneSearchEnabled` 字段
- [ ] 添加 `locationEnabled` 字段
- [ ] 添加 `extraSettings` 字段
- [ ] 添加 `createdAt` 字段
- [ ] 添加 `updatedAt` 字段

#### 需要移除的字段

```java
// ❌ 删除字段（合并到 theme）
- private Integer autoDarkMode;
```

- [ ] 移除 `autoDarkMode` 字段

---

### Step 2.8: 修改 UserAddress.java

**文件**：`src/main/java/com/xx/xianqijava/entity/UserAddress.java`

#### 需要添加的字段

```java
// ✅ 新增字段
+ private LocalDateTime createdAt;
+ private LocalDateTime updatedAt;
+ private Integer deleted;  // 逻辑删除
```

- [ ] 添加 `createdAt` 字段
- [ ] 添加 `updatedAt` 字段
- [ ] 添加 `deleted` 字段

#### 需要移除的字段

```java
// ❌ 删除字段（使用 deleted 代替）
- private Integer status;
```

- [ ] 移除 `status` 字段

---

### Step 2.9: 修改 UserFollow.java

**文件**：`src/main/java/com/xx/xianqijava/entity/UserFollow.java`

#### 需要添加的字段

```java
// ✅ 新增字段
+ private LocalDateTime createdAt;
+ private LocalDateTime updatedAt;
+ private Integer deleted;  // 逻辑删除
```

- [ ] 添加 `createdAt` 字段
- [ ] 添加 `updatedAt` 字段
- [ ] 添加 `deleted` 字段

---

### Step 2.10: 修改 Blacklist.java

**文件**：`src/main/java/com/xx/xianqijava/entity/Blacklist.java`

#### 需要添加的字段

```java
// ✅ 新增字段
+ private Integer deleted;  // 逻辑删除
```

- [ ] 添加 `deleted` 字段

#### 需要调整的字段映射

```java
// ⚙️ 调整字段映射
@TableField(value = "created_at", fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updateTime;
```

- [ ] 调整 `createTime` 字段映射到 `created_at`
- [ ] 调整 `updateTime` 字段映射到 `updated_at`

---

## Part 3: Mapper 层修改

### Step 3.1: 新建 UserProfileMapper.java

**文件**：`src/main/java/com/xx/xianqijava/mapper/UserProfileMapper.java`

```java
package com.xx.xianqijava.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xx.xianqijava.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
```

- [ ] 创建 `UserProfileMapper.java` 文件

---

## Part 4: Service 层修改

### Step 4.1: 新建 UserProfileService.java

**文件**：`src/main/java/com/xx/xianqijava/service/UserProfileService.java`

```java
package com.xx.xianqijava.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xx.xianqijava.entity.UserProfile;

public interface UserProfileService extends IService<UserProfile> {
}
```

- [ ] 创建 `UserProfileService.java` 文件

### Step 4.2: 新建 UserProfileServiceImpl.java

**文件**：`src/main/java/com/xx/xianqijava/service/impl/UserProfileServiceImpl.java`

```java
package com.xx.xianqijava.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xx.xianqijava.entity.UserProfile;
import com.xx.xianqijava.mapper.UserProfileMapper;
import com.xx.xianqijava.service.UserProfileService;
import org.springframework.stereotype.Service;

@Service
public class UserProfileServiceImpl extends ServiceImpl<UserProfileMapper, UserProfile>
        implements UserProfileService {
}
```

- [ ] 创建 `UserProfileServiceImpl.java` 文件

### Step 4.3: 修改 UserService.java

**文件**：`src/main/java/com/xx/xianqijava/service/UserService.java`

#### 需要调整的方法

```java
// ⚙️ 调整返回类型（如果需要返回更多信息）
- UserVO getUserInfo(Long userId);
+ UserFullVO getUserInfo(Long userId);
```

- [ ] 调整 `getUserInfo` 方法的返回类型

### Step 4.4: 修改 UserServiceImpl.java

**文件**：`src/main/java/com/xx/xianqijava/service/impl/UserServiceImpl.java`

#### 需要调整的查询逻辑

```java
// ⚙️ 旧代码
public UserVO getUserInfo(Long userId) {
    User user = userMapper.selectById(userId);
    return BeanUtil.copyProperties(user, UserVO.class);
}

// ⚙️ 新代码
@Autowired
private UserProfileService userProfileService;

@Autowired
private UserRealNameAuthService realNameAuthService;

@Autowired
private UserStudentAuthService studentAuthService;

public UserFullVO getUserInfo(Long userId) {
    // 1. 查询用户基础信息
    User user = userMapper.selectById(userId);

    // 2. 查询用户资料
    UserProfile profile = userProfileService.getById(userId);

    // 3. 查询认证状态
    UserRealNameAuth realnameAuth = realNameAuthService.getOne(
        new QueryWrapper<UserRealNameAuth>().eq("user_id", userId)
    );

    UserStudentAuth studentAuth = studentAuthService.getOne(
        new QueryWrapper<UserStudentAuth>().eq("user_id", userId)
    );

    // 4. 组装返回对象
    UserFullVO vo = new UserFullVO();
    vo.setUser(BeanUtil.copyProperties(user, UserDTO.class));
    vo.setProfile(BeanUtil.copyProperties(profile, UserProfileDTO.class));
    vo.setRealnameStatus(realnameAuth != null ? realnameAuth.getStatus() : 0);
    vo.setStudentStatus(studentAuth != null ? studentAuth.getStatus() : 0);

    return vo;
}
```

- [ ] 注入 `UserProfileService`
- [ ] 注入认证相关的 Service
- [ ] 调整 `getUserInfo` 方法实现
- [ ] 调整其他需要关联查询的方法

---

## Part 5: Controller 层修改

### Step 5.1: 修改 UserController.java

**文件**：`src/main/java/com/xx/xianqijava/controller/UserController.java`

#### 需要调整的接口

```java
// ⚙️ 调整返回类型
@GetMapping("/{id}")
- public Result<UserVO> getUserInfo(@PathVariable Long id) {
+ public Result<UserFullVO> getUserInfo(@PathVariable Long id) {
     UserFullVO vo = userService.getUserInfo(id);
     return Result.success(vo);
}
```

- [ ] 调整接口返回类型

---

## Part 6: DTO/VO 层修改

### Step 6.1: 新建 UserFullVO.java

**文件**：`src/main/java/com/xx/xianqijava/dto/vo/UserFullVO.java`

```java
package com.xx.xianqijava.dto.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户完整信息")
public class UserFullVO {

    @Schema(description = "用户基础信息")
    private UserDTO user;

    @Schema(description = "用户资料")
    private UserProfileDTO profile;

    @Schema(description = "实名认证状态")
    private Integer realnameStatus;

    @Schema(description = "学生认证状态")
    private Integer studentStatus;
}
```

- [ ] 创建 `UserFullVO.java` 文件

### Step 6.2: 新建 UserProfileDTO.java

**文件**：`src/main/java/com/xx/xianqijava/dto/dto/UserProfileDTO.java`

```java
package com.xx.xianqijava.dto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户资料")
public class UserProfileDTO {

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "生日")
    private String birthday;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "位置")
    private String location;

    @Schema(description = "个人主页")
    private String homepage;

    @Schema(description = "个人标签")
    private String tags;

    @Schema(description = "粉丝数")
    private Integer followersCount;

    @Schema(description = "关注数")
    private Integer followingCount;

    @Schema(description = "发布商品数")
    private Integer productsCount;

    @Schema(description = "收藏数")
    private Integer favoritesCount;
}
```

- [ ] 创建 `UserProfileDTO.java` 文件

### Step 6.3: 修改 UserPreferenceDTO.java

**文件**：`src/main/java/com/xx/xianqijava/dto/dto/UserPreferenceDTO.java`

#### 需要添加的字段

```java
// ✅ 新增字段
+ private Integer showOnlineStatus;
+ private Integer allowStrangerMessage;
+ private Integer phoneSearchEnabled;
+ private Integer locationEnabled;
+ private String extraSettings;
```

- [ ] 添加 `showOnlineStatus` 字段
- [ ] 添加 `allowStrangerMessage` 字段
- [ ] 添加 `phoneSearchEnabled` 字段
- [ ] 添加 `locationEnabled` 字段
- [ ] 添加 `extraSettings` 字段

---

## Part 7: 前端修改

### Step 7.1: 修改用户资料页面

**文件**：`XianQiUniapp/src/pages/mine/profile.vue`

- [ ] 调整用户信息展示字段
- [ ] 调整认证信息展示逻辑
- [ ] 测试页面显示正常

### Step 7.2: 修改用户主页

**文件**：`XianQiUniapp/src/pages/user/home.vue`

- [ ] 调整用户信息查询接口
- [ ] 处理新的返回数据结构
- [ ] 测试页面显示正常

### Step 7.3: 修改 API 接口

**文件**：`XianQiUniapp/src/api/user.js`

- [ ] 调整用户信息查询接口
- [ ] 调整隐私设置相关接口
- [ ] 测试接口调用正常

---

## Part 8: 测试验证

### Step 8.1: 单元测试

- [ ] 编写 UserService 单元测试
- [ ] 编写 UserProfileService 单元测试
- [ ] 编写认证相关 Service 单元测试
- [ ] 所有单元测试通过

### Step 8.2: 接口测试

- [ ] 测试用户信息查询接口
- [ ] 测试用户资料更新接口
- [ ] 测试认证相关接口
- [ ] 测试隐私设置接口
- [ ] 所有接口测试通过

### Step 8.3: 功能测试

- [ ] 测试用户注册功能
- [ ] 测试用户登录功能
- [ ] 测试实名认证功能
- [ ] 测试学生认证功能
- [ ] 测试关注功能
- [ ] 测试黑名单功能
- [ ] 所有功能测试通过

---

## 完成统计

### Entity 层

- [ ] User.java
- [ ] UserProfile.java (新建)
- [ ] UserRealNameAuth.java
- [ ] UserStudentAuth.java
- [ ] UserCreditExt.java
- [ ] LoginDevice.java
- [ ] UserPreference.java
- [ ] UserAddress.java
- [ ] UserFollow.java
- [ ] Blacklist.java

**进度**：0/10

### Mapper 层

- [ ] UserProfileMapper.java (新建)

**进度**：0/1

### Service 层

- [ ] UserProfileService.java (新建)
- [ ] UserProfileServiceImpl.java (新建)
- [ ] UserService.java
- [ ] UserServiceImpl.java

**进度**：0/4

### Controller 层

- [ ] UserController.java

**进度**：0/1

### DTO/VO 层

- [ ] UserFullVO.java (新建)
- [ ] UserProfileDTO.java (新建)
- [ ] UserPreferenceDTO.java

**进度**：0/3

---

## 备注

- 每完成一项，请在对应的 [ ] 中填写 ✅
- 遇到问题时，请在下方记录：

**问题记录**：

1.
2.
3.

---

*检查清单版本：v1.0*
*最后更新：2026-03-12*
