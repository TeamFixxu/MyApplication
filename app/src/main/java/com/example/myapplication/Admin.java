package com.example.myapplication;
public class Admin {
    private String name;
    private String region; // 관할구역
    private String phone;  // 전화번호
    private String profileImageUrl; // 프로필 사진 URL

    public Admin(String name, String region, String phone, String profileImageUrl) {
        this.name = name;
        this.region = region;
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return region;
    }

    public String getPhone() {
        return phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}