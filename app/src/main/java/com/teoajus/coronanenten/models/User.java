package com.teoajus.coronanenten.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

public class User {

    private String id;
    private String telp;
    private GeoPoint pos;
    private String token;
    private Timestamp time;
    private int status;

    public User(String id, String telp, GeoPoint pos, String token, Timestamp time, int status) {
        this.id = id;
        this.telp = telp;
        this.pos = pos;
        this.token = token;
        this.time = time;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getTelp() {
        return telp;
    }

    public GeoPoint getPos() {
        return pos;
    }

    public String getToken() {
        return token;
    }

    public Timestamp getTime() {
        return time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
