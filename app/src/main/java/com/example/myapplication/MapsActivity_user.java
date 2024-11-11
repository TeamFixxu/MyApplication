package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.myapplication.databinding.ActivityMapsUserBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class MapsActivity_user extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private BottomSheetBehavior<View> infoBottomSheetBehavior;
    private GestureDetector gestureDetector;

    private ActivityMapsUserBinding binding;
    Dialog AddDialog; //의견추가하는 다이얼로그라서 add라고 이름지음
    private Marker userMarker;

    public int addPersonCount= 0;
    public int pinType = 0;
    private int pin_height = 110;
    private int pin_width = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);


        View infoBottomSheet = findViewById(R.id.bottom_sheet);
        infoBottomSheetBehavior = BottomSheetBehavior.from(infoBottomSheet);
        infoBottomSheetBehavior.setPeekHeight(200);
        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if(e2.getY()<e1.getY()) {
                    if (infoBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        AddDialog = new Dialog(MapsActivity_user.this); //다이얼로그 관련 코드
        AddDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        AddDialog.setContentView(R.layout.add_dialog);

        if (mapFragment != null){
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //숭실대 위치를 기준으로 첫 세팅
        LatLng soongsil_univ = new LatLng(37.4963, 126.9572); //숭실대 위치
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(soongsil_univ, 17));

        //세팅 버튼 클릭 시 Setting 액티비티 실행
        binding.setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity_user.this, Setting.class);
                startActivity(intent);
            }
        });

        //클릭 시 adminList 관리자리스트 액티비티 실행
        binding.setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity_user.this, AdminList.class);
                startActivity(intent);
            }
        });

        mMap.setOnMapLongClickListener(latLng -> {
            //바텀시트에 체크한 마커 종류를 구분할 부분이 필요.
            if (pinType == 0){
                userMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng).draggable(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.pink_pin, pin_width, pin_height))));
            } else if (pinType == 1) {
                userMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng).draggable(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.yellow_pin, pin_width, pin_height))));
            }
            else if (pinType == 2) {
                userMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng).draggable(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.blue_pin, pin_width, pin_height))));
            }
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker userMarker) {
                CameraUpdate center = CameraUpdateFactory.newLatLng(userMarker.getPosition());
                mMap.animateCamera(center);

                showMarkerDialog(userMarker);

                return true;
            }
        });
    }

    public void showMarkerDialog(Marker marker){
        AddDialog.show();

        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder. */

        Button addButton = AddDialog.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MapsActivity_user.this, "의견을 추가했습니다.",Toast.LENGTH_SHORT).show();
                addPersonCount++;
            }
        });
    }

    private Bitmap getResizedBitmap(int drawableRes, int width, int height) {
        // Drawable 리소스를 Bitmap으로
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        // Bitmap 크기 조절
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }
}