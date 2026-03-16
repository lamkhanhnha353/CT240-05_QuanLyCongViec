package com.teamwork.core;

import java.time.LocalDateTime;

public class Task {

    private String name;
    private String status;
    private String userEmail;
    private LocalDateTime deadline;

    private boolean notified3Days = false;
    private boolean notifiedToday = false;
    private boolean notifiedOverdue = false;

    public Task(String name, String status, String userEmail, LocalDateTime deadline) {
        this.name = name;
        this.status = status;
        this.userEmail = userEmail;
        this.deadline = deadline;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setStatus(String status) {

        if (!this.status.equals(status)) {
            this.status = status;
            NotificationService.notifyStatusChange(this);
        }
    }

    public boolean isNotified3Days() {
        return notified3Days;
    }

    public void setNotified3Days(boolean notified3Days) {
        this.notified3Days = notified3Days;
    }

    public boolean isNotifiedToday() {
        return notifiedToday;
    }

    public void setNotifiedToday(boolean notifiedToday) {
        this.notifiedToday = notifiedToday;
    }

    public boolean isNotifiedOverdue() {
        return notifiedOverdue;
    }

    public void setNotifiedOverdue(boolean notifiedOverdue) {
        this.notifiedOverdue = notifiedOverdue;
    }
}