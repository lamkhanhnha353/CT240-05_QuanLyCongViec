package com.teamwork.patterns;

import java.util.ArrayList;
import java.util.List;

public class TaskSubject {

    private List<TaskObserver> observers = new ArrayList<>();


    public void attach(TaskObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detach(TaskObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(int taskId, String oldStatus, String newStatus) {
        for (TaskObserver observer : observers) {
            observer.updateStatus(taskId, oldStatus, newStatus);
        }
    }
}