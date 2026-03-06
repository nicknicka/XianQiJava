# 测试字符编码配置

## 当前配置（application.yml）

```yaml
datasource:
  url: jdbc:mysql://localhost:3306/XianQi?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&connectionCollation=utf8mb4_unicode_ci
  hikari:
    connection-init-sql: SET NAMES utf8mb4
    connection-test-query: SELECT 1
```

## 验证步骤

1. **重启后端应用**
   - 方法1：在IntelliJ IDEA中点击重启按钮
   - 方法2：命令行执行 `./mvnw spring-boot:run`
   - 方法3：kill进程ID后重新启动

2. **测试API返回**
   ```bash
   # 获取token（替换YOUR_TOKEN）
   curl -X POST "http://localhost:8080/api/auth/login" \
     -H "Content-Type: application/json" \
     -d '{"username":"test123","password":"123456"}'

   # 调用会话列表API
   curl -X GET "http://localhost:8080/api/conversation?page=1&size=5" \
     -H "Authorization: Bearer YOUR_TOKEN" \
     -H "Accept: application/json"
   ```

3. **验证响应头**
   - Content-Type: application/json;charset=UTF-8
   - 响应体中的中文应该正确显示

## 如果仍然乱码

### 方案1：在Controller中添加字符编码过滤器

创建 `CharsetConfig.java`：

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

    @Bean
    public String jsonConverter() {
        return "org.springframework.http.converter.json.MappingJackson2HttpMessageConverter";
    }
}
```

### 方案2：在MessageConverters中添加UTF-8支持

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        ObjectMapper objectMapper = new ObjectMapper();
        // 设置JSON输出使用UTF-8
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // 注册模块以支持UTF-8
        SimpleModule module = new SimpleModule();
        objectMapper.registerModule(module);

        converter.setObjectMapper(objectMapper);
        converters.add(0, converter);

        // 设置支持的媒体类型
        List<MediaType> supportedMediaTypes = new ArrayList<>();
        supportedMediaTypes.add(new MediaType("application", "json", Charset.forName("UTF-8")));
        supportedMediaTypes.add(new MediaType("text", "html", Charset.forName("UTF-8")));
        supportedMediaTypes.add(MediaType.APPLICATION_JSON);
        converter.setSupportedMediaTypes(supportedMediaTypes);
    }
}
```

### 方案3：在数据库连接URL中添加额外参数

```yaml
url: jdbc:mysql://localhost:3306/XianQi?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&connectionCollation=utf8mb4_unicode_ci&sessionVariables=time_zone='%2B08:00'
```

## 前端处理

如果后端返回的数据仍然有乱码，前端可以进行字符修复：

```typescript
// 在 src/utils/encoding.ts
export function fixChineseEncoding(text: string): string {
  if (!text) return ''

  // 检测是否是双重编码
  // 如果包含类似 "å¥½ç" 这样的乱码，尝试修复
  try {
    const encoder = new TextEncoder()
    const decoder = new TextDecoder('utf-8')
    const bytes = encoder.encode(text)
    return decoder.decode(bytes)
  } catch {
    return text
  }
}
```

然后在API响应处理中使用：

```typescript
const conversations = await messageApi.getConversationList({ page: 1, size: 20 })
conversations.data.list.forEach(conv => {
  if (conv.lastMessageContent) {
    conv.lastMessageContent = fixChineseEncoding(conv.lastMessageContent)
  }
})
```

## 紧急修复：直接在Service层处理

在 `ConversationServiceImpl.java` 的 `convertToVO` 方法中：

```java
private ConversationVO convertToVO(Conversation conversation, Long currentUserId) {
    ConversationVO vo = new ConversationVO();
    BeanUtil.copyProperties(conversation, vo);

    // ... 其他代码 ...

    // 修复lastMessageContent的编码
    if (conversation.getLastMessageContent() != null) {
        String fixedContent = fixEncoding(conversation.getLastMessageContent());
        vo.setLastMessageContent(fixedContent);
    }

    // ... 其他代码 ...
    return vo;
}

private String fixEncoding(String content) {
    try {
        // 获取ISO-8859-1字节，然后解码为UTF-8
        byte[] bytes = content.getBytes(StandardCharsets.ISO_8859_1);
        return new String(bytes, StandardCharsets.UTF_8);
    } catch (Exception e) {
        log.warn("修复编码失败: {}", e.getMessage());
        return content;
    }
}
```
