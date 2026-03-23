package com.teamwork.model;

public class Comment {
    private String id;
    private String user;
    private String content;
    private String time;
    private String fileUrl;

    public Comment(String id, String user, String content, String time) {
        this.id = id;
        this.user = user;
        this.content = content;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
}