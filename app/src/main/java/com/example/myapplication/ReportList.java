package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.ActivityReportListBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReportList extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminAdapter adminAdapter;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);

        // Action Bar 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        db = FirebaseFirestore.getInstance();
        CollectionReference reportsRef = db.collection("reports");

        reportsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<Report> items = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String location = document.getString("location");
                    String detail = document.getString("detail");
                    String reportTime = document.getString("reportTime");
                    String imageUri = document.getString("imageUri");

                    Report report = new Report(location, detail, reportTime, imageUri);
                    items.add(report);
                }

                // 어댑터 설정
                ReportAdapter adapter = new ReportAdapter(this, items);
                recyclerView = findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(adapter);
            } else {
                Log.d("Firebase", "Error getting documents: ", task.getException());
            }
        });
    }
}