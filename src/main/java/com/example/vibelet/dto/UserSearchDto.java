package com.example.vibelet.dto;

public class UserSearchDto {
    private Long id;
    private String username;
    private String avatarUrl;
    private String friendshipStatus;

    public UserSearchDto(Long id, String username, String avatarUrl, String friendshipStatus) {
        this.id = id;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.friendshipStatus = friendshipStatus;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getFriendshipStatus() { return friendshipStatus; }
}