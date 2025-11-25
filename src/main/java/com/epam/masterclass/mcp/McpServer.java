package com.epam.masterclass.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.epam.masterclass.mcp.tools.TaskToolsProvider;

@SpringBootApplication
public class McpServer {
    public static void main(String[] args) {
        SpringApplication.run(McpServer.class, args);
    }

    // Note: this is not MCP Annotations related, but demonstrates how to register a SpringAI tool 
	// callback provider as MCP tools along with the @McpTool such
	@Bean
	ToolCallbackProvider taskTools(TaskToolsProvider taskService) {
		return MethodToolCallbackProvider.builder().toolObjects(taskService).build();
	}
}
