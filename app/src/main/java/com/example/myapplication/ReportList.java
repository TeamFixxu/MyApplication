package com.example.myapplication;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
public class ReportList extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ReportAdapter reportAdapter;
    private FirebaseFirestore db;

    private List<Report> reportList = new ArrayList<>();
    private List<Report> filteredReportList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_list);

        // Action Bar 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        EditText searchBar = findViewById(R.id.searchBar);

        db = FirebaseFirestore.getInstance();
        CollectionReference reportsRef = db.collection("fixxu");

        // Firebase에서 데이터 가져오기
        reportsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Firestore에서 가져온 데이터를 reportList에 추가
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String location = document.getString("location");
                    String detail = document.getString("description");
                    String date = document.getString("createdAt"); // 사건 일시 추가
                    String imageUrl = document.getString("imageUrl");

                    // Report 객체 생성
                    Report report = new Report(location, detail, date, imageUrl);
                    reportList.add(report); // reportList에 추가
                }

                // filteredReportList에 초기화된 데이터를 추가
                filteredReportList.addAll(reportList);

                // 어댑터 설정
                reportAdapter = new ReportAdapter(this, filteredReportList);
                recyclerView = findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(this));
                recyclerView.setAdapter(reportAdapter);
            } else {
                Log.d("Firebase", "Error getting documents: ", task.getException());
            }
        });

        // 검색바에 TextWatcher 추가
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterReports(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    // 장소 검색 결과에 맞춰 리사이클러뷰를 필터링하고 상단으로 이동
    private void filterReports(String query) {
        filteredReportList.clear();

        // 입력된 검색어에 맞는 항목만 필터링하여 filteredReportList에 추가
        for (Report report : reportList) {
            if (report.getLocation().toLowerCase().contains(query.toLowerCase())) {
                filteredReportList.add(report);
            }
        }

        // 어댑터에 변경 사항 반영
        reportAdapter.notifyDataSetChanged();

        // 결과가 있을 경우 리스트 상단으로 스크롤
        if (!filteredReportList.isEmpty()) {
            recyclerView.scrollToPosition(0);
        }
    }
}
