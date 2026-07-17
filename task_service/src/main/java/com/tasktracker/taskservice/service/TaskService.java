package com.tasktracker.taskservice.service;

import com.tasktracker.taskservice.dto.TaskRequest;
import com.tasktracker.taskservice.dto.TaskResponse;
import com.tasktracker.taskservice.entity.Task;
import com.tasktracker.taskservice.entity.TaskStatus;
import com.tasktracker.taskservice.exception.TaskNotFoundException;
import com.tasktracker.taskservice.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task(request.getTitle(), TaskStatus.TODO, request.getCreatorId(), null);
        Task savedTask = taskRepository.save(task);
        return toResponse(savedTask);
    }

    public List<TaskResponse> getTasksByUserId(Long userId) {
        return taskRepository.findByCreatorIdOrAssigneeId(userId, userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public Task getTaskEntityById(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getStatus(),
                task.getCreatorId(),
                task.getAssigneeId());
    }
}