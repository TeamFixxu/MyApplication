package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.databinding.ActivityAdminListBinding;
import com.example.myapplication.databinding.ActivityAdminMembershipBinding;
import com.example.myapplication.databinding.ActivityMembershipBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class
AdminRegisterActivity extends AppCompatActivity {
    private ActivityAdminMembershipBinding binding;

    private FirebaseAuth mFirebaseAuth; // 파이어 베이스 인증
    //private DatabaseReference mDatabaseRef; //실시간 데이터 베이스
    FirebaseFirestore mFirebaseStore;
    private EditText mEtManagerNum,mEtManagerName,mEtPwd,mEtConfirmPwd, mEtPhoneNum;
    private Button mBtnRegister;

    private Spinner spinnerLocation;
    private String[] locations;
    private boolean[] selectedLocations;
    private ArrayList<Integer> selectedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminMembershipBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Action Bar 숨기기
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStore = FirebaseFirestore.getInstance();

        mEtManagerNum = findViewById(R.id.editTextStudentId);

        mEtPwd = findViewById(R.id.editTextPassword);
        mEtManagerName = findViewById(R.id.editTextManagerName);
        mBtnRegister = findViewById(R.id.btnSignUp);
        mEtConfirmPwd = findViewById(R.id.editTextPasswordConfirm);
        mEtPhoneNum = findViewById(R.id.editTextPhone);

        Button buttonShowDialog = findViewById(R.id.buttonShowDialog);

        // 체크박스 목록 데이터
        String[] items = {"베어드홀","숭덕경상관",
                "문화관","미래관","안익태기념관","형남공학관",
                "교육관","백마관","한경직기념관","벤처중소기업센터",
                "신양관","진리관","조만식기념관",
                "한국기독교박물관","중앙도서관","연구관",
                "창신관","Global Brain Hall","Residence Hall",
                "전산관","정보과학관","웨스트민스터홀","학생회관","창의관","대운동장"
        };
        boolean[] checkedItems = new boolean[items.length]; // 초기 선택 상태
        ArrayList<String> selectedItems = new ArrayList<>(); // 선택된 항목 저장용

        buttonShowDialog.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminRegisterActivity.this);
            builder.setTitle("관활 구역 선택");

            builder.setMultiChoiceItems(items, checkedItems, (dialog, which, isChecked) -> {
                if (isChecked) {
                    // 항목이 선택되면 추가
                    selectedItems.add(items[which]);
                } else {
                    // 항목이 선택 해제되면 제거
                    selectedItems.remove(items[which]);
                }
            });

            // 확인 버튼
            builder.setPositiveButton("OK", (dialog, which) -> {
                Log.d("관할 구역을 선택하셨습니다.", selectedItems.toString());
            });

            // 취소 버튼
            builder.setNegativeButton("취소", (dialog, which) -> dialog.dismiss());

            builder.create().show();
        });


        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("mBtnRegister", "onClick");
                String strManagerNum = mEtManagerNum.getText().toString();
                String strPwd = mEtPwd.getText().toString();
                String strPhone = mEtPhoneNum.getText().toString();
                String strConfirmPwd = mEtConfirmPwd.getText().toString();
                String name = mEtManagerName.getText().toString();

                // 비밀번호와 확인 비밀번호가 일치하는지 확인
                if (!strPwd.equals(strConfirmPwd)) {
                    Toast.makeText(AdminRegisterActivity.this, "비밀번호가 일치하지 않습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                    mEtConfirmPwd.setText(""); // 비밀번호 확인란 초기화
                    mEtConfirmPwd.requestFocus(); // 비밀번호 확인란에 포커스 이동
                    return;
                }

                // 학번을 이메일 형식으로 변환하여 Firebase Auth에 전달
                String email = strManagerNum + "@myapp.com";

                mFirebaseAuth.createUserWithEmailAndPassword(email, strPwd)
                        .addOnCompleteListener(AdminRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("mBtnRegister", "이메일 패스워드 함수 실행");
                        if(task.isSuccessful() && mFirebaseAuth.getCurrentUser() != null){
                            FirebaseUser firebaseUser = task.getResult().getUser();

                            if(firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                Log.d("mBtnRegister", "사용자 인증 성공 : " + userId);

                                Map<String, Object> manager = new HashMap<>();
                                manager.put("Manager Num", strManagerNum);
                                manager.put("name", name);
                                manager.put("Phone Num", strPhone);
                                manager.put("role", "manager");
                                manager.put("report", 0);
                                manager.put("point", 0);
                                manager.put("regions", selectedItems);
                                Log.d("mBtnRegister", "맵");

                                mFirebaseStore.collection("users")
                                        .document(userId)
                                        .set(manager)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("mBtnRegister", "스토어 저장");
                                        Toast.makeText(AdminRegisterActivity.this, "회원가입 완료, 사용자정보 저장 성공", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("mBtnRegister", "스토어 저장 실패" + e.getMessage(),e);
                                        Log.d("mBtnRegister", "Manager Num: " + strManagerNum);
                                        Log.d("mBtnRegister", "Phone Num: " + strPhone);
                                        Toast.makeText(AdminRegisterActivity.this, "회원가입 완료, 사용자정보 저장실패" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            Intent intent = new Intent(AdminRegisterActivity.this, MapsActivity_admin.class);
                            intent.putExtra("userNum", strManagerNum);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("mBtnRegister", "회원가입 실패 원인: ", task.getException());
                            Toast.makeText(AdminRegisterActivity.this, "회원가입에 실패했습니다."+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}