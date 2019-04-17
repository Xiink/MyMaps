package com.example.user.mymaps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class MapsActivity_Test extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener, GoogleMap.OnCameraIdleListener,
        ActivityCompat.OnRequestPermissionsResultCallback {


    private TextView mMyTextView;
    private TextView mChickTextView;
    private TextView textView;
    private Button button, button2;
    private GoogleMap mMap;
    int num = 0, num2 = 0, num3 = 0;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;
    MarkerOptions markerOptions1 = new MarkerOptions();
    MarkerOptions markerOptions2 = new MarkerOptions();
    Handler handler = new Handler();

    LocationManager mLocationManager;

    Location mylocation;
    Criteria criteria;

    double dLat;
    double dLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps__test);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mMyTextView = (TextView) findViewById(R.id.my_text);
        mChickTextView = (TextView) findViewById(R.id.chick_text);
        textView = (TextView) findViewById(R.id.textView);
        button = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);

        //開始導航
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacks(timerDirection);
                handler.postDelayed(timerDirection, 500);
            }
        });

        //暫停導航
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacksAndMessages(null);
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    //當google地圖準備好自動執行
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnCameraIdleListener(this);

        enableMyLocation();

        mMap.setOnMyLocationButtonClickListener(this);
    }

    LatLng latLng1, latLng2, latLng3;

    //點擊地圖
    @Override
    public void onMapClick(LatLng point) {
        mChickTextView.setText("Maker, point=" + point);
        if (num == 0) {
            //建立標記並加入地圖中
            markerOptions1.position(new LatLng(point.latitude, point.longitude));
            markerOptions1.title("Destination!");
            markerOptions1.draggable(true);
            mMap.addMarker(markerOptions1);
            latLng2 = new LatLng(point.latitude, point.longitude);
            mChickTextView.setText("latitude:" + point.latitude + ",longitude:" + point.longitude);
            //備份座標點
            latLng3 = latLng2;
            String url = getRequestUrl(latLng1, latLng2);
            //串接導航API
            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
            taskRequestDirections.execute(url);
            num++;
        } else if (num != 0) {
            //初始話
            latLng1 = new LatLng(0, 0);
            latLng2 = new LatLng(0, 0);
            latLng3 = new LatLng(0, 0);
            num = 0;
            mMap.clear();
        }
    }

    private String getRequestUrl(LatLng origin, LatLng dest) {
        //製作URL
        //Value of origin
        String str_org = "origin=" + origin.latitude + "," + origin.longitude;
        //Value of destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        //Set value enable the sensor
        String sensor = "sensor=false";
        //Mode for find direction
        String mode = "mode=driving";
        //Build the full param
        String param = str_org + "&" + str_dest + "&" + "key=AIzaSyDlAaBb42EL4x41PTTz_uVyq6uiNxhRSBY";
        //Output format
        String output = "json";
        //Create url to request
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param;
        return url;
    }

    private String requestDirection(String reqUrl) throws IOException {
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    public class TaskRequestDirections extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try {
                responseString = requestDirection(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Parse json here
            TaskParser taskParser = new TaskParser();
            taskParser.execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            //Get list route and display it into the map

            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for (List<HashMap<String, String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for (HashMap<String, String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("lon"));

                    //印出導航資訊
                    String howlong = point.get("howlong");

                    howlong = howlong.replace("<b>", " ");
                    howlong = howlong.replace("</b>", " ");
                    howlong = howlong.replace("</div>", " ");
                    howlong = howlong.replace("<div style=" + '"' + "font-size:0.9em" + '"' + '>', " ");
                    textView.setText(howlong);

                    points.add(new LatLng(lat, lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }

            if (polylineOptions != null) {
                mMap.addPolyline(polylineOptions);
            } else {
                Toast.makeText(getApplicationContext(), "Direction not found!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        //mChickTextView.setText("long pressed, point=" + point);
    }

    @Override
    public void onCameraIdle() {
        //mMyTextView.setText(mMap.getCameraPosition().toString());
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        if (num2 == 0) {
            latLng1 = new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
            markerOptions2.position(new LatLng(latLng1.latitude, latLng1.longitude));
            markerOptions2.title("Origin!");
            markerOptions2.draggable(true);
            mMap.addMarker(markerOptions2);
            mMyTextView.setText("latitude:" + latLng1.latitude + ",longitude:" + latLng1.longitude);
            num2++;
        } else if (num == 0 && num2 != 0) {
            latLng1 = new LatLng(0, 0);
            mMap.clear();
            num2 = 0;
        } else if (num != 0 && num2 != 0) {
            mMap.clear();
            latLng1 = new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);
            markerOptions1.position(new LatLng(latLng2.latitude, latLng2.longitude));
            markerOptions1.title("Destination!");
            markerOptions1.draggable(true);
            markerOptions2.position(new LatLng(latLng1.latitude, latLng1.longitude));
            markerOptions2.title("Origin!");
            markerOptions2.draggable(true);
            mMap.addMarker(markerOptions1);
            mMap.addMarker(markerOptions2);
            mMyTextView.setText("latitude:" + latLng1.latitude + ",longitude:" + latLng1.longitude);
            num2++;
            String url = getRequestUrl(latLng1, latLng2);
            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
            taskRequestDirections.execute(url);
        }

        return false;
    }

    private Runnable timerDirection = new Runnable() {
        @Override
        public void run() {
            mMap.clear();
            String best;
            best = LocationManager.NETWORK_PROVIDER;
            mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(MapsActivity_Test.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity_Test.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.requestLocationUpdates(best, 0, 10, myListener);
            Location location = mLocationManager.getLastKnownLocation(best);

            if(location!=null){
                dLat = location.getLatitude();
                dLng = location.getLongitude();
                latLng1 = new LatLng(dLat, dLng);
            }

            //latLng1 = new LatLng(mMap.getCameraPosition().target.latitude,mMap.getCameraPosition().target.longitude);

            markerOptions1.position(new LatLng(latLng3.latitude, latLng3.longitude));
            markerOptions1.title("Destination!");
            markerOptions1.draggable(true);
            markerOptions2.position(new LatLng(latLng1.latitude, latLng1.longitude));
            markerOptions2.title("Origin!");
            markerOptions2.draggable(true);
            mMap.addMarker(markerOptions1);
            mMyTextView.setText("latitude:" + latLng1.latitude + ",longitude:" + latLng1.longitude);
            String url = getRequestUrl(latLng1, latLng2);
            TaskRequestDirections taskRequestDirections = new TaskRequestDirections();
            taskRequestDirections.execute(url);
            handler.postDelayed(timerDirection, 500);

        }
    };

    LocationListener myListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dLat = location.getLatitude();
            dLng = location.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        String best = mLocationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLocationManager.requestLocationUpdates(best, 1000, 10, myListener);
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }


    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }



}






