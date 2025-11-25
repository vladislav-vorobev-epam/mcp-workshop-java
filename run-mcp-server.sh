#!/bin/bash
# Run the MyTasks MCP Server on port 8002

echo "=========================================="
echo "Starting MyTasks MCP Server"
echo "=========================================="
echo "Server will run on: http://localhost:8002"
echo "MCP Endpoint: http://localhost:8002/sse"
echo "Server Name: MyTasks MCP"
echo ""
echo "Available Tools:"
echo "  - read_tasks: Read tasks by ID or list all/filtered tasks"
echo "  - write_tasks: Create, update status, or delete tasks"
echo ""
echo "Press Ctrl+C to stop the server"
echo "=========================================="
echo ""

mvn spring-boot:run -Dspring-boot.run.profiles=mcp
