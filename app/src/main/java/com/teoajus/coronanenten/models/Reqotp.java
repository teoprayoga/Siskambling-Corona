package com.teoajus.coronanenten.models;

public class Reqotp {

    private String kode;
    private String kodenumberplus;
    private String telp;

    public Reqotp(String kode, String kodenumberplus, String telp) {
        this.kode = kode;
        this.kodenumberplus = kodenumberplus;
        this.telp = telp;
    }

    public String getKodenumberplus() {
        return kodenumberplus;
    }

    public String getKode() {
        return kode;
    }

    public String getTelp() {
        return telp;
    }
}
