# 字符编码问题修复指南

## 问题描述
会话列表的最后消息内容部分显示的不是中文，出现乱码。

## 根本原因
1. **MySQL客户端字符集配置不正确**
   - `character_set_client = latin1`（应该是 utf8mb4）
   - `character_set_connection = latin1`（应该是 utf8mb4）
   - `character_set_results = latin1`（应该是 utf8mb4）

2. **JDBC连接字符集参数**
   - 需要使用 `characterEncoding=UTF-8`（Java支持的标准字符集名称）
   - MySQL会自动将UTF-8映射到utf8mb4

## 已修复的配置

### 1. application.yml
```yaml
# 数据源配置
datasource:
  driver-class-name: com.mysql.cj.jdbc.Driver
  url: jdbc:mysql://localhost:3306/XianQi?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

**注意**：
- ❌ 错误：`characterEncoding=utf8mb4`（Java不支持）
- ✅ 正确：`characterEncoding=UTF-8`（Java标准字符集名称）

### 2. MySQL服务器字符集设置
```sql
-- 临时设置（重启后失效）
SET GLOBAL character_set_client = utf8mb4;
SET GLOBAL character_set_connection = utf8mb4;
SET GLOBAL character_set_results = utf8mb4;
```

### 3. 永久修复MySQL字符集（推荐）
编辑MySQL配置文件（macOS: `/opt/homebrew/etc/my.cnf`）：

```ini
[client]
default-character-set = utf8mb4

[mysql]
default-character-set = utf8mb4

[mysqld]
character-set-server = utf8mb4
collation-server = utf8mb4_unicode_ci
init-connect = 'SET NAMES utf8mb4'
```

然后重启MySQL服务：
```bash
brew services restart mysql
```

## 验证步骤

### 1. 检查MySQL字符集
```bash
mysql -uroot -p123456 Xianqi -e "SHOW VARIABLES LIKE 'character%';"
```

期望输出应该显示：
- `character_set_client = utf8mb4`
- `character_set_connection = utf8mb4`
- `character_set_results = utf8mb4`
- `character_set_database = utf8mb4`
- `character_set_server = utf8mb4`

### 2. 检查数据库数据
```bash
mysql -uroot -p123456 Xianqi -e "SELECT conversation_id, last_message_content FROM conversation LIMIT 5;"
```

中文应该正常显示。

### 3. 测试API响应
```bash
curl -s "http://localhost:8080/api/conversation?page=1&size=3"
```

返回的JSON中的中文应该正常显示。

### 4. 前端清除缓存
```javascript
// 在浏览器控制台执行
uni.clearStorageSync()
location.reload()
```

或者在uniapp中重新编译项目。

## 当前状态

✅ **已修复**：
- application.yml 中的JDBC连接URL已更新为 `characterEncoding=UTF-8`
- 后端应用已重启

⚠️ **待验证**：
- 前端清除缓存并重新加载数据
- 检查会话列表是否正确显示中文

## 如果问题仍然存在

### 方案1：前端数据转换
如果后端返回的数据仍有问题，可以在前端进行字符转换：

```typescript
// 在 src/pages/message/index.vue 中
const conversations = ref<Conversation[]>([])

// 数据加载后进行字符修复
async function loadData() {
  try {
    const res = await messageApi.getConversationList()
    conversations.value = res.data.list.map(conv => ({
      ...conv,
      lastMessageContent: fixChineseEncoding(conv.lastMessageContent)
    }))
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

// 修复中文编码的辅助函数
function fixChineseEncoding(text: string | undefined): string {
  if (!text) return ''
  // 如果文本包含乱码特征，尝试修复
  try {
    // 将ISO-8859-1编码的字节转换为UTF-8
    const bytes = new TextEncoder().encode(text)
    return new TextDecoder('utf-8').decode(bytes)
  } catch {
    return text
  }
}
```

### 方案2：后端统一字符编码
在后端Controller中添加字符编码过滤器：

```java
@Configuration
public class CharsetConfig {
    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> charsetFilter() {
        FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<>();
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        registration.setFilter(filter);
        registration.addUrlPatterns("/*");
        return registration;
    }
}
```

### 方案3：数据库数据修复
如果数据库中的数据已经损坏，重新插入正确编码的数据：

```sql
-- 检查是否有乱码
SELECT conversation_id, HEX(last_message_content) as content_hex
FROM conversation
WHERE conversation_id <= 10;

-- 如果确实有乱码，删除并重新插入
DELETE FROM conversation WHERE conversation_id IN (1, 2, 3);

-- 重新插入数据（使用之前创建的SQL文件）
source /Users/nickxiao/11project/XianQiJava/insert_conversations_and_messages_fixed.sql;
```

## 总结

字符编码问题的核心是确保整个数据流的字符集一致性：

**数据库** (utf8mb4) → **JDBC连接** (UTF-8) → **Java应用** (UTF-8) → **JSON响应** (UTF-8) → **前端显示** (UTF-8)

任何一个环节的字符集不匹配都可能导致乱码。
