package com.example.mcp.gen.util;

import com.example.mcp.gen.constant.GenConstants;
import com.example.mcp.gen.model.GenColumnInfo;
import com.example.mcp.gen.model.GenTableInfo;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GenUtils {

    private GenUtils() {}

    public static GenTableInfo initTable(String tableName, String tableComment,
                                         String packageName, String moduleName,
                                         String businessName, String functionName,
                                         String functionAuthor, String tplCategory,
                                         String tablePrefix) {
        GenTableInfo table = new GenTableInfo();
        table.setTableName(tableName);
        table.setTableComment(tableComment);
        table.setClassName(convertClassName(tableName, tablePrefix));
        table.setPackageName(packageName);
        table.setModuleName(moduleName != null ? moduleName : getModuleName(packageName));
        table.setBusinessName(businessName != null ? businessName : getBusinessName(tableName, tablePrefix));
        table.setFunctionName(functionName != null ? functionName : replaceText(tableComment));
        table.setFunctionAuthor(functionAuthor != null ? functionAuthor : "ruoyi");
        table.setTplCategory(tplCategory != null ? tplCategory : GenConstants.TPL_CRUD);
        return table;
    }

    public static void initColumnField(GenColumnInfo column) {
        String dataType = getDbType(column.getColumnType());
        String columnName = column.getColumnName().toLowerCase();

        column.setJavaField(toCamelCase(columnName));
        column.setJavaType(GenConstants.TYPE_STRING);
        column.setQueryType(GenConstants.QUERY_EQ);

        if (arraysContains(GenConstants.COLUMNTYPE_STR, dataType)) {
            Integer columnLength = getColumnLength(column.getColumnType());
            String htmlType = columnLength >= 500 ? GenConstants.HTML_TEXTAREA : GenConstants.HTML_INPUT;
            column.setHtmlType(htmlType);
        } else if (arraysContains(GenConstants.COLUMNTYPE_TEXT, dataType)) {
            column.setHtmlType(GenConstants.HTML_TEXTAREA);
        } else if (arraysContains(GenConstants.COLUMNTYPE_TIME, dataType)) {
            column.setJavaType(GenConstants.TYPE_DATE);
            column.setHtmlType(GenConstants.HTML_DATETIME);
        } else if (arraysContains(GenConstants.COLUMNTYPE_NUMBER, dataType)) {
            column.setHtmlType(GenConstants.HTML_INPUT);
            column.setJavaType(GenConstants.TYPE_LONG);
        }

        if (!arraysContains(GenConstants.COLUMNNAME_NOT_ADD, columnName) && !"1".equals(column.getIsPk())) {
            column.setIsInsert(GenConstants.REQUIRE);
        }
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_EDIT, columnName)) {
            column.setIsEdit(GenConstants.REQUIRE);
        }
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_LIST, columnName)) {
            column.setIsList(GenConstants.REQUIRE);
        }
        if (!arraysContains(GenConstants.COLUMNNAME_NOT_QUERY, columnName) && !"1".equals(column.getIsPk())) {
            column.setIsQuery(GenConstants.REQUIRE);
        }

        if (columnName.endsWith("name")) {
            column.setQueryType(GenConstants.QUERY_LIKE);
        }
        if (columnName.endsWith("status")) {
            column.setHtmlType(GenConstants.HTML_RADIO);
        } else if (columnName.endsWith("type") || columnName.endsWith("sex")) {
            column.setHtmlType(GenConstants.HTML_SELECT);
        } else if (columnName.endsWith("image")) {
            column.setHtmlType(GenConstants.HTML_IMAGE_UPLOAD);
        } else if (columnName.endsWith("file")) {
            column.setHtmlType(GenConstants.HTML_FILE_UPLOAD);
        } else if (columnName.endsWith("content")) {
            column.setHtmlType(GenConstants.HTML_EDITOR);
        }
    }

    public static String convertClassName(String tableName, String tablePrefix) {
        if (tablePrefix != null && !tablePrefix.isEmpty()) {
            String[] prefixes = tablePrefix.split(",");
            for (String prefix : prefixes) {
                if (tableName.startsWith(prefix)) {
                    tableName = tableName.substring(prefix.length());
                    break;
                }
            }
        }
        return convertToCamelCase(tableName);
    }

    public static String getModuleName(String packageName) {
        if (packageName == null) return "";
        int lastIndex = packageName.lastIndexOf(".");
        return lastIndex >= 0 ? packageName.substring(lastIndex + 1) : packageName;
    }

    public static String getBusinessName(String tableName, String tablePrefix) {
        if (tablePrefix != null && !tablePrefix.isEmpty()) {
            String[] prefixes = tablePrefix.split(",");
            for (String prefix : prefixes) {
                if (tableName.startsWith(prefix)) {
                    tableName = tableName.substring(prefix.length());
                    break;
                }
            }
        }
        int firstIndex = tableName.indexOf("_");
        String businessName = firstIndex >= 0 ? tableName.substring(firstIndex + 1) : tableName;
        return toCamelCase(businessName);
    }

    public static String getDbType(String columnType) {
        if (columnType == null) return "";
        int idx = columnType.indexOf("(");
        return idx > 0 ? columnType.substring(0, idx) : columnType;
    }

    public static int getColumnLength(String columnType) {
        if (columnType == null) return 0;
        Pattern p = Pattern.compile("\\((\\d+)\\)");
        Matcher m = p.matcher(columnType);
        return m.find() ? Integer.parseInt(m.group(1)) : 0;
    }

    public static String toCamelCase(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (char c : str.toCharArray()) {
            if (c == '_') {
                nextUpper = true;
            } else {
                result.append(nextUpper ? Character.toUpperCase(c) : c);
                nextUpper = false;
            }
        }
        return result.toString();
    }

    private static String convertToCamelCase(String str) {
        if (str == null || str.isEmpty()) return str;
        StringBuilder result = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (i == 0 && !nextUpper) {
                    result.append(Character.toUpperCase(c));
                } else if (nextUpper) {
                    result.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    result.append(c);
                }
            }
        }
        return result.toString();
    }

    private static String replaceText(String text) {
        if (text == null) return "";
        return text.replaceAll("(?:表|若依)", "");
    }

    public static boolean arraysContains(String[] arr, String targetValue) {
        return Arrays.asList(arr).contains(targetValue);
    }
}
