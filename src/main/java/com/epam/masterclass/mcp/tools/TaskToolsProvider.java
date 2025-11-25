package com.epam.masterclass.mcp.tools;

import java.util.List;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.epam.masterclass.mcp.client.TaskApiClient;
import com.epam.masterclass.model.Task;

@Component
@Profile("mcp")
public class TaskToolsProvider {

    private final TaskApiClient taskApiClient;

    public TaskToolsProvider(TaskApiClient taskApiClient) {
        this.taskApiClient = taskApiClient;
    }

    @Tool(name = "get_all_tasks", description = "Get all tasks from the task management system")
    public List<Task> getAllTasks() {
        return taskApiClient.getAllTasks();
    }

    @Tool(name = "get_task", description = "Get a specific task by ID")
    public Task getTask(String id) {
        return taskApiClient.getTaskById(id);
    }

    @Tool(name = "create_task", description = "Create a new task")
    public Task createTask(String title, String description, Boolean completed) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed != null ? completed : false);
        return taskApiClient.createTask(task);
    }

    @Tool(name = "update_task", description = "Update an existing task")
    public Task updateTask(String id, String title, String description, Boolean completed) {
        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setCompleted(completed != null ? completed : false);
        return taskApiClient.updateTask(id, task);
    }

    @Tool(name = "delete_task", description = "Delete a task by ID")
    public String deleteTask(String id) {
        taskApiClient.deleteTask(id);
        return "Task deleted successfully";
    }
}
