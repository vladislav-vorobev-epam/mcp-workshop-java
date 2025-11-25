package com.epam.masterclass.mcp;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.epam.masterclass.model.Task;
import com.epam.masterclass.model.TaskStatus;
import com.epam.masterclass.service.TaskService;

/**
 * MCP Tool Service that provides aggregated read and write operations
 * for the Task Tracking API according to the MCP Server Specification.
 */
@Service
@Profile("mcp")
public class TaskMcpToolService {

    private final TaskService taskService;

    public TaskMcpToolService(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Retrieves task information from the Task Tracking API.
     * This tool aggregates both single task retrieval and task listing operations.
     *
     * @param id     Task ID (UUID format). If provided, retrieves a specific task. If omitted, lists all tasks.
     * @param status Optional status filter when listing tasks. Only used when 'id' is not provided.
     *               Allowed values: NEW, IN_PROGRESS, DONE
     * @return Single task object or array of task objects
     */
    @Tool(description = """
            Retrieves task information from the Task Tracking API. 
            If 'id' is provided, retrieves a specific task by its UUID. 
            If 'id' is omitted, lists all tasks. 
            When listing, you can optionally filter by status (NEW, IN_PROGRESS, or DONE).
            Returns a single task object when id is provided, or an array of tasks when listing.
            """)
    public Object readTasks(String id, String status) {
        // If ID is provided, return specific task
        if (id != null && !id.isBlank()) {
            return taskService.getTask(id);
        }

        // Otherwise, list tasks with optional status filter
        TaskStatus statusFilter = null;
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = TaskStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Invalid status value. Allowed values are: NEW, IN_PROGRESS, DONE"
                );
            }
        }

        return taskService.getAllTasks(statusFilter);
    }

    /**
     * Performs write operations on tasks including creating, updating status, and deleting tasks.
     * This tool aggregates all mutation operations on the Task Tracking API.
     *
     * @param operation   The operation to perform: 'create', 'update_status', or 'delete'
     * @param id          Task ID (UUID format). Required for 'update_status' and 'delete' operations
     * @param title       Task title (1-200 characters). Required for 'create' operation
     * @param description Task description (max 1000 characters). Optional for 'create' operation
     * @param status      Target status. Required for 'update_status' operation. Allowed values: NEW, IN_PROGRESS, DONE
     * @return The created/updated task object or a success message for delete operation
     */
    @Tool(description = """
            Performs write operations on tasks. Specify the operation type:
            - 'create': Creates a new task with status NEW. Requires 'title', optional 'description'.
            - 'update_status': Updates task status. Requires 'id' and 'status'. 
              Status transitions must follow workflow: NEW->IN_PROGRESS, IN_PROGRESS->DONE or IN_PROGRESS->NEW. 
              DONE is terminal (no transitions allowed from DONE).
            - 'delete': Deletes a task. Requires 'id'.
            Returns the task object for create/update, or a success message for delete.
            """)
    public Object writeTasks(
            String operation,
            String id,
            String title,
            String description,
            String status
    ) {
        if (operation == null || operation.isBlank()) {
            throw new IllegalArgumentException("Operation is required. Allowed values: create, update_status, delete");
        }

        return switch (operation.trim().toLowerCase()) {
            case "create" -> handleCreate(title, description);
            case "update_status" -> handleUpdateStatus(id, status);
            case "delete" -> handleDelete(id);
            default -> throw new IllegalArgumentException(
                    "Invalid operation. Allowed values: create, update_status, delete"
            );
        };
    }

    /**
     * Handles task creation operation.
     */
    private Task handleCreate(String title, String description) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required for create operation");
        }
        return taskService.createTask(title, description);
    }

    /**
     * Handles task status update operation.
     */
    private Task handleUpdateStatus(String id, String status) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID is required for update_status operation");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Status is required for update_status operation");
        }

        TaskStatus newStatus;
        try {
            newStatus = TaskStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status value. Allowed values are: NEW, IN_PROGRESS, DONE"
            );
        }

        return taskService.updateTaskStatus(id, newStatus);
    }

    /**
     * Handles task deletion operation.
     */
    private String handleDelete(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID is required for delete operation");
        }
        taskService.deleteTask(id);
        return String.format("{\"message\": \"Task deleted successfully\", \"id\": \"%s\"}", id);
    }
}
