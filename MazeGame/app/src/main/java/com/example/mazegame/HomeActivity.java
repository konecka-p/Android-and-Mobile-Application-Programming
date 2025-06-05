package com.example.mazegame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    Button btnGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btnGenerator = findViewById(R.id.btn_generator);
    }

    public void startGenerator(View v) {
        Intent intent = new Intent(HomeActivity.this, GeneratorActivity.class);
        startActivity(intent);
    }

    public void setParameters(View v) {
        Intent intent = new Intent(HomeActivity.this, ParametersActivity.class);
        startActivity(intent);
    }
}

