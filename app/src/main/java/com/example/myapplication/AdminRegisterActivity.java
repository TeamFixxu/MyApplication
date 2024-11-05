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

public class AdminRegisterActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth; // 파이어 베이스 인증
    private DatabaseReference mDatabaseRef; //실시간 데이터 베이스
    private EditText mEtManagerNum, mEtPwd, mEtPwdConfirm, mEtPhoneNum;
    private Button mBtnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_membership);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mEtManagerNum = findViewById(R.id.editTextStudentId);
        mEtPwd = findViewById(R.id.editTextPassword);
        mBtnRegister = findViewById(R.id.btnSignUp);
        mEtPwdConfirm = findViewById(R.id.editTextPasswordConfirm);
        mEtPhoneNum = findViewById(R.id.editTextPhone);



        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strManagerNum = mEtManagerNum.getText().toString();
                String strPwd = mEtPwd.getText().toString();
                String strPhone = mEtPhoneNum.getText().toString();
                String strPwdConfirm = mEtPwdConfirm.getText().toString();

                // 비밀번호와 확인 비밀번호가 일치하는지 확인
                if (!strPwd.equals(strPwdConfirm)) {
                    Toast.makeText(AdminRegisterActivity.this, "비밀번호가 일치하지 않습니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show();
                    mEtPwdConfirm.setText(""); // 비밀번호 확인란 초기화
                    mEtPwdConfirm.requestFocus(); // 비밀번호 확인란에 포커스 이동
                    return;
                }

                String email = mEtManagerNum + "@myapp.com";

                mFirebaseAuth.createUserWithEmailAndPassword(email, strPwd).addOnCompleteListener(AdminRegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            UserAccount account = new UserAccount();
                            account.setIdToken(firebaseUser.getUid());
                            account.setManagerId(strManagerNum); // 관리자번호 저장
                            account.setPhoneNum(strPhone);
                            account.setPassword(strPwd);

                            mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(account);
                            Toast.makeText(AdminRegisterActivity.this, "회원가입에 성공했습니다.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AdminRegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(AdminRegisterActivity.this, "회원가입에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });




    }
}