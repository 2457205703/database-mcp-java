package com.example.mcp.tool;

import com.example.mcp.service.DynamicDataSourceService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 数据库 MCP 工具类
 * 数据源通过方法参数指定，不再依赖 ThreadLocal
 */
@Component
public class DatabaseMcpTool {

    private final DynamicDataSourceService dynamicDataSourceService;

    public DatabaseMcpTool(DynamicDataSourceService dynamicDataSourceService) {
        this.dynamicDataSourceService = dynamicDataSourceService;
    }

    @Tool(description = "在指定数据源上执行任意 SQL 语句。" +
            "支持 SELECT、INSERT、UPDATE、DELETE 以及 DDL 语句（CREATE、DROP、ALTER 等）。" +
            "返回 JSON 格式的执行结果，包含查询数据或影响行数。")
    public String executeSql(
            @ToolParam(description = "数据源名称，如 mysql-base、oceanbase-cim。可通过 listDatasources 查看所有可用数据源", required = true)
            String datasource,
            @ToolParam(description = "要执行的 SQL 语句", required = true)
            String sql
    ) {
        return dynamicDataSourceService.executeSql(datasource, sql);
    }

    @Tool(description = "列出所有已配置的数据源及其基本信息（名称、数据库类型、连接地址）。")
    public String listDatasources() {
        return dynamicDataSourceService.listDatasources();
    }
}
