package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserAccount {

    public UserAccount() {

    }
    public String getIdToken() { return idToken; }

    public void setIdToken(String idToken){
        this.idToken = idToken;
    }

    private String idToken;
    public String getEmailId() { return emailId;}

    public void setEmailId(String emailId){ this.emailId = emailId; }

    private String emailId;

    public String getPassword() { return password;}

    public void setPassword(String password){ this.password = password; }

    private String password;

    public void setPhoneNum(String phoneNum) { this.phoneNum = phoneNum; }

    private String phoneNum;



    public void setStudentId(String strStudentNum) {
        this.strSudentNum = strStudentNum;
    }

    public void setManagerId(String strManagerNum) { this.strManagerNum = strManagerNum;}

    private String strSudentNum, strManagerNum;
}