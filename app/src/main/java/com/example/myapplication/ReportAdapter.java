package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

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

        // 이미지 URI가 있을 경우 Firebase Storage에서 이미지를 불러오기
        if (currentItem.getImageUri() != null) {
            Uri imageUri = Uri.parse(currentItem.getImageUri());
            Glide.with(context)
                    .load(imageUri)
                    .into(holder.imageView);
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
