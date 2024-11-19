package com.example.myapplication;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.databinding.ActivityLoginBinding;
import com.example.myapplication.databinding.ActivityRegisterBinding;
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

//영주 파일 상 AdminRegister
public class RegisterActivity extends AppCompatActivity {

    public ActivityRegisterBinding binding;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore mFirebaseStore;

    //private EditText mEtIdNum, mEtPwd;
    //private Button mBtnRegister, mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //바인딩으로 변경함.
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStore = FirebaseFirestore.getInstance();

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("eun", "onClick");
                String strStudentNum = binding.editTextStudentId.getText().toString();
                String strPhone = binding.editTextPhone.getText().toString();
                String strPwd = binding.editTextPassword.getText().toString();
                String strConfirmPwd = binding.editTextPasswordConfirm.getText().toString();

                // 비밀번호와 확인 비밀번호가 일치하는지 확인
                if (!strPwd.equals(strConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                    binding.editTextPasswordConfirm.setText(""); // 비밀번호 확인란 초기화
                    binding.editTextPasswordConfirm.requestFocus(); // 비밀번호 확인란에 포커스 이동
                    return;
                }

                // 학번을 이메일 형식으로 변환하여 Firebase Auth에 전달
                String email = strStudentNum + "@myapp.com";

                mFirebaseAuth.createUserWithEmailAndPassword(email, strPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
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
                                Map<String, Object> student = new HashMap<>();
                                student.put("User Num", strStudentNum);
                                student.put("Phone Num", strPhone);
                                Log.d("mBtnRegister", "맵");

                                mFirebaseStore.collection("student").document(userId).set(student).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("mBtnRegister", "스토어 저장");
                                        Toast.makeText(RegisterActivity.this, "회원가입 완료, 사용자정보 저장 성공", Toast.LENGTH_SHORT).show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("mBtnRegister", "스토어 저장 실패");
                                        Toast.makeText(RegisterActivity.this, "회원가입 완료, 사용자정보 저장실패", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            //mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);
                            //Toast.makeText(RegisterActivity.this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MapsActivity_user.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e("mBtnRegister", "회원가입 실패 원인: ", task.getException());
                            Toast.makeText(RegisterActivity.this, "회원가입에 실패했습니다."+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}