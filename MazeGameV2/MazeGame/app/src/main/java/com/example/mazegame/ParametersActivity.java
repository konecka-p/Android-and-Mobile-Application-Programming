package com.example.mazegame;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;


import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ParametersActivity extends AppCompatActivity {
    private NumberPicker sizePicker;
    private Spinner mazeNamesSpinner;

    private int minSize = 10;
    private int maxSize = 50;
    private int currentSize = 20;

    private ArrayList<String> mazeNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);

        sizePicker = findViewById(R.id.size_picker);
        mazeNamesSpinner = findViewById(R.id.maze_names_spinner);


        // Get maze_size
        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        currentSize = prefs.getInt("maze_size", 20);

        // Get maze_names
        Set<String> mazeNamesSet = prefs.getStringSet("maze_names", new HashSet<>());
        mazeNames = new ArrayList<>(mazeNamesSet);
        mazeNames.add(0, "");


        // Set NumberPicker
        sizePicker.setMinValue(minSize);
        sizePicker.setMaxValue(maxSize);
        sizePicker.setValue(currentSize);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, mazeNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mazeNamesSpinner.setAdapter(adapter);
        mazeNamesSpinner.setSelection(0); // Set empty by deffault
    }


    public void saveParameters(View view) {
        int selectedSize = sizePicker.getValue();

        SharedPreferences prefs = getSharedPreferences("maze_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("maze_size", selectedSize);

        String selectedMazeName = (String) mazeNamesSpinner.getSelectedItem();
        if (selectedMazeName != null) {
            editor.putString("selected_maze_name", selectedMazeName);
        }
        else {
            editor.remove("selected_maze_name");
        }
        editor.apply();
        finish();
//        Intent intent = new Intent(ParametersActivity.this, GeneratorActivity.class);
//        startActivity(intent);
    }

    public void onCancelClick(View view) {
        finish();
//        Intent intent = new Intent(ParametersActivity.this, GeneratorActivity.class);
//        startActivity(intent);
    }

}
