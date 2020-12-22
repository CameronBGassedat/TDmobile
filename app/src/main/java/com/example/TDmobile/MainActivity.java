package com.example.TDmobile;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.TDmobile.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    Context context;
    TextView textView;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView5;
    String url = "";

    Location gps_loc = null, network_loc = null;
    FloatingActionButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        context = getApplicationContext();
        textView = findViewById(R.id.weekDays);
        textView2 = findViewById(R.id.tempMin);
        textView3 = findViewById(R.id.tempMax);
        textView4 = findViewById(R.id.sunSet);
        textView5 = findViewById(R.id.sunRise);
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
                    String villeGPS = addresses.get(0).getLocality();
                    Log.d("DEBUG", "posi: " + villeGPS);
                    url = "https://www.prevision-meteo.ch/services/json/" + villeGPS;
                }
                Bundle extra = getIntent().getExtras();
                if (extra != null)
                    url = "https://www.prevision-meteo.ch/services/json/" + extra.getString("input_key");

                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    // city_info
                                    JSONObject city_info = jsonObject.getJSONObject("city_info");
                                    String ville = city_info.getString("name");
                                    String leveSoleil = city_info.getString("sunrise");
                                    String coucheSoleil = city_info.getString("sunset");
                                    JSONObject current_condition = jsonObject.getJSONObject("current_condition");
                                    String icone = current_condition.getString("icon_big");
                                    String tmp = current_condition.getString("tmp");
                                    String condition = current_condition.getString("condition");
                                    String humidite = current_condition.getString("humidity");
                                    String vent = current_condition.getString("wnd_gust");

                                    textView.setText("Condition :" + condition);
                                    textView2.setText("Température :" + leveSoleil);
                                    //textView2.setText();

                                    Log.d("DEBUG", "onResponse: " + ville);

                                    // Icon of Weather
                                    ImageView imageView = findViewById(R.id.icon);
                                    Picasso.get().load(icone).into(imageView);

                                    // Sunset
                                    ImageView imageView2 = findViewById(R.id.sunSet_id);

                                    // Sun Rise
                                    ImageView imageView3 = findViewById(R.id.sunRise_id);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        textView.setText("That didn't work!");
                    }
                });
                queue.add(stringRequest);
                // Gestion des erreurs
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
