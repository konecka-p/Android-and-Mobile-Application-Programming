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
    Button btnGenerate;
    Button btnOpenParams;
    Button btnSaveMaze;
    Button btnGame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generator);

        btnGenerate = findViewById(R.id.btn_generate_maze);
        btnOpenParams = findViewById(R.id.btn_open_parameters);
        btnSaveMaze = findViewById(R.id.btn_save_maze);
        mazeImageView = findViewById(R.id.maze_image);
        mazeNameInput = findViewById(R.id.maze_name_input);
        btnGame = findViewById(R.id.btn_start_game);

        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        mazeSize = prefs.getInt("maze_size", mazeSize); // 20 by default
        String selectedName = prefs.getString("selected_maze_name", null);


        if (selectedName != null) {
            String mazeJson = prefs.getString(selectedName, null);
            if (mazeJson != null) {
                try {
                    maze = jsonToMaze(mazeJson);
                    mazeSize = maze.length;
                    currentMazeBitmap = drawMaze();
                    mazeImageView.setImageBitmap(currentMazeBitmap);
                    mazeNameInput.setText(selectedName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        mazeSize = prefs.getInt("maze_size", 20); // update maze_size
//        Log.d("onResume - maze_size", String.valueOf(mazeSize));
    }

    public void startGame(View v) {
        if (maze == null) {
            Toast.makeText(this, "Please generate or load a maze first", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            String mazeJson = mazeToJson(maze);
            Intent intent = new Intent(GeneratorActivity.this, GameActivity.class);
            intent.putExtra("maze_data", mazeJson);
            startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error starting game", Toast.LENGTH_SHORT).show();
        }
    }


    public void createMaze(View v) {
        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        String selectedName = prefs.getString("selected_maze_name", null);
//        Log.d("SELECTTED NAME", String.valueOf(selectedName));
        if (selectedName != null) {
            String mazeJson = prefs.getString("maze_" + selectedName, null);
            if (mazeJson != null) {
                try {
                    maze = jsonToMaze(mazeJson);
                    mazeSize = maze.length;
                    currentMazeBitmap = drawMaze();
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
        generateMaze();
        currentMazeBitmap = drawMaze();
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
            String mazeJson = mazeToJson(maze);
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
//        startActivity(intent);

        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        Set<String> savedMazes = prefs.getStringSet("saved_mazes", new HashSet<>());

        intent.putStringArrayListExtra("mazeNames", new ArrayList<>(savedMazes));
        startActivity(intent);
    }



    // ****************   MAZE GENERATION   ****************
    //  Prim's algorithm from https://en.wikipedia.org/wiki/Maze_generation_algorithm
    // with some modifications

    private void generateMaze() {
        maze = new int[mazeSize][mazeSize]; //  0 — walls

        int startX = 0;
        int startY = mazeSize - 2;
        maze[startY][startX] = 1; // Entrance - fix this

        ArrayList<int[]> walls = new ArrayList<>();
        addWalls(startX, startY, walls);

        Random random = new Random();

        while (!walls.isEmpty()) {
            int index = random.nextInt(walls.size());
            int[] wall = walls.remove(index);

            int wx = wall[0];
            int wy = wall[1];
            int dx = wall[2];
            int dy = wall[3];

            int nx = wx + dx;
            int ny = wy + dy;

            if (inBounds(nx, ny) && maze[ny][nx] == 0) {
                maze[wy][wx] = 1;
                maze[ny][nx] = 1;

                addWalls(nx, ny, walls);
            }
        }

//        for (int y = 0; y < mazeSize; y++) {
//            StringBuilder row = new StringBuilder();
//            for (int x = 0; x < mazeSize; x++) {
//                row.append(maze[y][x]).append(" ");
//            }
//            Log.d("MazeDebug", row.toString());
//        }

        // Entrance
        maze[mazeSize - 2][0] = 1;      // Left bottom.
        maze[1][mazeSize - 1] = 1;      // Right top.

        for (int i = 0; i < mazeSize; i++) {
            if (i != 0) maze[mazeSize - 1][i] = 0;  // bottom
            if (i != mazeSize - 1) maze[0][i] = 0;  // top
            if (i != mazeSize - 2) maze[i][0] = 0;  // left
            if (i != 1) maze[i][mazeSize - 1] = 0;  // right
        }
    }

    private boolean isInArray(int i, int count, int size) {
        for (int j = 0; j < count; j++) {
            int val = count == 3 ? size - 2 - j * ((size - 3) / (count - 1)) : 1 + j * ((size - 3) / (count - 1));
            if (i == val) return true;
        }
        return false;
    }


    private void addWalls(int x, int y, ArrayList<int[]> walls) {
        int[][] directions = {
                {0, -1}, // top
                {1, 0}, // right
                {0, 1}, // bottom
                {-1, 0} // left
        };

        for (int[] d : directions) {
            int wx = x + d[0];
            int wy = y + d[1];
            int nx = x + d[0] * 2;
            int ny = y + d[1] * 2;

            if (inBounds(nx, ny) && maze[ny][nx] == 0 && maze[wy][wx] == 0) {
                walls.add(new int[]{wx, wy, d[0], d[1]});
            }
        }
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < mazeSize && y < mazeSize;
    }

    private Bitmap drawMaze() {
        int cellSize = 20;
        int imgSize = mazeSize * cellSize;
        Bitmap bmp = Bitmap.createBitmap(imgSize, imgSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint();

        for (int y = 0; y < mazeSize; y++) {
            for (int x = 0; x < mazeSize; x++) {
                paint.setColor(maze[y][x] == 1 ? Color.WHITE : Color.BLACK);
                canvas.drawRect(x * cellSize, y * cellSize,
                        (x + 1) * cellSize, (y + 1) * cellSize, paint);
            }
        }
        return bmp;
    }


    public String mazeToJson(int[][] maze) throws JSONException {
        JSONArray outerArray = new JSONArray();
        for (int y = 0; y < maze.length; y++) {
            JSONArray innerArray = new JSONArray();
            for (int x = 0; x < maze[y].length; x++) {
                innerArray.put(maze[y][x]);
            }
            outerArray.put(innerArray);
        }
        return outerArray.toString();
    }

    public int[][] jsonToMaze(String jsonString) throws JSONException {
        JSONArray outerArray = new JSONArray(jsonString);
        int[][] maze = new int[outerArray.length()][];
        for (int y = 0; y < outerArray.length(); y++) {
            JSONArray innerArray = outerArray.getJSONArray(y);
            maze[y] = new int[innerArray.length()];
            for (int x = 0; x < innerArray.length(); x++) {
                maze[y][x] = innerArray.getInt(x);
            }
        }
        return maze;
    }
}

