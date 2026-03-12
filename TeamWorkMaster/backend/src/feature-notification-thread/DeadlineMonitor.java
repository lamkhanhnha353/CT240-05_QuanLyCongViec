import java.time.LocalDateTime;
import java.util.List;

public class DeadlineMonitor implements Runnable {

    private List<Task> tasks;

    public DeadlineMonitor(List<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void run() {

        while (true) {

            for (Task task : tasks) {

                LocalDateTime now = LocalDateTime.now();

                if (task.getDeadline().minusMinutes(1).isBefore(now)) {
                    NotificationService.notifyDeadline(task);
                }
            }

            try {
                Thread.sleep(5000); // kiểm tra mỗi 5 giây
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}