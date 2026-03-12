import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        List<Task> tasks = new ArrayList<>();

        Task task1 = new Task(
                "Thiết kế giao diện",
                "Đang làm",
                LocalDateTime.now().plusSeconds(20));

        tasks.add(task1);

        // tạo thread kiểm tra deadline
        Thread monitor = new Thread(new DeadlineMonitor(tasks));
        monitor.start();

        // giả lập thay đổi trạng thái
        try {
            Thread.sleep(10000);
        } catch (Exception e) {
        }

        task1.setStatus("Hoàn thành");
    }
}