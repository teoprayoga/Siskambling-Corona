package com.teoajus.coronanenten.customs;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.teoajus.coronanenten.databinding.AdapterInfowindowBinding;
import com.teoajus.coronanenten.databinding.AdapterRsinfowindowBinding;
import com.teoajus.coronanenten.models.RsRujukan;
import com.teoajus.coronanenten.models.User;

import java.util.List;

public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

    Activity activity;
    List<Marker> list;

    public CustomInfoWindowAdapter(Activity activity, List<Marker> list){
        this.activity    = activity;
        this.list       = list;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = null;
        if(marker.getSnippet().equals("user"+marker.getId())){
            view = getViewUser(marker);
        }
        else if(marker.getSnippet().equals("rs"+marker.getId())){
            view = getViewRs(marker);
        }
        return view;
    }

    private View getViewUser(Marker marker){
        AdapterInfowindowBinding binding = AdapterInfowindowBinding.inflate(activity.getLayoutInflater());
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

        binding.tvtelepon.setText(temp);
        binding.tvposisi.setText(latlng);
        return binding.getRoot();
    }

    private View getViewRs(Marker marker){
        AdapterRsinfowindowBinding binding = AdapterRsinfowindowBinding.inflate(activity.getLayoutInflater());
        RsRujukan rsRujukan = (RsRujukan) marker.getTag();
        binding.tvnama.setText(rsRujukan.getJenis()+" "+rsRujukan.getNama());
        binding.tvtelepon.setText(rsRujukan.getTelp());
        binding.tvstatus.setText(rsRujukan.getBuka());
        binding.tvstatus.setTextColor(Color.GREEN);
        return binding.getRoot();
    }
}
