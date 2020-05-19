package com.teoajus.coronanenten.models;

import com.google.firebase.firestore.GeoPoint;

public class RsRujukan {

    String nama;
    String jenis;
    String telp;
    String buka;
    String alamat;
    GeoPoint pos;

    public RsRujukan(String nama, String jenis, String telp, String buka, String alamat, GeoPoint pos) {
        this.nama = nama;
        this.jenis = jenis;
        this.telp = telp;
        this.buka = buka;
        this.alamat = alamat;
        this.pos = pos;
    }

    public String getNama() {
        return nama;
    }

    public String getJenis() {
        return jenis;
    }

    public String getTelp() {
        return telp;
    }

    public String getBuka() {
        return buka;
    }

    public String getAlamat() {
        return alamat;
    }

    public GeoPoint getPos() {
        return pos;
    }
}
