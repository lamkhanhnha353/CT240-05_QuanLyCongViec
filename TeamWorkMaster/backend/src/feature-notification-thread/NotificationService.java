public class NotificationService {

    public static void notifyStatusChange(Task task) {
        System.out.println("🔔 Thông báo: Công việc '" + task.getName()
                + "' đã đổi trạng thái thành: " + task.getStatus());
    }

    public static void notifyDeadlineSoon(Task task) {
        System.out.println("⏰ Nhắc nhở: Công việc '" + task.getName()
                + "' còn 3 ngày nữa đến deadline!");
    }

    public static void notifyDeadlineToday(Task task) {
        System.out.println("📅 Hôm nay là deadline của công việc '"
                + task.getName() + "'!");
    }

    public static void notifyOverdue(Task task) {
        System.out.println("⚠️ Quá hạn: Công việc '" + task.getName()
                + "' đã quá deadline 1 ngày!");
    }
}