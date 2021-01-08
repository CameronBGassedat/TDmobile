package com.example.TDmobile;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NomActivity extends AppCompatActivity {

    EditText m_nom;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nom);

        m_nom = findViewById(R.id.nom);
        button = findViewById(R.id.button);

        SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
        String nom = prefs.getString("name", null);
        if (nom != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("name", nom);
            startActivity(intent);
            finish();
        }


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = m_nom.getText().toString();

                // Envoyer une donnée dans la mémoire
                SharedPreferences prefs = getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("name", name);
                editor.apply();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("name", name);
                startActivity(intent);
                finish();
            }
        });
    }
}