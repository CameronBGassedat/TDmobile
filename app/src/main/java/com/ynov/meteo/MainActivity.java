package com.ynov.meteo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
FloatingActionButton button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.villeButton);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), VilleActivity.class);
            startActivity(intent);
            finish();
        });
    }
}