package com.example.myapplication;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AdminList extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminAdapter adminAdapter;
    private List<Admin> adminList = new ArrayList<>(); // 전체 데이터
    private List<Admin> searchResultList = new ArrayList<>(); // 검색 결과 데이터
    private FirebaseFirestore db;
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        // Action Bar 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        recyclerView = findViewById(R.id.recyclerView);
        searchBar = findViewById(R.id.searchBar); // 검색바 연결
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adminAdapter = new AdminAdapter(new ArrayList<>()); // 초기 데이터 없음
        recyclerView.setAdapter(adminAdapter);

        db = FirebaseFirestore.getInstance();
        fetchAdminsFromFirestore();

        setupSearchBar();
    }

    private void fetchAdminsFromFirestore() {
        db.collection("users")
                .whereEqualTo("role", "manager")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        adminList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            List<String> regions = (List<String>) document.get("regions");
                            String phone = document.getString("Phone Num");
                            String profileImageUrl = document.getString("profileImageUrl");

                            adminList.add(new Admin(name, regions, phone, profileImageUrl));
                        }
                        updateRecyclerView(adminList); // 초기 데이터 표시
                    } else {
                        // 오류 처리
                    }
                });
    }

    private void setupSearchBar() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchInFirestore(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void searchInFirestore(String query) {
        if (query.isEmpty()) {
            updateRecyclerView(adminList); // 검색어가 없으면 전체 데이터 표시
            return;
        }
        db.collection("users")
                .whereArrayContains("regions", query) // regions 필드에 query 포함 여부 확인
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        searchResultList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            List<String> regions = (List<String>) document.get("regions");
                            String phone = document.getString("Phone Num");
                            String profileImageUrl = document.getString("profileImageUrl");

                            searchResultList.add(new Admin(name, regions, phone, profileImageUrl));
                        }
                        // 검색 결과만 RecyclerView에 표시
                        updateRecyclerView(searchResultList);
                    } else {
                        // 오류 처리
                        Toast.makeText(this, "검색 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        searchResultList.clear();
                        updateRecyclerView(searchResultList); // RecyclerView 초기화
                    }
                });
    }

    private void updateRecyclerView(List<Admin> data) {
        adminAdapter.updateData(data); // Adapter에서 데이터 갱신
    }

}