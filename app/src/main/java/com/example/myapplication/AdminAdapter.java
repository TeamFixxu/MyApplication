package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AdminAdapter extends RecyclerView.Adapter<AdminAdapter.AdminViewHolder> {
    private List<Admin> adminList;

    public AdminAdapter(List<Admin> adminList) {
        this.adminList = adminList;
    }

    @NonNull
    @Override
    public AdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin, parent, false);
        return new AdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminViewHolder holder, int position) {
        Admin admin = adminList.get(position);
        holder.textViewName.setText(admin.getName());
        holder.textViewRegion.setText(admin.getRegion());
        holder.textViewPhone.setText(admin.getPhone());

        // Glide로 프로필 이미지 로드
        Glide.with(holder.imageViewProfile.getContext())
                .load(admin.getProfileImageUrl())
                .placeholder(R.drawable.blue_pin) // 로드 중 표시할 기본 이미지
                .into(holder.imageViewProfile);
    }

    @Override
    public int getItemCount() {
        return adminList.size();
    }

    public static class AdminViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewEmail, textViewRegion, textViewPhone;
        ImageView imageViewProfile;

        public AdminViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            textViewRegion = itemView.findViewById(R.id.textViewRegion);
            textViewPhone = itemView.findViewById(R.id.textViewPhone);
            imageViewProfile = itemView.findViewById(R.id.imageViewProfile);
        }
    }
}