package com.example.anjultyagi.bumpy;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends Activity implements SensorEventListener {
    private static final int UPDATE_THRESHOLD = 500;
    private Sensor mAccelerometer;
    private SensorManager mSensorManager;
    private Button start, stop;
    private long lastUpdate;
    public final static String fileName = "readings.csv";
   // private String TAG = "ExternalFileWriteReadActivity";
    private File file;
    private PrintWriter pw;

    //-----------------------------
    private static final long ONE_MIN = 1000 * 60;
    private static final long TWO_MIN = ONE_MIN * 2;
    private static final long FIVE_MIN = ONE_MIN * 5;
    private static final long MEASURE_TIME = 1000 * 30;
    private static final long POLLING_FREQ = 1000 * 10;
    private static final float MIN_ACCURACY = 25.0f;
    private static final float MIN_LAST_READ_ACCURACY = 500.0f;
    private static final float MIN_DISTANCE = 10.0f;

    // Views for display location information
    private TextView mAccuracyView;
    private TextView mTimeView;
    private TextView mLatView;
    private TextView mLngView;
    private int lati;
    private int logi;

    private int mTextViewColor = Color.GRAY;

    // Current best location estimate
    private Location mBestReading;

    // Reference to the LocationManager and LocationListener
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;

    private final String TAG = "LocationGetLocationActivity";

    private boolean mFirstUpdate = true;
    //-----------------------------

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    String currentDateandTime;
    String lock;
    private Context context;
    private TextView mXView;private TextView mYView;private TextView mZView;

    private void createFile() throws FileNotFoundException {
        file = new File(Environment.getExternalStorageDirectory(), fileName);
        try {
            file.createNewFile();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if(mAccelerometer == null){
            //add a toast message to say that accelerometer is not present in this phone
            finish();
        }
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {

            file = new File(
                    Environment.getExternalStorageDirectory(),
                    fileName);
            if (!file.exists()) {
                try {
                    createFile();
                } catch (FileNotFoundException e) {
                    Log.i(TAG, "FileNotFoundException");
                }
            }
            try {
                pw = new PrintWriter(new FileWriter(file, true));
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        else{
            finish();
        }
        //--------yash-----------------

        mAccuracyView = (TextView) findViewById(R.id.accuracy_view);
        mTimeView = (TextView) findViewById(R.id.time_view);
        mLatView = (TextView) findViewById(R.id.lat_view);
        mLngView = (TextView) findViewById(R.id.lng_view);
        mXView=(TextView) findViewById(R.id.xaxis);
        mYView=(TextView) findViewById(R.id.yaxis);
        mZView=(TextView) findViewById(R.id.zaxis);


        // Acquire reference to the LocationManager
        if (null == (mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE)))
            finish();

        // Get best last location measurement
        mBestReading = bestLastKnownLocation(MIN_LAST_READ_ACCURACY, FIVE_MIN);

        // Display last reading information
        if (null != mBestReading) {

            updateDisplay(mBestReading);

        } else {

            mAccuracyView.setText("No Initial Reading Available");

        }

        mLocationListener = new LocationListener()
        {

            // Called back when location changes

            public void onLocationChanged(Location location)
            {

                ensureColor();

                // Determine whether new location is better than current best
                // estimate

                if (null == mBestReading || location.getAccuracy() < mBestReading.getAccuracy()) {

                    // Update best estimate
                    mBestReading = location;

                    // Update display
                    updateDisplay(location);

                    if (mBestReading.getAccuracy() < MIN_ACCURACY)
                        mLocationManager.removeUpdates(mLocationListener);

                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                // NA
            }

            public void onProviderEnabled(String provider) {
                // NA
            }

            public void onProviderDisabled(String provider) {
                // NA
            }
        };
        //--------yash-----------------

    }

    //--------yash-----------------
    private Location bestLastKnownLocation(float minAccuracy, long maxAge) {

        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestAge = Long.MIN_VALUE;

        List<String> matchingProviders = mLocationManager.getAllProviders();

        for (String provider : matchingProviders) {

            Location location = mLocationManager.getLastKnownLocation(provider);

            if (location != null) {

                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if (accuracy < bestAccuracy) {

                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestAge = time;

                }
            }
        }

        // Return best reading or null
        if (bestAccuracy > minAccuracy
                || (System.currentTimeMillis() - bestAge) > maxAge) {
            return null;
        } else {
            return bestResult;
        }
    }

    // Update display
    private void updateDisplay(Location location)
    {
        mAccuracyView.setText("Accuracy:" + location.getAccuracy());
        mTimeView.setText("Time:" + new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(location.getTime())));
        mLatView.setText("Longitude:" + location.getLongitude());
        mLngView.setText("Latitude:" + location.getLatitude());
    }

    private void ensureColor() {
        if (mFirstUpdate) {
            setTextViewColor(mTextViewColor);
            mFirstUpdate = false;
        }
    }
    private void setTextViewColor(int color) {

        mAccuracyView.setTextColor(color);
        mTimeView.setTextColor(color);
        mLatView.setTextColor(color);
        mLngView.setTextColor(color);

    }
    //--------yash-----------------

    public void onStart(View v){
        mSensorManager.registerListener(this, mAccelerometer, 5);
        lastUpdate = System.currentTimeMillis();
        Toast.makeText(getApplicationContext(), "Service has started", Toast.LENGTH_SHORT).show();
    }
    public void onStop(View v){
        mSensorManager.unregisterListener(this);
        //pw.println("-----------------");
        Toast.makeText(getApplicationContext(), "Service has stopped", Toast.LENGTH_SHORT).show();
        pw.close();
    }
    @Override
    public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            long current_time = System.currentTimeMillis();
            if(current_time - lastUpdate > UPDATE_THRESHOLD){
                lastUpdate = current_time;
                float x = event.values[0], y = event.values[1], z = event.values[2];


                mXView.setText("X : " + x);
                mYView.setText("Y : " + y);
                mZView.setText("Z : " + z);


                currentDateandTime = sdf.format(new Date());
                pw.println(x +" " + y +" " + z+" " +currentDateandTime+" " + mBestReading.getLongitude()+" " + mBestReading.getLatitude()+" " +mBestReading.getAccuracy());
            }
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause(){
        mSensorManager.unregisterListener(this);
        //pw.println("**********************");
        pw.close();
        super.onPause();
        mLocationManager.removeUpdates(mLocationListener);
    }

    protected void onResume() {
        super.onResume();

        // Determine whether initial reading is
        // "good enough". If not, register for
        // further location updates

        if (null == mBestReading
                || mBestReading.getAccuracy() > MIN_LAST_READ_ACCURACY
                || mBestReading.getTime() < System.currentTimeMillis()
                - TWO_MIN) {

            // Register for network location updates
            if (null != mLocationManager
                    .getProvider(LocationManager.NETWORK_PROVIDER)) {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, POLLING_FREQ,
                        MIN_DISTANCE, mLocationListener);
            }

            // Register for GPS location updates
            if (null != mLocationManager
                    .getProvider(LocationManager.GPS_PROVIDER)) {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, POLLING_FREQ,
                        MIN_DISTANCE, mLocationListener);
            }

            // Schedule a runnable to unregister location listeners
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {

                @Override
                public void run() {

                    Log.i(TAG,"locn upd canld");

                    mLocationManager.removeUpdates(mLocationListener);

                }
            }, MEASURE_TIME, TimeUnit.MILLISECONDS);
        }
    }

}
