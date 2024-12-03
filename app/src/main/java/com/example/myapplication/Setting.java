package com.example.myapplication;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


import com.example.myapplication.databinding.ActivitySettingBinding;

public class Setting extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingBinding binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseFirestore db = FirebaseFirestore.getInstance();


    }
}