package com.tyagi.anjul.bumpahead;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MapsActivity extends FragmentActivity implements LocationListener, SensorEventListener {

    GoogleMap googleMap;
    String s = null;
    TextView locationTv;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    public static String fileName = "readingsgps.csv";
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    String currentDateandTime;
    private String TAG1 = "ExternalFileWriteReadActivity";
    private File file;
    private PrintWriter pw;
    private Location location;
    float x = 0, y = 0, z = 0;
    private long lastUpdate = 0;
    BigDecimal xx, yy, zz, magnitude;
    private boolean flag;
    private static final String TAG = "Debug";
    double latitude = 0;
    double longitude = 0;

    public void createFile() throws FileNotFoundException {
        file = new File(Environment.getExternalStorageDirectory(), fileName);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //show error dialog if GoolglePlayServices not available

        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_maps);
        SupportMapFragment supportMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.googleMap);
        googleMap = supportMapFragment.getMap();
        googleMap.setMyLocationEnabled(true);


        locationTv = (TextView) findViewById(R.id.latlongLocation);
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        if (senAccelerometer == null) {
            //add a toast message to say that accelerometer is not present in this phone
            Toast.makeText(getBaseContext(), "Accelerometer Reading are not available", Toast.LENGTH_SHORT).show();
            finish();
        }

        flag = displayGpsStatus();//check whether gps is on or off
        if (flag) {

            Log.v(TAG, "onClick");
            //s="Please!! move your device to see the changes in coordinates.";
            System.out.println(s);
            locationTv.setText("Please!! move your device to see the changes in coordinates.");

        } else {
            alertbox("Gps Status!!", "Your GPS is: OFF");
        }


        boolean say = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        System.out.println(say);
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            file = new File(Environment.getExternalStorageDirectory(), fileName);
            if (!file.exists()) {
                try {
                    createFile();
                    System.out.println("--11---");
                } catch (FileNotFoundException e) {
                    Log.i(TAG1, "FileNotFoundException");
                }
            }
            try {
                pw = new PrintWriter(new FileWriter(file, true));
                System.out.println("--21 ---");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            finish();
        }

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        String bestProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(bestProvider, 1, 1, this);
        if (location != null) {
            onLocationChanged(location);
        }

        locationManager.requestLocationUpdates(bestProvider, 1, 1, this);

    }

    @Override
    public void onLocationChanged(Location location) {

        System.out.println("--1---");
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        System.out.println("--2---");
        LatLng latLng = new LatLng(latitude, longitude);

        //googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true).visible(true));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16));


        System.out.println("--3---");
        currentDateandTime = sdf.format(new Date());
        xx = round(x, 1);
        yy = round(y, 1);
        zz = round(z, 1);
        s = currentDateandTime + " " + latitude + " " + longitude + " " + xx + " " + yy + " " + zz;
        pw.println(s);
        locationTv.setText(s);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(this, "Please turn ON the GPS", Toast.LENGTH_SHORT).show();
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        locationTv = (TextView) findViewById(R.id.latlongLocation);
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 10) {
                lastUpdate = curTime;
                x = sensorEvent.values[0];
                y = sensorEvent.values[1];
                z = sensorEvent.values[2];
                System.out.println("--1111---");
            }
        }
        magnitude = round((float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2)), 2);
//------------------------------------------
 /*       latitude = location.getLatitude();
        longitude = location.getLongitude();

        System.out.println("--2---");
        LatLng latLng = new LatLng(latitude, longitude);

        googleMap.clear();
        googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true).visible(true));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(16));
*/
        //---------------------------------
        currentDateandTime = sdf.format(new Date());
        xx = round(x, 1);
        yy = round(y, 1);
        zz = round(z, 1);
        if (latitude != 0 && longitude != 0) {
            //s=currentDateandTime+" "+latitude+" "+longitude+" "+xx+" "+yy+" "+zz+" ";
            s = currentDateandTime + " " + latitude + " " + longitude + " " + magnitude;
            pw.println(s);
            locationTv.setText(latitude + " " + longitude + " " + xx + " " + yy + " " + zz);
        }
    }

    public static BigDecimal round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd;
    }

    /*----Method to Check GPS is enable or disable ----- */
    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);
        if (gpsStatus) {
            return true;

        } else {
            return false;
        }
    }

    /*----------Method to create an AlertBox ------------- */
    protected void alertbox(String title, String mymessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your Device's GPS is Disable")
                .setCancelable(false)
                .setTitle("** Gps Status **")
                .setPositiveButton("Gps On",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // finish the current activity
                                // AlertBoxAdvance.this.finish();
                                Intent myIntent = new Intent(Settings.ACTION_SETTINGS);
                                startActivity(myIntent);
                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // cancel the dialog box
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    public void upload_file(View v){

    }
    public void onStopp(View view) {
        pw.close();
        System.out.println("---out---");
        senSensorManager.unregisterListener(this);
    }

}