# MCP Server Specification for Task Tracking API

## Overview

This document specifies a Model Context Protocol (MCP) server that provides simplified access to the Task Tracking REST API. The MCP server exposes two high-level tools that aggregate all underlying API operations for seamless task management.

**MCP Server Name:** `task-tracker`  
**Version:** 1.0  
**Target API:** Task Tracking REST API v1.0  
**Base URL:** `http://localhost:8001/api`

---

## MCP Tools

### 1. read_tasks

**Description:**  
Retrieves task information from the Task Tracking API. This tool aggregates both single task retrieval and task listing operations.

**Input Schema:**

```json
{
  "type": "object",
  "properties": {
    "id": {
      "type": "string",
      "description": "Task ID (UUID format). If provided, retrieves a specific task. If omitted, lists all tasks."
    },
    "status": {
      "type": "string",
      "enum": ["NEW", "IN_PROGRESS", "DONE"],
      "description": "Optional status filter when listing tasks. Only used when 'id' is not provided."
    }
  }
}
```

**Behavior:**

- **When `id` is provided:** Calls `GET /api/tasks/{id}` to retrieve a specific task
- **When `id` is omitted:** Calls `GET /api/tasks` to list all tasks
- **When `id` is omitted and `status` is provided:** Calls `GET /api/tasks?status={status}` to filter tasks by status

**Returns:**

Single task object or array of task objects:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication to the API",
  "status": "NEW",
  "createdAt": "2025-11-25T10:30:00Z",
  "updatedAt": "2025-11-25T10:30:00Z"
}
```

**Error Handling:**

- `404 Not Found`: Task with specified ID does not exist
- `400 Bad Request`: Invalid status value provided
- `500 Internal Server Error`: Server error occurred

**Example Usage:**

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

---

### 2. write_tasks

**Description:**  
Performs write operations on tasks including creating, updating status, and deleting tasks. This tool aggregates all mutation operations on the Task Tracking API.

**Input Schema:**

```json
{
  "type": "object",
  "properties": {
    "operation": {
      "type": "string",
      "enum": ["create", "update_status", "delete"],
      "description": "The operation to perform on tasks"
    },
    "id": {
      "type": "string",
      "description": "Task ID (UUID format). Required for 'update_status' and 'delete' operations."
    },
    "title": {
      "type": "string",
      "minLength": 1,
      "maxLength": 200,
      "description": "Task title. Required for 'create' operation."
    },
    "description": {
      "type": "string",
      "maxLength": 1000,
      "description": "Task description. Optional for 'create' operation."
    },
    "status": {
      "type": "string",
      "enum": ["NEW", "IN_PROGRESS", "DONE"],
      "description": "Target status. Required for 'update_status' operation."
    }
  },
  "required": ["operation"]
}
```

**Behavior:**

#### Operation: `create`
- **Required fields:** `title`
- **Optional fields:** `description`
- **API Call:** `POST /api/tasks`
- **Returns:** The newly created task with status `NEW`

#### Operation: `update_status`
- **Required fields:** `id`, `status`
- **API Call:** `PATCH /api/tasks/{id}/status`
- **Validates:** Status transition rules (e.g., cannot transition from `NEW` to `DONE`)
- **Returns:** The updated task

#### Operation: `delete`
- **Required fields:** `id`
- **API Call:** `DELETE /api/tasks/{id}`
- **Returns:** Success confirmation message

**Status Transition Rules:**

The tool enforces the following workflow:
- `NEW` → `IN_PROGRESS` ✓
- `IN_PROGRESS` → `DONE` ✓
- `IN_PROGRESS` → `NEW` ✓
- `NEW` → `DONE` ✗ (must go through `IN_PROGRESS`)
- `DONE` → any status ✗ (terminal state)

**Returns:**

Task object (for create/update) or success message (for delete):

```json
// Create/Update response
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication to the API",
  "status": "IN_PROGRESS",
  "createdAt": "2025-11-25T10:30:00Z",
  "updatedAt": "2025-11-25T10:35:00Z"
}

// Delete response
{
  "message": "Task deleted successfully",
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Error Handling:**

- `400 Bad Request`: Invalid status transition, validation errors, or missing required fields
- `404 Not Found`: Task with specified ID does not exist
- `422 Unprocessable Entity`: Invalid status transition attempt
- `500 Internal Server Error`: Server error occurred

**Example Usage:**

```json
// Create a new task
{
  "operation": "create",
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication to the API"
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

---

## Implementation Guidelines

### Connection Configuration

The MCP server should support configuration for the Task Tracking API base URL:

```json
{
  "mcpServers": {
    "task-tracker": {
      "command": "java",
      "args": ["-jar", "mcp-task-tracker-server.jar"],
      "env": {
        "TASK_API_BASE_URL": "http://localhost:8001/api"
      }
    }
  }
}
```

### Authentication

Currently, the Task Tracking API does not require authentication. Future versions may add JWT-based authentication, which should be configured via environment variables:

```json
{
  "env": {
    "TASK_API_BASE_URL": "http://localhost:8001/api",
    "TASK_API_TOKEN": "your-jwt-token"
  }
}
```

### Error Response Format

All errors should be returned in a consistent format:

```json
{
  "error": "TaskNotFound",
  "message": "Task with ID '550e8400-e29b-41d4-a716-446655440000' not found",
  "timestamp": "2025-11-25T10:30:00Z"
}
```

### Logging

The MCP server should log:
- All incoming tool requests with parameters
- All outgoing HTTP requests to the Task API
- Response status codes and error details
- Status transition validation results

### Performance Considerations

- Implement connection pooling for HTTP client
- Set reasonable timeouts (e.g., 30 seconds for API calls)
- Cache task status transition rules to avoid repeated validation logic
- Consider implementing retry logic for transient failures (e.g., network issues)

---

## Testing Recommendations

### Unit Tests

Test each operation independently:
- `read_tasks` with and without ID
- `read_tasks` with status filter
- `write_tasks` create operation
- `write_tasks` update_status with valid/invalid transitions
- `write_tasks` delete operation

### Integration Tests

Test against a running Task Tracking API instance:
- Create → Read → Update → Delete workflow
- Status transition validation
- Error handling for non-existent tasks
- Concurrent operations

### MCP Protocol Tests

Verify MCP protocol compliance:
- Tool discovery (list available tools)
- JSON-RPC request/response format
- Error message format
- Schema validation

---

## Future Enhancements

Potential additions for future versions:

1. **Bulk Operations:**
   - `bulk_create_tasks`: Create multiple tasks at once
   - `bulk_update_status`: Update status for multiple tasks
   - `bulk_delete_tasks`: Delete multiple tasks

2. **Advanced Filtering:**
   - Filter by date range (createdAt, updatedAt)
   - Search by title/description text
   - Sort results by various fields

3. **Task Relationships:**
   - Support for subtasks
   - Task dependencies

4. **Notifications:**
   - Subscribe to task status changes
   - Webhook support for task events

---

## Appendix: API Endpoint Mapping

| MCP Tool Operation | HTTP Method | API Endpoint |
|-------------------|-------------|--------------|
| `read_tasks` (by ID) | GET | `/api/tasks/{id}` |
| `read_tasks` (list all) | GET | `/api/tasks` |
| `read_tasks` (filtered) | GET | `/api/tasks?status={status}` |
| `write_tasks` (create) | POST | `/api/tasks` |
| `write_tasks` (update_status) | PATCH | `/api/tasks/{id}/status` |
| `write_tasks` (delete) | DELETE | `/api/tasks/{id}` |
