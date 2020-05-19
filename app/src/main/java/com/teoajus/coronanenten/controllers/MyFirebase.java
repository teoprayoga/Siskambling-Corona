package com.teoajus.coronanenten.controllers;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.teoajus.coronanenten.MainActivity;
import com.teoajus.coronanenten.models.User;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MyFirebase implements FirebaseAuth.AuthStateListener {

    private FirebaseInstanceId id;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private PhoneAuthProvider provider;
    private MainActivity activity;

    public MyFirebase(Context context) {
        id          = FirebaseInstanceId.getInstance();
        firestore   = FirebaseFirestore.getInstance();
        auth        = FirebaseAuth.getInstance();
        provider    = PhoneAuthProvider.getInstance();
        activity    = (MainActivity) context;

        auth.addAuthStateListener(this);
    }

    public FirebaseFirestore getFirestore() {
        return firestore;
    }

    public FirebaseAuth getAuth() {
        return auth;
    }

    public PhoneAuthProvider getProvider() {
        return provider;
    }

    public FirebaseInstanceId getId() {
        return id;
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        checklogin(auth);
    }

    public void removeAuthStateListener(){
        auth.removeAuthStateListener(this);
    }

    private void checklogin(FirebaseAuth firebaseAuth){
        FirebaseUser firebaseUser           = firebaseAuth.getCurrentUser();
        if(firebaseUser!=null){
            activity.dialogLoading.show();
            activity.handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    activity.navigateTo(activity.homeFragment, false);
                    activity.dialogLoading.dismiss();
                }
            }, 3000);
        }
        else{
            activity.navigateTo(activity.loginFragment, false);
        }
    }
}
