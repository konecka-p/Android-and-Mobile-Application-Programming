package com.example.mazegame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Random;

public class MazeUtils {
    private static int[][] maze;
    public static int[][] createMaze(int mazeSize) {
        generateMaze(mazeSize);
        return maze;
    }

    // ****************   MAZE GENERATION   ****************
    //  Prim's algorithm from https://en.wikipedia.org/wiki/Maze_generation_algorithm
    // with some modifications


    private static void generateMaze(int mazeSize) {
        maze = new int[mazeSize][mazeSize]; //  0 — walls

        int startX = 0;
        int startY = mazeSize - 2;
        maze[startY][startX] = 1; // Entrance - fix this

        ArrayList<int[]> walls = new ArrayList<>();
        addWalls(startX, startY, walls, mazeSize);

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

            if (inBounds(nx, ny, mazeSize) && maze[ny][nx] == 0) {
                maze[wy][wx] = 1;
                maze[ny][nx] = 1;

                addWalls(nx, ny, walls, mazeSize);
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

    private static void addWalls(int x, int y, ArrayList<int[]> walls, int mazeSize) {
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

            if (inBounds(nx, ny, mazeSize) && maze[ny][nx] == 0 && maze[wy][wx] == 0) {
                walls.add(new int[]{wx, wy, d[0], d[1]});
            }
        }
    }

    private static boolean inBounds(int x, int y, int mazeSize) {
        return x >= 0 && y >= 0 && x < mazeSize && y < mazeSize;
    }

    public static String mazeToJson(int[][] maze) throws JSONException {
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

    public static int[][] jsonToMaze(String jsonString) throws JSONException {
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

    static Bitmap drawMaze(int mazeSize,  int[][] maze) {
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

}
