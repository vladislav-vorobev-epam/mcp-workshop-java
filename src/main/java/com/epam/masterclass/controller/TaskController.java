package com.epam.masterclass.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.epam.masterclass.model.CreateTaskRequest;
import com.epam.masterclass.model.Task;
import com.epam.masterclass.model.TaskStatus;
import com.epam.masterclass.model.UpdateStatusRequest;
import com.epam.masterclass.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Creates a new task.
     * POST /api/tasks
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = taskService.createTask(request.title(), request.description());

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(task.id())
                .toUri();

        return ResponseEntity.created(location).body(task);
    }

    /**
     * Gets a task by ID.
     * GET /api/tasks/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTask(@PathVariable String id) {
        Task task = taskService.getTask(id);
        return ResponseEntity.ok(task);
    }

    /**
     * Lists all tasks, optionally filtered by status.
     * GET /api/tasks
     * GET /api/tasks?status=IN_PROGRESS
     */
    @GetMapping
    public ResponseEntity<List<Task>> listTasks(
            @RequestParam(required = false) TaskStatus status
    ) {
        List<Task> tasks = taskService.getAllTasks(status);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Updates the status of a task.
     * PATCH /api/tasks/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Task> updateTaskStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatusRequest request
    ) {
        Task task = taskService.updateTaskStatus(id, request.status());
        return ResponseEntity.ok(task);
    }

    /**
     * Deletes a task.
     * DELETE /api/tasks/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable String id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
