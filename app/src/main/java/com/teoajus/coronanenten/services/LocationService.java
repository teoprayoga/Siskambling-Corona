package com.teoajus.coronanenten.services;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.teoajus.coronanenten.R;
import com.teoajus.coronanenten.statics.MyLog;

import java.util.HashMap;
import java.util.Map;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class LocationService extends Service implements OnSuccessListener<Location>, OnFailureListener {

    String[] perms = {ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    SharedPreferences preferences;
    Intent intentLocation;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    LocationRequest locationRequest;
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("interval", locationRequest.getFastestInterval());
            editor.apply();

            if (locationResult == null) {
                return;
            }
            int i = 0;
            for (Location location : locationResult.getLocations()) {
                if(i==0){
                    updateLocFirestore(location);
                    updatePrefLocation(location);
                    updateBcLocation(location);
                }
                i++;
            }
        }
    };

    private void removeLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    FusedLocationProviderClient fusedLocationProviderClient;
//    long interval = 1800000;//30min
//    long interval = 300000;//5min
    long interval = 60000;//1min
    @Override
    public void onCreate() {
        super.onCreate();

        intentLocation              = new Intent(MyLog.PREF_LOCATION);
        preferences                 = getSharedPreferences(MyLog.PREF_LOCATION, Context.MODE_PRIVATE);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
//        locationRequest.setInterval(interval);
        locationRequest.setFastestInterval(interval);
//        locationRequest.setSmallestDisplacement(50);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        if(EasyPermissions.hasPermissions(getApplicationContext(), perms)){
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this);
        }
//        setLocationManager();
    }

    @Override
    public void onFailure(@NonNull Exception e) {

    }

    @Override
    public void onSuccess(Location location) {
        if(location!=null){
            updateLocFirestore(location);
            updatePrefLocation(location);
            updateBcLocation(location);
        }
    }

    LocationManager locationManager;
    private void setLocationManager(){
        if(locationManager == null){
            locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        if(EasyPermissions.hasPermissions(getApplicationContext(), perms)){
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 360000, 50, locationListeners[0]);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1800000, 30, locationListeners[1]);
//            locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 1800000, 10, locationListeners[2]);
        }
    }

    LocationListener[] locationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER),
            new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };

    private class LocationListener implements android.location.LocationListener{

        Location location;

        public LocationListener(String provider){
            location = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            if(location!=null){
                this.location.set(location);

                updateLocFirestore(location);
                updatePrefLocation(location);
                updateBcLocation(location);
            }
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeLocationUpdates();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("interval", 0).apply();
//        locationManager.removeUpdates(locationListeners[1]);
    }

    private void updateLocFirestore(Location location){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(location!=null && user!=null){
            DocumentReference reference         = FirebaseFirestore.getInstance().document("users/"+user.getUid());
            HashMap<String, Object> hashMap     = new HashMap<>();
            hashMap.put("pos", new GeoPoint(location.getLatitude(), location.getLongitude()));
            reference.set(hashMap, SetOptions.merge());
        }
    }

    private void updatePrefLocation(Location location){
        if(location!=null){
            SharedPreferences.Editor editor = preferences.edit();
            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            editor.putString("pos", new Gson().toJson(geoPoint));
            editor.apply();
        }
    }

    private void updateBcLocation(Location location){
        if(location!=null){
            GeoPoint geoPoint   = new GeoPoint(location.getLatitude(), location.getLongitude());
            intentLocation.putExtra("pos", new Gson().toJson(geoPoint));
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentLocation);
        }
    }
}