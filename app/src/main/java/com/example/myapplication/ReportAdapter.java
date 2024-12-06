package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private List<Report> itemList;
    private Context context;

    public ReportAdapter(Context context, List<Report> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report currentItem = itemList.get(position);

        // 텍스트 뷰에 데이터 설정
        holder.locationTextView.setText(currentItem.getLocation());
        holder.detailTextView.setText(currentItem.getDetail());
        holder.reportTimeTextView.setText(currentItem.getReportTime());

        // Firebase Storage에서 이미지 URI를 처리
        if (currentItem.getImageUri() != null && !currentItem.getImageUri().isEmpty()) {
            Log.d("FirebaseStorage", "imageUri: " + currentItem.getImageUri());
            FirebaseStorage storage = FirebaseStorage.getInstance("gs://fabea-f579a.firebasestorage.app");
            StorageReference storageRef = storage.getReferenceFromUrl(currentItem.getImageUri());

            // Firebase Storage에서 다운로드 URL 가져오기
            storageRef.getDownloadUrl()
                    .addOnSuccessListener(uri -> {
                        // 성공적으로 URL을 가져왔을 경우 Glide로 이미지 로드
                        Glide.with(holder.imageView.getContext())
                                .load(uri.toString())
                                .placeholder(R.drawable.person) // 로딩 중 기본 이미지
                                .error(R.drawable.red_circle)      // 로드 실패 시 기본 이미지
                                .into(holder.imageView);
                    })
                    .addOnFailureListener(e -> {
                        // URL 가져오기 실패 시 기본 이미지 설정
                        holder.imageView.setImageResource(R.drawable.red_circle);
                        Log.e("FirebaseStorage", "이미지 로드 실패: " + e.getMessage());
                    });
        } else {
            // URI가 없을 경우 기본 이미지 설정
            holder.imageView.setImageResource(R.drawable.list);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // 뷰홀더 클래스
    public static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView locationTextView, detailTextView, reportTimeTextView;
        ShapeableImageView imageView;

        public ReportViewHolder(View itemView) {
            super(itemView);
            locationTextView = itemView.findViewById(R.id.location);
            detailTextView = itemView.findViewById(R.id.detail);
            reportTimeTextView = itemView.findViewById(R.id.report_time);
            imageView = itemView.findViewById(R.id.image);
        }
    }
}
