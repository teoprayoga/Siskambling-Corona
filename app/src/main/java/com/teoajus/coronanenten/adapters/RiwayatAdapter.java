package com.teoajus.coronanenten.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.LottieDrawable;
import com.airbnb.lottie.LottieImageAsset;
import com.teoajus.coronanenten.R;
import com.teoajus.coronanenten.databinding.AdapterRiwayatBinding;
import com.teoajus.coronanenten.models.Riwayat;
import com.teoajus.coronanenten.models.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RiwayatAdapter extends ArrayAdapter<User> {

    private Context context;
    private List<User> list;
    private List<String> stringsAnimation = new ArrayList<>();
    private String urlAnimation;

    public RiwayatAdapter(@NonNull Context context, @NonNull List<User> objects) {
        super(context, 0, objects);

        this.context    = context;
        this.list       = objects;

        stringsAnimation.add("https://assets3.lottiefiles.com/private_files/lf30_WdTEui.json");
        stringsAnimation.add("https://assets4.lottiefiles.com/private_files/lf30_Rq4Htd.json");
        stringsAnimation.add("https://assets2.lottiefiles.com/private_files/lf30_oGbdoA.json");
        stringsAnimation.add("https://assets5.lottiefiles.com/private_files/lf30_B7dCQ1.json");
        stringsAnimation.add("https://assets3.lottiefiles.com/private_files/lf30_ym2HXd.json");
        stringsAnimation.add("https://assets6.lottiefiles.com/packages/lf20_qFttfS.json");
        stringsAnimation.add("https://assets1.lottiefiles.com/packages/lf20_87JrST.json");
        stringsAnimation.add("https://assets9.lottiefiles.com/packages/lf20_CYBIbn.json");
        stringsAnimation.add("https://assets1.lottiefiles.com/packages/lf20_kkOS7q.json");
        stringsAnimation.add("https://assets6.lottiefiles.com/packages/lf20_AQ3M8U.json");
        stringsAnimation.add("https://assets8.lottiefiles.com/packages/lf20_4iPre3.json");
        stringsAnimation.add("https://assets1.lottiefiles.com/packages/lf20_gsiJ2w.json");
        stringsAnimation.add("https://assets10.lottiefiles.com/packages/lf20_iSK2a6.json");
        stringsAnimation.add("https://assets8.lottiefiles.com/packages/lf20_emIAZ3.json");
        stringsAnimation.add("https://assets7.lottiefiles.com/private_files/lf30_agyQw8.json");
        stringsAnimation.add("https://assets5.lottiefiles.com/packages/lf20_Hg1eiy.json");
        stringsAnimation.add("https://assets7.lottiefiles.com/private_files/lf30_L620n8.json");
        stringsAnimation.add("https://assets8.lottiefiles.com/packages/lf20_3azMaE.json");
        stringsAnimation.add("https://assets4.lottiefiles.com/packages/lf20_wdXBRc.json");
        stringsAnimation.add("https://assets7.lottiefiles.com/private_files/lf30_ONrIKs.json");

        urlAnimation = stringsAnimation.get(new Random().nextInt(stringsAnimation.size()));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        AdapterRiwayatBinding binding   = AdapterRiwayatBinding.inflate(LayoutInflater.from(context), parent, false);

        User user                       = list.get(position);
        String telepon                  = user.getTelp();
        String temp                     = telepon;
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

    @Override
    public int getCount() {
        return list.size();
    }

    public void resetData(List<User> lista){
        this.list = lista;
        notifyDataSetChanged();
    }
}
