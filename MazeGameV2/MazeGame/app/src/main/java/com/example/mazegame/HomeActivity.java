package com.example.mazegame;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

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

    public void startGame(View v) throws JSONException {
        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        String selectedName = prefs.getString("selected_maze_name", null);

        int[][] maze = MazeUtils.createMaze(prefs.getInt("maze_size", 20));
        if (selectedName != null) {
            String mazeJson = prefs.getString("maze_" + selectedName, null);
            if (mazeJson != null) {
                try {
                    maze = MazeUtils.jsonToMaze(mazeJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        String mazeJson = MazeUtils.mazeToJson(maze);
        Intent intent = new Intent(HomeActivity.this, GameActivity.class);
        intent.putExtra("maze_data", mazeJson);
        startActivity(intent);
    }
}
