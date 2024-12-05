package com.example.myapplication;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminList extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminAdapter adminAdapter;
    private List<Admin> adminList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adminAdapter = new AdminAdapter(adminList);
        recyclerView.setAdapter(adminAdapter);

        db = FirebaseFirestore.getInstance();
        fetchAdminsFromFirestore();
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
                            String region = document.getString("region"); // 관할구역
                            String phone = document.getString("Phone Num");   // 전화번호
                            String profileImageUrl = document.getString("profileImageUrl"); // 이미지 URL

                            adminList.add(new Admin(name, region, phone, profileImageUrl));
                        }
                        adminAdapter.notifyDataSetChanged();
                    } else {
                        // 오류 처리
                    }
                });
    }

}