package com.ynov.meteo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
Button button;
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