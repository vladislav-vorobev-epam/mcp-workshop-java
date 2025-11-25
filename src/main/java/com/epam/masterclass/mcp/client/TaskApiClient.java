package com.epam.masterclass.mcp.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.epam.masterclass.model.Task;

@Component
@Profile("mcp")
public class TaskApiClient {
    private final RestTemplate restTemplate;
    private final String taskApiUrl;

    public TaskApiClient(RestTemplate restTemplate, @Value("${task.api.url}") String taskApiUrl) {
        this.restTemplate = restTemplate;
        this.taskApiUrl = taskApiUrl;
    }

    public List<Task> getAllTasks() {
        ResponseEntity<List<Task>> response = restTemplate.exchange(
                taskApiUrl + "/api/tasks",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Task>>() {}
        );
        return response.getBody();
    }

    public Task getTaskById(String id) {
        return restTemplate.getForObject(taskApiUrl + "/api/tasks/" + id, Task.class);
    }

    public Task createTask(Task task) {
        return restTemplate.postForObject(taskApiUrl + "/api/tasks", task, Task.class);
    }

    public Task updateTask(String id, Task task) {
        restTemplate.put(taskApiUrl + "/api/tasks/" + id, task);
        return getTaskById(id);
    }

    public void deleteTask(String id) {
        restTemplate.delete(taskApiUrl + "/api/tasks/" + id);
    }
}
