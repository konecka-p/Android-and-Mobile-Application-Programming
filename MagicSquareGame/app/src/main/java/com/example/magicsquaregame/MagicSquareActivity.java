package com.example.magicsquaregame;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MagicSquareActivity extends AppCompatActivity {

    int level;
    private EditText[][] cells = new EditText[3][3];
    private TextView resultText;
    int[][] magicSquare = new int[3][3];
    int[] rowSumsValues = new int[3];
    int[] colSumsValues = new int[3];
    TextView[] rowSums = new TextView[3];
    TextView[] colSums = new TextView[3];

//    private static final String TAG = "MagicSquareActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magic_square);

        level = getIntent().getIntExtra("level", 1);
        resultText = findViewById(R.id.tv_message);


        initializeBoard();
        generateMagicSquare();
        displayMagicSquare();

//        if (savedInstanceState != null) {
//            Log.d(TAG, "onCreate called");

            // Restore data - FIX THIS
//            if (savedInstanceState != null) {
//                level = savedInstanceState.getInt("level", 1);
//                Log.d(TAG, "Restored level: " + level);
//
//                rowSumsValues = savedInstanceState.getIntArray("rowSumsValues");
//                colSumsValues = savedInstanceState.getIntArray("colSumsValues");
//
//                Log.d(TAG, "Restored rowSumsValues: " + rowSumsValues[0] + ", " + rowSumsValues[1] + ", " + rowSumsValues[2]);
//                Log.d(TAG, "Restored colSumsValues: " + colSumsValues[0] + ", " + colSumsValues[1] + ", " + colSumsValues[2]);
//
//                for (int i = 0; i < 3; i++) {
//                    for (int j = 0; j < 3; j++) {
//                        String cellValue = savedInstanceState.getString("cell_" + i + "_" + j, "");
//                        Log.d(TAG, "Restored cell[" + i + "][" + j + "]: " + cellValue);
//                        magicSquare[i][j] = cellValue;
//                    }
//                }
//                displayMagicSquare();
//            }
//        }
//        else {
//            initializeBoard();
//            generateMagicSquare();
//            displayMagicSquare();}
    }

    private void initializeBoard() {
        cells[0][0] = findViewById(R.id.cell11);
        cells[0][1] = findViewById(R.id.cell12);
        cells[0][2] = findViewById(R.id.cell13);
        cells[1][0] = findViewById(R.id.cell21);
        cells[1][1] = findViewById(R.id.cell22);
        cells[1][2] = findViewById(R.id.cell23);
        cells[2][0] = findViewById(R.id.cell31);
        cells[2][1] = findViewById(R.id.cell32);
        cells[2][2] = findViewById(R.id.cell33);


        rowSums[0] = findViewById(R.id.cell14);
        rowSums[1] = findViewById(R.id.cell24);
        rowSums[2] = findViewById(R.id.cell34);

        colSums[0] = findViewById(R.id.cell41);
        colSums[1] = findViewById(R.id.cell42);
        colSums[2] = findViewById(R.id.cell43);
    }

    private void generateMagicSquare() {
        int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9};

        Random rnd = new Random();
        for (int i = numbers.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            // swap
            int a = numbers[index];
            numbers[index] = numbers[i];
            numbers[i] = a;
        }
        int index = 0;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                magicSquare[row][col] = numbers[index++];
                rowSumsValues[row] += magicSquare[row][col];
                colSumsValues[col] += magicSquare[row][col];
            }
        }
    }

    private void displayMagicSquare() {
        Random rnd = new Random();
        Set<Integer> emptyCells = new HashSet<>();
        while (emptyCells.size() < level) {
            emptyCells.add(rnd.nextInt(9));
        }

        int index = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (emptyCells.contains(index)) {
                    cells[i][j].setText("");
                    cells[i][j].setEnabled(true);
                } else {
                    cells[i][j].setText(String.valueOf(magicSquare[i][j]));
                    cells[i][j].setEnabled(false);
                }
                index++;
            }
        }

        for (int i = 0; i < 3; i++) {
            int rowSum = magicSquare[i][0] + magicSquare[i][1] + magicSquare[i][2];
            rowSums[i].setText(String.valueOf(rowSum));
            rowSums[i].setEnabled(false);
        }

        for (int j = 0; j < 3; j++) {
            int colSum = magicSquare[0][j] + magicSquare[1][j] + magicSquare[2][j];
            colSums[j].setText(String.valueOf(colSum));
            colSums[j].setEnabled(false);
        }
    }

    private boolean checkGameResultWithValidation() {
        Set<Integer> usedNumbers = new HashSet<>();

        for (int i = 0; i < 3; i++) {
            int rowSum = 0;
            for (int j = 0; j < 3; j++) {
                String valStr = cells[i][j].getText().toString().trim();

                if (valStr.isEmpty()) {
                    resultText.setText("All cells must be filled.");
                    resultText.setTextColor(Color.RED);
                    return false;
                }

                int val;
                try {
                    val = Integer.parseInt(valStr);
                } catch (NumberFormatException e) {
                    resultText.setText("Only numbers are allowed in the cells.");
                    return false;
                }

                if (val == 0 || val > 9) {
                    resultText.setText("Numbers must be between 1 and 9.");
                    resultText.setTextColor(Color.RED);
                    return false;
                }

                if (usedNumbers.contains(val)) {
                    resultText.setText("Each number must be used only once.");
                    resultText.setTextColor(Color.RED);
                    return false;
                }

                usedNumbers.add(val);
                rowSum += val;
            }

            int expectedRowSum = Integer.parseInt(rowSums[i].getText().toString());
            if (rowSum != expectedRowSum) {
                resultText.setText("Incorrect sum in row " + (i + 1));
                resultText.setTextColor(Color.RED);
                return false;
            }
        }


        for (int j = 0; j < 3; j++) {
            int colSum = 0;
            for (int i = 0; i < 3; i++) {
                colSum += Integer.parseInt(cells[i][j].getText().toString());
            }
            int expectedColSum = Integer.parseInt(colSums[j].getText().toString());
            if (colSum != expectedColSum) {
                resultText.setText("Incorrect sum in column " + (j + 1));
                resultText.setTextColor(Color.RED);
                return false;
            }
        }
        return true;
    }



    public void checkResult(View v) {
        boolean isCorrect = checkGameResultWithValidation();
        if (isCorrect) {
            resultText.setText("Congratulations! You solved the Magic Square correctly!");
            resultText.setTextColor(Color.GREEN);
        }
    }

    public void openWikiPage(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://en.wikipedia.org/wiki/Magic_square"));
        startActivity(browserIntent);
    }

    public void exitGame(View view) {
        boolean isCorrect = checkGameResultWithValidation();
        Intent resultIntent = new Intent();
        resultIntent.putExtra("gameResult", isCorrect);
        setResult(RESULT_OK, resultIntent);
        this.finish();
    }


//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
////        Toast.makeText(this, "AAA",  Toast.LENGTH_SHORT).show();
//        super.onSaveInstanceState(outState);
//        outState.putInt("level", level);
//
//        for (int i = 0; i < 3; i++) {
//            for (int j = 0; j < 3; j++) {
//                outState.putString("cell_" + i + "_" + j, cells[i][j].getText().toString());
//            }
//        }
//
//        outState.putIntArray("rowSumsValues", rowSumsValues);
//        outState.putIntArray("colSumsValues", colSumsValues);
//    }



    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
