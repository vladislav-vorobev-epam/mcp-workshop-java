# Task Tracking REST API Specification

## Overview
A simple REST API for tracking tasks with status management. This service allows users to create, retrieve, list, update status, and delete tasks.

**Version:** 1.0  
**Base URL:** `http://localhost:8001/api`  
**Content-Type:** `application/json`

---

## Data Model

### Task Entity

| Field | Type | Description | Constraints |
|-------|------|-------------|-------------|
| `id` | `string` (UUID) | Unique task identifier | Auto-generated, read-only |
| `title` | `string` | Task title | Required, 1-200 characters |
| `description` | `string` | Task description | Optional, max 1000 characters |
| `status` | `string` | Current task status | Enum: `NEW`, `IN_PROGRESS`, `DONE` |
| `createdAt` | `string` (ISO 8601) | Task creation timestamp | Auto-generated, read-only |
| `updatedAt` | `string` (ISO 8601) | Last modification timestamp | Auto-generated, read-only |

### Task Status Workflow

```
NEW → IN_PROGRESS → DONE
```

**Allowed Transitions:**
- `NEW` → `IN_PROGRESS`
- `IN_PROGRESS` → `DONE`
- `IN_PROGRESS` → `NEW` (allow moving back)

**Forbidden Transitions:**
- `NEW` → `DONE` (must go through `IN_PROGRESS`)
- `DONE` → Any status (terminal state)

---

## API Endpoints

### 1. Create Task

Creates a new task with status `NEW`.

**Endpoint:** `POST /api/tasks`

**Request Body:**
```json
{
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication to the API"
}
```

**Validation Rules:**
- `title`: Required, not blank, 1-200 characters
- `description`: Optional, max 1000 characters

**Success Response:**
- **Status Code:** `201 Created`
- **Headers:** `Location: /api/tasks/{id}`
- **Body:**
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

**Error Responses:**

- **Status Code:** `400 Bad Request`
```json
{
  "timestamp": "2025-11-25T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "errors": [
    {
      "field": "title",
      "message": "Title is required and cannot be blank"
    }
  ]
}
```

---

### 2. Get Task Details

Retrieves details of a specific task by ID.

**Endpoint:** `GET /api/tasks/{id}`

**Path Parameters:**
- `id` (required): Task UUID

**Success Response:**
- **Status Code:** `200 OK`
- **Body:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication to the API",
  "status": "IN_PROGRESS",
  "createdAt": "2025-11-25T10:30:00Z",
  "updatedAt": "2025-11-25T11:15:00Z"
}
```

**Error Responses:**

- **Status Code:** `404 Not Found`
```json
{
  "timestamp": "2025-11-25T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

### 3. List All Tasks

Retrieves a list of all tasks.

**Endpoint:** `GET /api/tasks`

**Query Parameters (Optional):**
- `status`: Filter by status (`NEW`, `IN_PROGRESS`, `DONE`)

**Examples:**
- `GET /api/tasks` - Returns all tasks
- `GET /api/tasks?status=IN_PROGRESS` - Returns only tasks with status IN_PROGRESS

**Success Response:**
- **Status Code:** `200 OK`
- **Body:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "title": "Implement user authentication",
    "description": "Add JWT-based authentication to the API",
    "status": "IN_PROGRESS",
    "createdAt": "2025-11-25T10:30:00Z",
    "updatedAt": "2025-11-25T11:15:00Z"
  },
  {
    "id": "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
    "title": "Write API documentation",
    "description": "Document all REST endpoints",
    "status": "NEW",
    "createdAt": "2025-11-25T09:00:00Z",
    "updatedAt": "2025-11-25T09:00:00Z"
  }
]
```

**Note:** Returns an empty array `[]` if no tasks exist.

---

### 4. Update Task Status

Updates the status of a task following the allowed workflow transitions.

**Endpoint:** `PATCH /api/tasks/{id}/status`

**Path Parameters:**
- `id` (required): Task UUID

**Request Body:**
```json
{
  "status": "IN_PROGRESS"
}
```

**Validation Rules:**
- `status`: Required, must be one of: `NEW`, `IN_PROGRESS`, `DONE`
- Transition must be allowed according to workflow rules

**Success Response:**
- **Status Code:** `200 OK`
- **Body:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Implement user authentication",
  "description": "Add JWT-based authentication to the API",
  "status": "IN_PROGRESS",
  "createdAt": "2025-11-25T10:30:00Z",
  "updatedAt": "2025-11-25T11:15:00Z"
}
```

