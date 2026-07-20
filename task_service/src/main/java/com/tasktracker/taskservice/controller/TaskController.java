package com.tasktracker.taskservice.controller;

import com.tasktracker.taskservice.dto.TaskRequest;
import com.tasktracker.taskservice.dto.TaskResponse;
import com.tasktracker.taskservice.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse response = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TaskResponse>> getTasksByUser(@PathVariable Long userId) {
        List<TaskResponse> tasks = taskService.getTasksByUserId(userId);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/{taskId}/delegate")
    public ResponseEntity<TaskResponse> delegate(
            @PathVariable Long taskId,
            @RequestParam Long assigneeId) {
        TaskResponse response = taskService.delegateTask(taskId, assigneeId);
        return ResponseEntity.ok(response);
    }
}