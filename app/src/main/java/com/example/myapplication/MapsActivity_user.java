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
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.example.myapplication.MarkerData;
import com.example.myapplication.CameraActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity_user extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener{

    private static final int PIN_DELETE = 1;
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
    FirebaseFirestore mFirebaseStore;
    private ActivityMapsUserBinding binding;
    Dialog AddDialog; //의견추가하는 다이얼로그라서 add라고 이름지음

    public HashMap<Marker, MarkerData> markerDataMap = new HashMap<>();
    Marker thisMarker;

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

        mFirebaseStore = FirebaseFirestore.getInstance(); //firebase 인스턴스 생성



        //권한 요청
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);


        //초기화
        infoBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        infoBottomSheetBehavior.setPeekHeight(200);
        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

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

        //마커 추가
        mMap.setOnMapLongClickListener(this::addMarker);
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

    public void addMarker(LatLng latLng) {        //addMarker로 이름 바꿈 원래 addCustomMarker
        thisMarker = null; //초기값 설정

        thisMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng).draggable(true)
                .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.gray_pin, 80, 100))));

        binding.pinRepair.setOnClickListener(this);
        binding.pinDelete.setOnClickListener(this);
        binding.pinSolve.setOnClickListener(this);
        binding.pinComplete.setOnClickListener(this);

        CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
        mMap.animateCamera(center);

        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        //Map에 저장할 정보들을 바텀시트를 통해 가져와야함.
        //upload
        binding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //값을 받아야함.
                MarkerData data = new MarkerData();

                data.setPinType(pin_type);
                data.setLocation("정보과학관");
                data.setIsCreator(false);
                data.setDescription(binding.detailEdit.getText().toString());
                data.setAddPersonCount(0);

                View markerView = createCustomMarkerView(0,pin_type); //비활성화 마커에서 커스텀마커로 변경
                thisMarker.setIcon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerView)));

                markerDataMap.put(thisMarker, data);
                Log.d("eun","hashmap에 put 성공");
                saveMarkerToFirestore(thisMarker, data);
                Log.d("eun", "마커 database에 저장 성공");
                infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                Log.d("eun", "upload 됨");
                clear();
            }
        });


        //여기서 pintype을 위의 data의 값으로 대신 해줘야함. 테스트를 위해 그냥 pintype씀
