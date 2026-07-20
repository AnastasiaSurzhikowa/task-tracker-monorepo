package com.tasktracker.taskservice.service;

import com.tasktracker.taskservice.dto.TaskRequest;
import com.tasktracker.taskservice.dto.TaskResponse;
import com.tasktracker.taskservice.entity.Task;
import com.tasktracker.taskservice.entity.TaskStatus;
import com.tasktracker.taskservice.exception.TaskNotFoundException;
import com.tasktracker.taskservice.exception.InvalidTaskStatusException;
import com.tasktracker.taskservice.exception.UserNotFoundException;
import com.tasktracker.taskservice.repository.TaskRepository;
import org.springframework.stereotype.Service;
import com.tasktracker.taskservice.client.UserServiceClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserServiceClient userServiceClient;

    public TaskService(TaskRepository taskRepository, UserServiceClient userServiceClient) {
        this.taskRepository = taskRepository;
        this.userServiceClient = userServiceClient;
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

    public TaskResponse delegateTask(Long taskId, Long assigneeId) {
        // пока без обращения к UserService, проверяем существование задачи
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));
        // пока без обращения к UserService, проверяем валидность статуса задачи
        if (task.getStatus() == TaskStatus.DONE) {
            throw new InvalidTaskStatusException(
                    "Cannot delegate task with id " + taskId + " because it is already DONE");
        }
        // теперь проверяем существование пользователя, обращаясь к UserService
        boolean exists = userServiceClient.userExists(assigneeId);
        if (!exists) {
            throw new UserNotFoundException(assigneeId);
        }
        // успех, обновляем, сохраняем
        task.setAssigneeId(assigneeId);
        Task updatedTask = taskRepository.save(task);
        return toResponse(updatedTask);
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