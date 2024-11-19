package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UserCheckActivity extends AppCompatActivity {

    private Button mBtnUser;
    private Button mBtnManager;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_check);

        mBtnUser = findViewById(R.id.button3);
        mBtnManager = findViewById(R.id.button4);

        mBtnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("UserCheck", "User button clicked");
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