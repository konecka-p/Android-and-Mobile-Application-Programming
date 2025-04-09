package com.example.magicsquaregame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    Button startGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        startGameButton = findViewById(R.id.startGame);
    }

    public void startGame(View v) {
        Intent intent = new Intent(HomeActivity.this, MagicSquareHomeActivity.class);
        startActivity(intent);
    }
}