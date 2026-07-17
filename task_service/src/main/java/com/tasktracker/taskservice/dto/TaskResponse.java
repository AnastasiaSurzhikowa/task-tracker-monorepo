package com.tasktracker.taskservice.dto;

import com.tasktracker.taskservice.entity.TaskStatus;

public class TaskResponse {

    private Long id;
    private String title;
    private TaskStatus status;
    private Long creatorId;
    private Long assigneeId;

    public TaskResponse() {
    }

    public TaskResponse(Long id, String title, TaskStatus status, Long creatorId, Long assigneeId) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.creatorId = creatorId;
        this.assigneeId = assigneeId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }
}