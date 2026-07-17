package com.tasktracker.taskservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TaskRequest {

    @NotBlank(message = "Title must not be blank")
    private String title;

    @NotNull(message = "Creator id must not be null")
    private Long creatorId;

    public TaskRequest() {
    }

    public TaskRequest(String title, Long creatorId) {
        this.title = title;
        this.creatorId = creatorId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }
}