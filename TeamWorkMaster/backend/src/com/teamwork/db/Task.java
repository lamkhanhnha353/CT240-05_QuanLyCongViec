package com.teamwork.db;

import java.sql.Timestamp;

public class Task {
    private int id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Timestamp deadline;
    private Integer assigneeId; // Dùng Integer thay vì int để có thể nhận giá trị null
    private Timestamp createdAt;

    // Constructor mặc định
    public Task() {
    }

    // Constructor đầy đủ
    public Task(int id, String title, String description, String status, String priority, Timestamp deadline, Integer assigneeId, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.deadline = deadline;
        this.assigneeId = assigneeId;
        this.createdAt = createdAt;
    }

    // --- GETTERS & SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Timestamp getDeadline() { return deadline; }
    public void setDeadline(Timestamp deadline) { this.deadline = deadline; }

    public Integer getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Integer assigneeId) { this.assigneeId = assigneeId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}