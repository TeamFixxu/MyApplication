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

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth; // 파이어 베이스 인증
    //private DatabaseReference mDatabaseRef; //실시간 데이터 베이스
    private EditText mEtIdNum, mEtPwd; //매니저넘도 학번이랑 비슷한 구조로 가져가야겟다. 아이디로 받고, 넘버 확인해서 로그인시 관리자뷰, 사용자 뷰 구분 필요
    private Button mBtnRegister, mBtnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        //mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mEtIdNum = findViewById(R.id.button);
        mEtPwd = findViewById(R.id.button2);

        mBtnLogin = findViewById(R.id.button3);
        mBtnRegister = findViewById(R.id.button4);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //로그인처리
                String strNum = mEtIdNum.getText().toString();
                String PwdNum = mEtPwd.getText().toString();
                String  email = strNum + "@myapp.com";

                mFirebaseAuth.signInWithEmailAndPassword(email, PwdNum).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Intent intent = new Intent(LoginActivity.this, MapsActivity_user.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(LoginActivity.this, "로그인에 성공했습니다.",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(LoginActivity.this, "로그인에 실패했습니다.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, UserCheckActivity.class);
                startActivity(intent);
            }
        });



    }
}