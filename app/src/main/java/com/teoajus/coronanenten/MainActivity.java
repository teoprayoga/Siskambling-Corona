package com.teoajus.coronanenten;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.GeoPoint;
import com.google.maps.android.ui.IconGenerator;
import com.teoajus.coronanenten.controllers.HomeController;
import com.teoajus.coronanenten.controllers.LoginController;
import com.teoajus.coronanenten.databinding.ActivityMainBinding;
import com.teoajus.coronanenten.databinding.DialogLoadingBinding;
import com.teoajus.coronanenten.databinding.DialogTentangappliasiBinding;
import com.teoajus.coronanenten.fragments.home.HomeFragment;
import com.teoajus.coronanenten.fragments.login.LoginFragment;
import com.teoajus.coronanenten.controllers.MyFirebase;
import com.teoajus.coronanenten.statics.MyLog;
import com.teoajus.coronanenten.utils.JsonUtil;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CALL_PHONE;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks{

    private ActivityMainBinding binding;

    public LoginFragment loginFragment;
    public HomeFragment homeFragment;

    public MyFirebase myFirebase;
    public LoginController loginController;
    public HomeController homeController;

    public FragmentManager manager;
    public FragmentTransaction transaction;

    public SharedPreferences preferencesSetting;
    public SharedPreferences preferencesRujukan;
    public SharedPreferences preferencesLocation;
    public SharedPreferences preferencesToken;

    public Handler handler;
    public AlertDialog dialogLoading;

    public IconGenerator iconGenerator;
    public SimpleExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding     = ActivityMainBinding.inflate(getLayoutInflater());
        View view   = binding.getRoot();
        setContentView(view);

        preferencesSetting  = getSharedPreferences(MyLog.PREF_SETTING, Context.MODE_PRIVATE);
        preferencesRujukan  = getSharedPreferences(MyLog.PREF_RSRUJUKAN, Context.MODE_PRIVATE);
        preferencesLocation = getSharedPreferences(MyLog.PREF_LOCATION, Context.MODE_PRIVATE);
        preferencesToken    = getSharedPreferences(MyLog.PREF_TOKEN, Context.MODE_PRIVATE);

        String setting      = preferencesSetting.getString("setting", null);
        if(setting == null){
            String dataSetting  = JsonUtil.getJsonFromAssets(this, "setting.json");
            SharedPreferences.Editor editor = preferencesSetting.edit();
            editor.putString("setting", dataSetting);
            editor.apply();
        }
        String rujukan      = preferencesRujukan.getString("rujukan", null);
        if(rujukan == null){
            String datarujukan  = JsonUtil.getJsonFromAssets(this, "rujukan.json");
            SharedPreferences.Editor editor = preferencesRujukan.edit();
            editor.putString("rujukan", datarujukan);
            editor.apply();
        }

        loginFragment   = new LoginFragment();
        homeFragment    = new HomeFragment();

        myFirebase      = new MyFirebase(this);
        loginController = new LoginController(this);
        homeController  = new HomeController(this);
        iconGenerator   = new IconGenerator(this);

        handler         = new Handler();
        player          = new SimpleExoPlayer.Builder(this).build();

        manager         = getSupportFragmentManager();
        transaction     = manager.beginTransaction();
        transaction.replace(binding.flmain.getId(), loginFragment);

        perms();
        createAlertLoading();
    }

    public void navigateTo(Fragment fragment, boolean addToBackStack){
        transaction = getSupportFragmentManager().
                beginTransaction().
                replace(binding.flmain.getId(), fragment);
        if(addToBackStack){
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    public LoginFragment getLoginFragment(){
        return loginFragment;
    }

    @Override
    public void onBackPressed() {
        for (Fragment frag : manager.getFragments()) {
            if (frag.isVisible()) {
                FragmentManager childFm = frag.getChildFragmentManager();
                if (childFm.getBackStackEntryCount() > 0) {
                    childFm.popBackStack();
                    return;
                }
            }
        }
        super.onBackPressed();
    }

    @AfterPermissionGranted(123)
    private void perms(){
        String[] perms = {ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, CALL_PHONE};
        if(EasyPermissions.hasPermissions(this, perms)){

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
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if(EasyPermissions.somePermissionPermanentlyDenied(this, perms)){
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE){

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    AlertDialog.Builder builder;
    public void createAlertLoading(){
        builder                         = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        DialogLoadingBinding binding    = DialogLoadingBinding.inflate(getLayoutInflater());
        builder.setView(binding.getRoot());
        dialogLoading                   = builder.create();
    }
}