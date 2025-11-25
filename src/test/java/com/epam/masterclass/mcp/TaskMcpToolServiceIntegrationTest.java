package com.epam.masterclass.mcp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.epam.masterclass.exception.InvalidStatusTransitionException;
import com.epam.masterclass.exception.TaskNotFoundException;
import com.epam.masterclass.model.Task;

/**
 * Integration tests for MCP Tool Service.
 * Tests the MCP tools directly through the service layer.
 */
@SpringBootTest
@ActiveProfiles("mcp")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("MCP Tool Service Integration Tests")
class TaskMcpToolServiceIntegrationTest {

    @Autowired
    private TaskMcpToolService taskMcpToolService;

    private static Path testDataDirectory;

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) throws IOException {
        // Create a temporary test data directory before Spring context loads
        testDataDirectory = Files.createTempDirectory("mcp-integration-test");
        registry.add("DATA_DIRECTORY", () -> testDataDirectory.toString());
    }

    @AfterAll
    static void tearDown() throws IOException {
        // Clean up test data directory
        if (testDataDirectory != null && Files.exists(testDataDirectory)) {
            Files.walk(testDataDirectory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    @Test
    @DisplayName("read_tasks: Should list all tasks when no parameters provided")
    void shouldListAllTasks() {
        // Create some tasks
        taskMcpToolService.writeTasks("create", null, "Task 1", "Description 1", null);
        taskMcpToolService.writeTasks("create", null, "Task 2", "Description 2", null);
        taskMcpToolService.writeTasks("create", null, "Task 3", "Description 3", null);

        // List all tasks
        Object result = taskMcpToolService.readTasks(null, null);

        assertThat(result).isInstanceOf(List.class);
        List<?> tasks = (List<?>) result;
        assertThat(tasks).hasSize(3);
    }

    @Test
    @DisplayName("read_tasks: Should return specific task by ID")
    void shouldReturnTaskById() {
        // Create a task
        Task createdTask = (Task) taskMcpToolService.writeTasks(
                "create", null, "Specific Task", "Find me", null
        );
        String taskId = createdTask.id();

        // Read the task by ID
        Object result = taskMcpToolService.readTasks(taskId, null);

        assertThat(result).isInstanceOf(Task.class);
        Task task = (Task) result;
        assertThat(task.id()).isEqualTo(taskId);
        assertThat(task.title()).isEqualTo("Specific Task");
        assertThat(task.description()).isEqualTo("Find me");
    }

    @Test
    @DisplayName("read_tasks: Should filter tasks by status")
    void shouldFilterTasksByStatus() {
        // Create tasks with different statuses
        Task task1 = (Task) taskMcpToolService.writeTasks("create", null, "Task 1", "Desc 1", null);
        Task task2 = (Task) taskMcpToolService.writeTasks("create", null, "Task 2", "Desc 2", null);
        Task task3 = (Task) taskMcpToolService.writeTasks("create", null, "Task 3", "Desc 3", null);

        // Update some to IN_PROGRESS
        taskMcpToolService.writeTasks("update_status", task2.id(), null, null, "IN_PROGRESS");

        // Filter by NEW status
        List<?> newTasks = (List<?>) taskMcpToolService.readTasks(null, "NEW");
        assertThat(newTasks).hasSize(2);

        // Filter by IN_PROGRESS status
        List<?> inProgressTasks = (List<?>) taskMcpToolService.readTasks(null, "IN_PROGRESS");
        assertThat(inProgressTasks).hasSize(1);

        // Filter by DONE status
        List<?> doneTasks = (List<?>) taskMcpToolService.readTasks(null, "DONE");
        assertThat(doneTasks).isEmpty();
    }

    @Test
    @DisplayName("read_tasks: Should throw exception for invalid status")
    void shouldThrowExceptionForInvalidStatus() {
        assertThatThrownBy(() -> taskMcpToolService.readTasks(null, "INVALID_STATUS"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status value");
    }

    @Test
    @DisplayName("read_tasks: Should throw exception for non-existent task ID")
    void shouldThrowExceptionForNonExistentTaskId() {
        assertThatThrownBy(() -> taskMcpToolService.readTasks("non-existent-id", null))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("write_tasks: Should create task with create operation")
    void shouldCreateTask() {
        // Create a task
        Object result = taskMcpToolService.writeTasks(
                "create", null, "New Task", "Task description", null
        );

        assertThat(result).isInstanceOf(Task.class);
        Task task = (Task) result;
        assertThat(task.id()).isNotNull();
        assertThat(task.title()).isEqualTo("New Task");
        assertThat(task.description()).isEqualTo("Task description");
        assertThat(task.status().name()).isEqualTo("NEW");
        assertThat(task.createdAt()).isNotNull();
        assertThat(task.updatedAt()).isNotNull();
    }

    @Test
    @DisplayName("write_tasks: Should create task without description")
    void shouldCreateTaskWithoutDescription() {
        Object result = taskMcpToolService.writeTasks(
                "create", null, "Task without description", null, null
        );

        assertThat(result).isInstanceOf(Task.class);
        Task task = (Task) result;
        assertThat(task.title()).isEqualTo("Task without description");
        assertThat(task.description()).isNull();
    }

    @Test
    @DisplayName("write_tasks: Should throw exception when creating without title")
    void shouldThrowExceptionWhenCreatingWithoutTitle() {
        assertThatThrownBy(() -> taskMcpToolService.writeTasks("create", null, null, "Desc", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Title is required");
    }

    @Test
    @DisplayName("write_tasks: Should update task status")
    void shouldUpdateTaskStatus() {
        // Create a task
        Task task = (Task) taskMcpToolService.writeTasks(
                "create", null, "Task to update", "Description", null
        );
        assertThat(task.status().name()).isEqualTo("NEW");

        // Update to IN_PROGRESS
        Object result = taskMcpToolService.writeTasks(
                "update_status", task.id(), null, null, "IN_PROGRESS"
        );

        assertThat(result).isInstanceOf(Task.class);
        Task updatedTask = (Task) result;
        assertThat(updatedTask.id()).isEqualTo(task.id());
        assertThat(updatedTask.status().name()).isEqualTo("IN_PROGRESS");
        assertThat(updatedTask.updatedAt()).isAfter(task.updatedAt());
    }

    @Test
    @DisplayName("write_tasks: Should enforce status transition rules")
    void shouldEnforceStatusTransitionRules() {
        // Create a task (NEW status)
        Task task = (Task) taskMcpToolService.writeTasks(
                "create", null, "Transition Test", "Testing transitions", null
        );

        // Try invalid transition: NEW -> DONE (should fail)
        assertThatThrownBy(() ->
                taskMcpToolService.writeTasks("update_status", task.id(), null, null, "DONE")
        ).isInstanceOf(InvalidStatusTransitionException.class);

        // Valid transition: NEW -> IN_PROGRESS
        Task inProgress = (Task) taskMcpToolService.writeTasks(
                "update_status", task.id(), null, null, "IN_PROGRESS"
        );
        assertThat(inProgress.status().name()).isEqualTo("IN_PROGRESS");

        // Valid transition: IN_PROGRESS -> DONE
        Task done = (Task) taskMcpToolService.writeTasks(
                "update_status", task.id(), null, null, "DONE"
        );
        assertThat(done.status().name()).isEqualTo("DONE");

        // Try transition from terminal state: DONE -> NEW (should fail)
        assertThatThrownBy(() ->
                taskMcpToolService.writeTasks("update_status", task.id(), null, null, "NEW")
        ).isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    @DisplayName("write_tasks: Should allow rollback from IN_PROGRESS to NEW")
    void shouldAllowRollbackToNew() {
        // Create and move to IN_PROGRESS
        Task task = (Task) taskMcpToolService.writeTasks(
                "create", null, "Rollback Task", "Testing rollback", null
        );
        taskMcpToolService.writeTasks("update_status", task.id(), null, null, "IN_PROGRESS");

        // Rollback to NEW
        Task rolledBack = (Task) taskMcpToolService.writeTasks(
                "update_status", task.id(), null, null, "NEW"
        );

        assertThat(rolledBack.status().name()).isEqualTo("NEW");
    }

    @Test
    @DisplayName("write_tasks: Should delete task")
    void shouldDeleteTask() {
        // Create a task
        Task task = (Task) taskMcpToolService.writeTasks(
                "create", null, "Task to delete", "Will be deleted", null
        );
        String taskId = task.id();

        // Delete the task
        Object result = taskMcpToolService.writeTasks("delete", taskId, null, null, null);

        assertThat(result).isInstanceOf(String.class);
        String message = (String) result;
        assertThat(message).contains("Task deleted successfully");
        assertThat(message).contains(taskId);

        // Verify task is deleted
        assertThatThrownBy(() -> taskMcpToolService.readTasks(taskId, null))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("write_tasks: Should throw exception when deleting non-existent task")
    void shouldThrowExceptionWhenDeletingNonExistentTask() {
        assertThatThrownBy(() ->
                taskMcpToolService.writeTasks("delete", "non-existent-id", null, null, null)
        ).isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    @DisplayName("write_tasks: Should throw exception for invalid operation")
    void shouldThrowExceptionForInvalidOperation() {
        assertThatThrownBy(() ->
                taskMcpToolService.writeTasks("invalid_op", null, null, null, null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid operation");
    }

    @Test
    @DisplayName("write_tasks: Should throw exception when operation is null")
    void shouldThrowExceptionWhenOperationIsNull() {
        assertThatThrownBy(() ->
                taskMcpToolService.writeTasks(null, null, "Title", null, null)
        ).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Operation is required");
    }

    @Test
    @DisplayName("Complete workflow: Create, list, update multiple times, and delete")
    void shouldHandleCompleteWorkflow() {
        // Create multiple tasks
        Task task1 = (Task) taskMcpToolService.writeTasks("create", null, "Workflow 1", "Desc 1", null);
        Task task2 = (Task) taskMcpToolService.writeTasks("create", null, "Workflow 2", "Desc 2", null);
        Task task3 = (Task) taskMcpToolService.writeTasks("create", null, "Workflow 3", "Desc 3", null);

        // List all tasks
        List<?> allTasks = (List<?>) taskMcpToolService.readTasks(null, null);
        assertThat(allTasks).hasSize(3);

        // Update tasks through different status transitions
        taskMcpToolService.writeTasks("update_status", task1.id(), null, null, "IN_PROGRESS");
        taskMcpToolService.writeTasks("update_status", task2.id(), null, null, "IN_PROGRESS");
        taskMcpToolService.writeTasks("update_status", task2.id(), null, null, "DONE");

        // Verify status filters
        List<?> newTasks = (List<?>) taskMcpToolService.readTasks(null, "NEW");
        assertThat(newTasks).hasSize(1);

        List<?> inProgressTasks = (List<?>) taskMcpToolService.readTasks(null, "IN_PROGRESS");
        assertThat(inProgressTasks).hasSize(1);

        List<?> doneTasks = (List<?>) taskMcpToolService.readTasks(null, "DONE");
        assertThat(doneTasks).hasSize(1);

        // Read specific task
        Task specificTask = (Task) taskMcpToolService.readTasks(task1.id(), null);
        assertThat(specificTask.status().name()).isEqualTo("IN_PROGRESS");

        // Delete one task
        taskMcpToolService.writeTasks("delete", task3.id(), null, null, null);

        // Verify only 2 tasks remain
        List<?> remainingTasks = (List<?>) taskMcpToolService.readTasks(null, null);
        assertThat(remainingTasks).hasSize(2);
    }
}
