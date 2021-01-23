package com.log.compass;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.log.compass.viewmodel.ViewModel;


public class LocationManager {

    private final Activity activity;
    private final ViewModel viewModel;
    public Boolean permission = false;

    public LocationManager(Activity activity, ViewModel viewModel) {
        this.activity = activity;
        this.viewModel = viewModel;

        if (checkPermissions()) {
            permission = true;
            startServiceIntent();
        }
    }

    public void startServiceIntent() {
        LocationBroadcastReceiver locationBroadcastReceiver = new LocationBroadcastReceiver(viewModel);
        IntentFilter filter = new IntentFilter("broadcast_location");
        activity.registerReceiver(locationBroadcastReceiver, filter);
        Intent intent = new Intent(activity, LocationBroadcast.class);
        activity.startService(intent);
    }

    public Boolean checkPermissions() {
        int fineLocation = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        return fineLocation == PackageManager.PERMISSION_GRANTED;
    }


    public static class LocationBroadcast extends Service {

        private FusedLocationProviderClient fusedLocationClient;
        private LocationCallback locationCallback;

        @Override
        public void onCreate() {
            super.onCreate();

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    Intent intent = new Intent("broadcast_location");
                    intent.putExtra("latitude", locationResult.getLastLocation().getLatitude());
                    intent.putExtra("longitude", locationResult.getLastLocation().getLongitude());
                    sendBroadcast(intent);
                }
            };
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            requestLocation();
            return super.onStartCommand(intent, flags, startId);
        }

        @SuppressLint("MissingPermission")
        private void requestLocation() {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(1000);
            locationRequest.setFastestInterval(500);
            locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    }

    private static class LocationBroadcastReceiver extends BroadcastReceiver {

        private final ViewModel viewModel;

        public LocationBroadcastReceiver(ViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getAction().equals("broadcast_location")) {
                    double latitude = intent.getExtras().getDouble("latitude");
                    double longitude = intent.getExtras().getDouble("longitude");
                    Log.d("asdfasdf", "onReceive: " + latitude + " : " + longitude);
                    Double[] newLocation = new Double[2];
                    newLocation[0] = latitude;
                    newLocation[1] = longitude;
                    viewModel.updateLocation(newLocation);
                }
            }
        }
    }


}
