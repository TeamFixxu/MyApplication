package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.databinding.ActivityMapsAdminBinding;
import com.example.myapplication.databinding.ActivityMapsUserBinding;
import com.example.myapplication.databinding.ActivityMapsUserBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.IOException;

public class MapsActivity_admin extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    private BottomSheetBehavior<View> infoBottomSheetBehavior;
    private GestureDetector gestureDetector;
    private String imagePath;
    private Marker userMarker;
    private int pinType = 0;
    private int pin_height = 110;
    private int pin_width = 90;
    private int pin_type;

    private @NonNull ActivityMapsAdminBinding binding;

    private static final int PIN_Delete = 1;
    private static final int PIN_REPAIR = 2;
    private static final int PIN_COMPLETE = 3;
    private static final int PIN_SOLVE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent getintent = getIntent();
        String managerNum = getintent.getStringExtra("userNum");
        Log.d("PBY", "managerNum : " + managerNum);

        super.onCreate(savedInstanceState);

        binding = ActivityMapsAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Google Maps 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 바텀시트 초기화
        infoBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        infoBottomSheetBehavior.setPeekHeight(200);

        // 클릭 리스너 설정
        binding.pinRepair.setOnClickListener(this);
        binding.pinDelete.setOnClickListener(this);
        binding.pinSolve.setOnClickListener(this);
        binding.pinComplete.setOnClickListener(this);

        binding.setupButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity_admin.this, Setting_admin.class);
            intent.putExtra("userNum", managerNum);
            startActivity(intent);
        });

        // 권한 요청
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);

        // 카메라 및 이미지 업로드
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK) {
                            imagePath = result.getData().getStringExtra("image_path");
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            binding.image.setImageBitmap(bitmap);
                            binding.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            binding.imageButton.setVisibility(View.INVISIBLE);
                            binding.image.setVisibility(View.VISIBLE);
                        }
                    }
                });

        binding.imageButton.setOnClickListener(v -> launcher.launch(new Intent(this, CameraActivity.class)));

        binding.upload.setOnClickListener(view -> {
            Log.d("PBY", "detail : " + binding.detailEdit.getText().toString());
            Log.d("PBY", "image_path : " + imagePath);
            Log.d("PBY", "pin type : " + pin_type);
            infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            clear();
        });
    }

    @Override
    public void onClick(View v) {
        clear_pin();
        if (v == binding.pinDelete) {
            binding.pinDelete.setBackgroundColor(ContextCompat.getColor(this, R.color.button_pressed_color_pink));
            pin_type = PIN_Delete;
        } else if (v == binding.pinComplete) {
            binding.pinComplete.setBackgroundColor(ContextCompat.getColor(this, R.color.button_pressed_color_blue));
            pin_type = PIN_COMPLETE;
        } else if (v == binding.pinSolve) {
            binding.pinSolve.setBackgroundColor(ContextCompat.getColor(this, R.color.button_pressed_color_blue));
            pin_type = PIN_SOLVE;
        } else if (v == binding.pinRepair) {
            binding.pinRepair.setBackgroundColor(ContextCompat.getColor(this, R.color.button_pressed_color_blue));
            pin_type = PIN_REPAIR;
        }
    }

    private void clear() {
        clear_pin();
        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        binding.detailEdit.setText("");
        binding.detailEdit.clearFocus();
        binding.imageButton.setVisibility(View.VISIBLE);
        binding.image.setVisibility(View.INVISIBLE);
    }

    private void clear_pin() {
        pin_type = 0;
        binding.pinDelete.setBackgroundColor(ContextCompat.getColor(this, R.color.button_default_color));
        binding.pinComplete.setBackgroundColor(ContextCompat.getColor(this, R.color.button_default_color));
        binding.pinSolve.setBackgroundColor(ContextCompat.getColor(this, R.color.button_default_color));
        binding.pinRepair.setBackgroundColor(ContextCompat.getColor(this, R.color.button_default_color));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng soongsil_univ = new LatLng(37.4963, 126.9572);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(soongsil_univ, 18));

        mMap.setOnMapLongClickListener(latLng -> {
            userMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.pink_pin, pin_width, pin_height))));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        });
    }
    private Bitmap getResizedBitmap(int drawableRes, int width, int height) {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }
}
