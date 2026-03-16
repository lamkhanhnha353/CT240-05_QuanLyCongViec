package com.teamwork.db;

import java.sql.Date; 
import java.sql.Timestamp;

public class Task {
    private int id;
    private String title;
    private String description; 
    private String priority;
    private Date deadline;      // Chuyển về java.sql.Date để khớp với DB
    private String status;      
    private Integer assigneeId; 
    private Integer projectId;  
    private Timestamp createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Integer assigneeId) { this.assigneeId = assigneeId; }
    public Integer getProjectId() { return projectId; }
    public void setProjectId(Integer projectId) { this.projectId = projectId; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}