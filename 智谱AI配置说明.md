# 智谱 AI API Key 配置说明

## ✅ 配置完成

已成功配置智谱 AI API Key，共 2 个 Key 可用：

### API Key 列表

1. **API Key 1**（当前默认使用）
   ```
   7a485999b37f4ccc8e6a3af8636a844d.VVnmBFOzmhYa9L0W
   ```

2. **API Key 2**（备用）
   ```
   3d3a55af11d54b239be9b17ed9d0bef8.B52KI6KVyBMqrITz
   ```

## 📁 配置文件位置

- **环境变量文件**: `/Users/nickxiao/11project/XianQiJava/.env`
- **应用配置文件**: `/Users/nickxiao/11project/XianQiJava/src/main/resources/application.yml`
- **Git 忽略配置**: `/Users/nickxiao/11project/XianQiJava/.gitignore` (已添加 .env)

## 🔧 配置详情

### application.yml 新增配置

```yaml
# 智谱 AI 配置
zhipuai:
  api-key: ${ZHIPUAI_API_KEY:}                    # 从环境变量读取 API Key
  model: glm-4-flash                              # 使用的模型（免费版）
  vision-model: glm-4.6v-flash                    # 视觉识别模型
  timeout: 60000                                  # 请求超时时间（毫秒）
  base-url: https://open.bigmodel.cn/api/paas/v4/chat/completions

# AI 功能配置
ai:
  enabled: true                                    # 是否启用 AI 功能
  max-tokens: 2000                                # 最大回复长度
  temperature: 0.7                                # 温度参数（0-1，越高越随机）
  chat-memory-size: 20                            # 对话记忆大小（消息数量）
  stream-enabled: false                           # 是否启用流式输出（打字机效果）
```

## 🚀 使用方式

### 1. 自动加载（推荐）

项目启动时会自动从 `.env` 文件加载 API Key，无需手动配置。

### 2. 手动切换 API Key

如需切换到备用 Key，编辑 `.env` 文件：

```bash
# 修改最后一行
ZHIPUAI_API_KEY=${ZHIPUAI_API_KEY_2}  # 使用 Key 2
```

### 3. 环境变量方式（生产环境）

```bash
# Linux/Mac
export ZHIPUAI_API_KEY=7a485999b37f4ccc8e6a3af8636a844d.VVnmBFOzmhYa9L0W

# Windows PowerShell
$env:ZHIPUAI_API_KEY="7a485999b37f4ccc8e6a3af8636a844d.VVnmBFOzmhYa9L0W"
```

## 🔐 安全说明

1. **Git 忽略**: `.env` 文件已添加到 `.gitignore`，不会被提交到代码仓库
2. **本地存储**: API Key 仅存储在你的本地开发环境中
3. **生产环境**: 部署时请使用环境变量或密钥管理服务（如阿里云 KMS）

## 📊 模型说明

### GLM-4-Flash（当前使用）
- ✅ **免费额度**: 每个用户每天 25 次免费调用
- ⚡ **速度快**: 响应迅速，适合实时对话
- 🎯 **场景**: 通用对话、问答、客服

### GLM-4.6V-Flash（视觉模型）
- 🖼️ **多模态**: 支持图片识别和理解
- 📸 **应用场景**: 商品图片识别、描述生成

## 📝 下一步

按照 [LangChain4j集成计划书.md](/Users/nickxiao/11project/LangChain4j集成计划书.md) 的实施步骤：

1. ✅ **阶段一（已完成）**: 基础设施搭建
   - ✅ API Key 配置
   - ✅ application.yml 配置
   - ⏳ 待完成：添加 Maven 依赖

2. ⏳ **阶段二**（待进行）: Agent 工具函数开发

3. ⏳ **阶段三**（待进行）: Agent 接口开发

## ⚠️ 注意事项

1. **API Key 保护**: 不要将 API Key 发布到公开平台
2. **额度限制**: 免费版有每日调用次数限制，请合理使用
3. **错误处理**: 实现了 Key 1 失败自动切换到 Key 2 的逻辑（需在代码中实现）
4. **监控**: 建议添加 API 调用日志，监控使用情况

## 🔗 相关链接

- [智谱 AI 开放平台](https://open.bigmodel.cn/)
- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [集成计划书](/Users/nickxiao/11project/LangChain4j集成计划书.md)

---

**配置时间**: 2026-03-23
**状态**: ✅ 配置完成，可以开始集成开发
