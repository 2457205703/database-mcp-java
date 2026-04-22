package com.example.mcp.gen.model;

import com.example.mcp.gen.constant.GenConstants;

import java.util.Arrays;

public class GenColumnInfo {
    private String columnName;
    private String columnComment;
    private String columnType;
    private String javaType;
    private String javaField;
    private String isPk;
    private String isIncrement;
    private String isRequired;
    private String isInsert;
    private String isEdit;
    private String isList;
    private String isQuery;
    private String queryType;
    private String htmlType;
    private String dictType;
    private int sort;

    public boolean isSuperColumn() {
        return Arrays.asList(GenConstants.BASE_ENTITY).contains(this.javaField);
    }

    // === Getters for Velocity template access ===

    public boolean getQuery() {
        return "1".equals(this.isQuery);
    }

    public boolean getList() {
        return "1".equals(this.isList);
    }

    public boolean getInsert() {
        return "1".equals(this.isInsert);
    }

    public boolean getEdit() {
        return "1".equals(this.isEdit);
    }

    public boolean getRequired() {
        return "1".equals(this.isRequired);
    }

    public boolean getPk() {
        return "1".equals(this.isPk);
    }

    public boolean getIncrement() {
        return "1".equals(this.isIncrement);
    }

    // === Standard getters and setters ===

    public String getColumnName() { return columnName; }
    public void setColumnName(String columnName) { this.columnName = columnName; }

    public String getColumnComment() { return columnComment; }
    public void setColumnComment(String columnComment) { this.columnComment = columnComment; }

    public String getColumnType() { return columnType; }
    public void setColumnType(String columnType) { this.columnType = columnType; }

    public String getJavaType() { return javaType; }
    public void setJavaType(String javaType) { this.javaType = javaType; }

    public String getJavaField() { return javaField; }
    public void setJavaField(String javaField) { this.javaField = javaField; }

    public String getIsPk() { return isPk; }
    public void setIsPk(String isPk) { this.isPk = isPk; }

    public String getIsIncrement() { return isIncrement; }
    public void setIsIncrement(String isIncrement) { this.isIncrement = isIncrement; }

    public String getIsRequired() { return isRequired; }
    public void setIsRequired(String isRequired) { this.isRequired = isRequired; }

    public String getIsInsert() { return isInsert; }
    public void setIsInsert(String isInsert) { this.isInsert = isInsert; }

    public String getIsEdit() { return isEdit; }
    public void setIsEdit(String isEdit) { this.isEdit = isEdit; }

    public String getIsList() { return isList; }
    public void setIsList(String isList) { this.isList = isList; }

    public String getIsQuery() { return isQuery; }
    public void setIsQuery(String isQuery) { this.isQuery = isQuery; }

    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }

    public String getHtmlType() { return htmlType; }
    public void setHtmlType(String htmlType) { this.htmlType = htmlType; }

    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }

    public int getSort() { return sort; }
    public void setSort(int sort) { this.sort = sort; }
}
