package com.example.myapplication;

import java.util.List;

public class Report {
    private String location;
    private String detail;
    private String reportTime;
    private String imageUri;

    // Firebase에 데이터를 저장하고 가져올 수 있도록 기본 생성자와 getter, setter 추가
    public Report() {}

    public Report(String location, String detail, String reportTime, String imageUri) {
        this.location = location;
        this.detail = detail;
        this.reportTime = reportTime;
        this.imageUri = imageUri;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getReportTime() {
        return reportTime;
    }

    public void setReportTime(String reportTime) {
        this.reportTime = reportTime;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
