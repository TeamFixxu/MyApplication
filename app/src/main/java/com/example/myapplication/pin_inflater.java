package com.example.myapplication;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class pin_inflater extends AppCompatActivity {
    private BottomSheetBehavior<View> infoBottomSheetBehavior;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_inflater);

        View infoBottomSheet = findViewById(R.id.bottom_sheet);
        infoBottomSheetBehavior = BottomSheetBehavior.from(infoBottomSheet);
        infoBottomSheetBehavior.setPeekHeight(200);
        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if(e2.getY()<e1.getY()) {
                    if (infoBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }
// 바텀시트에 정보를 변경하면, 그거를 받아서 저장하는,
    // 정보 저장.

}