package com.example.mazegame;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class GameActivity extends AppCompatActivity implements SensorEventListener {
    private ImageView mazeView;
    private int[][] maze;
    private int mazeSize;
    private Bitmap mazeBitmap;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private int playerX = 0;
    private int playerY = 0;

    private ArrayList<int[]> path = new ArrayList<>();

    private final int cellSize = 60;

    private static final String KEY_PLAYER_X = "player_x";
    private static final String KEY_PLAYER_Y = "player_y";
    private static final String KEY_PATH = "path";
    private static final String KEY_MAZE = "maze";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mazeView = findViewById(R.id.maze_image);

        // Load maze savedInstanceState
        if (savedInstanceState != null) {
            playerX = savedInstanceState.getInt(KEY_PLAYER_X);
            playerY = savedInstanceState.getInt(KEY_PLAYER_Y);
            path = restorePath(savedInstanceState.getString(KEY_PATH));
            try {
                maze = jsonToMaze(savedInstanceState.getString(KEY_MAZE));
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading maze", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Intent intent = getIntent();
            String mazeJson = intent.getStringExtra("maze_data");
            try {
                maze = jsonToMaze(mazeJson);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error loading maze", Toast.LENGTH_SHORT).show();
                finish();
            }
            // Start position
            mazeSize = maze.length;
            playerX = 0;
            playerY = mazeSize - 2;
            path.add(new int[]{playerX, playerY});
        }

        mazeSize = maze.length;

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        drawMazeWithPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PLAYER_X, playerX);
        outState.putInt(KEY_PLAYER_Y, playerY);
        outState.putString(KEY_PATH, serializePath(path));
        try {
            outState.putString(KEY_MAZE, mazeToJson(maze));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

private long lastMoveTime = 0;
    private final long MOVE_DELAY_MS = 300;
    private final float TILT_THRESHOLD = 2.0f;

    @Override
    public void onSensorChanged(SensorEvent event) {
        long now = System.currentTimeMillis();

        float x = event.values[0];
        float y = event.values[1];

        // Check delay.
        if (now - lastMoveTime > MOVE_DELAY_MS) {
            boolean moved = false;

            if (x > TILT_THRESHOLD) {          // LEFT
                if (canMoveTo(playerX - 1, playerY)) {
                    playerX -= 1;
                    moved = true;
                }
            } else if (x < -TILT_THRESHOLD) {  // RIGHT
                if (canMoveTo(playerX + 1, playerY)) {
                    playerX += 1;
                    moved = true;
                }
            }

            if (y > TILT_THRESHOLD) {          // DOWN
                if (canMoveTo(playerX, playerY + 1)) {
                    playerY += 1;
                    moved = true;
                }
            } else if (y < -TILT_THRESHOLD) {  // TOP
                if (canMoveTo(playerX, playerY - 1)) {
                    playerY -= 1;
                    moved = true;
                }
            }

            if (moved) {
                lastMoveTime = now;
                path.add(new int[]{playerX, playerY});
                drawMazeWithPlayer();
            }
        }
    }

    private boolean canMoveTo(int x, int y) {
        return x >= 0 && y >= 0 && x < mazeSize && y < mazeSize && maze[y][x] == 1;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < mazeSize && y < mazeSize;
    }

    private void drawMazeWithPlayer() {
        int imgSize = mazeSize * cellSize;
        mazeBitmap = Bitmap.createBitmap(imgSize, imgSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mazeBitmap);
        Paint paint = new Paint();

        for (int y = 0; y < mazeSize; y++) {
            for (int x = 0; x < mazeSize; x++) {
                paint.setColor(maze[y][x] == 1 ? Color.WHITE : Color.BLACK);
                canvas.drawRect(x * cellSize, y * cellSize,
                        (x + 1) * cellSize, (y + 1) * cellSize, paint);
            }
        }
        // Draw player's path
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(5);
        for (int i = 1; i < path.size(); i++) {
            int[] prev = path.get(i - 1);
            int[] curr = path.get(i);
            canvas.drawLine(prev[0] * cellSize + cellSize / 2f,
                    prev[1] * cellSize + cellSize / 2f,
                    curr[0] * cellSize + cellSize / 2f,
                    curr[1] * cellSize + cellSize / 2f,
                    paint);
        }

        paint.setColor(Color.RED);
        canvas.drawCircle(playerX * cellSize + cellSize / 2f,
                playerY * cellSize + cellSize / 2f,
                cellSize / 3f, paint);

        mazeView.setImageBitmap(mazeBitmap);
    }

    private String serializePath(ArrayList<int[]> path) {
        JSONArray array = new JSONArray();
        for (int[] pos : path) {
            JSONArray point = new JSONArray();
            point.put(pos[0]);
            point.put(pos[1]);
            array.put(point);
        }
        return array.toString();
    }

    private ArrayList<int[]> restorePath(String json) {
        ArrayList<int[]> restoredPath = new ArrayList<>();
        if (json == null || json.isEmpty()) return restoredPath;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONArray point = array.getJSONArray(i);
                restoredPath.add(new int[]{point.getInt(0), point.getInt(1)});
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return restoredPath;
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


    public void onBackGeneratorClick(View view) {
        Intent intent = new Intent(GameActivity.this, GeneratorActivity.class);
        startActivity(intent);
        finish();
    }
    public void onBackHomeClick(View view) {
        Intent intent = new Intent(GameActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