**Error Responses:**

- **Status Code:** `400 Bad Request` (Invalid Transition)
```json
{
  "timestamp": "2025-11-25T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid status transition from NEW to DONE. Allowed transitions: NEW → IN_PROGRESS"
}
```

- **Status Code:** `400 Bad Request` (Task Already in Terminal State)
```json
{
  "timestamp": "2025-11-25T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot change status of completed task. Current status: DONE"
}
```

- **Status Code:** `404 Not Found`
```json
{
  "timestamp": "2025-11-25T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

### 5. Delete Task

Deletes a task permanently.

**Endpoint:** `DELETE /api/tasks/{id}`

**Path Parameters:**
- `id` (required): Task UUID

**Success Response:**
- **Status Code:** `204 No Content`
- **Body:** Empty

**Error Responses:**

- **Status Code:** `404 Not Found`
```json
{
  "timestamp": "2025-11-25T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Task not found with id: 550e8400-e29b-41d4-a716-446655440000"
}
```

---

## HTTP Status Codes Summary

| Code | Description | Usage |
|------|-------------|-------|
| `200 OK` | Success | GET, PATCH operations |
| `201 Created` | Resource created | POST operations |
| `204 No Content` | Success, no body | DELETE operations |
| `400 Bad Request` | Validation error or invalid operation | Invalid input, business rule violations |
| `404 Not Found` | Resource not found | Task with given ID doesn't exist |
| `500 Internal Server Error` | Server error | Unexpected errors |

---

## Implementation Notes

### Technology Stack
- **Spring Boot 4.0.0** with Spring Web
- **Java 21** (use Records for DTOs)
- **Jakarta Bean Validation** for request validation
- **Jackson** for JSON serialization/deserialization

### Recommended Package Structure
```
com.epam.masterclass/
├── controller/
│   └── TaskController.java
├── service/
│   └── TaskService.java
├── model/
│   ├── Task.java
│   ├── TaskStatus.java (enum)
│   ├── CreateTaskRequest.java
│   └── UpdateStatusRequest.java
├── repository/
│   └── TaskRepository.java (in-memory or file-based)
└── exception/
    ├── TaskNotFoundException.java
    └── InvalidStatusTransitionException.java
```

### Validation Annotations
- Use `@Valid` on request bodies
- Use `@NotBlank`, `@Size`, `@NotNull` on DTOs
- Create custom validator for status transitions if needed

### Error Handling
- Implement `@RestControllerAdvice` for global exception handling
- Use `@ExceptionHandler` for specific exceptions
- Return consistent error response structure

### Data Persistence
- **Option 1:** In-memory storage using `ConcurrentHashMap`
- **Option 2:** File-based JSON storage in `./data` directory (configured in `application.yml`)
- **Option 3:** Add Spring Data JPA with H2 database for production-ready solution

### Testing Recommendations
- Unit tests for service layer (status transition logic)
- Integration tests for REST endpoints using `@SpringBootTest` and `MockMvc`
- Test all validation scenarios and error cases

---

## Examples

### Complete Workflow Example

**1. Create a new task:**
```bash
curl -X POST http://localhost:8001/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title": "Review code changes", "description": "Review PR #123"}'
```

Response: `201 Created` with task ID `abc-123`

**2. List all tasks:**
```bash
curl http://localhost:8001/api/tasks
```

**3. Move task to IN_PROGRESS:**
```bash
curl -X PATCH http://localhost:8001/api/tasks/abc-123/status \
  -H "Content-Type: application/json" \
  -d '{"status": "IN_PROGRESS"}'
```

**4. Get task details:**
```bash
curl http://localhost:8001/api/tasks/abc-123
```

**5. Complete the task:**
```bash
curl -X PATCH http://localhost:8001/api/tasks/abc-123/status \
  -H "Content-Type: application/json" \
  -d '{"status": "DONE"}'
```

**6. Delete the task:**
```bash
curl -X DELETE http://localhost:8001/api/tasks/abc-123
```

---

## Future Enhancements (Out of Scope)

- Pagination and sorting for task lists
- Full task update endpoint (PUT /api/tasks/{id})
- Task search by title/description
- Task assignment to users
- Due dates and priority levels
- Audit log for status changes
- Authentication and authorization
- WebSocket notifications for real-time updates
