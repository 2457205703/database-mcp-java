package com.example.mcp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HTTP 方式的 MCP 协议控制器
 * 相比 SSE 长连接方式，HTTP 请求/响应模式更稳定可靠
 * 收集所有注册的 ToolCallbackProvider，统一暴露 MCP JSON-RPC 端点
 */
@RestController
@RequestMapping("/mcp")
public class HttpMcpController {

    private static final Logger log = LoggerFactory.getLogger(HttpMcpController.class);

    private final List<ToolCallbackProvider> toolProviders;
    private final ObjectMapper objectMapper;

    public HttpMcpController(List<ToolCallbackProvider> toolProviders, ObjectMapper objectMapper) {
        this.toolProviders = toolProviders;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/rpc")
    public JsonNode handleMcpRequest(@RequestBody JsonNode request) {
        String method = request.get("method").asText();
        JsonNode id = request.get("id");

        ObjectNode response = objectMapper.createObjectNode();
        response.put("jsonrpc", "2.0");
        response.set("id", id);

        try {
            switch (method) {
                case "initialize":
                    response.set("result", handleInitialize());
                    break;
                case "notifications/initialized":
                    response.set("result", objectMapper.createObjectNode());
                    break;
                case "tools/list":
                    response.set("result", handleToolsList());
                    break;
                case "tools/call":
                    response.set("result", handleToolsCall(request.get("params")));
                    break;
                case "ping":
                    response.set("result", objectMapper.createObjectNode());
                    break;
                default:
                    response.set("error", createError(-32601, "Method not found: " + method));
            }
        } catch (Exception e) {
            log.error("MCP 请求处理失败: method={}, error={}", method, e.getMessage(), e);
            response.set("error", createError(-32603, "Internal error: " + e.getMessage()));
        }

        return response;
    }

    private ObjectNode handleInitialize() {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("protocolVersion", "2024-11-05");

        ObjectNode serverInfo = objectMapper.createObjectNode();
        serverInfo.put("name", "database-mcp-java");
        serverInfo.put("version", "1.0.0");
        result.set("serverInfo", serverInfo);

        ObjectNode capabilities = objectMapper.createObjectNode();
        capabilities.set("tools", objectMapper.createObjectNode());
        result.set("capabilities", capabilities);

        return result;
    }

    private ObjectNode handleToolsList() {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode tools = objectMapper.createArrayNode();

        for (ToolCallbackProvider provider : toolProviders) {
            for (FunctionCallback callback : provider.getToolCallbacks()) {
                tools.add(convertToMcpTool(callback));
            }
        }

        result.set("tools", tools);
        return result;
    }

    private ObjectNode handleToolsCall(JsonNode params) {
        String toolName = params.get("name").asText();
        JsonNode arguments = params.get("arguments");

        FunctionCallback targetTool = findToolByName(toolName);
        if (targetTool == null) {
            throw new RuntimeException("工具未找到: " + toolName);
        }

        String toolInput = arguments != null ? arguments.toString() : "{}";
        String callResult = targetTool.call(toolInput);

        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode content = objectMapper.createArrayNode();
        ObjectNode textContent = objectMapper.createObjectNode();
        textContent.put("type", "text");
        textContent.put("text", callResult);
        content.add(textContent);
        result.set("content", content);

        return result;
    }

    private ObjectNode convertToMcpTool(FunctionCallback callback) {
        ObjectNode mcpTool = objectMapper.createObjectNode();
        mcpTool.put("name", callback.getName());
        mcpTool.put("description", callback.getDescription());

        String inputSchema = callback.getInputTypeSchema();
        if (inputSchema != null && !inputSchema.isEmpty()) {
            try {
                JsonNode schemaNode = objectMapper.readTree(inputSchema);
                mcpTool.set("inputSchema", schemaNode);
            } catch (Exception e) {
                ObjectNode basicSchema = objectMapper.createObjectNode();
                basicSchema.put("type", "object");
                basicSchema.set("properties", objectMapper.createObjectNode());
                mcpTool.set("inputSchema", basicSchema);
            }
        }

        return mcpTool;
    }

    private FunctionCallback findToolByName(String toolName) {
        for (ToolCallbackProvider provider : toolProviders) {
            for (FunctionCallback callback : provider.getToolCallbacks()) {
                if (callback.getName().equals(toolName)) {
                    return callback;
                }
            }
        }
        return null;
    }

    private ObjectNode createError(int code, String message) {
        ObjectNode error = objectMapper.createObjectNode();
        error.put("code", code);
        error.put("message", message);
        return error;
    }
}
