# XianQi 后端项目迁移指南

## 📦 迁移步骤

### 第一步：在 Mac 上导出

```bash
# 进入项目目录
cd /Users/nickxiao/11project/XianQiJava

# 运行导出脚本
./scripts/export_for_migration.sh
```

脚本会自动：
- 导出 MySQL 数据库
- 复制项目代码
- 复制上传文件（如果有）
- 打包成 `xianqi_backup_YYYYMMDD_HHMMSS.tar.gz`

### 第二步：传输到 Windows

将生成的 `.tar.gz` 文件复制到 Windows 机器，可以使用：
- U盘
- 网络共享
- 云盘
- 其他文件传输方式

### 第三步：在 Windows 上导入

1. 解压 `.tar.gz` 文件（使用 7-Zip 或 WinRAR）

2. 双击运行 `import_windows.bat`

3. 按照提示完成安装

---

## ⚙️ 环境要求

### 必须安装

| 软件 | 版本 | 下载地址 |
|------|------|----------|
| JDK | 17+ | https://adoptium.net/ |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/mysql/ |
| Redis | 最新 | https://github.com/tporadowski/redis/releases |

### 可选安装

| 软件 | 用途 |
|------|------|
| Maven | 构建工具（项目自带 mvnw） |
| IntelliJ IDEA | 开发工具 |

---

## 🔧 配置修改

导入完成后，检查并修改 `application.yml`：

```yaml
# 数据库密码（如果不同）
spring:
  datasource:
    password: 你的密码

# Redis 密码（如果设置了）
  data:
    redis:
      password: 你的Redis密码

# 文件上传路径（Windows 格式）
file:
  upload:
    path: C:/xianqi-uploads
```

---

## 🚀 启动项目

### 方式一：使用启动脚本

```cmd
双击 XianQiJava\start.bat
```

### 方式二：使用 Maven

```cmd
cd XianQiJava
mvnw.cmd spring-boot:run
```

### 方式三：打包运行

```cmd
cd XianQiJava
mvnw.cmd clean package
java -jar target\XianQiJava-0.0.1-SNAPSHOT.jar
```

---

## ✅ 验证安装

启动成功后访问：

- **API 文档**: http://localhost:8080/api/doc.html
- **健康检查**: http://localhost:8080/api/public/health

---

## ❓ 常见问题

### Q: MySQL 连接失败？

1. 检查 MySQL 服务是否启动
   - Win+R → services.msc → 找到 MySQL

2. 检查密码是否正确

3. 检查端口 3306 是否被占用
   ```cmd
   netstat -ano | findstr 3306
   ```

### Q: 端口 8080 被占用？

1. 查看占用进程
   ```cmd
   netstat -ano | findstr 8080
   ```

2. 结束进程或修改 `application.yml` 中的端口

### Q: Redis 连接失败？

1. 检查 Redis 服务是否启动
   ```cmd
   redis-cli ping
   ```

2. 如果不需要缓存，在 `application.yml` 中禁用：
   ```yaml
   spring:
     autoconfigure:
       exclude:
         - org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration
   ```

---

## 📞 技术支持

如有问题，请检查：
1. 日志文件: `XianQiJava/logs/xianqi-java.log`
2. 控制台输出
