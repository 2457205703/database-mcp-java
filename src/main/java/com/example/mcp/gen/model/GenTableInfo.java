package com.example.mcp.gen.model;

import com.example.mcp.gen.constant.GenConstants;

import java.util.Arrays;
import java.util.List;

public class GenTableInfo {
    private String tableName;
    private String tableComment;
    private String className;
    private String packageName;
    private String moduleName;
    private String businessName;
    private String functionName;
    private String functionAuthor;
    private String tplCategory;
    private List<GenColumnInfo> columns;
    private GenColumnInfo pkColumn;

    public boolean isCrud() {
        return GenConstants.TPL_CRUD.equals(this.tplCategory);
    }

    public boolean isTree() {
        return GenConstants.TPL_TREE.equals(this.tplCategory);
    }

    public boolean isSuperColumn(String javaField) {
        return Arrays.asList(GenConstants.BASE_ENTITY).contains(javaField);
    }

    // === Getters and Setters ===

    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }

    public String getTableComment() { return tableComment; }
    public void setTableComment(String tableComment) { this.tableComment = tableComment; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public String getFunctionAuthor() { return functionAuthor; }
    public void setFunctionAuthor(String functionAuthor) { this.functionAuthor = functionAuthor; }

    public String getTplCategory() { return tplCategory; }
    public void setTplCategory(String tplCategory) { this.tplCategory = tplCategory; }

    public List<GenColumnInfo> getColumns() { return columns; }
    public void setColumns(List<GenColumnInfo> columns) { this.columns = columns; }

    public GenColumnInfo getPkColumn() { return pkColumn; }
    public void setPkColumn(GenColumnInfo pkColumn) { this.pkColumn = pkColumn; }
}
