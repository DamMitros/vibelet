package com.example.vibelet.dto;

import java.util.List;

public class DataExportDto {
    private String username;
    private String email;
    private String bio;
    private List<VibeExportDto> vibes;

    public DataExportDto() {
    }

    public DataExportDto(String username, String email, String bio, List<VibeExportDto> vibes) {
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.vibes = vibes;
    }

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

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public List<VibeExportDto> getVibes() {
        return vibes;
    }

    public void setVibes(List<VibeExportDto> vibes) {
        this.vibes = vibes;
    }
}