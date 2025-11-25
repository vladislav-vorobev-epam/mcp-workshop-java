#!/bin/bash
# Run the MCP server on port 8081
mvn spring-boot:run -Dspring-boot.run.mainClass=com.epam.masterclass.mcp.McpServer -Dspring-boot.run.profiles=mcp
