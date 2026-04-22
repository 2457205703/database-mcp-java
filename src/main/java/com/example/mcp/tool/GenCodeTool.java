package com.example.mcp.tool;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.example.mcp.gen.constant.GenConstants;
import com.example.mcp.gen.model.*;
import com.example.mcp.gen.util.GenUtils;
import com.example.mcp.gen.util.VelocityInitializer;
import com.example.mcp.gen.util.VelocityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.StringWriter;
import java.sql.*;
import java.util.*;

@Component
public class GenCodeTool {

    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GenCodeTool(DataSource dataSource) {
        this.dynamicRoutingDataSource = (DynamicRoutingDataSource) dataSource;
    }

    @Tool(description = "读取指定数据源中某张表的完整结构信息，同时返回每个字段的智能推断配置。" +
            "这是生成代码前的第一步，用于审查每个字段的配置，就像在代码生成页面的'编辑'弹窗中查看字段信息一样。" +
            "\n返回的每个字段包含：" +
            "\n- 原始信息: columnName(列名), columnType(物理类型), columnComment(字段描述), isPk(是否主键), isAutoIncrement(是否自增), isNullable(是否可空)" +
            "\n- 推断配置: javaType(Java类型), javaField(Java属性名), htmlType(显示类型), queryType(查询方式), isInsert(是否插入), isEdit(是否编辑), isList(是否列表), isQuery(是否查询), isRequired(是否必填), dictType(字典类型)" +
            "\n\n可用的 Java 类型 (javaType):" +
            "\n  Long=长整型, String=字符串, Integer=整数, Double=双精度, BigDecimal=高精度, Date=日期, Boolean=布尔" +
            "\n可用的显示类型 (htmlType):" +
            "\n  input=文本框, textarea=文本域, select=下拉框, radio=单选框, checkbox=复选框, datetime=日期时间控件, imageUpload=图片上传, fileUpload=文件上传, editor=富文本控件" +
            "\n可用的查询方式 (queryType):" +
            "\n  EQ=等于(=), NE=不等于(!=), GT=大于(>), GE=大于等于(>=), LT=小于(<), LE=小于等于(<=), LIKE=模糊匹配, BETWEEN=范围查询" +
            "\n\n审查完推断配置后，如需修改某个字段，在 generateCode 时通过 columnOverrides 参数覆盖。")
    public String getTableInfo(
            @ToolParam(description = "数据源名称，如 mysql-base、oceanbase-cim。可通过 listDatasources 查看所有可用数据源")
            String datasource,
            @ToolParam(description = "要查询的表名称，如 sys_user")
            String tableName
    ) {
        Connection connection = null;
        try {
            DataSource ds = dynamicRoutingDataSource.getDataSource(datasource);
            if (ds == null) {
                return errorJson("数据源不存在: " + datasource);
            }
            connection = ds.getConnection();
            DatabaseMetaData meta = connection.getMetaData();

            String catalog = null;
            String schema = getSchema(meta, connection);

            // 获取表注释
            String tableComment = getTableComment(meta, catalog, schema, tableName);

            // 获取主键
            Set<String> pkColumns = new HashSet<>();
            try (ResultSet pks = meta.getPrimaryKeys(catalog, schema, tableName)) {
                while (pks.next()) {
                    pkColumns.add(pks.getString("COLUMN_NAME"));
                }
            }

            // 获取列信息 + 推断配置
            List<Map<String, Object>> columns = new ArrayList<>();
            int sort = 0;
            try (ResultSet rs = meta.getColumns(catalog, schema, tableName, null)) {
                while (rs.next()) {
                    // 构建推断对象
                    GenColumnInfo colInfo = new GenColumnInfo();
                    colInfo.setColumnName(rs.getString("COLUMN_NAME"));
                    String type = rs.getString("TYPE_NAME");
                    int size = rs.getInt("COLUMN_SIZE");
                    colInfo.setColumnType(type.toLowerCase() + "(" + size + ")");
                    colInfo.setColumnComment(rs.getString("REMARKS") != null ? rs.getString("REMARKS") : "");
                    colInfo.setIsPk(pkColumns.contains(colInfo.getColumnName()) ? "1" : "0");
                    colInfo.setIsIncrement("YES".equals(rs.getString("IS_AUTOINCREMENT")) ? "1" : "0");
                    colInfo.setSort(sort++);

                    // 执行智能推断
                    GenUtils.initColumnField(colInfo);

                    // 构建返回数据：原始信息 + 推断配置
                    Map<String, Object> col = new LinkedHashMap<>();
                    // 原始信息
                    col.put("columnName", colInfo.getColumnName());
                    col.put("columnType", colInfo.getColumnType());
                    col.put("columnComment", colInfo.getColumnComment());
                    col.put("isPk", colInfo.getIsPk());
                    col.put("isAutoIncrement", colInfo.getIsIncrement());
                    col.put("isNullable", "YES".equals(rs.getString("IS_NULLABLE")) ? "1" : "0");
                    // 推断配置
                    col.put("javaType", colInfo.getJavaType());
                    col.put("javaField", colInfo.getJavaField());
                    col.put("htmlType", colInfo.getHtmlType());
                    col.put("queryType", colInfo.getQueryType());
                    col.put("isInsert", colInfo.getIsInsert());
                    col.put("isEdit", colInfo.getIsEdit());
                    col.put("isList", colInfo.getIsList());
                    col.put("isQuery", colInfo.getIsQuery());
                    col.put("isRequired", colInfo.getIsRequired());
                    col.put("dictType", colInfo.getDictType());
                    columns.add(col);
                }
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("tableName", tableName);
            result.put("tableComment", tableComment);
            result.put("columns", columns);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            return errorJson("读取表结构失败: " + e.getMessage());
        } finally {
            if (connection != null) {
                try { connection.close(); } catch (SQLException ignored) {}
            }
        }
    }

    @Tool(description = "根据数据库列类型和列名推断 Java 类型、HTML 控件类型、查询方式。" +
            "用于在生成代码前预览单个字段的推断结果，判断是否需要通过 columnOverrides 参数覆盖。" +
            "\n可用的 Java 类型 (javaType):" +
            "\n  Long=长整型, String=字符串, Integer=整数, Double=双精度, BigDecimal=高精度, Date=日期, Boolean=布尔" +
            "\n可用的显示类型 (htmlType):" +
            "\n  input=文本框, textarea=文本域, select=下拉框, radio=单选框, checkbox=复选框, datetime=日期时间控件, imageUpload=图片上传, fileUpload=文件上传, editor=富文本控件" +
            "\n可用的查询方式 (queryType):" +
            "\n  EQ=等于(=), NE=不等于(!=), GT=大于(>), GE=大于等于(>=), LT=小于(<), LE=小于等于(<=), LIKE=模糊匹配, BETWEEN=范围查询")
    public String getTypeMapping(
            @ToolParam(description = "数据库列类型，如 varchar(100)、int、datetime")
            String columnType,
            @ToolParam(description = "列名（用于推断 htmlType 和 queryType），如 user_name、status", required = false)
            String columnName
    ) {
        try {
            GenColumnInfo col = new GenColumnInfo();
            col.setColumnType(columnType);
            col.setColumnName(columnName != null ? columnName : "");
            col.setIsPk("0");
            GenUtils.initColumnField(col);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("columnType", columnType);
            result.put("columnName", columnName);
            result.put("inferredJavaType", col.getJavaType());
            result.put("inferredHtmlType", col.getHtmlType());
            result.put("inferredQueryType", col.getQueryType());
            result.put("inferredJavaField", col.getJavaField());
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            return errorJson("类型推断失败: " + e.getMessage());
        }
    }

    @Tool(description = "根据数据库表自动生成完整的前后端 CRUD 代码（RuoYi-Vue-Plus 标准结构）。" +
            "生成的文件包括: Domain实体、Vo、Bo、ImportVo、ImportListener、Mapper接口、Service接口、ServiceImpl、" +
            "Controller、MyBatis XML映射、前端API、TypeScript类型定义、Vue页面（含搜索组件和编辑抽屉）。" +
            "大模型只需传入 datasource + tableName 即可自动生成，也可通过可选参数精细控制。")
    public String generateCode(
            @ToolParam(description = "数据源名称，如 mysql-base")
            String datasource,
            @ToolParam(description = "要生成代码的表名称，如 sys_user")
            String tableName,
            @ToolParam(description = "Java 包路径，如 org.dromara.system")
            String packageName,
            @ToolParam(description = "模块名（通常为包路径最后一段），如 system")
            String moduleName,
            @ToolParam(description = "业务名（英文，用于 URL 路径），如 user。不传则从表名自动推导", required = false)
            String businessName,
            @ToolParam(description = "功能名（中文，用于页面标题和注释），如 用户管理。不传则从表注释推导", required = false)
            String functionName,
            @ToolParam(description = "作者名称，默认 ruoyi", required = false)
            String functionAuthor,
            @ToolParam(description = "模板类型：crud（单表增删改查，默认）或 tree（树表增删改查）", required = false)
            String tplCategory,
            @ToolParam(description = "表名前缀（逗号分隔），生成时会去除前缀，如 sys_,t_。不传则不去除", required = false)
            String tablePrefix,
            @ToolParam(description = "表描述（中文），不传则从数据库元数据自动读取", required = false)
            String tableComment,
            @ToolParam(description = "列级别覆盖配置，JSON 格式。key 为列名（columnName），value 为要覆盖的字段。" +
                    "例如: {\"status\":{\"htmlType\":\"radio\",\"dictType\":\"sys_normal_disable\"}," +
                    "\"user_name\":{\"queryType\":\"LIKE\"}}。" +
                    "\n可选覆盖项:" +
                    "\n  javaType: Long=长整型, String=字符串, Integer=整数, Double=双精度, BigDecimal=高精度, Date=日期, Boolean=布尔" +
                    "\n  htmlType: input=文本框, textarea=文本域, select=下拉框, radio=单选框, checkbox=复选框, datetime=日期时间控件, imageUpload=图片上传, fileUpload=文件上传, editor=富文本控件" +
                    "\n  queryType: EQ=等于, NE=不等于, GT=大于, GE=大于等于, LT=小于, LE=小于等于, LIKE=模糊匹配, BETWEEN=范围查询" +
                    "\n  dictType: 字典编码，如 sys_normal_disable（可通过 executeSql 查询 sys_dict_type 表获取所有可用字典）" +
                    "\n  isInsert(1/0): 是否插入字段" +
                    "\n  isEdit(1/0): 是否编辑字段" +
                    "\n  isList(1/0): 是否在列表显示" +
                    "\n  isQuery(1/0): 是否作为查询条件" +
                    "\n  isRequired(1/0): 是否必填", required = false)
            String columnOverrides,
            @ToolParam(description = "模板集名称，对应 resources 下的文件夹名。默认 aivm（自定义组件模板），" +
                    "可选 vm（官方 plus-ui Element Plus 模板）", required = false)
            String tplName
    ) {
        try {
            VelocityInitializer.init();

            // 1. 读取数据库表结构
            DataSource ds = dynamicRoutingDataSource.getDataSource(datasource);
            if (ds == null) {
                return errorJson("数据源不存在: " + datasource);
            }

            List<GenColumnInfo> columns;
            GenColumnInfo pkColumn = null;
            String actualTableComment;

            try (Connection conn = ds.getConnection()) {
                DatabaseMetaData meta = conn.getMetaData();
                String catalog = null;
                String schema = getSchema(meta, conn);

                actualTableComment = tableComment != null ? tableComment :
                    getTableComment(meta, catalog, schema, tableName);

                Set<String> pkColumns = new HashSet<>();
                try (ResultSet pks = meta.getPrimaryKeys(catalog, schema, tableName)) {
                    while (pks.next()) {
                        pkColumns.add(pks.getString("COLUMN_NAME"));
                    }
                }

                columns = new ArrayList<>();
                int sort = 0;
                try (ResultSet rs = meta.getColumns(catalog, schema, tableName, null)) {
                    while (rs.next()) {
                        GenColumnInfo col = new GenColumnInfo();
                        col.setColumnName(rs.getString("COLUMN_NAME"));
                        String type = rs.getString("TYPE_NAME");
                        int size = rs.getInt("COLUMN_SIZE");
                        col.setColumnType(type.toLowerCase() + "(" + size + ")");
                        col.setColumnComment(rs.getString("REMARKS") != null ? rs.getString("REMARKS") : "");
                        col.setIsPk(pkColumns.contains(col.getColumnName()) ? "1" : "0");
                        col.setIsIncrement("YES".equals(rs.getString("IS_AUTOINCREMENT")) ? "1" : "0");
                        col.setSort(sort++);
                        columns.add(col);
                    }
                }
            }

            // 2. 自动推断每个字段
            for (GenColumnInfo col : columns) {
                GenUtils.initColumnField(col);
                if ("1".equals(col.getIsPk()) && pkColumn == null) {
                    pkColumn = col;
                }
            }

            // 3. 应用列覆盖
            if (columnOverrides != null && !columnOverrides.isEmpty()) {
                applyOverrides(columns, columnOverrides);
            }

            // 4. 构建表信息
            GenTableInfo table = GenUtils.initTable(tableName, actualTableComment, packageName,
                moduleName, businessName, functionName, functionAuthor, tplCategory, tablePrefix);
            table.setColumns(columns);
            table.setPkColumn(pkColumn);

            if (pkColumn == null && !columns.isEmpty()) {
                table.setPkColumn(columns.get(0));
            }

            // 5. 渲染模板
            String actualTplName = (tplName != null && !tplName.isEmpty()) ? tplName : VelocityUtils.DEFAULT_TPL;
            VelocityContext context = VelocityUtils.prepareContext(table);
            List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), actualTplName);
            Map<String, String> files = new LinkedHashMap<>();

            for (String templatePath : templates) {
                try {
                    StringWriter writer = new StringWriter();
                    Template template = Velocity.getTemplate(templatePath, "UTF-8");
                    template.merge(context, writer);

                    String fileName = VelocityUtils.getFileName(templatePath, table);
                    files.put(fileName, writer.toString());
                } catch (Exception e) {
                    files.put("ERROR_" + templatePath, "模板渲染失败: " + e.getMessage());
                }
            }

            GenResult result = GenResult.ok(files);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            return errorJson("代码生成失败: " + e.getMessage());
        }
    }

    private void applyOverrides(List<GenColumnInfo> columns, String overridesJson) {
        try {
            Map<String, ColumnOverride> overrides = objectMapper.readValue(overridesJson,
                objectMapper.getTypeFactory().constructMapType(LinkedHashMap.class, String.class, ColumnOverride.class));

            for (GenColumnInfo col : columns) {
                ColumnOverride override = overrides.get(col.getColumnName());
                if (override == null) continue;

                if (override.getJavaType() != null) col.setJavaType(override.getJavaType());
                if (override.getHtmlType() != null) col.setHtmlType(override.getHtmlType());
                if (override.getQueryType() != null) col.setQueryType(override.getQueryType());
                if (override.getDictType() != null) col.setDictType(override.getDictType());
                if (override.getIsInsert() != null) col.setIsInsert(override.getIsInsert() ? "1" : "0");
                if (override.getIsEdit() != null) col.setIsEdit(override.getIsEdit() ? "1" : "0");
                if (override.getIsList() != null) col.setIsList(override.getIsList() ? "1" : "0");
                if (override.getIsQuery() != null) col.setIsQuery(override.getIsQuery() ? "1" : "0");
                if (override.getIsRequired() != null) col.setIsRequired(override.getIsRequired() ? "1" : "0");
            }
        } catch (Exception e) {
            // overrides 解析失败不影响生成，忽略
        }
    }

    private String getSchema(DatabaseMetaData meta, Connection conn) throws SQLException {
        String dbName = meta.getDatabaseProductName().toLowerCase();
        if (dbName.contains("oracle") || dbName.contains("oceanbase")) {
            return meta.getUserName().toUpperCase();
        }
        return null;
    }

    private String getTableComment(DatabaseMetaData meta, String catalog, String schema, String tableName)
            throws SQLException {
        try (ResultSet tables = meta.getTables(catalog, schema, tableName, new String[]{"TABLE"})) {
            if (tables.next()) {
                return tables.getString("REMARKS");
            }
        }
        return "";
    }

    private String errorJson(String message) {
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", false);
            result.put("error", message);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
        } catch (Exception e) {
            return "{\"success\":false,\"error\":\"" + message + "\"}";
        }
    }
}
