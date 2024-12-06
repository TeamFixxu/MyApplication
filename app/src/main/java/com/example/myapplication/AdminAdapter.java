package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import android.Manifest;

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
        holder.textViewPhone.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("전화 걸기")
                    .setMessage(admin.getPhone() + "로 전화를 걸겠습니까?")
                    .setPositiveButton("예", (dialog, which) -> {
                        Intent intent = new Intent(Intent.ACTION_CALL);
                        intent.setData(Uri.parse("tel:" + admin.getPhone()));
                        if (ActivityCompat.checkSelfPermission(holder.itemView.getContext(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions((Activity) holder.itemView.getContext(), new String[]{Manifest.permission.CALL_PHONE}, 1);
                        } else {
                            holder.itemView.getContext().startActivity(intent);
                        }
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        });
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

    public void updateData(List<Admin> newAdminList) {
        this.adminList.clear();
        this.adminList.addAll(newAdminList);
        notifyDataSetChanged();
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