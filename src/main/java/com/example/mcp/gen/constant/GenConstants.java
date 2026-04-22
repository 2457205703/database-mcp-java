package com.example.mcp.gen.constant;

public interface GenConstants {
    String TPL_CRUD = "crud";
    String TPL_TREE = "tree";

    String TREE_CODE = "treeCode";
    String TREE_PARENT_CODE = "treeParentCode";
    String TREE_NAME = "treeName";
    String PARENT_MENU_ID = "parentMenuId";

    String[] COLUMNTYPE_STR = {"char", "varchar", "enum", "set", "nchar", "nvarchar", "varchar2", "nvarchar2"};
    String[] COLUMNTYPE_TEXT = {"tinytext", "text", "mediumtext", "longtext", "binary", "varbinary", "blob", "ntext", "image", "bytea"};
    String[] COLUMNTYPE_TIME = {"datetime", "time", "date", "timestamp", "year", "interval", "smalldatetime", "datetime2", "datetimeoffset", "timestamptz"};
    String[] COLUMNTYPE_NUMBER = {"tinyint", "smallint", "mediumint", "int", "int2", "int4", "int8", "number", "integer",
        "bit", "bigint", "float", "float4", "float8", "double", "decimal", "numeric", "real", "double precision",
        "smallserial", "serial", "bigserial", "money", "smallmoney"};

    String[] COLUMNNAME_NOT_ADD = {"create_dept", "create_by", "create_time", "del_flag", "update_by", "update_time", "version", "tenant_id"};
    String[] COLUMNNAME_NOT_EDIT = {"create_dept", "create_by", "create_time", "del_flag", "update_by", "update_time", "version", "tenant_id"};
    String[] COLUMNNAME_NOT_LIST = {"create_dept", "create_by", "create_time", "del_flag", "update_by", "update_time", "version", "tenant_id"};
    String[] COLUMNNAME_NOT_QUERY = {"id", "create_dept", "create_by", "create_time", "del_flag", "update_by", "update_time", "remark", "version", "tenant_id"};

    String[] BASE_ENTITY = {"createDept", "createBy", "createTime", "updateBy", "updateTime", "tenantId"};

    String HTML_INPUT = "input";
    String HTML_TEXTAREA = "textarea";
    String HTML_SELECT = "select";
    String HTML_RADIO = "radio";
    String HTML_CHECKBOX = "checkbox";
    String HTML_DATETIME = "datetime";
    String HTML_IMAGE_UPLOAD = "imageUpload";
    String HTML_FILE_UPLOAD = "fileUpload";
    String HTML_EDITOR = "editor";

    String TYPE_STRING = "String";
    String TYPE_INTEGER = "Integer";
    String TYPE_LONG = "Long";
    String TYPE_DOUBLE = "Double";
    String TYPE_BIGDECIMAL = "BigDecimal";
    String TYPE_DATE = "Date";
    String TYPE_BOOLEAN = "Boolean";

    String QUERY_LIKE = "LIKE";
    String QUERY_EQ = "EQ";
    String REQUIRE = "1";
}
