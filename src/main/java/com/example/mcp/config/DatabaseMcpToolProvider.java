package com.example.mcp.config;

import com.example.mcp.tool.DatabaseMcpTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Database MCP 工具提供者配置类
 */
@Configuration
public class DatabaseMcpToolProvider {

    /**
     * 注册数据库 MCP 工具
     */
    @Bean
    public ToolCallbackProvider databaseToolCallbackProvider(DatabaseMcpTool databaseMcpTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(databaseMcpTool)
                .build();
    }
}
