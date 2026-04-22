package com.example.mcp.gen.model;

public class ColumnOverride {
    private String javaType;
    private String htmlType;
    private String queryType;
    private String dictType;
    private Boolean isInsert;
    private Boolean isEdit;
    private Boolean isList;
    private Boolean isQuery;
    private Boolean isRequired;

    public String getJavaType() { return javaType; }
    public void setJavaType(String javaType) { this.javaType = javaType; }

    public String getHtmlType() { return htmlType; }
    public void setHtmlType(String htmlType) { this.htmlType = htmlType; }

    public String getQueryType() { return queryType; }
    public void setQueryType(String queryType) { this.queryType = queryType; }

    public String getDictType() { return dictType; }
    public void setDictType(String dictType) { this.dictType = dictType; }

    public Boolean getIsInsert() { return isInsert; }
    public void setIsInsert(Boolean isInsert) { this.isInsert = isInsert; }

    public Boolean getIsEdit() { return isEdit; }
    public void setIsEdit(Boolean isEdit) { this.isEdit = isEdit; }

    public Boolean getIsList() { return isList; }
    public void setIsList(Boolean isList) { this.isList = isList; }

    public Boolean getIsQuery() { return isQuery; }
    public void setIsQuery(Boolean isQuery) { this.isQuery = isQuery; }

    public Boolean getIsRequired() { return isRequired; }
    public void setIsRequired(Boolean isRequired) { this.isRequired = isRequired; }
}
