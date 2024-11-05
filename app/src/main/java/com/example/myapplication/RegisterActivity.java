package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth; // 파이어 베이스 인증
    private DatabaseReference mDatabaseRef; //실시간 데이터 베이스
    private EditText mEtStudentNum,mEtPwd,mEtConfirmPwd, mEtPhoneNum;
    private Button mBtnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_membership);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mEtStudentNum = findViewById(R.id.editTextStudentId);
        mEtPwd = findViewById(R.id.editTextPassword);
        mBtnRegister = findViewById(R.id.btnSignUp);
        mEtConfirmPwd = findViewById(R.id.editTextPasswordConfirm);
        mEtPhoneNum = findViewById(R.id.editTextPhone);


        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strStudentNum = mEtStudentNum.getText().toString();
                String strPwd = mEtPwd.getText().toString();
                String strPhone = mEtPhoneNum.getText().toString();
                String strConfirmPwd = mEtConfirmPwd.getText().toString();

                // 비밀번호와 확인 비밀번호가 일치하는지 확인
                if (!strPwd.equals(strConfirmPwd)) {
                    Toast.makeText(RegisterActivity.this, "비밀번호가 일치하지 않습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                    mEtConfirmPwd.setText(""); // 비밀번호 확인란 초기화
                    mEtConfirmPwd.requestFocus(); // 비밀번호 확인란에 포커스 이동
                    return;
                }

                // 학번을 이메일 형식으로 변환하여 Firebase Auth에 전달
                String email = strStudentNum + "@myapp.com";

                mFirebaseAuth.createUserWithEmailAndPassword(email, strPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful() && mFirebaseAuth.getCurrentUser() != null){
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            UserAccount account = new UserAccount();
                            account.setIdToken(firebaseUser.getUid());
                            account.setStudentId(strStudentNum); // 학번 저장
                            account.setPhoneNum(strPhone); //전화번호 저장
                            account.setPassword(strPwd); //비밀번호 저장

                            mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);
                            Toast.makeText(RegisterActivity.this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "회원가입에 실패했습니다."+ task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }
}