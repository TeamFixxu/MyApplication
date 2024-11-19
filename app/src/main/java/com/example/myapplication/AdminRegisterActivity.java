package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class
AdminRegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth; // 파이어 베이스 인증
    //private DatabaseReference mDatabaseRef; //실시간 데이터 베이스
    FirebaseFirestore mFirebaseStore;
    private EditText mEtManagerNum,mEtPwd,mEtConfirmPwd, mEtPhoneNum;
    private Button mBtnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);

        mFirebaseAuth = FirebaseAuth.getInstance();
        //mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mFirebaseStore = FirebaseFirestore.getInstance();

        mEtManagerNum = findViewById(R.id.editTextStudentId);
        mEtPwd = findViewById(R.id.editTextPassword);
        mBtnRegister = findViewById(R.id.btnSignUp);
        mEtConfirmPwd = findViewById(R.id.editTextPasswordConfirm);
        mEtPhoneNum = findViewById(R.id.editTextPhone);


        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("mBtnRegister", "onClick");
                String strManagertNum = mEtManagerNum.getText().toString();
                String strPwd = mEtPwd.getText().toString();
                String strPhone = mEtPhoneNum.getText().toString();
                String strConfirmPwd = mEtConfirmPwd.getText().toString();

                // 비밀번호와 확인 비밀번호가 일치하는지 확인
                if (!strPwd.equals(strConfirmPwd)) {
                    Toast.makeText(AdminRegisterActivity.this, "비밀번호가 일치하지 않습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                    mEtConfirmPwd.setText(""); // 비밀번호 확인란 초기화
                    mEtConfirmPwd.requestFocus(); // 비밀번호 확인란에 포커스 이동
                    return;
                }

                // 학번을 이메일 형식으로 변환하여 Firebase Auth에 전달
                String email = strManagertNum + "@myapp.com";

                mFirebaseAuth.createUserWithEmailAndPassword(email, strPwd).addOnCompleteListener(AdminRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("mBtnRegister", "이메일 패스워드 함수 실행");
                        if(task.isSuccessful() && mFirebaseAuth.getCurrentUser() != null){
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            Log.d("mBtnRegister", "만약 회원가입 성공이라면...");

                            if(firebaseUser != null) {
                                String userId = firebaseUser.getUid();
                                Log.d("mBtnRegister", "만약 파베 유저가 널이 아니라면...");

//                            UserAccount account = new UserAccount();
//                            account.setIdToken(firebaseUser.getUid());
//                            account.setStudentId(strStudentNum); // 학번 저장
//                            account.setPhoneNum(strPhone); //전화번호 저장
//                            account.setPassword(strPwd); //비밀번호 저장
                                Map<String, Object> manager = new HashMap<>();
                                manager.put("Manager Num", strManagertNum);
                                manager.put("Phone Num", strPhone);
                                manager.put("role", "manager");
                                Log.d("mBtnRegister", "맵");

                                mFirebaseStore.collection("manager").document(userId).set(manager).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("mBtnRegister", "스토어 저장");
                                        Toast.makeText(AdminRegisterActivity.this, "회원가입 완료, 사용자정보 저장 성공", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("mBtnRegister", "스토어 저장 실패");
                                        Toast.makeText(AdminRegisterActivity.this, "회원가입 완료, 사용자정보 저장실패", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            //mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);
                            //Toast.makeText(RegisterActivity.this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AdminRegisterActivity.this, MapsActivity_user.class);
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