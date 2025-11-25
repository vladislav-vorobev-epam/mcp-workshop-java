package com.epam.masterclass.repository;

import com.epam.masterclass.model.Task;
import com.epam.masterclass.model.TaskStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Repository
public class TaskRepository {
    private static final Logger log = LoggerFactory.getLogger(TaskRepository.class);
    private static final String TASKS_FILE = "tasks.json";

    private final Path dataDirectory;
    private final Path tasksFilePath;
    private final ObjectMapper objectMapper;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public TaskRepository(@Value("${app.data.directory}") String dataDirectory) {
        this.dataDirectory = Paths.get(dataDirectory);
        this.tasksFilePath = this.dataDirectory.resolve(TASKS_FILE);
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        initializeDataDirectory();
    }

    private void initializeDataDirectory() {
        try {
            if (!Files.exists(dataDirectory)) {
                Files.createDirectories(dataDirectory);
                log.info("Created data directory: {}", dataDirectory.toAbsolutePath());
            }
            if (!Files.exists(tasksFilePath)) {
                saveTasks(new HashMap<>());
                log.info("Initialized tasks file: {}", tasksFilePath.toAbsolutePath());
            }
        } catch (IOException e) {
            log.error("Failed to initialize data directory", e);
            throw new RuntimeException("Failed to initialize data directory", e);
        }
    }

    /**
     * Saves a task to the filesystem.
     *
     * @param task the task to save
     * @return the saved task
     */
    public Task save(Task task) {
        lock.writeLock().lock();
        try {
            Map<String, Task> tasks = loadTasks();
            tasks.put(task.id(), task);
            saveTasks(tasks);
            log.debug("Saved task: {}", task.id());
            return task;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Finds a task by ID.
     *
     * @param id the task ID
     * @return Optional containing the task if found, empty otherwise
     */
    public Optional<Task> findById(String id) {
        lock.readLock().lock();
        try {
            Map<String, Task> tasks = loadTasks();
            return Optional.ofNullable(tasks.get(id));
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all tasks.
     *
     * @return list of all tasks
     */
    public List<Task> findAll() {
        lock.readLock().lock();
        try {
            Map<String, Task> tasks = loadTasks();
            return new ArrayList<>(tasks.values());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Finds all tasks with a specific status.
     *
     * @param status the task status to filter by
     * @return list of tasks with the specified status
     */
    public List<Task> findByStatus(TaskStatus status) {
        lock.readLock().lock();
        try {
            Map<String, Task> tasks = loadTasks();
            return tasks.values().stream()
                    .filter(task -> task.status() == status)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Deletes a task by ID.
     *
     * @param id the task ID
     * @return true if task was deleted, false if not found
     */
    public boolean deleteById(String id) {
        lock.writeLock().lock();
        try {
            Map<String, Task> tasks = loadTasks();
            boolean removed = tasks.remove(id) != null;
            if (removed) {
                saveTasks(tasks);
                log.debug("Deleted task: {}", id);
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Checks if a task exists.
     *
     * @param id the task ID
     * @return true if task exists, false otherwise
     */
    public boolean existsById(String id) {
        lock.readLock().lock();
        try {
            Map<String, Task> tasks = loadTasks();
            return tasks.containsKey(id);
        } finally {
            lock.readLock().unlock();
        }
    }

    private Map<String, Task> loadTasks() {
        try {
            if (!Files.exists(tasksFilePath)) {
                return new HashMap<>();
            }
            return objectMapper.readValue(
                    tasksFilePath.toFile(),
                    new TypeReference<Map<String, Task>>() {}
            );
        } catch (IOException e) {
            log.error("Failed to load tasks from file", e);
            throw new RuntimeException("Failed to load tasks from file", e);
        }
    }

    private void saveTasks(Map<String, Task> tasks) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(tasksFilePath.toFile(), tasks);
        } catch (IOException e) {
            log.error("Failed to save tasks to file", e);
            throw new RuntimeException("Failed to save tasks to file", e);
        }
    }
}
