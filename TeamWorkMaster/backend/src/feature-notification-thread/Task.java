import java.time.LocalDateTime;

public class Task {

    private String name;
    private String status;
    private LocalDateTime deadline;

    // tránh gửi trùng thông báo
    private boolean notified3Days = false;
    private boolean notifiedToday = false;
    private boolean notifiedOverdue = false;

    public Task(String name, String status, LocalDateTime deadline) {
        this.name = name;
        this.status = status;
        this.deadline = deadline;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getDeadline() {
        return deadline;
    }

    public void setStatus(String status) {
        this.status = status;
        NotificationService.notifyStatusChange(this);
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