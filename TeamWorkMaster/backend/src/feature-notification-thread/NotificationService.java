public class NotificationService {

    public static void notifyStatusChange(Task task) {
        System.out.println("🔔 Thông báo: Công việc '" + task.getName()
                + "' đã đổi trạng thái thành: " + task.getStatus());
    }

    public static void notifyDeadline(Task task) {
        System.out.println("⏰ Cảnh báo: Deadline của '"
                + task.getName() + "' sắp đến!");
    }
}