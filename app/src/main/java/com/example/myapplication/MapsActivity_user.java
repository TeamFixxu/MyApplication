package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup.LayoutParams;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.databinding.AddDialogBinding;
import com.example.myapplication.databinding.PinItemBinding;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity_user extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener{

    private static final int PIN_CRASH = 1;
    private static final int PIN_LOST = 2;
    private static final int PIN_WORK = 3;
    private static final int PIN_HELP = 4;

    private String imagePath;
    private String location;
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private String  imageFilePath;
    private Uri photoUri;
    private int pin_type;

    private GoogleMap mMap;
    private BottomSheetBehavior<View> infoBottomSheetBehavior;
    private GestureDetector gestureDetector;
    FirebaseFirestore mFirebase;
    public ActivityMapsUserBinding binding;
    Dialog AddDialog; //의견추가하는 다이얼로그라서 add라고 이름지음

    public HashMap<Marker, MarkerData> markerDataMap = new HashMap<>();

    //public int addPersonCount= 0;
    //public int pinType = 0;
    private int pin_height = 110;
    private int pin_width = 90;

    private String studentNum;
    private PinAdapter adapter; // 핀 어댑터
    private int currentKey = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Intent 데이터 수신
        Intent getintent = getIntent();
        studentNum = getintent.getStringExtra("userNum");

        binding = ActivityMapsUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mFirebase = FirebaseFirestore.getInstance(); // Firebase 인스턴스 생성
        // Firestore 인스턴스 생성
        CollectionReference tagsRef = mFirebase.collection("tags");

// 태그 데이터 가져오기 및 RecyclerView 업데이트
        tagsRef.orderBy("usageCount", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error fetching tags", error);
                        return;
                    }

                    if (value != null) {
                        ArrayList<String> tagList = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            String tagName = doc.getId(); // 문서 ID를 태그 이름으로 사용
                            tagList.add(tagName);
                        }
                        adapter.updateTags(tagList); // RecyclerView 갱신
                    }
                });



        binding.pinCrash.setOnClickListener(this);
        binding.pinLost.setOnClickListener(this);
        binding.pinHelp.setOnClickListener(this);
        binding.pinWork.setOnClickListener(this);


        // RecyclerView와 어댑터 초기화

        adapter = new PinAdapter(this); //핀관련

        binding.pinRecyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.pinRecyclerview.setAdapter(adapter); //핀관련


        // 태그 추가 버튼 이벤트 처리
        binding.addTagButton.setOnClickListener(view -> showInputDialog()); //핀관련

        // Spinner 이벤트 처리
        binding.locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                location = adapterView.getItemAtPosition(i).toString();
                Log.d("PBY", location);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        // 지도 설정
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // 이미지 버튼 설정
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        imagePath = result.getData().getStringExtra("image_path");
                        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

                        binding.image.setImageBitmap(bitmap);
                        binding.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        binding.imageButton.setVisibility(View.INVISIBLE);
                        binding.image.setVisibility(View.VISIBLE);
                    }
                });

        binding.imageButton.setOnClickListener(view -> launcher.launch(new Intent(MapsActivity_user.this, CameraActivity.class)));

        // 업로드 버튼 설정
        binding.upload.setOnClickListener(view -> {
            Log.d("PBY", "detail : " + binding.detailEdit.getText().toString());
            Log.d("PBY", "image_path : " + imagePath);
            Log.d("PBY", "pin type : " + pin_type);

            infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            clear();
        });

        // BottomSheet 초기화
        infoBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        infoBottomSheetBehavior.setPeekHeight(200);
        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // 권한 요청
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
    }

    private void fetchTagsFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("tags")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tagName = document.getString("name");
                            if (tagName != null) {
                                adapter.addItem(tagName);
                            }
                        }
                        Log.d("MapsActivity_user", "태그 로드 성공");
                    } else {
                        Log.e("MapsActivity_user", "태그 로드 실패", task.getException());
                    }
                });
    }
    public void handleTagClick(String tagName) {
        DocumentReference tagDocRef = mFirebase.collection("tags").document(tagName);

        tagDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 태그가 존재하면 usageCount 증가
                long usageCount = documentSnapshot.getLong("usageCount") != null ? documentSnapshot.getLong("usageCount") : 0;
                mFirebase.collection("tags").document(tagName).update("usageCount", usageCount + 1);
            } else {
                // 태그가 없으면 새로 추가
                Map<String, Object> newTag = new HashMap<>();
                newTag.put("name", tagName);
                newTag.put("usageCount", 1);
                tagDocRef.set(newTag);
            }
        });
    }
    private void showInputDialog() {
        // 다이얼로그 뷰를 inflate
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_tag, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        // 뷰에서 요소 참조
        EditText input = dialogView.findViewById(R.id.tag_input);
        Button cancelButton = dialogView.findViewById(R.id.cancel_button);
        Button addButton = dialogView.findViewById(R.id.add_button);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 추가 버튼 동작
        addButton.setOnClickListener(v -> {
            String newTag = input.getText().toString().trim();
            if (!newTag.isEmpty()) {
                adapter.addItem(newTag); // 태그 추가
                handleTagClick(newTag); // 빈도 업데이트
                dialog.dismiss();
            }
        });

        // 취소 버튼 동작
        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
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
                intent.putExtra("userNum", studentNum);
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

                        DocumentReference changePersonCountdb = mFirebase.collection("fixxu").document(marker.getId());
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
        mFirebase.collection("fixxu")
                .document(marker.getId())
                .set(data) // add() 메서드는 문서 ID를 자동 생성
                .addOnSuccessListener(aVoid ->
                        Log.d("eun", "Marker saved successfully with ID: " + marker.getId()))
                .addOnFailureListener(e ->
                        Log.e("eun", "Error saving marker", e));
    }

    public void onClick(View v){
        clear_pin();

        if(v == binding.pinCrash){
            binding.pinCrash.setBackground(getResources().getDrawable(R.drawable.pin_pressed));
            pin_type=PIN_CRASH;}
        else if(v == binding.pinLost){
            binding.pinLost.setBackground(getResources().getDrawable(R.drawable.pin_pressed));
            pin_type=PIN_LOST;}
        else if(v== binding.pinWork){
            binding.pinWork.setBackground(getResources().getDrawable(R.drawable.pin_pressed));
            pin_type=PIN_WORK;}
        else if(v==binding.pinHelp){
            binding.pinHelp.setBackground(getResources().getDrawable(R.drawable.pin_pressed));
            pin_type=PIN_HELP;
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
        binding.pinCrash.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
        binding.pinLost.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
        binding.pinWork.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
        binding.pinHelp.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
    }
    private Bitmap getResizedBitmap(int drawableRes, int width, int height) {
        // Drawable 리소스를 Bitmap으로
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        // Bitmap 크기 조절
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }
}