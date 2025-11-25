package com.epam.masterclass.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.epam.masterclass.model.Task;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TaskService {
    private final Path tasksDirectory;
    private final ObjectMapper objectMapper;

    public TaskService(@Value("${app.data.directory}") String dataDirectory, ObjectMapper objectMapper) {
        this.tasksDirectory = Paths.get(dataDirectory, "tasks");
        this.objectMapper = objectMapper;
        initializeDirectory();
    }

    private void initializeDirectory() {
        try {
            Files.createDirectories(tasksDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create tasks directory", e);
        }
    }

    public List<Task> getAllTasks() {
        try (Stream<Path> paths = Files.list(tasksDirectory)) {
            return paths
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(this::readTask)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read tasks", e);
        }
    }

    public Optional<Task> getTaskById(String id) {
        Path taskPath = tasksDirectory.resolve(id + ".json");
        return readTask(taskPath);
    }

    public Task createTask(Task task) {
        task.setId(UUID.randomUUID().toString());
        saveTask(task);
        return task;
    }

    public Optional<Task> updateTask(String id, Task updatedTask) {
        Path taskPath = tasksDirectory.resolve(id + ".json");
        if (!Files.exists(taskPath)) {
            return Optional.empty();
        }
        updatedTask.setId(id);
        saveTask(updatedTask);
        return Optional.of(updatedTask);
    }

    public boolean deleteTask(String id) {
        Path taskPath = tasksDirectory.resolve(id + ".json");
        try {
            return Files.deleteIfExists(taskPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete task", e);
        }
    }

    private void saveTask(Task task) {
        Path taskPath = tasksDirectory.resolve(task.getId() + ".json");
        try {
            objectMapper.writeValue(taskPath.toFile(), task);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save task", e);
        }
    }

    private Optional<Task> readTask(Path path) {
        try {
            return Optional.of(objectMapper.readValue(path.toFile(), Task.class));
        } catch (IOException e) {
            return Optional.empty();
        }
    }
}
