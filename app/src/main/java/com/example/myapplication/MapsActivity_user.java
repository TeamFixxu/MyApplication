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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import com.example.myapplication.MarkerData;
import com.example.myapplication.CameraActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity_user extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener{

    private static final int PIN_CRASH = 1;
    private static final int PIN_LOST = 2;
    private static final int PIN_WORK = 3;
    private static final int PIN_HELP = 4;
    private String imagePath;
    private Uri fileUri;
    private String location;
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

    private String studentNum;
    private PinAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent getintent = getIntent();
        studentNum = getintent.getStringExtra("userNum");

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
        //카메라
        ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK){

                            imagePath = result.getData().getStringExtra("image_path");
                            fileUri = Uri.fromFile(new File(imagePath));
                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                            binding.image.setImageBitmap(bitmap);
                            binding.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            binding.imageButton.setVisibility(View.INVISIBLE);
                            binding.image.setVisibility(View.VISIBLE);
                        }
                    }
                });
        ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    // Process selected image from gallery
                    fileUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                        binding.image.setImageBitmap(bitmap);
                        binding.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        binding.imageButton.setVisibility(View.INVISIBLE);
                        binding.image.setVisibility(View.VISIBLE);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        binding.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] options = {"카메라로 찍기", "갤러리에서 선택"};

                AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity_user.this);
                builder.setTitle("이미지 선택");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            cameraLauncher.launch(new Intent(MapsActivity_user.this,CameraActivity.class));

                        } else if (which == 1) {
                            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            galleryLauncher.launch(intent);

                        }
                    }
                });
                builder.show();


            }
        });
        //카메라 끝

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
        Log.i("eun","load 시작");
        loadMarkersFromFirestore();

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
                Intent intent = new Intent(MapsActivity_user.this, Setting.class)
                        .putExtra("userNum", studentNum);
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

        ////////////////////태그 태그 태그 태그
        CollectionReference tagsRef = mFirebaseStore.collection("tags");
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
        /////////////태그 끝
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
        DocumentReference tagDocRef = mFirebaseStore.collection("tags").document(tagName);

        tagDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // 태그가 존재하면 usageCount 증가
                long usageCount = documentSnapshot.getLong("usageCount") != null ? documentSnapshot.getLong("usageCount") : 0;
                mFirebaseStore.collection("tags").document(tagName).update("usageCount", usageCount + 1);
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
    public void addMarker(LatLng latLng) {        //addMarker로 이름 바꿈 원래 addCustomMarker
        thisMarker = null; //초기값 설정

        thisMarker = mMap.addMarker(new MarkerOptions()
                .position(latLng).draggable(true)
                .icon(BitmapDescriptorFactory.fromBitmap(getResizedBitmap(R.drawable.gray_pin, 80, 100))));

        binding.pinCrash.setOnClickListener(this);
        binding.pinLost.setOnClickListener(this);
        binding.pinHelp.setOnClickListener(this);
        binding.pinWork.setOnClickListener(this);

        CameraUpdate center = CameraUpdateFactory.newLatLng(latLng);
        mMap.animateCamera(center);

        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        binding.upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //값을 받아야함.
                MarkerData data = new MarkerData();

                data.setPinType(pin_type);
                data.setLocation(location);
                data.setIsCreator(false);
                data.setDescription(binding.detailEdit.getText().toString());
                data.setAddPersonCount(0);

                updateSingoCnt(studentNum); /////////사용자 신고횟수, 포인트 변경
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

        DocumentReference thisMarkerDoc = mFirebaseStore.collection("fixxu").document(getDocumentNameFromMarker(marker));

        // Firestore에서 클릭한 마커 신고 데이터들 가져오기
        thisMarkerDoc.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // 저장된 이미지 URL, 위치, 상세내역 가져오기
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            String location = documentSnapshot.getString("location");
                            String description = documentSnapshot.getString("description");


                            if (imageUrl != null && !imageUrl.isEmpty() && location!=null && description!=null) {
                                // Glide를 사용하여 이미지 로드
                                Glide.with(MapsActivity_user.this)
                                        .load(imageUrl)
                                        .into(dialogBinding.singoPicture);
                                dialogBinding.descriptionDialogText.setText(description);
                                dialogBinding.locationDialogText.setText(location);
                            } else {
                                // 이미지 URL이 없는 경우 기본 이미지 설정
                                dialogBinding.singoPicture.setImageResource(R.drawable.pin_pressed);
                            }
                        } else {
                            Toast.makeText(MapsActivity_user.this, "문서를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapsActivity_user.this, "문서 읽기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        AddDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
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

        dialogBinding.dialogCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddDialog.dismiss();
            }
        });
        return true;
    }

    private void saveMarkerToFirestore(Marker marker, MarkerData markerData) {
        CollectionReference markerColleciton = mFirebaseStore.collection("fixxu");
       //데이터 준비
        int state = 1;
        Map<String, Object> data = new HashMap<>();
        data.put("addPersonCount", markerData.getAddPersonCount());
        data.put("isCreator", markerData.getIsCreator());
        data.put("location", markerData.getLocation());
        data.put("description", markerData.getDescription());
        data.put("pinType", markerData.getPinType());
        data.put("latitude", marker.getPosition().latitude);
        data.put("longitude", marker.getPosition().longitude);
        data.put("state", state);
        //Task<Void> set = markers.document(marker.getId()).set(data); //마커 저장하는데 시간이 걸릴 수도 있으니 이렇게 쓰라고 여기서 추천함.
        //markers.document(marker.getID()).set(data); //원래 코드
        Log.d("eun", "data firebase에 저장함.");


        // Firestore에 데이터 저장 (문서 ID는 자동 생성)
        String filename = getDocumentNameFromMarker(marker);
        markerColleciton.document(filename)
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
        uploadImageToStorage(fileUri,filename);
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
        pin_type = 0;
        binding.pinCrash.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
        binding.pinLost.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
        binding.pinWork.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
        binding.pinHelp.setBackground(getResources().getDrawable(R.drawable.button_not_pressed));
    }

    public String getDocumentNameFromMarker(Marker marker) {
        // 위도와 경도를 문자열로 변환하고 소수점을 언더바로 대체=> 점이나 슬래쉬 등은 firebase문서 이름으로 설정 불가능
        String latitude = String.valueOf(marker.getPosition().latitude).replace('.', '_');
        String longitude = String.valueOf(marker.getPosition().longitude).replace('.', '_');

        // 위도와 경도를 조합하여 문서 이름 생성
        String documentName = latitude + "_" + longitude;
        return documentName;
    }

    private Bitmap getResizedBitmap(int drawableRes, int width, int height) {
        // Drawable 리소스를 Bitmap으로
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), drawableRes);
        // Bitmap 크기 조절
        return Bitmap.createScaledBitmap(originalBitmap, width, height, false);
    }

    private Bitmap createBitmapFromView(View view) {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void uploadImageToStorage(Uri fileUri,String filename) {

        String filename2 = filename;
        String fileNameTemp = "images/" + System.currentTimeMillis() + ".jpg";

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(fileNameTemp);

        storageRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String downloadUrl = uri.toString();
                                saveImageUrlToFirestore(downloadUrl,filename2);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MapsActivity_user.this, "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //다운로드 url을 firestore 해당 마커(filename을 통해)의 필드에 추가.
    private void saveImageUrlToFirestore(String imageUrl,String filename) {
        DocumentReference documentReference = mFirebaseStore.collection("fixxu").document(filename);

        Map<String, Object> imageData = new HashMap<>();
        imageData.put("imageUrl", imageUrl);
        documentReference.set(imageData, SetOptions.merge());
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
                                String description = data.containsKey("description") ? (String) data.get("description") : "";
                                int pinType = data.containsKey("pinType") ? ((Long) data.get("pinType")).intValue() : 0; // Firestore에서 Long으로 저장됨
                                boolean isCreator = data.containsKey("isCreator") ? (boolean) data.get("isCreator") : false;
                                int addPersonCount = data.containsKey("addPersonCount") ? ((Long) data.get("addPersonCount")).intValue() : 0;

                                LatLng position = new LatLng(latitude, longitude);

                                // 커스텀 마커 뷰 생성
                                View markerView = createCustomMarkerView(addPersonCount, pinType);

                                // Google Maps에 마커 추가
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(position)
                                        .icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromView(markerView)))
                                        .draggable(true) // 필요 시 마커를 드래그 가능하게 설정
                                );
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
                        Toast.makeText(MapsActivity_user.this, "마커 불러오기 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateSingoCnt(String userNum){

        CollectionReference usersFirestore = mFirebaseStore.collection("users");
        usersFirestore
                .whereEqualTo("userNum", userNum)  // userNum이 일치하는 사용자 찾기
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (!querySnapshot.isEmpty()) {
                            // 첫 번째 일치하는 문서 가져오기
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            DocumentReference documentRef = document.getReference();
                            Long pointLong = document.getLong("point");
                            pointLong += 10;
                            documentRef.update("point", pointLong);
                            Log.d("eun",userNum + "포인트 + 10");

                            Long reportLong = document.getLong("report");
                            reportLong++;
                            documentRef.update("report", reportLong);
                            Log.d("eun",userNum + "신고횟수 + 1");

                        } else {
                            // 일치하는 문서가 없을 경우
                            Log.d("Firestore", "No documents found with the specified userNum");
                        }
                    } else {
                        Log.d("Firestore", "Query failed with ", task.getException());
                    }
                });
    }

}