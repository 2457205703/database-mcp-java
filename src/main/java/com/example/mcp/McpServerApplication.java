package com.example.mcp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ai.mcp.server.autoconfigure.McpServerAutoConfiguration;
import org.springframework.ai.mcp.server.autoconfigure.McpWebMvcServerAutoConfiguration;

@SpringBootApplication(exclude = {
    McpServerAutoConfiguration.class,
    McpWebMvcServerAutoConfiguration.class
})
public class McpServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerApplication.class, args);
    }
}
