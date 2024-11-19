package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.databinding.AddDialogBinding;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.example.myapplication.MarkerData;
import com.example.myapplication.CameraActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity_user extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener{

    private static final int PIN_Delete = 1;
    private static final int PIN_REPAIR = 2;
    private static final int PIN_COMPLETE = 3;
    private static final int PIN_SOLVE = 4;

    private String imagePath;
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String  imageFilePath;
    private Uri photoUri;
    private int pin_type;

    private GoogleMap mMap;
    private BottomSheetBehavior<View> infoBottomSheetBehavior;
    private GestureDetector gestureDetector;
    FirebaseFirestore testdb;
    public ActivityMapsUserBinding binding;
    Dialog AddDialog; //의견추가하는 다이얼로그라서 add라고 이름지음

    public HashMap<Marker, MarkerData> markerDataMap = new HashMap<>();

    //public int addPersonCount= 0;
    //public int pinType = 0;
    private int pin_height = 110;
    private int pin_width = 90;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        testdb = FirebaseFirestore.getInstance(); //firebase 인스턴스 생성

        binding.pinRepair.setOnClickListener(this);
        binding.pinDelete.setOnClickListener(this);
        binding.pinSolve.setOnClickListener(this);
        binding.pinComplete.setOnClickListener(this);

        //권한 요청
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);

        View infoBottomSheet = findViewById(R.id.bottom_sheet);
        infoBottomSheetBehavior = BottomSheetBehavior.from(infoBottomSheet);
        infoBottomSheetBehavior.setPeekHeight(200);
        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        //초기화
        infoBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        infoBottomSheetBehavior.setPeekHeight(200);
        clear();

        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK){
                            // binding.image.
                            // image 표시
                            imagePath = result.getData().getStringExtra("image_path");
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                            binding.image.setImageBitmap(bitmap);
                            binding.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            binding.imageButton.setVisibility(View.INVISIBLE);
                            binding.image.setVisibility(View.VISIBLE);
                        }
                    }
                });
        binding.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launcher.launch(new Intent(MapsActivity_user.this,CameraActivity.class));
//
            }
        });

        binding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("PBY","detail : " + binding.detailEdit.getText().toString());
                Log.d("PBY","image_path : " + imagePath);
                Log.d("PBY","pin type : " + pin_type);

                infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                clear();
            }
        });

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

        mMap.setOnMapLongClickListener(this::addCustomMarker);
        Log.i("eun","클릭");
        //mMap.setOnMarkerClickListener(this::onMarkerClick);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker userMarker) {
                onMarkerClick2(userMarker);

                return true;
            }
        });

        //세팅 버튼 클릭 시 Setting 액티비티 실행
        binding.setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity_user.this, Setting.class);
                startActivity(intent);
            }
        });

        //클릭 시 adminList 관리자리스트 액티비티 실행
        binding.helpListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity_user.this, AdminList.class);
                startActivity(intent);
            }
        });
    }

    public void addCustomMarker(LatLng latLng) {
        Marker thisMarker = null; //초기값 설정

        //Map에 저장할 정보들을 바텀시트를 통해 가져와야함.
        MarkerData data = new MarkerData(0, false, "Hi", 0);

        int pinType = data.getPinType();
        //여기서 pintype을 위의 data의 값으로 대신 해줘야함. 테스트를 위해 그냥 pintype씀
        if (pinType == 0){
            thisMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng).draggable(true)
                    .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.pink_pin, 80, 100))));
        } else if (pinType == 1) {
            thisMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng).draggable(true)
                    .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.yellow_pin, 80, 100))));
        }
        else if (pinType == 2) {
            thisMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng).draggable(true)
                    .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.blue_pin, 80, 100))));
        }
        else {
            Toast.makeText(this, "잘못된 핀 타입입니다.", Toast.LENGTH_SHORT).show();
        }

        if (thisMarker != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            markerDataMap.put(thisMarker, data);
            Log.d("eun","map에 put 성공");
            saveMarkerToFirestore(thisMarker, data);
            Toast.makeText(this, "마커와 데이터가 저장됐습니다." + data.getDescription(), Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(this, "마커를 추가하지 못했습니다.", Toast.LENGTH_SHORT).show();
        }

    }

