package com.example.mcp.service;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * 动态数据源 SQL 执行服务
 * 封装 DynamicDataSourceContextHolder push/poll 数据源切换 + SQL 执行
 */
@Service
public class DynamicDataSourceService {

    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Environment environment;

    public DynamicDataSourceService(DataSource dataSource, Environment environment) {
        this.dynamicRoutingDataSource = (DynamicRoutingDataSource) dataSource;
        this.environment = environment;
    }

    /**
     * 在指定数据源上执行 SQL
     */
    public String executeSql(String datasourceName, String sql) {
        Connection connection = null;
        try {
            DynamicDataSourceContextHolder.push(datasourceName);
            DataSource ds = dynamicRoutingDataSource.getDataSource(datasourceName);
            if (ds == null) {
                return buildErrorResponse("数据源不存在", new RuntimeException("未找到数据源: " + datasourceName));
            }
            connection = ds.getConnection();

            String trimmedSql = sql.trim().toUpperCase();
            if (isQuerySql(trimmedSql)) {
                return executeQuery(connection, sql);
            } else {
                return executeUpdate(connection, sql);
            }
        } catch (Exception e) {
            return buildErrorResponse("SQL 执行失败", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ignored) {
                }
            }
            DynamicDataSourceContextHolder.poll();
        }
    }

    /**
     * 列出所有已配置的数据源及其基本信息（含数据库类型和 SQL 语法提示）
     */
    public String listDatasources() {
        try {
            Map<String, DataSource> dataSources = dynamicRoutingDataSource.getDataSources();
            List<Map<String, String>> result = new ArrayList<>();

            for (Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
                Map<String, String> info = new LinkedHashMap<>();
                info.put("name", entry.getKey());
                try (Connection conn = entry.getValue().getConnection()) {
                    DatabaseMetaData meta = conn.getMetaData();
                    info.put("databaseType", meta.getDatabaseProductName());
                    info.put("version", meta.getDatabaseProductVersion());
                    info.put("url", meta.getURL());
                    info.put("username", meta.getUserName());
                    info.put("sqlDialect", environment.getProperty("datasourcedesc." + entry.getKey(), ""));
                    info.put("status", "connected");
                } catch (Exception e) {
                    info.put("status", "连接失败: " + e.getMessage());
                }
                result.add(info);
            }

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            return buildErrorResponse("获取数据源列表失败", e);
        }
    }

    /**
     * 检查数据源是否存在
     */
    public boolean datasourceExists(String datasourceName) {
        return dynamicRoutingDataSource.getDataSources().containsKey(datasourceName);
    }

    // ==================== 私有方法 ====================

    private boolean isQuerySql(String trimmedSql) {
        return trimmedSql.startsWith("SELECT") ||
                trimmedSql.startsWith("WITH") ||
                trimmedSql.startsWith("SHOW") ||
                trimmedSql.startsWith("DESC") ||
                trimmedSql.startsWith("DESCRIBE") ||
                trimmedSql.startsWith("EXPLAIN");
    }

    private String executeQuery(Connection connection, String sql) {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            List<Map<String, Object>> results = new ArrayList<>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    Object value = resultSet.getObject(i);
                    row.put(columnName, value);
                }
                results.add(row);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("type", "query");
            response.put("rowCount", results.size());
            response.put("data", results);

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception e) {
            return buildErrorResponse("查询执行失败", e);
        }
    }

    private String executeUpdate(Connection connection, String sql) {
        try (Statement statement = connection.createStatement()) {
            String trimmedSql = sql.trim().toUpperCase();
            boolean isDdl = trimmedSql.startsWith("CREATE") ||
                    trimmedSql.startsWith("DROP") ||
                    trimmedSql.startsWith("ALTER") ||
                    trimmedSql.startsWith("TRUNCATE");

            if (isDdl) {
                statement.execute(sql);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("type", "ddl");
                response.put("message", "DDL 语句执行成功");
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            } else {
                int affectedRows = statement.executeUpdate(sql);
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("type", "update");
                response.put("affectedRows", affectedRows);
                response.put("message", "更新成功，影响了 " + affectedRows + " 行记录");
                return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
            }
        } catch (Exception e) {
            return buildErrorResponse("更新执行失败", e);
        }
    }

    private String buildErrorResponse(String message, Exception e) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", message + ": " + e.getMessage());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
        } catch (Exception jsonException) {
            return "{\"success\":false,\"error\":\"" + message + ": " + e.getMessage() + "\"}";
        }
    }
}
