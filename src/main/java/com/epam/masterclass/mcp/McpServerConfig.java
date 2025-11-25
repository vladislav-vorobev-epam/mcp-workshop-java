package com.epam.masterclass.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * MCP Server Configuration for Spring AI.
 * This configuration is only active when the 'mcp' profile is enabled.
 */
@Configuration
@Profile("mcp")
public class McpServerConfig {

    /**
     * Registers the TaskMcpToolService tools as callbacks for Spring AI MCP server.
     * The MCP Server autoconfiguration in spring-ai-starter-mcp-server-webmvc
     * automatically wires up these tool callbacks and exposes them via the /sse endpoint.
     *
     * @param taskMcpToolService the service containing MCP tools
     * @return ToolCallbackProvider for Spring AI
     */
    @Bean
    public ToolCallbackProvider taskMcpTools(TaskMcpToolService taskMcpToolService) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(taskMcpToolService)
                .build();
    }
}
