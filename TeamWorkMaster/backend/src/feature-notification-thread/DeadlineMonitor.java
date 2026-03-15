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

            LocalDateTime now = LocalDateTime.now();

            for (Task task : tasks) {

                LocalDateTime deadline = task.getDeadline();

                // Nhắc trước 3 ngày
                if (!task.isNotified3Days() &&
                        deadline.minusDays(3).isBefore(now) &&
                        deadline.isAfter(now)) {

                    NotificationService.notifyDeadlineSoon(task);
                    task.setNotified3Days(true);
                }

                // Đúng ngày deadline
                if (!task.isNotifiedToday() &&
                        now.toLocalDate().isEqual(deadline.toLocalDate())) {

                    NotificationService.notifyDeadlineToday(task);
                    task.setNotifiedToday(true);
                }

                // Quá hạn 1 ngày
                if (!task.isNotifiedOverdue() &&
                        now.isAfter(deadline.plusDays(1))) {

                    NotificationService.notifyOverdue(task);
                    task.setNotifiedOverdue(true);
                }
            }

            try {
                Thread.sleep(10000); // kiểm tra mỗi 10 giây
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}