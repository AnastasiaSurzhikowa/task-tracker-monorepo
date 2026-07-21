package com.tasktracker.taskservice.service;

import com.tasktracker.taskservice.client.UserServiceClient;
import com.tasktracker.taskservice.dto.TaskResponse;
import com.tasktracker.taskservice.entity.Task;
import com.tasktracker.taskservice.entity.TaskStatus;
import com.tasktracker.taskservice.exception.InvalidTaskStatusException;
import com.tasktracker.taskservice.exception.TaskNotFoundException;
import com.tasktracker.taskservice.exception.UserNotFoundException;
import com.tasktracker.taskservice.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldDelegateTaskWhenUserExistsAndTaskIsTodo() {
        Task task = new Task("Write report", TaskStatus.TODO, 1L, null);
        task.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userServiceClient.userExists(2L)).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.delegateTask(1L, 2L);

        assertThat(response.getAssigneeId()).isEqualTo(2L);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldThrowTaskNotFoundAndSkipUserServiceCall() {
        when(taskRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delegateTask(9999L, 1L))
                .isInstanceOf(TaskNotFoundException.class);

        // Ключевая проверка по sequence diagram:
        // при отсутствии задачи обращения к user_service быть не должно
        verify(userServiceClient, never()).userExists(any());
    }

    @Test
    void shouldThrowInvalidStatusAndSkipUserServiceCallWhenTaskIsDone() {
        Task doneTask = new Task("Old task", TaskStatus.DONE, 1L, null);
        doneTask.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(doneTask));

        assertThatThrownBy(() -> taskService.delegateTask(1L, 2L))
                .isInstanceOf(InvalidTaskStatusException.class);

        // Согласно диаграмме - при статусе DONE обращения к user_service тоже быть не
        // должно
        verify(userServiceClient, never()).userExists(any());
    }

    @Test
    void shouldReturn404WhenAssigneeNotFound() {
        Task task = new Task("Write report", TaskStatus.TODO, 1L, null);
        task.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userServiceClient.userExists(999L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delegateTask(1L, 999L))
                .isInstanceOf(UserNotFoundException.class);

        verify(taskRepository, never()).save(any(Task.class));
    }
}