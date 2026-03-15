import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        List<Task> tasks = new ArrayList<>();

        Task task1 = new Task(
                "Làm báo cáo",
                "Đang làm",
                LocalDateTime.now().plusSeconds(15));

        tasks.add(task1);

        Thread monitor = new Thread(new DeadlineMonitor(tasks));
        monitor.start();
    }
}