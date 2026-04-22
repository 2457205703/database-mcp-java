package com.example.mcp.gen.util;

import com.example.mcp.gen.constant.GenConstants;
import com.example.mcp.gen.model.GenColumnInfo;
import com.example.mcp.gen.model.GenTableInfo;
import org.apache.velocity.VelocityContext;

import java.time.LocalDate;
import java.util.*;

public final class VelocityUtils {

    private static final String PROJECT_PATH = "main/java";
    private static final String MYBATIS_PATH = "main/resources/mapper";
    public static final String DEFAULT_TPL = "aivm";

    private VelocityUtils() {}

    public static VelocityContext prepareContext(GenTableInfo table) {
        VelocityContext ctx = new VelocityContext();

        String moduleName = table.getModuleName();
        String businessName = table.getBusinessName();
        String packageName = table.getPackageName();

        ctx.put("tplCategory", table.getTplCategory());
        ctx.put("tableName", table.getTableName());
        ctx.put("functionName", table.getFunctionName() != null ? table.getFunctionName() : "【请填写功能名称】");
        ctx.put("ClassName", table.getClassName());
        ctx.put("className", uncapitalize(table.getClassName()));
        ctx.put("moduleName", moduleName);
        ctx.put("BusinessName", capitalize(businessName));
        ctx.put("businessName", businessName);
        ctx.put("basePackage", getPackagePrefix(packageName));
        ctx.put("packageName", packageName);
        ctx.put("author", table.getFunctionAuthor());
        ctx.put("datetime", LocalDate.now().toString());
        ctx.put("pkColumn", table.getPkColumn());
        ctx.put("importList", getImportList(table.getColumns()));
        ctx.put("permissionPrefix", moduleName + ":" + businessName);
        ctx.put("columns", table.getColumns());
        ctx.put("table", table);
        ctx.put("dicts", getDicts(table.getColumns()));

        return ctx;
    }

    public static List<String> getTemplateList(String tplCategory, String tplName) {
        String prefix = tplName + "/";
        List<String> templates = new ArrayList<>();

        if ("vm".equals(tplName)) {
            templates.add(prefix + "java/domain.java.vm");
            templates.add(prefix + "java/vo.java.vm");
            templates.add(prefix + "java/bo.java.vm");
            templates.add(prefix + "java/mapper.java.vm");
            templates.add(prefix + "java/service.java.vm");
            templates.add(prefix + "java/serviceImpl.java.vm");
            templates.add(prefix + "java/controller.java.vm");
            templates.add(prefix + "xml/mapper.xml.vm");
            templates.add(prefix + "sql/sql.vm");
            templates.add(prefix + "ts/api.ts.vm");
            templates.add(prefix + "ts/types.ts.vm");
            if (GenConstants.TPL_CRUD.equals(tplCategory)) {
                templates.add(prefix + "vue/index.vue.vm");
            } else if (GenConstants.TPL_TREE.equals(tplCategory)) {
                templates.add(prefix + "vue/index-tree.vue.vm");
            }
        } else {
            // aivm (默认)
            templates.add(prefix + "java/domain.java.vm");
            templates.add(prefix + "java/vo.java.vm");
            templates.add(prefix + "java/bo.java.vm");
            templates.add(prefix + "java/importVo.java.vm");
            templates.add(prefix + "java/mapper.java.vm");
            templates.add(prefix + "java/service.java.vm");
            templates.add(prefix + "java/serviceImpl.java.vm");
            templates.add(prefix + "java/controller.java.vm");
            templates.add(prefix + "java/importListener.java.vm");
            templates.add(prefix + "xml/mapper.xml.vm");
            templates.add(prefix + "ts/api.ts.vm");
            templates.add(prefix + "ts/types.ts.vm");
            templates.add(prefix + "vue/index.vue.vm");
            templates.add(prefix + "vue/api/index.ts.vm");
            templates.add(prefix + "vue/api/types.ts.vm");
            templates.add(prefix + "vue/modules/operate-drawer.vue.vm");
            templates.add(prefix + "vue/modules/table-search.vue.vm");
        }

        return templates;
    }

