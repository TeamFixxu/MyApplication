package com.example.myapplication;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.databinding.PinItemBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;


import java.util.ArrayList;
import java.util.HashMap;

public class PinAdapter extends RecyclerView.Adapter<PinAdapter.PinViewHolder> {

    private final ArrayList<String> itemList = new ArrayList<>(); // 태그 리스트
    private final Context context;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final HashMap<String, Boolean> selectedTags = new HashMap<>(); // 선택된 태그 추적


    public PinAdapter(Context context) {
        this.context = context;
    }

    // RecyclerView의 데이터 갱신
    public void updateTags(ArrayList<String> newTagList) {
        itemList.clear();
        itemList.addAll(newTagList);
        notifyDataSetChanged(); // RecyclerView 업데이트
    }

    @NonNull
    @Override
    public PinViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PinItemBinding binding = PinItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PinViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PinViewHolder holder, int position) {
        String tag = itemList.get(position);
        holder.binding.pin.setText("#" + tag);

        // 선택된 태그는 클릭하지 않도록 설정
        if (selectedTags.containsKey(tag) && selectedTags.get(tag)) {
            // 태그가 선택된 상태이면 배경을 변경하고 클릭 이벤트를 무효화
            Drawable pressedDrawable = context.getResources().getDrawable(R.drawable.tag_pressed);
            holder.binding.pin.setBackground(pressedDrawable);
            holder.binding.pin.setClickable(false); // 클릭을 비활성화
        } else {
            // 선택되지 않은 상태에서는 클릭 가능
            holder.binding.pin.setClickable(true);
            holder.binding.pin.setOnClickListener(view -> {
                selectedTags.put(tag, true); // 선택된 태그로 마킹
                Drawable pressedDrawable = context.getResources().getDrawable(R.drawable.tag_pressed);
                holder.binding.pin.setBackground(pressedDrawable);
                handleTagClick(tag); // 태그 클릭 시 사용 빈도 업뎃
            });
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
    public void addItem(String newItem) {
        db = FirebaseFirestore.getInstance();

        // Firestore에서 해당 태그가 존재하는지 확인
        db.collection("tags")
                .document(newItem)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        // 태그가 존재하지 않으면 새로 추가
                        db.collection("tags")
                                .document(newItem)
                                .set(new Tag(newItem, 1)) // 사용 빈도를 1로 초기화
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("PinAdapter", "태그 저장 성공: " + newItem);
                                    //notifyItemInserted(itemList.size() - 1);  // RecyclerView 업데이트
                                    notifyDataSetChanged(); // RecyclerView 업데이트
                                })
                                .addOnFailureListener(e -> Log.e("PinAdapter", "태그 저장 실패", e));
                    } else {
                        // 태그가 이미 존재하면 중복 추가 방지
                        Log.d("PinAdapter", "태그가 이미 존재: " + newItem);
                    }
                })
                .addOnFailureListener(e -> Log.e("PinAdapter", "태그 체크 실패", e));
    }

    private void handleTagClick(String tagName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 태그 클릭 시, 사용 빈도를 업데이트
        db.collection("tags").document(tagName)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        long usageCount = documentSnapshot.getLong("usageCount");
                        // 기존의 사용 빈도를 1 증가
                        db.collection("tags").document(tagName)
                                .update("usageCount", usageCount + 1)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("PinAdapter", "usageCount 증가 성공");
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("PinAdapter", "usageCount 증가 실패", e);
                                });
                    } else {
                        Log.e("PinAdapter", "태그가 존재하지 않음");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PinAdapter", "문서 가져오기 실패", e);
                });
    }


    // ViewHolder 클래스
    public static class PinViewHolder extends RecyclerView.ViewHolder {
        private final PinItemBinding binding;

        public PinViewHolder(PinItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    // 태그 객체 정의
    public static class Tag {
        private String name;
        private int usageCount;

        public Tag() { }

        public Tag(String name, int usageCount) {
            this.name = name;
            this.usageCount = usageCount;
        }

        public String getName() {
            return name;
        }

        public int getUsageCount() {
            return usageCount;
        }
    }
}