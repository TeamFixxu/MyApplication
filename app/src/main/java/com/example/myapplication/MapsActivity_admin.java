package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.databinding.ActivityMapsAdminBinding;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity_admin extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener{

    private static final int PIN_CRASH = 1;
    private static final int PIN_LOST = 2;
    private static final int PIN_WORK = 3;
    private static final int PIN_HELP = 4;
    private int tagCount = 0;
    private String imagePath;
    private String location;
    private int pin_type=0;

    private GoogleMap mMap;
    private BottomSheetBehavior<View> infoBottomSheetBehavior;
    private GestureDetector gestureDetector;
    private Marker userMarker;
    FirebaseFirestore mFirebaseStore;
    public ActivityMapsAdminBinding binding;
    Dialog AddDialog; //의견추가하는 다이얼로그라서 add라고 이름지음

    public HashMap<Marker, MarkerData> markerDataMap = new HashMap<>();

    //public int addPersonCount= 0;
    //public int pinType = 0;
    private int pin_height = 110;
    private int pin_width = 90;
    private boolean markerRemove = false;
    private String userNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent getintent = getIntent();
        userNum = getintent.getStringExtra("userNum");
        Log.d("PBY", "userNum : " + userNum);
        super.onCreate(savedInstanceState);

        binding = ActivityMapsAdminBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mFirebaseStore = FirebaseFirestore.getInstance(); //firebase 인스턴스 생성

        if (mapFragment != null){
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        loadMarkersFromFirestore();
        //숭실대 위치를 기준으로 첫 세팅
        LatLng soongsil_univ = new LatLng(37.4963, 126.9572); //숭실대 위치
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(soongsil_univ, 17));

        Log.d("eun","movecamara");
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker userMarker) {
                Log.d("eun","clickmarker");
                onMarkerClick2(userMarker);
                return true;
            }
        });

        // BottomSheet 초기화
        View infoBottomSheet = findViewById(R.id.admin_bottom_sheet);
        infoBottomSheetBehavior = BottomSheetBehavior.from(binding.adminBottomSheet);
        infoBottomSheetBehavior.setPeekHeight(200);
        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // 리소스 초기화
        clear();
        //세팅 버튼 클릭 시 Setting 액티비티 실행
        binding.setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity_admin.this, Setting_admin.class)
                        .putExtra("userNum", userNum);
                startActivity(intent);
            }
        });

        //클릭 시 신고내역 리사이클러 액티비티 실행
        binding.helpListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapsActivity_admin.this, ReportList.class);
                startActivity(intent); ////////////////////////////////////////////////////////신고내역 리사이클러뷰로 가야함
            }
        });
    }

    public boolean onMarkerClick2(Marker marker) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
        mMap.animateCamera(center);
        Log.d("eun","clickmarker1");
        DocumentReference thisMarkerDoc = mFirebaseStore.collection("fixxu").document(getDocumentNameFromMarker(marker));
        thisMarkerDoc.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // 저장된 이미지 URL, 위치, 상세내역 가져오기
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            String location = documentSnapshot.getString("location");
                            String description = documentSnapshot.getString("description");
                            Log.d("eun","clickmarker2");

                            if (imageUrl != null && !imageUrl.isEmpty() && location!=null && description!=null) {
                                // Glide를 사용하여 이미지 로드
                                Glide.with(MapsActivity_admin.this)
                                        .load(imageUrl)
                                        .into(binding.adminSingoPicture);
                                binding.adminBottomsheetDiscription.setText(description);
                                binding.adminBottomsheetLocation.setText(location);
                                Log.d("eun","clickmarker4");
                                infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                            } else {
                                // 이미지 URL이 없는 경우 기본 이미지 설정
                                binding.adminSingoPicture.setImageResource(R.drawable.pin_pressed);
                            }
                        } else {
                            Toast.makeText(MapsActivity_admin.this, "문서를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapsActivity_admin.this, "문서 읽기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        //////버튼 선택한 거
        markerRemove = false;
        binding.adminPinRepair.setOnClickListener(this);
        binding.adminPinComplete.setOnClickListener(this);
        binding.adminPinSolve.setOnClickListener(this);
        binding.adminPinUmm.setOnClickListener(this);


        binding.adminUpload.setOnClickListener(new View.OnClickListener() { // 업로드 버튼
            @Override
            public void onClick(View view) {
                String documentName = getDocumentNameFromMarker(marker);
                if (documentName == null || documentName.isEmpty()) {
                    Log.d("eun", "Invalid document name from marker.");
                }
                DocumentReference userRef = mFirebaseStore.collection("users").document(userNum);

                DocumentReference thisMarkerDoc = mFirebaseStore.collection("fixxu").document(documentName);
                thisMarkerDoc.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        if(markerRemove == true){
                            // 마커 아이콘 업데이트
                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.blue_pin, 80, 100)));
                            // Firebase에 pinType 업데이트
                            thisMarkerDoc.update("pinType", 0);

                            mFirebaseStore.collection("users")
                                    .whereEqualTo("userNum", userNum)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            if (!task.getResult().isEmpty()) {
                                                // 문서가 존재하면 report 필드를 +1 업데이트
                                                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                                                int currentReportValue = document.getLong("report").intValue();
                                                userRef.update("report", currentReportValue + 1)
                                                        .addOnSuccessListener(aVoid -> {
                                                            Log.d("Firestore", "Report field updated successfully");
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Log.w("Firestore", "Error updating report field", e);
                                                        });
                                            }
                                        } else {
                                            Log.d("Firestore", "Error getting documents: ", task.getException());
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(MapsActivity_admin.this, "해당 마커의 문서를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    }
                }).addOnFailureListener(e -> {
                    Log.d("eun", "문서 가져오기 실패: " + e.getMessage(), e);
                    Toast.makeText(MapsActivity_admin.this, "문서 가져오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

                infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });
        clear();
        return true;
    }

    public void onClick(View v){
        clear_pin();
        if(v == binding.adminPinRepair){
            binding.adminPinRepair.setBackground(getResources().getDrawable(R.drawable.pin_pressed));
            markerRemove = true;}
        else if(v == binding.adminPinComplete){
            binding.adminPinComplete.setBackground(getResources().getDrawable(R.drawable.pin_pressed));
            markerRemove = true;}
        else if(v== binding.adminPinSolve){
            binding.adminPinSolve.setBackground(getResources().getDrawable(R.drawable.pin_pressed));
            markerRemove = true;}
        else if(v==binding.adminPinUmm){
            binding.adminPinUmm.setBackground(getResources().getDrawable(R.drawable.pin_pressed));
            markerRemove = true;
        }
    }

    public String getDocumentNameFromMarker(Marker marker) {
        // 위도와 경도를 문자열로 변환하고 소수점을 언더바로 대체=> 점이나 슬래쉬 등은 firebase문서 이름으로 설정 불가능
        String latitude = String.valueOf(marker.getPosition().latitude).replace('.', '_');
        String longitude = String.valueOf(marker.getPosition().longitude).replace('.', '_');

        // 위도와 경도를 조합하여 문서 이름 생성
        String documentName = latitude + "_" + longitude;
        return documentName;
    }

    private void clear(){
        clear_pin();
        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        binding.adminBottomsheetDiscription.setText("");
        binding.adminBottomsheetLocation.setText("");
    }

    private void clear_pin() {
        pin_type = 0;
        binding.adminPinRepair.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
        binding.adminPinComplete.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
        binding.adminPinSolve.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
        binding.adminPinUmm.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
    }
    private Bitmap getResizedBitmap(int drawableRes, int width, int height) {
        // Drawable 리소스를 Bitmap으로
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        // Bitmap 크기 조절
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }
    private void loadMarkersFromFirestore() {
        CollectionReference markerCollection = mFirebaseStore.collection("fixxu");

        markerCollection.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        int i = 0;
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            try {
                                // Firestore 문서 데이터를 읽어옴
                                Log.i("eun", "Firestore에서 데이터 로드 중...");
                                Map<String, Object> data = document.getData();

                                // 데이터 변환 및 검증
                                double latitude = data.containsKey("latitude") ? (double) data.get("latitude") : 0.0;
                                double longitude = data.containsKey("longitude") ? (double) data.get("longitude") : 0.0;
                                int pinType = data.containsKey("pinType") ? ((Long) data.get("pinType")).intValue() : 0; // Firestore에서 Long으로 저장됨

                                LatLng position = new LatLng(latitude, longitude);


                                if (pinType == 1){
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(position)
                                            .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.pink_pin, 80, 100)))
                                            .draggable(true) // 필요 시 마커를 드래그 가능하게 설정
                                    );
                                }
                                else if(pinType == 2){
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(position)
                                            .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.yellow_pin, 80, 100)))
                                            .draggable(true) // 필요 시 마커를 드래그 가능하게 설정
                                    );
                                }
                                else if(pinType == 3){
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(position)
                                            .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.blue_pin, 80, 100)))
                                            .draggable(true) // 필요 시 마커를 드래그 가능하게 설정
                                    );
                                }
                                else {
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(position)
                                            .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.gray_pin, 80, 100)))
                                            .draggable(true) // 필요 시 마커를 드래그 가능하게 설정
                                    );
                                }
                                i++;
                                Log.i("eun", "마커 추가 완료: " + i + "개");
                            } catch (Exception e) {
                                Log.e("eun", "마커 로드 중 오류 발생: " + e.getMessage(), e);
                            }
                        }
                        Log.d("eun", "모든 마커가 지도에 표시되었습니다.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("eun", "Firestore에서 마커 불러오기 실패: " + e.getMessage(), e);
                        Toast.makeText(MapsActivity_admin.this, "마커 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private Bitmap createBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
    private View createCustomMarkerView(int badgeCnt, int pin){
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View markerView = inflater.inflate(R.layout.custom_marker, null);
        ImageView markerImage = markerView.findViewById(R.id.marker_image);
        switch (pin) {
            case 1:
                markerImage.setImageResource(R.drawable.pink_pin); // pin_type 1에 해당하는 이미지
                break;
            case 2:
                markerImage.setImageResource(R.drawable.yellow_pin); // pin_type 2에 해당하는 이미지
                break;
            case 3:
                markerImage.setImageResource(R.drawable.blue_pin); // pin_type 3에 해당하는 이미지
                break;
            default:
                markerImage.setImageResource(R.drawable.gray_pin); // 기본 이미지
                break;
        }
        TextView badge = markerView.findViewById(R.id.marker_badge);
        if (badgeCnt > 0) {
            badge.setVisibility(View.VISIBLE);
            badge.setText(String.valueOf(badgeCnt));
        } else {
            badge.setVisibility(View.GONE);
        }


        return markerView;
    }
}