    public static String getFileName(String template, GenTableInfo table) {
        String packageName = table.getPackageName();
        String moduleName = table.getModuleName();
        String className = table.getClassName();
        String businessName = table.getBusinessName();

        String javaPath = PROJECT_PATH + "/" + packageName.replace(".", "/");
        String mybatisPath = MYBATIS_PATH + "/" + moduleName;

        // vm 模板的前端路径结构：api/{moduleName}/{businessName}/, views/{moduleName}/{businessName}/
        // aivm 模板统一放在 vue/ 下
        boolean isVm = template.startsWith("vm/");
        String vueBase = isVm ? "vue" : "vue";

        if (template.contains("domain.java.vm")) {
            return javaPath + "/domain/" + className + ".java";
        }
        if (template.contains("importVo.java.vm")) {
            return javaPath + "/domain/vo/" + className + "ImportVo.java";
        }
        if (template.contains("importListener.java.vm")) {
            return javaPath + "/listener/" + className + "ImportListener.java";
        }
        if (template.contains("vo.java.vm")) {
            return javaPath + "/domain/vo/" + className + "Vo.java";
        }
        if (template.contains("bo.java.vm")) {
            return javaPath + "/domain/bo/" + className + "Bo.java";
        }
        if (template.contains("mapper.java.vm")) {
            return javaPath + "/mapper/" + className + "Mapper.java";
        }
        if (template.contains("service.java.vm") && !template.contains("serviceImpl")) {
            return javaPath + "/service/I" + className + "Service.java";
        }
        if (template.contains("serviceImpl.java.vm")) {
            return javaPath + "/service/impl/" + className + "ServiceImpl.java";
        }
        if (template.contains("controller.java.vm")) {
            return javaPath + "/controller/" + className + "Controller.java";
        }
        if (template.contains("mapper.xml.vm")) {
            return mybatisPath + "/" + className + "Mapper.xml";
        }
        // SQL 菜单脚本
        if (template.contains("sql.vm") && template.contains("sql/")) {
            return businessName + "Menu.sql";
        }
        // 前端 API
        if (template.contains("/ts/api.ts.vm") || template.contains("api/index.ts.vm")) {
            return vueBase + "/api/" + moduleName + "/" + businessName + "/index.ts";
        }
        if (template.contains("/ts/types.ts.vm") || template.contains("api/types.ts.vm")) {
            return vueBase + "/api/" + moduleName + "/" + businessName + "/types.ts";
        }
        // Vue 页面
        if (template.contains("index-tree.vue.vm")) {
            return vueBase + "/views/" + moduleName + "/" + businessName + "/index.vue";
        }
        if (template.contains("index.vue.vm")) {
            if (isVm) {
                return vueBase + "/views/" + moduleName + "/" + businessName + "/index.vue";
            } else {
                return vueBase + "/" + moduleName + "/" + businessName + "/index.vue";
            }
        }
        if (template.contains("operate-drawer.vue.vm")) {
            return vueBase + "/" + moduleName + "/" + businessName + "/modules/operate-drawer.vue";
        }
        if (template.contains("table-search.vue.vm")) {
            return vueBase + "/" + moduleName + "/" + businessName + "/modules/table-search.vue";
        }

        return template;
    }

    public static HashSet<String> getImportList(List<GenColumnInfo> columns) {
        HashSet<String> importList = new HashSet<>();
        for (GenColumnInfo column : columns) {
            if (!column.isSuperColumn()) {
                if (GenConstants.TYPE_DATE.equals(column.getJavaType())) {
                    importList.add("java.util.Date");
                    importList.add("com.fasterxml.jackson.annotation.JsonFormat");
                } else if (GenConstants.TYPE_BIGDECIMAL.equals(column.getJavaType())) {
                    importList.add("java.math.BigDecimal");
                } else if ("imageUpload".equals(column.getHtmlType())) {
                    importList.add("org.dromara.common.translation.annotation.Translation");
                    importList.add("org.dromara.common.translation.constant.TransConstant");
                }
            }
        }
        return importList;
    }

    private static String getDicts(List<GenColumnInfo> columns) {
        List<String> dicts = new ArrayList<>();
        for (GenColumnInfo column : columns) {
            if (!column.isSuperColumn()
                && column.getDictType() != null && !column.getDictType().isEmpty()
                && (GenConstants.HTML_SELECT.equals(column.getHtmlType())
                    || GenConstants.HTML_RADIO.equals(column.getHtmlType())
                    || GenConstants.HTML_CHECKBOX.equals(column.getHtmlType()))) {
                dicts.add("'" + column.getDictType() + "'");
            }
        }
        return String.join(", ", dicts);
    }

    private static String getPackagePrefix(String packageName) {
        if (packageName == null) return "";
        int lastIndex = packageName.lastIndexOf(".");
        return lastIndex >= 0 ? packageName.substring(0, lastIndex) : packageName;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private static String uncapitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }
}
