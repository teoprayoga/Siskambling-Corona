package com.teoajus.coronanenten.services;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.teoajus.coronanenten.statics.MyLog;

import java.util.HashMap;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    FirebaseFirestore firestore;
    FirebaseAuth auth;
    SharedPreferences preferences;

    public MyFirebaseMessagingService(){
        firestore   = FirebaseFirestore.getInstance();
        auth        = FirebaseAuth.getInstance();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = getSharedPreferences(MyLog.PREF_TOKEN, Context.MODE_PRIVATE);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        setToken(s);
        setPrefToken(s);
    }

    private void setToken(String token){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(token!=null && user!=null){
            DocumentReference reference         = firestore.document("users/"+user.getUid());
            HashMap<String, Object> hashMap     = new HashMap<>();
            hashMap.put("token", token);
            reference.set(hashMap, SetOptions.merge());
        }
    }

    private void setPrefToken(String token){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("token", token);
        editor.apply();
    }
}
