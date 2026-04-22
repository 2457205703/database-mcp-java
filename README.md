# database-mcp-java

基于 **Spring AI** 的 **MCP (Model Context Protocol)** 服务器，提供数据库 SQL 执行与 CRUD 代码生成能力。

- **框架**: Spring Boot 3.4.4 + Spring AI 1.0.0-M7
- **Java版本**: 21
- **构建工具**: Maven
- **协议**: SSE (Server-Sent Events)
- **默认端口**: 28081

## 功能

- **executeSql** — 在指定数据源上执行任意 SQL（SELECT / INSERT / UPDATE / DELETE / DDL）
- **getTableInfo** — 读取数据库表结构信息及字段智能推断配置
- **getTypeMapping** — 根据列类型推断 Java 类型、HTML 控件类型、查询方式
- **listDatasources** — 列出所有已配置的数据源
- **generateCode** — 根据数据库表自动生成完整的前后端 CRUD 代码（适配 RuoYi-Vue-Plus + Element Plus）

## 快速开始

### 1. 配置数据源

编辑 `src/main/resources/application.yml`，填写你的数据库连接信息：

```yaml
spring:
  datasource:
    type: com.zaxxer.hikari.HikariDataSource
    dynamic:
      primary: mysql-base
      strict: true
      lazy: true
      datasource:
        mysql-base:
          type: ${spring.datasource.type}
          driver-class-name: com.mysql.cj.jdbc.Driver
          url: jdbc:mysql://<your-mysql-host>:<your-mysql-port>/your_database?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT%2B8&autoReconnect=true&rewriteBatchedStatements=true&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true
          username: <your-username>
          password: <your-password>
          hikari:
            connection-test-query: SELECT 1
            validation-timeout: 5000
            max-lifetime: 600000
            keepalive-time: 120000
        oceanbase-cim:
          type: ${spring.datasource.type}
          driver-class-name: com.oceanbase.jdbc.Driver
          url: jdbc:oceanbase://<your-oceanbase-host>:<your-oceanbase-port>/your_schema
          username: <your-username>
          password: <your-password>
          hikari:
            connection-test-query: SELECT 1 FROM DUAL
            validation-timeout: 5000
            max-lifetime: 600000
            keepalive-time: 120000

datasourcedesc:
  mysql-base: 使用mysql 语法 连接库 baseframe
  oceanbase-cim: oracle版本的oceanbase 使用 oracle 语法
```

### 2. 启动服务

```bash
mvn spring-boot:run

# 或打包后运行
mvn package
java -jar target/database-mcp-java-1.0.0-SNAPSHOT.jar
```

### 3. 配置 MCP 客户端

在 Claude Code / Cursor / VS Code 等 MCP 客户端中添加：

```json
{
  "mcpServers": {
    "database-mcp-java": {
      "type": "sse",
      "url": "http://localhost:28081/sse"
    }
  }
}
```

## 项目结构

```
src/main/java/com/example/mcp/
├── McpServerApplication.java         # 主启动类
├── config/
│   ├── DatabaseMcpToolProvider.java  # 数据库工具注册
│   └── GenMcpToolProvider.java       # 代码生成工具注册
├── service/
│   └── DynamicDataSourceService.java # 动态数据源服务
├── tool/
│   ├── DatabaseMcpTool.java          # 数据库操作工具
│   └── GenCodeTool.java              # 代码生成工具
└── gen/                              # 代码生成引擎
    ├── model/                        # 数据模型
    ├── util/                         # 工具类
    └── constant/                     # 常量

src/main/resources/
├── application.yml                   # 配置文件
└── vm/                               # Velocity 代码生成模板
    ├── java/                         # Java 后端模板
    ├── ts/                           # TypeScript 模板
    ├── vue/                          # Vue 前端模板
    └── xml/                          # MyBatis XML 模板
```

## 依赖说明

项目使用 Spring Milestones 仓库获取 Spring AI 里程碑版本。如果 `mvn compile` 报找不到依赖，确认 `pom.xml` 中的仓库配置存在。
