package com.teamwork.db;

public class LoginResult {
    public int id;
    public String role;
    public String fullName;
    public boolean banned;

    public LoginResult(int id, String role, String fullName, boolean banned) {
        this.id = id;
        this.role = role;
        this.fullName = fullName;
        this.banned = banned;
    }
}
