package com.teamwork.patterns;

public class NotificationFactory {

 
    public static NotificationStrategy createStrategy(String type) {
        if (type == null) {
            return new EmailNotificationStrategy(); // Mặc định
        }

        switch (type.toUpperCase()) {
            case "EMAIL":
                return new EmailNotificationStrategy();
           
            default:
                return new EmailNotificationStrategy();
        }
    }
}