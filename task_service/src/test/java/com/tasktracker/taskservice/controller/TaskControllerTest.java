package com.tasktracker.taskservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasktracker.taskservice.client.UserServiceClient;
import com.tasktracker.taskservice.dto.TaskRequest;
import com.tasktracker.taskservice.dto.TaskResponse;
import com.tasktracker.taskservice.entity.TaskStatus;
import com.tasktracker.taskservice.exception.InvalidTaskStatusException;
import com.tasktracker.taskservice.exception.TaskNotFoundException;
import com.tasktracker.taskservice.exception.UserNotFoundException;
import com.tasktracker.taskservice.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    // UserServiceClient не используется напрямую контроллером, но т.к. он входит в
    // контекст
    // приложения через TaskService -> UserServiceClient, при @WebMvcTest его можно
    // не мокать,
    // так как TaskService целиком замокан. Оставляем как явную заготовку на случай,
    // если в будущем контроллер начнёт обращаться к нему напрямую.
    @MockitoBean
    private UserServiceClient userServiceClient;

    @Test
    void shouldCreateTaskAndReturn201() throws Exception {
        TaskRequest request = new TaskRequest("Write report", 1L);
        TaskResponse response = new TaskResponse(1L, "Write report", TaskStatus.TODO, 1L, null);

        when(taskService.createTask(any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.assigneeId").doesNotExist());
    }

    @Test
    void shouldReturn400WhenCreateTaskWithBlankTitle() throws Exception {
        TaskRequest invalidRequest = new TaskRequest("", 1L);

        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnTasksForUser() throws Exception {
        TaskResponse task = new TaskResponse(1L, "Write report", TaskStatus.TODO, 1L, null);
        when(taskService.getTasksByUserId(eq(1L))).thenReturn(List.of(task));

        mockMvc.perform(get("/tasks/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Write report"));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoTasks() throws Exception {
        when(taskService.getTasksByUserId(eq(999L))).thenReturn(List.of());

        mockMvc.perform(get("/tasks/user/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void shouldDelegateTaskSuccessfully() throws Exception {
        TaskResponse response = new TaskResponse(1L, "Write report", TaskStatus.TODO, 1L, 2L);
        when(taskService.delegateTask(eq(1L), eq(2L))).thenReturn(response);

        mockMvc.perform(post("/tasks/1/delegate").param("assigneeId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assigneeId").value(2L));
    }

    @Test
    void shouldReturn404WhenTaskNotFoundOnDelegate() throws Exception {
        when(taskService.delegateTask(eq(9999L), eq(1L)))
                .thenThrow(new TaskNotFoundException(9999L));

        mockMvc.perform(post("/tasks/9999/delegate").param("assigneeId", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task not found with id: 9999"));
    }

    @Test
    void shouldReturn404WhenAssigneeNotFound() throws Exception {
        when(taskService.delegateTask(eq(1L), eq(999L)))
                .thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(post("/tasks/1/delegate").param("assigneeId", "999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
    }

    @Test
    void shouldReturn400WhenTaskAlreadyDone() throws Exception {
        when(taskService.delegateTask(eq(1L), eq(2L)))
                .thenThrow(new InvalidTaskStatusException(
                        "Cannot delegate task with id 1 because it is already DONE"));

        mockMvc.perform(post("/tasks/1/delegate").param("assigneeId", "2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}