//일단 오류가 계속 나니깐 이렇게 해둠
    public boolean onMarkerClick2(Marker marker) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
        mMap.animateCamera(center);

        Dialog AddDialog = new Dialog(MapsActivity_user.this); //다이얼로그 관련 코드
        AddDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        AddDialogBinding dialogBinding = AddDialogBinding.inflate(getLayoutInflater());
        AddDialog.setContentView(dialogBinding.getRoot());

        AddDialog.show();

        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder. */
        Button addButton = dialogBinding.addButton;

        addButton.setEnabled(true);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addButton.setEnabled(false);
                addButton.setBackgroundColor(Color.parseColor("#A0A0A0"));

                //Toast.makeText(MapsActivity_user.this, "의견이 추가 됐습니다.", Toast.LENGTH_SHORT).show();
                if (markerDataMap.containsKey(marker)) {
                    MarkerData clickedMarkerData = markerDataMap.get(marker);

                    if (clickedMarkerData != null) {
                        int newCount = clickedMarkerData.getAddPersonCount() + 1;
                        clickedMarkerData.setAddPersonCount(newCount);

                        DocumentReference changePersonCountdb = testdb.collection("fixxu").document(marker.getId());
                        changePersonCountdb
                                .update("addPersonCount", newCount)
                                .addOnSuccessListener(unused -> Log.d("eun", "update complete!" + newCount))
                                .addOnFailureListener(e -> Log.d("eun", "update fail..."));

                        Toast.makeText(MapsActivity_user.this, "의견이 추가 됐습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MapsActivity_user.this, "마커 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
                AddDialog.dismiss();
            }
        });
        return true;
    }

    /*public void showMarkerDialog(Marker marker){

        AddDialog.show();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.
        Button addButton = findViewById(R.id.add_button);

        addButton.setEnabled(true);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addButton.setEnabled(false);
                addButton.setBackgroundColor(Integer.parseInt("#A0A0A0"));

                String markerId = marker.getId();
                if (markerDataMap.containsKey(marker)){
                    MarkerData clickedMarkerData = markerDataMap.get(marker);

                    int newCount = clickedMarkerData.getAddPersonCount() + 1;
                    clickedMarkerData.setAddPersonCount(newCount);

                    Toast.makeText(MapsActivity_user.this, "의견이 추가 됐습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MapsActivity_user.this, "마커 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    } */

    private void saveMarkerToFirestore(Marker marker, MarkerData markerData) {
       //데이터 준비
        Map<String, Object> data = new HashMap<>();
        data.put("addPersonCount", markerData.getAddPersonCount());
        data.put("isCreator", markerData.getIsCreator());
        data.put("description", markerData.getDescription());
        data.put("pinType", markerData.getPinType());
        data.put("latitude", marker.getPosition().latitude);
        data.put("longitude", marker.getPosition().longitude);

        // Firestore에 데이터 저장 (문서 ID는 자동 생성)
        testdb.collection("fixxu")
                .document(marker.getId())
                .set(data) // add() 메서드는 문서 ID를 자동 생성
                .addOnSuccessListener(aVoid ->
                        Log.d("eun", "Marker saved successfully with ID: " + marker.getId()))
                .addOnFailureListener(e ->
                        Log.e("eun", "Error saving marker", e));
    }

    public void onClick(View v){
        clear_pin();

        if(v == binding.pinDelete){
            binding.pinDelete.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                    , R.color.button_pressed_color_pink));
            pin_type=PIN_Delete;}
        else if(v == binding.pinComplete){
            binding.pinComplete.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                    , R.color.button_pressed_color_blue));
            pin_type=PIN_COMPLETE;}
        else if(v== binding.pinSolve){
            binding.pinSolve.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                    , R.color.button_pressed_color_blue));
            pin_type=PIN_SOLVE;}
        else if(v==binding.pinRepair){
            binding.pinRepair.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                    , R.color.button_pressed_color_blue));
            pin_type=PIN_REPAIR;
        }
    }
    private void clear(){
        clear_pin();
        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        binding.detailEdit.setText("");
        binding.detailEdit.clearFocus();
        binding.imageButton.setVisibility(View.VISIBLE);
        binding.image.setVisibility(View.INVISIBLE);
    }
    private void clear_pin(){
        pin_type=0;
        binding.pinDelete.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                , R.color.button_default_color));
        binding.pinComplete.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                , R.color.button_default_color));
        binding.pinSolve.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                , R.color.button_default_color));
        binding.pinRepair.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                , R.color.button_default_color));

    }

    private Bitmap getResizedBitmap(int drawableRes, int width, int height) {
        // Drawable 리소스를 Bitmap으로
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        // Bitmap 크기 조절
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }
}