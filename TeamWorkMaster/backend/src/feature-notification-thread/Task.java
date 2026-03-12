import java.time.LocalDateTime;

public class Task {

    private String name;
    private String status;
    private LocalDateTime deadline;

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
}