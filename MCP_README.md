# MCP Server Implementation

This document describes the Model Context Protocol (MCP) server implementation for the Task Tracking API.

## Overview

The MCP server provides two high-level tools that aggregate all Task API operations:

- **`read_tasks`**: Retrieves task information (single task or list with optional filtering)
- **`write_tasks`**: Performs write operations (create, update status, delete)

## Architecture

### Components

1. **TaskMcpToolService** (`src/main/java/com/epam/masterclass/mcp/TaskMcpToolService.java`)
   - Contains the MCP tools with `@Tool` annotations
   - Implements business logic for read and write operations
   - Active only when `mcp` profile is enabled

2. **McpServerConfig** (`src/main/java/com/epam/masterclass/mcp/McpServerConfig.java`)
   - Spring configuration for MCP server
   - Registers tools as `ToolCallbackProvider` beans
   - Active only when `mcp` profile is enabled

3. **application-mcp.yml** (`src/main/resources/application-mcp.yml`)
   - Configuration for MCP server
   - Sets port to 8002
   - Configures SSE transport at `/sse` endpoint

## Running the Servers

### Prerequisites

Ensure you have Java 21 and Maven installed.

### Option 1: Using Shell Scripts (Recommended)

#### Run the REST API Server (Port 8001)
```bash
./run-api-server.sh
```

#### Run the MCP Server (Port 8002)
```bash
./run-mcp-server.sh
```

### Option 2: Using Maven Directly

#### Run the REST API Server
```bash
mvn spring-boot:run
```
Access at: http://localhost:8001/api

#### Run the MCP Server
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mcp
```
MCP Endpoint: http://localhost:8002/sse

## MCP Tools

### 1. read_tasks

**Purpose**: Retrieve task information

**Parameters**:
- `id` (string, optional): Task UUID. If provided, returns a specific task
- `status` (string, optional): Filter by status when listing. Values: `NEW`, `IN_PROGRESS`, `DONE`

**Examples**:
```json
// Get specific task
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}

// List all tasks
{}

// List tasks by status
{
  "status": "IN_PROGRESS"
}
```

**Returns**: Single task object or array of tasks

---

### 2. write_tasks

**Purpose**: Create, update, or delete tasks

**Parameters**:
- `operation` (string, required): Operation type - `create`, `update_status`, or `delete`
- `id` (string): Required for `update_status` and `delete`
- `title` (string): Required for `create` (1-200 chars)
- `description` (string): Optional for `create` (max 1000 chars)
- `status` (string): Required for `update_status`. Values: `NEW`, `IN_PROGRESS`, `DONE`

**Status Transition Rules**:
- `NEW` → `IN_PROGRESS` ✓
- `IN_PROGRESS` → `DONE` ✓
- `IN_PROGRESS` → `NEW` ✓
- `NEW` → `DONE` ✗ (must go through IN_PROGRESS)
- `DONE` → any status ✗ (terminal state)

**Examples**:
```json
// Create a new task
{
  "operation": "create",
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication"
}

// Update task status
{
  "operation": "update_status",
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "status": "IN_PROGRESS"
}

// Delete a task
{
  "operation": "delete",
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Returns**: Task object (create/update) or success message (delete)

## Testing with GitHub Copilot

### Setup

1. Start the MCP server:
   ```bash
   ./run-mcp-server.sh
   ```

2. In VS Code, open Copilot Chat and select **Agent** mode

3. Select **Tools** → **Add More Tools...** → **Add MCP Server**

4. Choose **HTTP (HTTP or Server-Sent Events)**

5. Enter:
   - **Server URL**: `http://localhost:8002/sse`
   - **Server ID**: `mytasks-mcp` (or any name you prefer)
   
6. Select **Workspace Settings**

### Example Prompts

Try these natural language prompts in Copilot Chat:

- "Show me all tasks"
- "List tasks that are in progress"
- "Create a new task called 'Update documentation' with description 'Add MCP server docs'"
- "Update task [id] to in progress"
- "Delete task [id]"

## Configuration

### MCP Server Configuration (application-mcp.yml)

```yaml
server:
  port: 8002

spring:
  application:
    name: MyTasks MCP
  ai:
    mcp:
      server:
        transport: sse
        sse:
          path: /sse
```

### Environment Variables

You can override configuration via environment variables:

```bash
# Change MCP server port
SERVER_PORT=8003 mvn spring-boot:run -Dspring-boot.run.profiles=mcp

# Change data directory
DATA_DIRECTORY=/path/to/data mvn spring-boot:run -Dspring-boot.run.profiles=mcp
```

## Troubleshooting

### MCP Server won't start

1. **Check if port 8002 is available**:
   ```bash
   lsof -i :8002
   ```

2. **Verify Maven dependencies**:
   ```bash
   mvn clean install
   ```

3. **Check logs** for Spring AI MCP configuration errors

### GitHub Copilot can't connect

1. **Verify MCP server is running**:
   ```bash
   curl http://localhost:8002/sse
   ```

2. **Check the URL** in Copilot settings matches `http://localhost:8002/sse`

3. **Restart VS Code** and try adding the MCP server again

### Tool calls fail

1. **Check that data directory exists** (default: `./data`)

2. **Verify tasks.json is readable/writable**

3. **Check server logs** for error details

## Development

### Adding New Tools

1. Add a method to `TaskMcpToolService` with `@Tool` annotation:
   ```java
   @Tool(description = "Your tool description")
   public Object myNewTool(String param1, String param2) {
       // Implementation
   }
   ```

2. Restart the MCP server

3. The tool will be automatically discovered by MCP clients

### Running Tests

```bash
mvn test
```

## Dependencies

- **Spring Boot 4.0.0**: Application framework
- **Spring AI 1.1.0**: MCP server implementation (`spring-ai-starter-mcp-server-webmvc`)
- **Java 21**: Language runtime

## References

- [MCP Server Specification](MCP_SERVER_SPEC.md)
- [Task API Specification](API_SPEC.md)
- [Model Context Protocol Documentation](https://modelcontextprotocol.io/)
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
