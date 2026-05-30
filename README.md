# database-mcp-java

基于 **Spring Boot 3 + Spring AI** 的 MCP (Model Context Protocol) 服务器，提供数据库操作和代码生成能力。支持多数据源动态切换（MySQL / OceanBase / Oracle / PostgreSQL / SQL Server），通过 HTTP 协议对外暴露 MCP 工具。

## 特性

- **多数据源**：基于 `dynamic-datasource-spring-boot-starter` 动态切换，配置即用
- **数据库操作**：建表 DDL、查询表结构、执行 SQL、跨库查询
- **代码生成**：根据表结构一键生成 Java 后端（Controller/Service/Mapper/Domain/VO/BO）+ Vue3 前端 + TS API
- **连接池保活**：`DataSourceKeepAliveConfig` 定时心跳检测，防止长时间空闲连接断开
- **HTTP 传输**：`HttpMcpController` 实现 Streamable HTTP MCP 协议，兼容 Claude Code / Cursor 等客户端

## 技术栈

| 依赖 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.4.4 | 基础框架 |
| Spring AI | 1.0.0-M7 | MCP 工具注册与调用 |
| dynamic-datasource | 4.3.1 | 多数据源动态切换 |
| Velocity | 2.3 | 代码生成模板引擎 |
| HikariCP | — | 连接池（Spring Boot 内置） |

## 快速开始

### 1. 配置数据源

编辑 `src/main/resources/application.yml`，替换为你的数据库信息：

```yaml
spring:
  datasource:
    dynamic:
      primary: mysql-base
      datasource:
        mysql-base:
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://<host>:<port>/<database>
          username: <username>
          password: <password>
        # 可继续添加其他数据源 ...
```

### 2. 启动

```bash
mvn spring-boot:run
```

服务运行在 `http://localhost:28081`。

### 3. 接入 AI 客户端

**Claude Code**（`.claude/settings.json`）：

```json
{
  "mcpServers": {
    "database-mcp": {
      "type": "url",
      "url": "http://localhost:28081/mcp"
    }
  }
}
```

**Cursor**：

```json
{
  "mcpServers": {
    "database-mcp": {
      "url": "http://localhost:28081/mcp",
      "transport": "streamable-http"
    }
  }
}
```

## 项目结构

```
src/main/java/com/example/mcp/
├── McpServerApplication.java              # 启动类
├── config/
│   ├── DatabaseMcpToolProvider.java       # 数据库工具注册
│   ├── GenMcpToolProvider.java            # 代码生成工具注册
│   └── DataSourceKeepAliveConfig.java     # 连接池保活
├── controller/
│   └── HttpMcpController.java            # HTTP MCP 端点
├── context/
│   └── DataSourceContext.java            # 当前数据源上下文
├── service/
│   └── DynamicDataSourceService.java     # 数据源管理
├── tool/
│   ├── DatabaseMcpTool.java              # 数据库操作工具
│   └── GenCodeTool.java                  # 代码生成工具
└── gen/
    ├── model/                            # 表结构模型
    ├── util/                             # Velocity 模板工具
    └── constant/                         # 生成常量

src/main/resources/
├── application.yml                       # 数据源配置
└── vm/                                   # 代码生成模板
    ├── java/    # BO/Controller/Domain/Mapper/Service/VO
    ├── vue/     # index.vue / index-tree.vue
    ├── ts/      # api.ts / types.ts
    ├── xml/     # mapper.xml
    └── sql/     # DDL 模板（MySQL/Oracle/PostgreSQL/SQL Server）
```

## MCP 工具列表

| 工具 | 说明 |
|------|------|
| DatabaseMcpTool | 数据库查询、DDL 执行、表结构查看、多数据源切换 |
| GenCodeTool | 根据表结构生成前后端代码（Java + Vue3 + TS + XML） |

## License

MIT
