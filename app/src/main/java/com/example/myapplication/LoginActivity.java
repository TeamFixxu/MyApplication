package com.example.myapplication;

import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView textView = findViewById(R.id.fixxu);
        textView.setText(Html.fromHtml(getString(R.string.fixxu), Html.FROM_HTML_MODE_LEGACY)); // HTML 형식 적용
    }
}