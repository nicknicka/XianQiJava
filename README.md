# 校园二手交易与共享平台 - 后端

**作者**：许佳宜
**学号**：2022035123021
**专业**：计算机科学与技术(职教师资)
**指导教师**：温清机

---

## 项目简介

这是一个面向高校学生的垂直化校园二手交易与物品共享平台，提供"交易+共享"双模式服务。后端使用 Spring Boot 4.0.2 + MyBatis-Plus + MySQL + Redis 构建。

---

## 技术栈

- **框架**：Spring Boot 4.0.2
- **Java 版本**：17
- **构建工具**：Maven (使用 Maven Wrapper)
- **数据库**：MySQL
- **缓存**：Redis
- **持久化**：MyBatis-Plus 3.5.7
- **API 文档**：Knife4j 4.5.0
- **安全认证**：Spring Security + JWT
- **即时通讯**：WebSocket

---

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 2. 数据库初始化

1. 创建数据库：
```bash
mysql -u root -p < src/main/resources/sql/schema.sql
```

2. 确认数据库名为 `Xianqi`，用户名为 `root`，密码为 `123456`

### 3. 修改配置

如需修改数据库或 Redis 连接信息，请编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/Xianqi?...
    username: root
    password: 123456

  data:
    redis:
      host: localhost
      port: 6379
```

### 4. 构建项目

```bash
./mvnw clean package
```

### 5. 运行应用

```bash
./mvnw spring-boot:run
```

或者直接运行打包后的 JAR：

```bash
java -jar target/XianQiJava-0.0.1-SNAPSHOT.jar
```

### 6. 访问应用

- **应用地址**：http://localhost:8080/api
- **API 文档**：http://localhost:8080/api/doc.html
- **健康检查**：http://localhost:8080/api/public/health

---

## 项目结构

```
com.xx.xianqijava
├── common/              # 公共类
│   ├── Result.java      # 统一响应结果
│   └── ErrorCode.java   # 错误码枚举
├── config/              # 配置类
├── controller/          # 控制器层
├── dto/                 # 数据传输对象
├── entity/              # 数据库实体
├── enums/               # 枚举类
├── exception/           # 异常处理
├── mapper/              # 数据访问层
├── security/            # 安全配置
├── service/             # 服务层
├── util/                # 工具类
├── vo/                  # 视图对象
└── websocket/           # WebSocket
```

---

## 核心功能模块

### 已完成基础架构

- [x] 项目基础结构搭建
- [x] 数据库表设计（22张表）
- [x] 实体类创建
- [x] 统一响应和异常处理
- [x] JWT 认证
- [x] MyBatis-Plus 配置
- [x] Redis 配置
- [x] WebSocket 支持
- [x] API 文档（Knife4j）

### 待开发功能

- [ ] 用户管理模块
- [ ] 商品管理模块
- [ ] 物品共享模块
- [ ] 交易订单模块
- [ ] 即时通讯模块
- [ ] 信用评价模块
- [ ] 内容审核模块
- [ ] 数据统计分析模块

---

## API 接口规范

### 基础路径

- 公开接口：`/api/public/*`
- 用户接口：`/api/*`（需要认证）
- 管理接口：`/api/admin/*`（需要管理员权限）

### 响应格式

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

### 认证方式

在请求头中添加：

```
Authorization: Bearer {token}
```

---

## 开发指南

### 创建新的 Controller

```java
@Tag(name = "模块名称")
@RestController
@RequestMapping("/module")
public class ModuleController {

    @Operation(summary = "接口说明")
    @GetMapping("/action")
    public Result<?> action() {
        return Result.success();
    }
}
```

### 创建新的 Service

```java
public interface ModuleService {
    // 接口定义
}

@Service
public class ModuleServiceImpl implements ModuleService {
    // 实现
}
```

### 使用 MyBatis-Plus

```java
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承 BaseMapper 获得基础 CRUD 方法
}
```

---

## 常见问题

### Q: 如何修改数据库密码？
A: 编辑 `src/main/resources/application.yml`，修改 `spring.datasource.password`

### Q: 如何关闭 Redis？
A: 在配置文件中设置 `spring.data.redis.enabled=false`，或注释掉 Redis 相关配置

### Q: 如何查看生成的 API 文档？
A: 启动应用后访问 http://localhost:8080/api/doc.html

---

## 许可证

本项目仅用于学习和教学目的。

---

**联系方式**：如有问题请联系指导教师或项目负责人。
