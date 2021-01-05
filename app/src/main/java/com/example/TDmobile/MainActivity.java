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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    //Global variables for app
    Context context;
    TextView weekDays;
    TextView tempMin;
    TextView tempMax;
    TextView sunSet_id;
    TextView sunRise_id;
    TextView humidity;
    String url = "";

    //Global variables for widget
    TextView ville;
    TextView tmp;

    //Global variables for connectivity
    Location gps_loc = null, network_loc = null;
    FloatingActionButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        context = getApplicationContext();

        //Catching IDs for app
        weekDays = findViewById(R.id.weekDays);
        tempMin = findViewById(R.id.tempMin);
        tempMax = findViewById(R.id.tempMax);
        sunSet_id = findViewById(R.id.sunSet);
        sunRise_id = findViewById(R.id.sunRise);
        //ville = findViewById(R.id.ville);
        //tmp = findViewById(R.id.tmp);
        button = findViewById(R.id.villeButton);

        //Button for Searching New Town
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

        // Check if Phone is connected to the internet and that the permissions are given
        if (isNetworkAvailable() == true && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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

            // Catch GPS coordinates
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

            // Find Position  with GPS coordinates
            try {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                if (addresses != null) {
                    String villeGPS = addresses.get(0).getLocality();
                    Log.d("DEBUG", "Town Found: " + villeGPS);
                    url = "https://www.prevision-meteo.ch/services/json/" + villeGPS;
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference("location");
                    myRef.setValue(url);
                }
                Bundle extra = getIntent().getExtras();
                if (extra != null) {
                    url = "https://www.prevision-meteo.ch/services/json/" + extra.getString("input_key");
                    // la sauvegarde ne fonctionne pas
                    if (url != "empty") {
                        url = extra.getString("url_key");
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("location");
                        myRef.setValue("empty");
                    }
                }
                Log.d("DEBUG", "onRequestPermissionsResult: "+url);
                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    /* city_info */
                                    JSONObject city_info = jsonObject.getJSONObject("city_info");
                                    String city = city_info.getString("name");
                                    String leveSoleil = city_info.getString("sunrise");
                                    String coucheSoleil = city_info.getString("sunset");

                                    /* Current_Condition */
                                    JSONObject current_condition = jsonObject.getJSONObject("current_condition");
                                    String icon = current_condition.getString("icon_big");
                                    String tmp_current = current_condition.getString("tmp");
                                    String condition = current_condition.getString("condition");
                                    String humidity_s = current_condition.getString("humidity");
                                    String wind = current_condition.getString("wnd_gust");

                                    /* FSCT_Day_0 */
                                    JSONObject fcst_day_0 = jsonObject.getJSONObject("fcst_day_0");
                                    String TempMin = fcst_day_0.getString("tmin");
                                    String TempMax = fcst_day_0.getString("tmax");

                                    /* Setting Text For Display */
                                    //ville.setText("" + city);
                                    weekDays.setText("Condition : " + condition);
                                    //tmp.setText("TempActuelle : " + tmp_current);
                                    tempMin.setText("TempMinimale : " + TempMin);
                                    tempMax.setText("TempMaximale : " + TempMax);
                                    //humidity.setText("Humidité : " + humidity_s);

                                    // Icon of Weather
                                    ImageView imageView = findViewById(R.id.icon);
                                    Picasso.get().load(icon).into(imageView);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        weekDays.setText("That didn't work!");
                    }
                });
                queue.add(stringRequest);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Vous n'avez pas l'autorisation", Toast.LENGTH_SHORT).show();
        }
        //Updating The Widget
        /*String temp_ville = ville.getText().toString();
        String temp_tmp = tmp.getText().toString();
        Context context = this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.meteo_widget);
        ComponentName thisWidget = new ComponentName(context, MeteoWidget.class);
        remoteViews.setTextViewText(R.id.widget_ville_id, temp_ville);
        remoteViews.setTextViewText(R.id.widget_tmp_id, temp_tmp + " °C");
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);*/
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Voulez vous vraiment quitter ?")
                .setTitle("Attention !")
                .setPositiveButton("Continuer", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                        dialog.dismiss();
                    }
                }).setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}