package com.semicolonlabs.navi;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;

import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;

import android.text.TextWatcher;
import android.util.Log;

import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.ar.sceneform.ux.ArFragment;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ActivityCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPEN_GL_VERSION = 3.0;

    private String destinationPlaceId;
    private Boolean startPressed;

    private Handler handler;
    private RendererRunnable rendererRunnable;

    private Navigator navigator;
    private AutoCompleteTextView autoCompleteTextView;
    private ArrayAdapter<String> PlaceAdapter;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check if device is supported or not
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        startPressed = false;

        ArFragment arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);
        handler = new Handler();
        rendererRunnable = new RendererRunnable(this, this.handler, arFragment);

        navigator = new Navigator(this);

        destinationPlaceId = "";
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Log.d(TAG, "onLocationResult: " + locationResult.toString());
                if (!destinationPlaceId.trim().equals(""))
                    navigator.getUpdate(locationResult.getLastLocation(), destinationPlaceId);
            }
        };

        autoCompleteTextView = findViewById(R.id.autoCompleteMaps);
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChange called");
                navigator.getPrediction(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        autoCompleteTextView.setThreshold(1);   // will start working from first character
        PlaceAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, new String[0]);
        PlaceAdapter.setNotifyOnChange(true);
        autoCompleteTextView.setAdapter(PlaceAdapter);

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Check if device is AR ready!
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        ActivityManager activityManager = ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE));
        if (activityManager != null) {
            String openGlVersionString = activityManager.getDeviceConfigurationInfo().getGlEsVersion();
            if (Double.parseDouble(openGlVersionString) < MIN_OPEN_GL_VERSION) {
                Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
                Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                        .show();
                activity.finish();
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public void onPredictions(List<String> places, List<String> place_ids) {

        PlaceAdapter.clear();
        PlaceAdapter.addAll(places);
        PlaceAdapter.notifyDataSetChanged();

        List<String> PlaceIdAdapter = new ArrayList<String>();
        PlaceIdAdapter.clear();
        PlaceIdAdapter.addAll(place_ids);

        /*
         * Get the index of the selected place from the list. (Starts with 0)
         */
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            Log.d(TAG, Integer.toString(position));
            Log.d(TAG, PlaceIdAdapter.get(position));
            destinationPlaceId = PlaceIdAdapter.get(position);
        });
    }

    @Override
    public void onRoutes(JSONObject jsonObject) {
        Log.d(TAG, "Routes received: " + jsonObject.toString());
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Permisan le lena chahiye tha
            return;
        }
        if (startPressed)
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    private void stopLocationUpdates() {
        if (startPressed)
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void startRendering() {
        if (startPressed)
            handler.post(rendererRunnable);
    }

    private void stopRendering() {
        if (startPressed)
            handler.removeCallbacks(rendererRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startRendering();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRendering();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRendering();
    }

    public void startButtonOnClick(View view) {
        // ImageView
        Animation buttonAnimation = AnimationUtils.loadAnimation(this, R.anim.button_animation);
        view.startAnimation(buttonAnimation);
        view.setVisibility(View.GONE);
        // AutoCompleteTextView
        int actvLoc[] = new int[2];
        autoCompleteTextView.getLocationOnScreen(actvLoc);
        autoCompleteTextView.animate().translationYBy(-actvLoc[1]).translationXBy(actvLoc[0]).alpha(0.5f).setDuration(700).withEndAction(new Runnable() {
            @Override
            public void run() {
                autoCompleteTextView.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.linear_layout_top).setVisibility(View.VISIBLE);
        // startRendering();
        // startLocationUpdates();
    }
}
