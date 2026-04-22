package com.example.mcp.gen.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class GenResult {
    private boolean success;
    private String message;
    private Map<String, String> files;

    public static GenResult ok(Map<String, String> files) {
        GenResult result = new GenResult();
        result.success = true;
        result.message = "生成成功，共 " + files.size() + " 个文件";
        result.files = files;
        return result;
    }

    public static GenResult fail(String message) {
        GenResult result = new GenResult();
        result.success = false;
        result.message = message;
        result.files = new LinkedHashMap<>();
        return result;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Map<String, String> getFiles() { return files; }
    public void setFiles(Map<String, String> files) { this.files = files; }
}
