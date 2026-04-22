package com.example.mcp.config;

import com.example.mcp.tool.GenCodeTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GenMcpToolProvider {

    @Bean
    public ToolCallbackProvider genToolCallbackProvider(GenCodeTool genCodeTool) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(genCodeTool)
                .build();
    }
}
