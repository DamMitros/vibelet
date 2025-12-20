package com.example.vibelet.dto;

public class VibeExportDto {
    private String content;
    private String createdAt;

    public VibeExportDto() {
    }

    public VibeExportDto(String content, String createdAt) {
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}