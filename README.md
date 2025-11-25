# MCP Workshop Java - Task Tracking System

A comprehensive Task Tracking system built with Spring Boot 4.0 and Java 21, featuring both a REST API and a Model Context Protocol (MCP) server for AI-powered integrations.

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Maven 3.8+

### Running the Servers

#### Option 1: REST API Server (Port 8001)
```bash
./run-api-server.sh
```
Access at: http://localhost:8001/api

#### Option 2: MCP Server (Port 8002)
```bash
./run-mcp-server.sh
```
MCP Endpoint: http://localhost:8002/sse

## 📋 Features

### REST API
- ✅ Create, read, update, and delete tasks
- ✅ Task status workflow (NEW → IN_PROGRESS → DONE)
- ✅ Status transition validation
- ✅ File-based persistence (JSON)
- ✅ Full input validation

### MCP Server
- ✅ Two aggregated tools: `read_tasks` and `write_tasks`
- ✅ GitHub Copilot integration
- ✅ Natural language task management
- ✅ Server-Sent Events (SSE) transport
- ✅ Profile-based configuration

## 🏗️ Architecture

```
mcp-workshop-java/
├── src/main/java/com/epam/masterclass/
│   ├── Main.java                      # Application entry point
│   ├── controller/
│   │   ├── TaskController.java        # REST API endpoints
│   │   └── GlobalExceptionHandler.java
│   ├── service/
│   │   └── TaskService.java           # Business logic
│   ├── repository/
│   │   └── TaskRepository.java        # Data persistence
│   ├── model/
│   │   ├── Task.java                  # Task entity
│   │   ├── TaskStatus.java            # Status enum
│   │   └── ...
│   ├── exception/
│   │   └── ...
│   └── mcp/                           # MCP Server (Profile: mcp)
│       ├── TaskMcpToolService.java    # MCP tools implementation
│       └── McpServerConfig.java       # MCP configuration
├── src/main/resources/
│   ├── application.yml                # API server config
│   └── application-mcp.yml            # MCP server config
├── data/
│   └── tasks.json                     # Task storage
├── run-api-server.sh                  # API server launcher
├── run-mcp-server.sh                  # MCP server launcher
├── API_SPEC.md                        # REST API specification
├── MCP_SERVER_SPEC.md                 # MCP server specification
└── MCP_README.md                      # MCP server documentation
```

## 📖 Documentation

- **[API Specification](API_SPEC.md)** - Complete REST API documentation with examples
- **[MCP Server Specification](MCP_SERVER_SPEC.md)** - MCP tools and implementation details
- **[MCP Setup Guide](MCP_README.md)** - How to run and test the MCP server

## 🔧 Technology Stack

- **Java 21** - Modern Java with records, pattern matching
- **Spring Boot 4.0.0** - Application framework
- **Spring AI 1.1.0** - MCP server implementation
- **Maven** - Build management
- **Jackson** - JSON serialization

## 🛠️ Development

### Build the Project
```bash
mvn clean install
```

### Run Tests
```bash
mvn test
```

### Run with Specific Profile
```bash
# API Server (default profile)
mvn spring-boot:run

# MCP Server (mcp profile)
mvn spring-boot:run -Dspring-boot.run.profiles=mcp
```

## 🤖 Using with GitHub Copilot

1. Start the MCP server:
   ```bash
   ./run-mcp-server.sh
   ```

2. In VS Code Copilot Chat (Agent mode):
   - Add MCP Server: `http://localhost:8002/sse`
   - Server ID: `mytasks-mcp`

3. Try natural language commands:
   - "Show me all tasks"
   - "Create a new task for implementing authentication"
   - "Update task [id] to in progress"
   - "List all completed tasks"

See [MCP_README.md](MCP_README.md) for detailed setup instructions.

## 📡 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/tasks` | Create a new task |
| `GET` | `/api/tasks` | List all tasks (optional `?status=` filter) |
| `GET` | `/api/tasks/{id}` | Get task by ID |
| `PATCH` | `/api/tasks/{id}/status` | Update task status |
| `DELETE` | `/api/tasks/{id}` | Delete task |

## 🧩 MCP Tools

| Tool | Description |
|------|-------------|
| `read_tasks` | Read single task by ID or list all/filtered tasks |
| `write_tasks` | Create, update status, or delete tasks |

## 🔄 Task Status Workflow

```
NEW → IN_PROGRESS → DONE
      ↓
    NEW (rollback allowed)
```

**Rules:**
- NEW can only transition to IN_PROGRESS
- IN_PROGRESS can transition to DONE or back to NEW
- DONE is terminal (no transitions allowed)
- Direct NEW → DONE transition is forbidden

## 📁 Data Storage

Tasks are persisted in `data/tasks.json` with the following structure:
```json
{
  "id": "uuid",
  "title": "Task title",
  "description": "Task description",
  "status": "NEW|IN_PROGRESS|DONE",
  "createdAt": "2025-11-25T10:30:00Z",
  "updatedAt": "2025-11-25T10:30:00Z"
}
```

## 🔐 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Server port | 8001 (API), 8002 (MCP) |
| `DATA_DIRECTORY` | Data storage directory | `./data` |

### Profiles

- **default**: REST API server on port 8001
- **mcp**: MCP server on port 8002

## 🧪 Testing

### Manual API Testing
```bash
# Create a task
curl -X POST http://localhost:8001/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Task","description":"Test Description"}'

# List all tasks
curl http://localhost:8001/api/tasks

# Get specific task
curl http://localhost:8001/api/tasks/{id}

# Update status
curl -X PATCH http://localhost:8001/api/tasks/{id}/status \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}'

# Delete task
curl -X DELETE http://localhost:8001/api/tasks/{id}
```

### MCP Server Testing
```bash
# Verify MCP endpoint is available
curl http://localhost:8002/sse
```

## 📝 Code Conventions

- Base package: `com.epam.masterclass`
- Use Java 21 features (records, pattern matching, switch expressions)
- Constructor injection preferred over field injection
- MCP-related beans marked with `@Profile("mcp")`
- Comprehensive JavaDoc for public APIs

## 🤝 Contributing

1. Follow existing code style and conventions
2. Add tests for new features
3. Update documentation as needed
4. Ensure all tests pass before committing

## 📄 License

[Add your license here]

## 🆘 Support

For issues or questions:
1. Check the documentation files
2. Review the troubleshooting section in [MCP_README.md](MCP_README.md)
3. Examine server logs for error details

---

**Built with ❤️ using Spring Boot 4.0 and Spring AI**
