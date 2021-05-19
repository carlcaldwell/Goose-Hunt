package com.example.goosehunt;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainMenu extends AppCompatActivity {

    Boolean isDev = false;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int ACCESS_FINE_LOCATION = 44;
    TextView conditionTxt, temperatureTxt, errorText;
    ImageView weatherImage;
    String REQUEST_URL = "https://api.weatherapi.com/v1/current.json?key=dcd9a3f09502487395c201931211605&q=";
    String REQUEST_URL_END = "&aqi=no";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // hook widgets
        conditionTxt = findViewById(R.id.condition);
        temperatureTxt = findViewById(R.id.temperature);
        errorText = findViewById(R.id.errorText);
        weatherImage = findViewById(R.id.weatherImage);
        Button b = findViewById(R.id.startHuntingBtn);

        // listen for the start game button click
        b.setOnClickListener(e->{
            startActivity(new Intent(this, Hunt.class));
        });

        // GET THE USERS LOCATION
        //Initialize fusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //Check to see if you have permission, if not request it. This will ask the user for permission
        //if checkForLocationPermission is true, then permission is already granted, otherwise the method will ask the user for permission
        if (checkForLocationPermission()) {
            getLocation(); // if successful, this method will use a weather api to get the weather conditions for the player's location
        }

    }


    //////////////////////////////////////////////////////
    ///    API REQUEST     //
    //////////////////////////////////////////////////////

    // Send a GET request for the player's current weather conditions
    public void getWeather(double lat, double lon){
        // if in dev, dont sent the request to save on limitations for free accounts
        if(isDev){
            errorText.setText("Dev mode. No Display");
            return;
        }
        // create the GET request
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, REQUEST_URL+lat+","+lon+REQUEST_URL_END, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // on success, this code block will execute
                try {
                    // parse the int from the return
                    int temp = (int)response.getJSONObject("current").getDouble("temp_f");
                    // parse the weather condition text from the return
                    String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                    // parse the icon url from the return
                    String iconUrl = "https:"+response.getJSONObject("current").getJSONObject("condition").getString("icon");

                    // put the items where they belong
                    temperatureTxt.setText(temp+"");
                    conditionTxt.setText(condition);
                    // load the weather icon
                    Picasso.with(MainMenu.this).load(iconUrl).into(weatherImage);
                    return;
                } catch (JSONException e) {
                    errorText.setText("Error parsing weather conditions");
                }
            }
        }, error -> {
            // did not return 200 status
            System.out.println("Error Getting Weather from API");
        });

        // Send the request
        requestQueue.add(jsonObjectRequest);

    }




    //////////////////////////////////////////////////////
    ///    LOCATION     //
    //////////////////////////////////////////////////////

    //check for location permission
    //if checkForLocationPermission is true, then permission is already granted, otherwise the method will ask the user for permission
    private boolean checkForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // calling ActivityCompat#requestPermissions here to request the missing permissions
            // to handle the case where the user grants the permission.
            ActivityCompat.requestPermissions(MainMenu.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            return false;
        }
        else {
            return true;
        }
    }

    //This method is called after the user accepts or declines the permission request for location.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }  else {
                    // Explain to the user that the feature is unavailable because
                    // the features requires a permission that the user has denied.
                    // At the same time, respect the user's decision. Don't link to
                    // system settings in an effort to convince the user to change
                    // their decision.
                    errorText.setText("Location permission is required to use the weather feature.");

                }
                return;
        }
        // Other 'case' lines to check for other
        // permissions this app might request.
    }


    private void getLocation() {

        //Check to see if you have permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // calling ActivityCompat#requestPermissions here to request the missing permissions
            errorText.setText("Permission Denied to access location");
            return;
        }

        //This will check the phones location
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                //Initialize location
                Location location = task.getResult();
                if (location != null) {
                    //Location isn't null
                    try {
                        //initialize the geoCoder
                        Geocoder geocoder = new Geocoder(MainMenu.this, Locale.getDefault());

                        //Initialize address list
                        List<Address> addresses = geocoder.getFromLocation(
                                location.getLatitude(), location.getLongitude(), 1
                        );

                        // get the weather from the lat/lon
                        getWeather(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    //location returned null from task.getResult()
                    String errorString = "Location is set to NULL on this device";
                    errorText.setText(errorString);
                }
            }
        });
    }


}