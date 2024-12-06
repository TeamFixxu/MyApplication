package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class UserCheckActivity extends AppCompatActivity {

    private Button mBtnUser;
    private Button mBtnManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_check);

        // Action Bar 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mBtnUser = findViewById(R.id.button3);
        mBtnManager = findViewById(R.id.btnManager);

        mBtnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserCheckActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        mBtnManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("UserCheck", "Manager button clicked");
                Intent intent = new Intent(UserCheckActivity.this, AdminRegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}