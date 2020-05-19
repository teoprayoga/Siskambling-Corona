package com.teoajus.coronanenten.fragments.home.menus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.teoajus.coronanenten.MainActivity;
import com.teoajus.coronanenten.R;
import com.teoajus.coronanenten.adapters.RiwayatAdapter;
import com.teoajus.coronanenten.customs.CustomInfoWindowAdapter;
import com.teoajus.coronanenten.databinding.AdapterInfowindowclickBinding;
import com.teoajus.coronanenten.databinding.AdapterRsinfowindowclickBinding;
import com.teoajus.coronanenten.databinding.DialogRiwayatBinding;
import com.teoajus.coronanenten.databinding.FragmentRadarBinding;
import com.teoajus.coronanenten.models.Riwayat;
import com.teoajus.coronanenten.models.RsRujukan;
import com.teoajus.coronanenten.models.Setting;
import com.teoajus.coronanenten.models.User;
import com.teoajus.coronanenten.statics.MyLog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class RadarFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback, EasyPermissions.PermissionCallbacks, GoogleMap.OnInfoWindowClickListener {

    FragmentRadarBinding binding;
    MainActivity activity;
    AlertDialog.Builder builder;
    AlertDialog.Builder builderMap;
    AlertDialog alertDialog;
    AlertDialog alertDialogMap;

    GoogleMap googleMap;
    String[] perms = {ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding     = FragmentRadarBinding.inflate(inflater, container, false);
        activity    = (MainActivity) getActivity();

        createDialog();
        activity.loginController.createTentangAplikasi();

        binding.tvtitle.setOnClickListener(this);
        binding.lavvirus.setOnClickListener(this);

        return binding.getRoot();
    }

    @AfterPermissionGranted(123)
    private void perms(){
        String[] perms = {ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if(EasyPermissions.hasPermissions(getActivity(), perms)){

        }
        else{
            EasyPermissions.requestPermissions(this, getString(R.string.permissiontitle), 123, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE){

        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        perms();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == binding.lavvirus.getId()){
            activity.dialogLoading.show();
            if(!alertDialog.isShowing()){
                activity.handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        activity.dialogLoading.dismiss();
                        alertDialog.show();
                    }
                }, 3000);
            }
        }
        if(v.getId() == binding.tvtitle.getId()){
            if(!activity.loginController.alertDialog.isShowing()){
                activity.loginController.alertDialog.show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        String setting      = activity.preferencesSetting.getString("setting", null);
        Setting settingObj  = new Gson().fromJson(setting, Setting.class);
        if(settingObj.isTrackwhomnear()){
            binding.lavvirus.playAnimation();
        }
        else{
            binding.lavvirus.pauseAnimation();
        }
        activity.handler.post(runnable);
        bcLocationPref();

        setUserData();
//        setSnapshot();
        setRiwayatData();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        activity.handler.removeCallbacks(runnable);
        if(receiverLocation!=null){
            LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiverLocation);
        }
//        listenerRegistration.remove();
        listenerRegistrationUser.remove();
        listenerRegistrationRiwayat.remove();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    CustomInfoWindowAdapter customInfoWindow;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if(EasyPermissions.hasPermissions(getActivity(), perms)){
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);

        customInfoWindow = new CustomInfoWindowAdapter(activity, activity.homeController.getMarkers());
        googleMap.setInfoWindowAdapter(customInfoWindow);
        googleMap.setOnInfoWindowClickListener(this);
        this.googleMap = googleMap;

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-8.659528, 115.191263), 10));
    }

    RiwayatAdapter adapter;
    DialogRiwayatBinding riwayatBinding;

    public void createDialog(){
        builder         = new AlertDialog.Builder(getActivity());
        riwayatBinding  = DialogRiwayatBinding.inflate(this.getLayoutInflater());

        adapter         = new RiwayatAdapter(activity, riwayats);
        riwayatBinding.lv.setAdapter(adapter);

        Setting setting = new Gson().fromJson(activity.preferencesSetting.getString("setting", null), Setting.class);

        if(setting.isTrackwhomnear()){
            riwayatBinding.lav.playAnimation();
            riwayatBinding.s.setChecked(true);

            binding.lavvirus.playAnimation();
        }
        else{
            riwayatBinding.lav.pauseAnimation();
            riwayatBinding.s.setChecked(false);

            binding.lavvirus.pauseAnimation();
        }

        riwayatBinding.s.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                Setting setting1 = new Gson().fromJson(activity.preferencesSetting.getString("setting", null), Setting.class);
                setting1.setTrackwhomnear(true);
                SharedPreferences.Editor editor = activity.preferencesSetting.edit();
                editor.putString("setting", new Gson().toJson(setting1)).apply();

                riwayatBinding.lav.playAnimation();
                riwayatBinding.s.setChecked(true);
                binding.lavvirus.playAnimation();
                Toast.makeText(getActivity(), "Enabled", Toast.LENGTH_SHORT).show();
            }
            else{
                Setting setting1 = new Gson().fromJson(activity.preferencesSetting.getString("setting", null), Setting.class);
                setting1.setTrackwhomnear(false);
                SharedPreferences.Editor editor = activity.preferencesSetting.edit();
                editor.putString("setting", new Gson().toJson(setting1)).apply();

                riwayatBinding.lav.pauseAnimation();
                riwayatBinding.s.setChecked(false);
                binding.lavvirus.pauseAnimation();
                Toast.makeText(getActivity(), "Disabled", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setView(riwayatBinding.getRoot());
        alertDialog = builder.create();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if(riwayatList.isEmpty()){
                riwayatBinding.lltv.setVisibility(View.VISIBLE);
                riwayatBinding.lavempty.setVisibility(View.VISIBLE);
                riwayatBinding.lv.setVisibility(View.INVISIBLE);
            }
            else{
                riwayatBinding.lltv.setVisibility(View.INVISIBLE);
                riwayatBinding.lavempty.setVisibility(View.INVISIBLE);
                riwayatBinding.lv.setVisibility(View.VISIBLE);
            }

            long interval = activity.preferencesLocation.getLong("interval", 0);
            if(interval>0){
                interval = interval - 1000;
                SharedPreferences.Editor editor = activity.preferencesLocation.edit();
                editor.putLong("interval", interval).apply();
                binding.lavrefresh.pauseAnimation();
            }
            else{
                interval = 0;
                binding.lavrefresh.playAnimation();
            }
            binding.tvinterval.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes(interval),
                    TimeUnit.MILLISECONDS.toSeconds(interval)
                            -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(interval))
            ));

            activity.handler.postDelayed(this, 1000);
        }
    };

    List<User> userList = new ArrayList<>();
    ListenerRegistration listenerRegistrationUser;
    private void setUserData(){
        listenerRegistrationUser = activity.myFirebase.getFirestore().collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e!=null || queryDocumentSnapshots==null){
                    return;
                }
                if(!queryDocumentSnapshots.isEmpty()){
                    userList.clear();
                    for(DocumentSnapshot snapshot : queryDocumentSnapshots){
                        Map<String, Object> map = snapshot.getData();
                        String string           = new Gson().toJson(map);
                        User user               = new Gson().fromJson(string, User.class);
                        userList.add(user);
                    }
                }
                else{

                }
                activity.homeController.setMap(googleMap, userList);
            }
        });
    }

    ListenerRegistration listenerRegistrationRiwayat;
    List<User> riwayats         = new ArrayList<>();
    List<Riwayat> riwayatList   = new ArrayList<>();
    private void setRiwayatData(){
        listenerRegistrationRiwayat = activity.myFirebase.getFirestore().collection("riwayats").
                whereEqualTo("id1", activity.myFirebase.getAuth().getCurrentUser().getUid()).
                addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e!=null || queryDocumentSnapshots==null){
                    return;
                }
                riwayatList.clear();
                for(DocumentSnapshot snapshot : queryDocumentSnapshots){
                    Map<String, Object> map = snapshot.getData();
                    String string           = new Gson().toJson(map);
                    Riwayat riwayat         = new Gson().fromJson(string, Riwayat.class);
                    riwayatList.add(riwayat);
                }
                adapter.resetData(activity.homeController.getRiwayat(riwayatList, userList));
            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(marker.getSnippet().equals("user"+marker.getId())){
            setUserView(marker);
            alertDialogMap.show();
        }
        else if(marker.getSnippet().equals("rs"+marker.getId())){
            setRsView(marker);
            alertDialogMap.show();
        }
    }

    private void setUserView(Marker marker){
        AdapterInfowindowclickBinding adapterInfowindowclickBinding;
        adapterInfowindowclickBinding   = AdapterInfowindowclickBinding.inflate(activity.getLayoutInflater());
        builderMap                      = new AlertDialog.Builder(activity);

        User user = (User) marker.getTag();

        String telepon                  = user.getTelp();
        String temp                     = "";
        String subs                     = "";
        String repl                     = "";
        if(telepon.length()>4){
            subs                        = telepon.substring(3, telepon.length()-2);
            for(int i=0; i<subs.length(); i++){
                repl += "X";
            }
            temp                        = telepon.replace(subs, repl);
        }
        else{
            temp = "XXXX";
        }

        String latlng                   = String.format("Lat: %.3f, Lng: %.3f", user.getPos().getLatitude(), user.getPos().getLongitude());

        adapterInfowindowclickBinding.tvtelepon.setText(temp);
        adapterInfowindowclickBinding.tvposisi.setText(latlng);
        adapterInfowindowclickBinding.tvstatus.setText(activity.homeController.getStats().get(user.getStatus()));
        adapterInfowindowclickBinding.tvstatus.setTextColor(activity.homeController.getColors().get(user.getStatus()));
        SimpleDateFormat simpleDateFormat   = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date date                           = new Date(user.getTime().toDate().getTime());
        adapterInfowindowclickBinding.tvcreate.setText(simpleDateFormat.format(date));
        builderMap.setView(adapterInfowindowclickBinding.getRoot());
        alertDialogMap = builderMap.create();

        final ListenerRegistration[] listenerRegistration = new ListenerRegistration[1];
        alertDialogMap.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                DocumentReference reference = activity.myFirebase.getFirestore().document("users/"+user.getId());
                listenerRegistration[0] = reference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if(e!=null){
                            return;
                        }
                        if(documentSnapshot.exists()){
                            Map<String, Object> map = documentSnapshot.getData();
                            String string           = new Gson().toJson(map);
                            JsonObject jsonObject   = new Gson().fromJson(string, JsonObject.class);
                            User user               = new User(
                                    jsonObject.get("id").getAsString(),
                                    jsonObject.get("telp").getAsString(),
                                    new Gson().fromJson(jsonObject.get("pos"), GeoPoint.class),
                                    jsonObject.get("token").getAsString(),
                                    new Gson().fromJson(jsonObject.get("created"), Timestamp.class),
                                    jsonObject.get("status").getAsInt()
                            );
                            adapterInfowindowclickBinding.tvstatus.setText(activity.homeController.getStats().get(user.getStatus()));
                            adapterInfowindowclickBinding.tvstatus.setTextColor(activity.homeController.getColors().get(user.getStatus()));
                        }
                    }
                });
            }
        });
        alertDialogMap.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                listenerRegistration[0].remove();
            }
        });
    }

    private void setRsView(Marker marker){
        AdapterRsinfowindowclickBinding binding = AdapterRsinfowindowclickBinding.inflate(activity.getLayoutInflater());
        RsRujukan rsRujukan = (RsRujukan) marker.getTag();
        binding.tvnama.setText(rsRujukan.getNama());
        binding.tvjenis.setText(rsRujukan.getJenis());
        binding.tvalamat.setText(rsRujukan.getAlamat());
        binding.tvtelepon.setText(rsRujukan.getTelp());
        binding.tvstatus.setText(rsRujukan.getBuka());
        binding.tvstatus.setTextColor(Color.GREEN);
        binding.ibcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String telp = rsRujukan.getTelp().replace("(", "");
                telp        = telp.replace(")", "").replace(" ", "");

                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + telp));
                startActivity(intent);
            }
        });
        builderMap                      = new AlertDialog.Builder(activity);
        builderMap.setView(binding.getRoot());
        alertDialogMap = builderMap.create();
    }

    BroadcastReceiver receiverLocation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String string       = intent.getStringExtra("pos");
            GeoPoint geoPoint   = new Gson().fromJson(string, GeoPoint.class);
            activity.homeController.setListUser();
        }
    };

    private void bcLocationPref(){
        LocalBroadcastManager.getInstance(activity).registerReceiver(receiverLocation, new IntentFilter(MyLog.PREF_LOCATION));
    }
}