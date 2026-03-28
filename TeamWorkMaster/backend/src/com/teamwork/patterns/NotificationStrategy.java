package com.teamwork.patterns;

public interface NotificationStrategy {
    void send(String recipient, String title, String message);
}