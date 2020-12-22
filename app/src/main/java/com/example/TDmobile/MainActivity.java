package com.example.TDmobile;
import android.Manifest;
import android.content.Context;
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
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
    EditText editText;
    TextView textView;
    TextView textView2;
    Button button;
    Location gps_loc = null, network_loc = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        editText = findViewById(R.id.editText);
        textView = findViewById(R.id.textView);
        textView2 = findViewById(R.id.textView2);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://www.prevision-meteo.ch/services/json/" + editText.getText();
                RequestQueue queue = Volley.newRequestQueue(context);

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                try {
                                    JSONObject jObj = new JSONObject(response);
                                    JSONObject jObjCurrent = jObj.getJSONObject("current_condition");
                                    String tmp = jObjCurrent.getString("tmp");
                                    String condition = jObjCurrent.getString("condition");
                                    String icon = jObjCurrent.getString("icon");

                                    textView.setText("Condition :" + condition);
                                    textView2.setText("Température :" + tmp);
                                    //ImageView imageView = findViewById(R.id.imageView);
                                    //Picasso.get().load(icon).into(imageView);

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
            }
        });
        // put mess of gps here
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        // demander perm à l'utilisateur
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
                    Object url = "https://www.prevision-meteo.ch/services/json/" + villeGPS;
                }

                // Gestion des erreurs
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
