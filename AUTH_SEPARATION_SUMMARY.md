# 用户认证分离实施总结

## 完成情况

### ✅ 数据库层
**文件**: [auth_tables_migration.sql](src/main/resources/db/auth_tables_migration.sql)

创建了两个新的认证表：
1. `user_real_name_auth` - 实名认证表
2. `user_student_auth` - 学生认证表

### ✅ Entity层
**文件**:
- [UserRealNameAuth.java](src/main/java/com/xx/xianqijava/entity/UserRealNameAuth.java)
- [UserStudentAuth.java](src/main/java/com/xx/xianqijava/entity/UserStudentAuth.java)

### ✅ Mapper层
**文件**:
- [UserRealNameAuthMapper.java](src/main/java/com/xx/xianqijava/mapper/UserRealNameAuthMapper.java)
- [UserStudentAuthMapper.java](src/main/java/com/xx/xianqijava/mapper/UserStudentAuthMapper.java)

### ✅ DTO/VO层
**文件**:
- [RealNameAuthSubmitDTO.java](src/main/java/com/xx/xianqijava/dto/RealNameAuthSubmitDTO.java)
- [StudentAuthSubmitDTO.java](src/main/java/com/xx/xianqijava/dto/StudentAuthSubmitDTO.java)
- [RealNameAuthVO.java](src/main/java/com/xx/xianqijava/vo/RealNameAuthVO.java)
- [StudentAuthVO.java](src/main/java/com/xx/xianqijava/vo/StudentAuthVO.java)

### ✅ Service层
**文件**:
- [UserRealNameAuthService.java](src/main/java/com/xx/xianqijava/service/UserRealNameAuthService.java)
- [UserRealNameAuthServiceImpl.java](src/main/java/com/xx/xianqijava/service/impl/UserRealNameAuthServiceImpl.java)
- [UserStudentAuthService.java](src/main/java/com/xx/xianqijava/service/UserStudentAuthService.java)
- [UserStudentAuthServiceImpl.java](src/main/java/com/xx/xianqijava/service/impl/UserStudentAuthServiceImpl.java)

### ✅ Controller层
**文件**:
- [UserAuthController.java](src/main/java/com/xx/xianqijava/controller/UserAuthController.java)

### ✅ 工具类
**文件**:
- [AESUtil.java](src/main/java/com/xx/xianqijava/util/AESUtil.java) - 用于敏感信息加密

### ✅ 更新的文件
**文件**:
- [User.java](src/main/java/com/xx/xianqijava/entity/User.java) - 添加了realNameStatus和studentStatus字段
- [UserInfoVO.java](src/main/java/com/xx/xianqijava/vo/UserInfoVO.java) - 添加了realNameStatus和studentStatus字段
- [ErrorCode.java](src/main/java/com/xx/xianqijava/common/ErrorCode.java) - 添加了认证相关错误码

---

## API接口

### 实名认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /user/verification/real-name | 提交实名认证 |
| GET | /user/verification/real-name | 获取实名认证信息 |

### 学生认证接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /user/verification/student | 提交学生认证 |
| GET | /user/verification/student | 获取学生认证信息 |

---

## 数据库执行步骤

### 1. 执行SQL脚本
```bash
mysql -u root -p Xianqi < src/main/resources/db/auth_tables_migration.sql
```

### 2. 检查user表字段
确保user表包含以下字段：
```sql
DESCRIBE user;
```

应该看到：
- real_name_status
- student_status

---

## 配置说明

### AES加密密钥配置
在 `application.yml` 中添加：
```yaml
auth:
  aes:
    secret-key: "your-16-byte-secret-key"
```

---

## 认证状态说明

### 实名认证状态 (real_name_status)
- `0` - 未认证
- `1` - 审核中
- `2` - 已认证
- `3` - 认证失败

### 学生认证状态 (student_status)
- `0` - 未认证
- `1` - 审核中
- `2` - 已认证
- `3` - 认证失败

---

## 后续步骤

1. **执行数据库脚本**
   ```bash
   cd /Users/nickxiao/11project/XianQiJava
   mysql -u root -p Xianqi < src/main/resources/db/auth_tables_migration.sql
   ```

2. **编译项目**
   ```bash
   ./mvnw clean compile
   ```

3. **启动应用测试**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **测试API接口**
   - 提交实名认证：POST `/user/verification/real-name`
   - 获取实名认证：GET `/user/verification/real-name`
   - 提交学生认证：POST `/user/verification/student`
   - 获取学生认证：GET `/user/verification/student`

---

## 注意事项

1. **数据迁移**：如果已有认证数据，需要执行SQL脚本中的数据迁移部分
2. **兼容性**：旧的 `user_verification` 表和 `UserVerification` 实体类暂时保留，建议验证新接口后逐步废弃
3. **安全**：身份证号使用AES加密存储，请确保密钥安全
4. **审核功能**：后续可添加管理端的审核接口，使用Service中的 `auditAuth` 方法

---

## 实现日期
2026-03-05
