package com.epam.masterclass.mcp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * End-to-End tests for the MCP Server.
 * Tests the MCP tools by invoking them through the Spring AI MCP server endpoint.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("mcp")
@DisplayName("MCP Server E2E Tests")
class McpServerE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private WebTestClient webTestClient;
    private Path testDataDirectory;

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary test data directory
        testDataDirectory = Files.createTempDirectory("mcp-test-data");
        System.setProperty("DATA_DIRECTORY", testDataDirectory.toString());

        // Initialize WebTestClient with increased timeout for MCP operations
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .responseTimeout(Duration.ofSeconds(30))
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Clean up test data directory
        if (testDataDirectory != null && Files.exists(testDataDirectory)) {
            Files.walk(testDataDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        System.clearProperty("DATA_DIRECTORY");
    }

    @Test
    @DisplayName("Should list MCP tools via SSE endpoint")
    void shouldListMcpTools() {
        // MCP protocol: list available tools
        Map<String, Object> listToolsRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 1,
                "method", "tools/list"
        );

        webTestClient.post()
                .uri("/sse")
                .bodyValue(listToolsRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result.tools").isArray()
                .jsonPath("$.result.tools[?(@.name=='readTasks')]").exists()
                .jsonPath("$.result.tools[?(@.name=='writeTasks')]").exists();
    }

    @Test
    @DisplayName("Scenario: Create, Read, Update Status, and Delete Task")
    void shouldHandleCompleteTaskLifecycle() throws Exception {
        String taskId;

        // Step 1: Create a new task using write_tasks
        Map<String, Object> createRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 1,
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "create",
                                "title", "Test E2E Task",
                                "description", "Testing MCP server end-to-end"
                        )
                )
        );

        String createResponse = webTestClient.post()
                .uri("/sse")
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(createResponse).contains("Test E2E Task");
        assertThat(createResponse).contains("NEW");

        // Extract task ID from response
        Map<String, Object> createResult = objectMapper.readValue(createResponse, Map.class);
        Map<String, Object> resultContent = (Map<String, Object>) ((List<?>) createResult.get("result")).get(0);
        Map<String, Object> taskData = (Map<String, Object>) resultContent.get("content");
        taskId = (String) taskData.get("id");
        assertThat(taskId).isNotNull();

        // Step 2: Read the task by ID using read_tasks
        Map<String, Object> readRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 2,
                "method", "tools/call",
                "params", Map.of(
                        "name", "readTasks",
                        "arguments", Map.of("id", taskId)
                )
        );

        webTestClient.post()
                .uri("/sse")
                .bodyValue(readRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result[0].content.id").isEqualTo(taskId)
                .jsonPath("$.result[0].content.title").isEqualTo("Test E2E Task")
                .jsonPath("$.result[0].content.status").isEqualTo("NEW");

        // Step 3: Update task status to IN_PROGRESS using write_tasks
        Map<String, Object> updateRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 3,
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "update_status",
                                "id", taskId,
                                "status", "IN_PROGRESS"
                        )
                )
        );

        webTestClient.post()
                .uri("/sse")
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result[0].content.id").isEqualTo(taskId)
                .jsonPath("$.result[0].content.status").isEqualTo("IN_PROGRESS");

        // Step 4: Update task status to DONE using write_tasks
        Map<String, Object> updateToDoneRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 4,
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "update_status",
                                "id", taskId,
                                "status", "DONE"
                        )
                )
        );

        webTestClient.post()
                .uri("/sse")
                .bodyValue(updateToDoneRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.result[0].content.status").isEqualTo("DONE");

        // Step 5: Delete the task using write_tasks
        Map<String, Object> deleteRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 5,
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "delete",
                                "id", taskId
                        )
                )
        );

        String deleteResponse = webTestClient.post()
                .uri("/sse")
                .bodyValue(deleteRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(deleteResponse).contains("Task deleted successfully");
        assertThat(deleteResponse).contains(taskId);
    }

    @Test
    @DisplayName("Scenario: List all tasks and filter by status")
    void shouldListAndFilterTasks() throws Exception {
        // Create multiple tasks with different statuses
        createTask("Task 1", "Description 1");
        String task2Id = createTask("Task 2", "Description 2");
        String task3Id = createTask("Task 3", "Description 3");

        // Update task 2 to IN_PROGRESS
        updateTaskStatus(task2Id, "IN_PROGRESS");

        // Update task 3 to IN_PROGRESS then DONE
        updateTaskStatus(task3Id, "IN_PROGRESS");
        updateTaskStatus(task3Id, "DONE");

        // List all tasks
        Map<String, Object> listAllRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 10,
                "method", "tools/call",
                "params", Map.of(
                        "name", "readTasks",
                        "arguments", Map.of()
                )
        );

        String allTasksResponse = webTestClient.post()
                .uri("/sse")
                .bodyValue(listAllRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(allTasksResponse).contains("Task 1");
        assertThat(allTasksResponse).contains("Task 2");
        assertThat(allTasksResponse).contains("Task 3");

        // Filter by NEW status
        Map<String, Object> filterNewRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 11,
                "method", "tools/call",
                "params", Map.of(
                        "name", "readTasks",
                        "arguments", Map.of("status", "NEW")
                )
        );

        String newTasksResponse = webTestClient.post()
                .uri("/sse")
                .bodyValue(filterNewRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(newTasksResponse).contains("Task 1");
        assertThat(newTasksResponse).doesNotContain("Task 2");
        assertThat(newTasksResponse).doesNotContain("Task 3");

        // Filter by IN_PROGRESS status
        Map<String, Object> filterInProgressRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 12,
                "method", "tools/call",
                "params", Map.of(
                        "name", "readTasks",
                        "arguments", Map.of("status", "IN_PROGRESS")
                )
        );

        String inProgressResponse = webTestClient.post()
                .uri("/sse")
                .bodyValue(filterInProgressRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(inProgressResponse).contains("Task 2");
        assertThat(inProgressResponse).doesNotContain("Task 1");
        assertThat(inProgressResponse).doesNotContain("Task 3");

        // Filter by DONE status
        Map<String, Object> filterDoneRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 13,
                "method", "tools/call",
                "params", Map.of(
                        "name", "readTasks",
                        "arguments", Map.of("status", "DONE")
                )
        );

        String doneResponse = webTestClient.post()
                .uri("/sse")
                .bodyValue(filterDoneRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(doneResponse).contains("Task 3");
        assertThat(doneResponse).doesNotContain("Task 1");
        assertThat(doneResponse).doesNotContain("Task 2");
    }

    @Test
    @DisplayName("Should enforce status transition rules")
    void shouldEnforceStatusTransitionRules() throws Exception {
        // Create a task (starts with NEW status)
        String taskId = createTask("Transition Test Task", "Testing status transitions");

        // Try to transition NEW -> DONE (should fail)
        Map<String, Object> invalidTransitionRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 20,
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "update_status",
                                "id", taskId,
                                "status", "DONE"
                        )
                )
        );

        String errorResponse = webTestClient.post()
                .uri("/sse")
                .bodyValue(invalidTransitionRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).contains("error");
        assertThat(errorResponse).containsAnyOf("transition", "not allowed", "invalid");

        // Valid transition: NEW -> IN_PROGRESS
        updateTaskStatus(taskId, "IN_PROGRESS");

        // Valid transition: IN_PROGRESS -> DONE
        updateTaskStatus(taskId, "DONE");

        // Try to transition from DONE (terminal state - should fail)
        Map<String, Object> fromDoneRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 21,
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "update_status",
                                "id", taskId,
                                "status", "NEW"
                        )
                )
        );

        String terminalErrorResponse = webTestClient.post()
                .uri("/sse")
                .bodyValue(fromDoneRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(terminalErrorResponse).contains("error");
    }

    @Test
    @DisplayName("Should handle invalid operations gracefully")
    void shouldHandleInvalidOperations() {
        // Try to create task without required title
        Map<String, Object> noTitleRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 30,
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "create",
                                "description", "Missing title"
                        )
                )
        );

        String errorResponse = webTestClient.post()
                .uri("/sse")
                .bodyValue(noTitleRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(errorResponse).contains("error");
        assertThat(errorResponse).containsAnyOf("title", "required");

        // Try to update status without ID
        Map<String, Object> noIdRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 31,
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "update_status",
                                "status", "IN_PROGRESS"
                        )
                )
        );

        String noIdError = webTestClient.post()
                .uri("/sse")
                .bodyValue(noIdRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(noIdError).contains("error");
        assertThat(noIdError).containsAnyOf("id", "ID", "required");

        // Try invalid operation type
        Map<String, Object> invalidOpRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 32,
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "invalid_operation"
                        )
                )
        );

        String invalidOpError = webTestClient.post()
                .uri("/sse")
                .bodyValue(invalidOpRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(invalidOpError).contains("error");
        assertThat(invalidOpError).containsAnyOf("Invalid operation", "operation");
    }

    @Test
    @DisplayName("Should handle non-existent task gracefully")
    void shouldHandleNonExistentTask() {
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        // Try to read non-existent task
        Map<String, Object> readRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 40,
                "method", "tools/call",
                "params", Map.of(
                        "name", "readTasks",
                        "arguments", Map.of("id", nonExistentId)
                )
        );

        String readError = webTestClient.post()
                .uri("/sse")
                .bodyValue(readRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(readError).contains("error");
        assertThat(readError).containsAnyOf("not found", "Not found", "does not exist");

        // Try to update non-existent task
        Map<String, Object> updateRequest = Map.of(
                "jsonrpc", "2.0",
                "id", 41,
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "update_status",
                                "id", nonExistentId,
                                "status", "IN_PROGRESS"
                        )
                )
        );

        String updateError = webTestClient.post()
                .uri("/sse")
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        assertThat(updateError).contains("error");
    }

    // Helper methods

    private String createTask(String title, String description) throws Exception {
        Map<String, Object> createRequest = Map.of(
                "jsonrpc", "2.0",
                "id", System.currentTimeMillis(),
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "create",
                                "title", title,
                                "description", description
                        )
                )
        );

        String response = webTestClient.post()
                .uri("/sse")
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        Map<String, Object> result = objectMapper.readValue(response, Map.class);
        Map<String, Object> resultContent = (Map<String, Object>) ((List<?>) result.get("result")).get(0);
        Map<String, Object> taskData = (Map<String, Object>) resultContent.get("content");
        return (String) taskData.get("id");
    }

    private void updateTaskStatus(String taskId, String status) {
        Map<String, Object> updateRequest = Map.of(
                "jsonrpc", "2.0",
                "id", System.currentTimeMillis(),
                "method", "tools/call",
                "params", Map.of(
                        "name", "writeTasks",
                        "arguments", Map.of(
                                "operation", "update_status",
                                "id", taskId,
                                "status", status
                        )
                )
        );

        webTestClient.post()
                .uri("/sse")
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk();
    }
}
