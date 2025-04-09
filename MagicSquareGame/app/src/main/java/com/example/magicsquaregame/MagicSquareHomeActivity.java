package com.example.magicsquaregame;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class MagicSquareHomeActivity extends AppCompatActivity {

    EditText gameLevel;
    TextView lastGameResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magic_square_home);

        gameLevel = findViewById(R.id.et_game_level);
        lastGameResult = findViewById(R.id.tv_last_game_result);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            boolean success = data.getBooleanExtra("gameResult", false);
            if (success) {
                lastGameResult.setText("Last game result: Success!");
                lastGameResult.setTextColor(Color.GREEN);
            } else {
                lastGameResult.setText("Last game result: Game failed");
                lastGameResult.setTextColor(Color.RED);
            }
        }
    }

    public void startGame(View v) {
        String levelStr = gameLevel.getText().toString();
        if (levelStr.isEmpty()) return;
        int level = Integer.parseInt(levelStr);
        if (level < 1 || level > 9) {
            return;
        }
        Intent intent = new Intent(MagicSquareHomeActivity.this, MagicSquareActivity.class);
        intent.putExtra("level", level);
        startActivityForResult(intent, 1);
    }

}