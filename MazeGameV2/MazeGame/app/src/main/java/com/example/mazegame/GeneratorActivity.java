package com.example.mazegame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class GeneratorActivity extends AppCompatActivity {

    private ImageView mazeImageView;
    private EditText mazeNameInput;
    private int mazeSize = 20; // default value
    private Bitmap currentMazeBitmap;
    private int[][] maze;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator);

        mazeImageView = findViewById(R.id.maze_image);
        mazeNameInput = findViewById(R.id.maze_name_input);

        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        mazeSize = prefs.getInt("maze_size", mazeSize); // 20 by default
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        mazeSize = prefs.getInt("maze_size", 20); // update maze_size
//        Log.d("onResume - maze_size", String.valueOf(mazeSize));
    }

    public void createMaze(View v) {
        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        String selectedName = prefs.getString("selected_maze_name", null);
        if (selectedName != null) {
            String mazeJson = prefs.getString("maze_" + selectedName, null);
            if (mazeJson != null) {
                try {
                    maze = MazeUtils.jsonToMaze(mazeJson);
                    mazeSize = maze.length;
                    currentMazeBitmap = MazeUtils.drawMaze(mazeSize, maze);
                    mazeImageView.setImageBitmap(currentMazeBitmap);
                    mazeNameInput.setText(selectedName);
                    Toast.makeText(this, "Loaded saved maze: " + selectedName, Toast.LENGTH_SHORT).show();
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error loading saved maze", Toast.LENGTH_SHORT).show();
                }
            }
        }
        maze = MazeUtils.createMaze(mazeSize);
        currentMazeBitmap = MazeUtils.drawMaze(mazeSize, maze);
        mazeImageView.setImageBitmap(currentMazeBitmap);
        Toast.makeText(this, "Maze generated!", Toast.LENGTH_SHORT).show();
    }

    public void saveMaze(View v) {
        if (maze == null) {
            Toast.makeText(this, "Generate a maze first!", Toast.LENGTH_SHORT).show();
            return;
        }

        String mazeName = mazeNameInput.getText().toString().trim();
        if (mazeName.isEmpty()) {
            Toast.makeText(this, "Enter a name for the maze", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            String mazeJson = MazeUtils.mazeToJson(maze);
            editor.putString("maze_" + mazeName, mazeJson);

            Set<String> mazeNames = prefs.getStringSet("maze_names", new HashSet<>());
            mazeNames = new HashSet<>(mazeNames);
            mazeNames.add(mazeName);
            editor.putStringSet("maze_names", mazeNames);

            editor.putString("selected_maze_name", mazeName);
            editor.apply();

            Toast.makeText(this, "Maze saved as \"" + mazeName + "\"", Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            Toast.makeText(this, "Error saving maze", Toast.LENGTH_SHORT).show();
        }
    }


    public void openParameters(View v) {
        Intent intent = new Intent(GeneratorActivity.this, ParametersActivity.class);
        // Pass current size
        intent.putExtra("currentMazeSize", mazeSize);
        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        Set<String> savedMazes = prefs.getStringSet("saved_mazes", new HashSet<>());

        intent.putStringArrayListExtra("mazeNames", new ArrayList<>(savedMazes));
        startActivity(intent);
    }
}

