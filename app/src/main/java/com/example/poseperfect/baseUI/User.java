package com.example.poseperfect.baseUI;

public class User {
    public String username;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String email;

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }
}