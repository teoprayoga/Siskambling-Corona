package com.teoajus.coronanenten.models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;


public class Riwayat {

    String idriwayat;
    GeoPoint posbertemu;
    String id1;
    String id2;

    public Riwayat(String idriwayat, GeoPoint posbertemu, String id1, String id2) {
        this.idriwayat = idriwayat;
        this.posbertemu = posbertemu;
        this.id1 = id1;
        this.id2 = id2;
    }

    public String getIdriwayat() {
        return idriwayat;
    }

    public GeoPoint getPosbertemu() {
        return posbertemu;
    }

    public String getId1() {
        return id1;
    }

    public String getId2() {
        return id2;
    }
}
