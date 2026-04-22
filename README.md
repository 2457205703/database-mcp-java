# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个基于 **Spring AI** 的 **MCP (Model Context Protocol)** 服务器项目，用于提供数据库操作和工具服务。

- **框架**: Spring Boot 3.4.4 + Spring AI 1.0.0-M7
- **Java版本**: 21
- **构建工具**: Maven
- **协议**: SSE (Server-Sent Events)
- **默认端口**: 28081

## 常用命令

```bash
# 编译项目
mvn compile

# 打包项目
mvn package

# 运行项目
mvn spring-boot:run

# 或直接运行 jar
java -jar target/database-mcp-java-1.0.0-SNAPSHOT.jar

# 清理构建
mvn clean
```

## 架构说明

### 核心组件

```
src/main/java/com/example/mcp/
├── McpServerApplication.java    # 主启动类，注册工具
└── tool/                         # 工具类目录
    └── HelloTool.java            # 示例工具
```

### 添加新工具的步骤

1. 在 `com.example.mcp.tool` 包下创建新类
2. 使用 `@Component` 注解标记类
3. 使用 `@Tool(description = "描述")` 注解标记方法
4. 在 `McpServerApplication.tools()` 方法中注册新工具

示例:
```java
@Component
public class MyTool {
    @Tool(description = "工具描述")
    public String myMethod(String param) {
        return "result";
    }
}
```

然后在 `McpServerApplication.java` 中:
```java
@Bean
public ToolCallbackProvider tools(HelloTool helloTool, MyTool myTool) {
    return MethodToolCallbackProvider.builder()
            .toolObjects(helloTool, myTool)
            .build();
}
```

### MCP 端点配置

- SSE 端点: `http://localhost:28081/sse`
- 消息路径: `/mcp/message`

配置位于 `src/main/resources/application.yml`。

## 依赖说明

项目使用 Spring Milestones 仓库获取 Spring AI 里程碑版本。如果 `mvn compile` 报找不到依赖，确认 `pom.xml` 中的仓库配置存在。
