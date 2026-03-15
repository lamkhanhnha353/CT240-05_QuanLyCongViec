package com.teamwork.db;

import java.sql.Timestamp;

public class Task {
    private int id;
    private String title;
    private String description; // Tương đương với Chú thích
    private String priority;
    private Timestamp deadline;
    private String status;      // Tương đương với Tiến độ
    private Integer assigneeId; // Dùng Integer để có thể lưu null nếu chưa giao việc
    private Integer projectId;
    private Timestamp createdAt;

    // --- BẠN NHỚ DÙNG VS CODE GENERATE LẠI GETTER/SETTER CHO CÁC BIẾN NÀY NHÉ ---
    // (Chuột phải -> Source Action -> Generate Getters and Setters)
    
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Timestamp getDeadline() { return deadline; }
    public void setDeadline(Timestamp deadline) { this.deadline = deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Integer assigneeId) { this.assigneeId = assigneeId; }
    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}