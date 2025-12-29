package com.example.vibelet.dto;

public class UserSecurityUpdateDto {
    private String newUsername;
    private String newEmail;
    private String currentPassword;
    private String newPassword;

    public String getNewUsername() {
        return newUsername;
    }
    public String getNewEmail() {
        return newEmail;
    }
    public String getCurrentPassword() {
        return currentPassword;
    }
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewUsername(String newUsername) {
        this.newUsername = newUsername;
    }
    public void setNewEmail(String newEmail) {
        this.newEmail = newEmail;
    }
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}