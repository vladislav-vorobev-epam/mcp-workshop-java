# Task Management System with MCP Server

This project consists of two Spring Boot applications:
1. **Task API Server** - REST API for CRUD operations on tasks (port 8080)
2. **MCP Server** - Model Context Protocol server that proxies the Task API (port 8081)

## Architecture

```
┌─────────────┐      HTTP      ┌─────────────┐      HTTP      ┌─────────────┐
│             │ ──────────────> │             │ ──────────────> │             │
│  MCP Client │                 │  MCP Server │                 │  Task API   │
│             │ <────────────── │  (port 8081)│ <────────────── │ (port 8080) │
└─────────────┘                 └─────────────┘                 └─────────────┘
                                       │                               │
                                       │                               │
                                       └───────────────────────────────┘
                                              Filesystem Storage
                                              (data/tasks/*.json)
```

## Prerequisites

- Java 21
- Maven 3.6+

## Running the Applications

### 1. Start the Task API Server (Terminal 1)

```bash
chmod +x run-task-api.sh
./run-task-api.sh
```

Or manually:
```bash
mvn spring-boot:run -Dspring-boot.run.mainClass=com.epam.masterclass.Main
```

The Task API will be available at `http://localhost:8080`

### 2. Start the MCP Server (Terminal 2)

```bash
chmod +x run-mcp-server.sh
./run-mcp-server.sh
```

Or manually:
```bash
mvn spring-boot:run -Dspring-boot.run.mainClass=com.epam.masterclass.mcp.McpServer -Dspring-boot.run.profiles=mcp
```

The MCP Server will be available at `http://localhost:8081`

## Task API Endpoints

- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/tasks` - Create new task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task

### Example: Create a Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Buy groceries",
    "description": "Milk, eggs, bread",
    "completed": false
  }'
```

## MCP Server Tools

The MCP server exposes the following tools via REST endpoints:

- `GET /mcp/tools/list` - List all available tools
- `POST /mcp/tools/call` - Call a tool with JSON body: `{"name": "tool_name", "arguments": {...}}`

Available tools:
- `get_all_tasks` - Get all tasks from the task management system
- `get_task` - Get a specific task by its ID (requires: id)
- `create_task` - Create a new task (requires: title, description, completed)
- `update_task` - Update an existing task (requires: id, title, description, completed)
- `delete_task` - Delete a task by its ID (requires: id)

## Configuration

### Task API Configuration
- Port: 8080 (default)
- Data directory: `./data/tasks` (configurable via `DATA_DIRECTORY` env var)

### MCP Server Configuration
- Port: 8081
- Task API URL: `http://localhost:8080` (configurable via `TASK_API_URL` env var)
- Transport: HTTP
- Profile: `mcp`

## Data Storage

Tasks are stored as individual JSON files in the `data/tasks/` directory. Each task is saved as `{id}.json`.

Example task file:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "title": "Buy groceries",
  "description": "Milk, eggs, bread",
  "completed": false
}
```

## MCP Tool Examples

### List available tools
```bash
curl http://localhost:8081/mcp/tools/list
```

### Call a tool
```bash
# Get all tasks
curl -X POST http://localhost:8081/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{"name": "get_all_tasks"}'

# Create a task
curl -X POST http://localhost:8081/mcp/tools/call \
  -H "Content-Type: application/json" \
  -d '{
    "name": "create_task",
    "arguments": {
      "title": "New task",
      "description": "Task description",
      "completed": false
    }
  }'
```

## Technology Stack

- Spring Boot 3.4.1
- Java 21
- Jackson (JSON processing)
- Maven
