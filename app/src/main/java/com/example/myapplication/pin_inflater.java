//package com.example.myapplication;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Environment;
//import android.provider.MediaStore;
//import android.renderscript.ScriptGroup;
//import android.util.Log;
//import android.view.GestureDetector;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.ImageView;
//
//import androidx.activity.result.ActivityResult;
//import androidx.activity.result.ActivityResultCallback;
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.core.content.FileProvider;
//
//import com.example.myapplication.databinding.ActivityPinInflaterBinding;
//import com.google.android.material.bottomsheet.BottomSheetBehavior;
//
//import java.io.File;
//import java.io.IOException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//
//public class pin_inflater extends AppCompatActivity implements View.OnClickListener{
//    private static final int PIN_Delete = 1;
//    private static final int PIN_REPAIR = 2;
//    private static final int PIN_COMPLETE = 3;
//    private static final int PIN_SOLVE = 4;
//    private ActivityPinInflaterBinding binding;
//    private BottomSheetBehavior<View> infoBottomSheetBehavior;
//    private GestureDetector gestureDetector;
//    private String imagePath;
//    private static final int REQUEST_IMAGE_CAPTURE = 672;
//    private String  imageFilePath;
//    private Uri photoUri;
//    private int pin_type;
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        binding = ActivityPinInflaterBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//
//        binding.pinRepair.setOnClickListener(this);
//        binding.pinDelete.setOnClickListener(this);
//        binding.pinSolve.setOnClickListener(this);
//        binding.pinComplete.setOnClickListener(this);
//
//        //권한 요청
//        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
//
//        //초기화
//        infoBottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
//        infoBottomSheetBehavior.setPeekHeight(200);
//        clear();
//
//        ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
//                new ActivityResultCallback<ActivityResult>() {
//                    @Override
//                    public void onActivityResult(ActivityResult result) {
//                        if (result.getResultCode() == RESULT_OK){
//                            // binding.image.
//                            // image 표시
//                            imagePath = result.getData().getStringExtra("image_path");
//                            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
//
//                            binding.image.setImageBitmap(bitmap);
//                            binding.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                            binding.imageButton.setVisibility(View.INVISIBLE);
//                            binding.image.setVisibility(View.VISIBLE);
//                        }
//                    }
//                });
////        gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
////            @Override
////            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
////                Log.d("PBY","onFling");
////                if(e2.getY()<e1.getY()) {
////                    if (infoBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
////                        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
////                    }
////                }
////                return super.onFling(e1, e2, velocityX, velocityY);
////            }
////        });
//
////         image 버튼 클릭시 사진 입력.
//        binding.imageButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                launcher.launch(new Intent(pin_inflater.this,CameraActivity.class));
////                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
////                if(intent.resolveActivity(getPackageManager())!=null){
////                    File photoFile = null;
////                    try {//파일 쓰기를 할 때는 항상 try catch 문을 적어야 한다는데 왜 그러는지 공부할 것.
////                        photoFile = createImageFile();
////                    }
////                    catch (IOException e) {
////                    }
////                    if(photoFile != null){
////                        photoUri = FileProvider.getUriForFile(getApplicationContext()
////                                ,getPackageName()
////                                ,photoFile);
////                        intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
////                        startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);
////                    }
////                }
//            }
//        });
//        // 업로드 버튼 클릭시 정보 저장 및 초기화
//        // 현재 텍스트 저장과 초기화만 되어있음.
//        // 이미지 저장, 초기화 해야 함.
//        binding.upload.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d("PBY","detail : " + binding.detailEdit.getText().toString());
//                Log.d("PBY","image_path : " + imagePath);
//                Log.d("PBY","pin type : " + pin_type);
//
//                infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                clear();
//            }
//        });
//    }
//    public void onClick(View v){
//        clear_pin();
//
//        if(v == binding.pinDelete){
//            binding.pinDelete.setBackgroundColor(ContextCompat.getColor(pin_inflater.this
//                    , R.color.button_pressed_color_pink));
//            pin_type=PIN_Delete;}
//        else if(v == binding.pinComplete){
//            binding.pinComplete.setBackgroundColor(ContextCompat.getColor(pin_inflater.this
//                    , R.color.button_pressed_color_blue));
//            pin_type=PIN_COMPLETE;}
//        else if(v== binding.pinSolve){
//            binding.pinSolve.setBackgroundColor(ContextCompat.getColor(pin_inflater.this
//                    , R.color.button_pressed_color_blue));
//            pin_type=PIN_SOLVE;}
//        else if(v==binding.pinRepair){
//            binding.pinRepair.setBackgroundColor(ContextCompat.getColor(pin_inflater.this
//                    , R.color.button_pressed_color_blue));
//            pin_type=PIN_REPAIR;
//        }
//    }
//    private void clear(){
//        clear_pin();
//        infoBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//        binding.detailEdit.setText("");
//        binding.detailEdit.clearFocus();
//        binding.imageButton.setVisibility(View.VISIBLE);
//        binding.image.setVisibility(View.INVISIBLE);
//    }
//    private void clear_pin(){
//        pin_type=0;
//        binding.pinDelete.setBackgroundColor(ContextCompat.getColor(pin_inflater.this
//                , R.color.button_default_color));
//        binding.pinComplete.setBackgroundColor(ContextCompat.getColor(pin_inflater.this
//                , R.color.button_default_color));
//        binding.pinSolve.setBackgroundColor(ContextCompat.getColor(pin_inflater.this
//                , R.color.button_default_color));
//        binding.pinRepair.setBackgroundColor(ContextCompat.getColor(pin_inflater.this
//                , R.color.button_default_color));
//
//    }
//
//
//// 바텀시트에 정보를 변경하면, 그거를 받아서 저장하는,
//    // 정보 저장.
////    private File createImageFile() throws IOException{
////            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
////                    .format(new Date());
////            String imageFileName = "TEST_"+timeStamp+"_";
////            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
////            File image = File.createTempFile(
////                    imageFileName
////                    ,".jpg"
////                    ,storageDir);
////            imageFilePath = image.getAbsolutePath();
////            return image;
////
////    }
//}