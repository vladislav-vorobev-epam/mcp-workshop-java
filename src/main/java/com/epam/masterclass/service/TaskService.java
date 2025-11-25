package com.epam.masterclass.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.epam.masterclass.exception.InvalidStatusTransitionException;
import com.epam.masterclass.exception.TaskNotFoundException;
import com.epam.masterclass.model.Task;
import com.epam.masterclass.model.TaskStatus;
import com.epam.masterclass.repository.TaskRepository;

@Service
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Creates a new task.
     *
     * @param title       task title
     * @param description task description
     * @return the created task
     */
    public Task createTask(String title, String description) {
        Task task = Task.create(title, description);
        Task savedTask = taskRepository.save(task);
        log.info("Created task: {} with title: {}", savedTask.id(), savedTask.title());
        return savedTask;
    }

    /**
     * Gets a task by ID.
     *
     * @param id task ID
     * @return the task
     * @throws TaskNotFoundException if task not found
     */
    public Task getTask(String id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    /**
     * Gets all tasks, optionally filtered by status.
     *
     * @param status optional status filter
     * @return list of tasks
     */
    public List<Task> getAllTasks(TaskStatus status) {
        if (status != null) {
            log.debug("Fetching tasks with status: {}", status);
            return taskRepository.findByStatus(status);
        }
        log.debug("Fetching all tasks");
        return taskRepository.findAll();
    }

    /**
     * Updates the status of a task.
     *
     * @param id        task ID
     * @param newStatus new status
     * @return the updated task
     * @throws TaskNotFoundException            if task not found
     * @throws InvalidStatusTransitionException if transition is not allowed
     */
    public Task updateTaskStatus(String id, TaskStatus newStatus) {
        Task task = getTask(id);

        // Check if transition is allowed
        if (!task.status().canTransitionTo(newStatus)) {
            log.warn("Invalid status transition attempt for task {}: {} -> {}",
                    id, task.status(), newStatus);
            throw new InvalidStatusTransitionException(task.status(), newStatus);
        }

        Task updatedTask = task.withStatus(newStatus);
        Task savedTask = taskRepository.save(updatedTask);
        log.info("Updated task {} status: {} -> {}", id, task.status(), newStatus);
        return savedTask;
    }

    /**
     * Deletes a task.
     *
     * @param id task ID
     * @throws TaskNotFoundException if task not found
     */
    public void deleteTask(String id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }
        taskRepository.deleteById(id);
        log.info("Deleted task: {}", id);
    }
}
