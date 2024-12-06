package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;


import com.example.myapplication.databinding.ActivitySettingBinding;
import com.google.firebase.firestore.QuerySnapshot;

public class Setting extends AppCompatActivity {

    String StudentNum;
    String report;
    String point;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivitySettingBinding binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Action Bar 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Intent getintent = getIntent();
        String userNum = getintent.getStringExtra("userNum");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("userNum", userNum)  // userNum이 일치하는 사용자 찾기
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // 첫 번째 일치하는 문서 가져오기
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            Long pointLong = document.getLong("point");
                            point = pointLong != null ? pointLong.toString() : "0";
                            Long reportLong = document.getLong("report");
                            report = reportLong != null ? reportLong.toString() : "0";

                            StudentNum = document.getString("userNum");

                            // 데이터를 가져온 후 UI 업데이트
                            binding.editTextStudentId.setText("사용자 번호 : " + StudentNum);
                            binding.editTextReports.setText("신고 건수 : " + report);
                            binding.editTextPoints.setText("내 포인트 : " + point);
                        } else {
                            // 일치하는 문서가 없을 경우
                            Log.d("Firestore", "No documents found with the specified userNum");
                        }
                    } else {
                        Log.d("Firestore", "Query failed with ", task.getException());
                    }
                });
        binding.btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut(); // Firebase 로그아웃
                Intent intent = new Intent(Setting.this, LoginActivity.class); // 로그인 화면으로 이동
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // 기존 액티비티 스택 삭제
                startActivity(intent);
                finish(); // 현재 액티비티 종료
            }
        });
    }
}
