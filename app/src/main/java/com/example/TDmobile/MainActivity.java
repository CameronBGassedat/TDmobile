package com.example.TDmobile;

import android.Manifest;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView weekDays;
    TextView temp;
    TextView sunSet_id;
    TextView sunRise_id;
    String url = "";

    String villeGPS = "";
    // Pour firebase si pas internet, prendre la dernière localisation sauvegardé.

    Location gps_loc = null, network_loc = null;
    FloatingActionButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        weekDays = findViewById(R.id.weekDays);
        temp = findViewById(R.id.temp);
        sunSet_id = findViewById(R.id.sunSet);
        sunRise_id = findViewById(R.id.sunRise);
        button = findViewById(R.id.villeButton);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), VilleActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("DEBUG", "onRequestPermissionsResult: COUCOU");

        // Checker si on a un internet et si pas bon, break


        // Si on a la permission LOCALISATION
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Vérifier les permissions réseaux et GPS plus précis
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                assert locationManager != null;
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    return;
                gps_loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                network_loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Récupérer les coordonnées fournies par le GPS
            double latitude;
            double longitude;
            Location final_loc;
            if (gps_loc != null) {
                final_loc = gps_loc;
                latitude = final_loc.getLatitude();
                longitude = final_loc.getLongitude();
            } else if (network_loc != null) {
                final_loc = network_loc;
                latitude = final_loc.getLatitude();
                longitude = final_loc.getLongitude();
            } else {
                latitude = 0.0;
                longitude = 0.0;
            }

            // Déterminer la position en fonction des coordonnées du GPS
            try {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null) {
                    // Récupérer le nom de la ville
                    villeGPS = addresses.get(0).getLocality();
                    Log.d("DEBUG", "posi: " + villeGPS);
                    url = "https://www.prevision-meteo.ch/services/json/" + villeGPS;
                    //  FirebaseDatabase database = FirebaseDatabase.getInstance();
                    //  DatabaseReference myRef = database.getReference("villeGPS");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (grantResults.length > 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.autorisation), Toast.LENGTH_SHORT).show();
        }
        Bundle extra = getIntent().getExtras();
        if (extra != null)
            url = "https://www.prevision-meteo.ch/services/json/" + extra.getString("input_key");


        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            // city_info
                            JSONObject city_info = jsonObject.getJSONObject("city_info");
                            String ville = city_info.getString("name");
                            String coucheSoleil = city_info.getString("sunrise");
                            String leveSoleil = city_info.getString("sunset");

                            // Current_Condition
                            JSONObject current_condition = jsonObject.getJSONObject("current_condition");
                            String icone = current_condition.getString("icon_big");
                            String tmp = current_condition.getString("tmp");
                            //String condition = current_condition.getString("condition");
                            String humidite = current_condition.getString("humidity");
                            String vent = current_condition.getString("wnd_gust");

                            //FSCT_Day_0
                            JSONObject fcst_day_0 = jsonObject.getJSONObject("fcst_day_0");
                            String TempMin = fcst_day_0.getString("tmin");
                           // String TempMax = fcst_day_0.getString("tmax");
                            String weekD = fcst_day_0.getString("day_long");

                            weekDays.setText(getString(R.string.jour, weekD));
                            temp.setText(getString(R.string.temperature, tmp));
                            sunSet_id.setText(getString(R.string.coucher_de_soleil, coucheSoleil));
                            sunRise_id.setText(getString(R.string.lever_de_soleil, leveSoleil));





                            Log.d("DEBUG", "onResponse: " + ville);

                            // Icon of Weather
                            ImageView imageView = findViewById(R.id.icon);
                            Picasso.get().load(icone).into(imageView);


                            //Mettre à jour le widget
                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                            RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.meteo_widget);
                            ComponentName thisWidget = new ComponentName(getApplicationContext(), MeteoWidget.class);
                            remoteViews.setTextViewText(R.id.widget_ville_id, villeGPS);
                            remoteViews.setTextViewText(R.id.widget_tmp_id, tmp + " °C");
                            appWidgetManager.updateAppWidget(thisWidget, remoteViews);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                weekDays.setText(R.string.error);
            }
        });
        queue.add(stringRequest);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setMessage(getString(R.string.quitter))
                .setTitle(R.string.attention)
                .setPositiveButton(R.string.continuer, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                        dialog.dismiss();
                    }
                }).setNegativeButton(R.string.annuler, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }
}