/*
        if (thisMarker != null){
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            markerDataMap.put(thisMarker, data);  //must change
            Log.d("eun","map에 put 성공");
            saveMarkerToFirestore(thisMarker, data);     //must change
            Toast.makeText(this, "마커와 데이터가 저장됐습니다." + data.getDescription(), Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(this, "마커를 추가하지 못했습니다.", Toast.LENGTH_SHORT).show();
        }
*/
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
                DocumentReference thisMarkerDoc = mFirebaseStore.collection("fixxu").document(getDocumentNameFromMarker(marker));
                thisMarkerDoc.get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 문서가 존재하면 addPersonCount 값을 가져와서 +1
                        Long addPersonCount = documentSnapshot.getLong("addPersonCount");
                        Long pinType = documentSnapshot.getLong("pinType");
                        if (addPersonCount != null) {
                            int newCount = addPersonCount.intValue() + 1;
                            int thisPinType = pinType.intValue();

                            // 마커 아이콘 업데이트
                            updateMarkerIcon(marker, newCount, thisPinType); // pinType은 적절히 수정 필요

                            // Firebase에 업데이트
                            thisMarkerDoc.update("addPersonCount", newCount)
                                    .addOnSuccessListener(unused -> {
                                        Log.d("eun", "update complete! " + newCount);
                                        Toast.makeText(MapsActivity_user.this, "의견이 추가 됐습니다.", Toast.LENGTH_SHORT).show();
                                        AddDialog.dismiss(); // 다이얼로그 종료
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("eun", "update fail... " + e.getMessage(), e);
                                        Toast.makeText(MapsActivity_user.this, "마커 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        AddDialog.dismiss(); // 다이얼로그 종료
                                    });
                        } else {
                            Toast.makeText(MapsActivity_user.this, "addPersonCount 값을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                            AddDialog.dismiss(); // 다이얼로그 종료
                        }
                    } else {
                        Toast.makeText(MapsActivity_user.this, "해당 마커의 문서를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        AddDialog.dismiss(); // 다이얼로그 종료
                    }
                }).addOnFailureListener(e -> {
                    Log.d("eun", "문서 가져오기 실패: " + e.getMessage(), e);
                    Toast.makeText(MapsActivity_user.this, "문서 가져오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    AddDialog.dismiss(); // 다이얼로그 종료
                });
            }
        });
        return true;
    }


    private void saveMarkerToFirestore(Marker marker, MarkerData markerData) {
        CollectionReference markerColleciton = mFirebaseStore.collection("fixxu");
       //데이터 준비
        Map<String, Object> data = new HashMap<>();
        data.put("addPersonCount", markerData.getAddPersonCount());
        data.put("isCreator", markerData.getIsCreator());
        data.put("description", markerData.getDescription());
        data.put("pinType", markerData.getPinType());
        data.put("latitude", marker.getPosition().latitude);
        data.put("longitude", marker.getPosition().longitude);
        //Task<Void> set = markers.document(marker.getId()).set(data); //마커 저장하는데 시간이 걸릴 수도 있으니 이렇게 쓰라고 여기서 추천함.
        //markers.document(marker.getID()).set(data); //원래 코드
        Log.d("eun", "data firebase에 저장함.");

        // Firestore에 데이터 저장 (문서 ID는 자동 생성)
        markerColleciton.document(getDocumentNameFromMarker(marker))
            .set(data)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    Log.d("eun", "마커 스토어 저장 성공");
                    Toast.makeText(MapsActivity_user.this, "마커 저장 성공!", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("eun", "스토어 저장 실패: " + e.getMessage(), e);
                    Toast.makeText(MapsActivity_user.this, "마커 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    public void onClick(View v){
        clear_pin();

        if(v == binding.pinDelete){
            Log.d("KMM","onCLickPinDelete");
            binding.pinDelete.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                    , R.color.button_pressed_color_pink));
            pin_type=PIN_DELETE;}
        else if(v == binding.pinComplete){
            binding.pinComplete.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                    , R.color.button_pressed_color_blue));
            pin_type=PIN_COMPLETE;}
        else if(v== binding.pinSolve){
            binding.pinSolve.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                    , R.color.button_pressed_color_blue));
            pin_type=PIN_SOLVE;}
        else if(v==binding.pinRepair){
            binding.pinRepair.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this, R.color.button_pressed_color_blue));
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
        binding.pinDelete.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this, R.color.button_default_color));
        binding.pinComplete.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                , R.color.button_default_color));
        binding.pinSolve.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                , R.color.button_default_color));
        binding.pinRepair.setBackgroundColor(ContextCompat.getColor(MapsActivity_user.this
                , R.color.button_default_color));

    }

    public String getDocumentNameFromMarker(Marker marker) {
        // 위도와 경도를 문자열로 변환하고 소수점을 언더바로 대체=> 점이나 슬래쉬 등은 firebase문서 이름으로 설정 불가능
        String latitude = String.valueOf(marker.getPosition().latitude).replace('.', '_');
        String longitude = String.valueOf(marker.getPosition().longitude).replace('.', '_');

        // 위도와 경도를 조합하여 문서 이름 생성
        String documentName = latitude + "_" + longitude;
        return documentName;
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
    private void updateMarkerIcon(Marker marker, int newBadgeCnt, int pinType) {
        if (marker == null) return;

        // 새로운 커스텀 마커 뷰 생성
        View markerView = createCustomMarkerView(newBadgeCnt, pinType);

        if (markerView != null) {
            // 마커 뷰를 비트맵으로 변환
            Bitmap markerBitmap = createBitmapFromView(markerView);

            // 아이콘 업데이트
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(markerBitmap));

        }
    }
}