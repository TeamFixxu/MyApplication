package com.example.myapplication;

import java.util.List;

public class Admin {
    private String name;
    private List<String> regions; // 타입 변경
    private String phone;  // 전화번호
    private String profileImageUrl; // 프로필 사진 URL

    public Admin(String name, List<String> regions, String phone, String profileImageUrl) {
        this.name = name;
        this.regions = regions; // null 방지
        this.phone = phone;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getRegion() {
        return String.join(", ", regions);
    }

    public String getPhone() {
        return phone;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}