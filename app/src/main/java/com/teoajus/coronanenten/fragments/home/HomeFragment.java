package com.teoajus.coronanenten.fragments.home;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.teoajus.coronanenten.MainActivity;
import com.teoajus.coronanenten.R;
import com.teoajus.coronanenten.controllers.HomeController;
import com.teoajus.coronanenten.databinding.FragmentHomeBinding;
import com.teoajus.coronanenten.fragments.home.menus.AkunFragment;
import com.teoajus.coronanenten.fragments.home.menus.InfoFragment;
import com.teoajus.coronanenten.fragments.home.menus.PeriksaFragment;
import com.teoajus.coronanenten.fragments.home.menus.PesanFragment;
import com.teoajus.coronanenten.fragments.home.menus.RadarFragment;
import com.teoajus.coronanenten.statics.MyLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;
    FragmentManager manager;
    FragmentTransaction transaction;
    List<Fragment> fragmentList = new ArrayList<>();

    public RadarFragment radarFragment;
    public PeriksaFragment periksaFragment;
    public PesanFragment pesanFragment;
    public InfoFragment infoFragment;
    public AkunFragment akunFragment;

    MainActivity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding     = FragmentHomeBinding.inflate(inflater, container, false);

        activity    = (MainActivity) getActivity();

        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.tab1, R.drawable.radar_ic, R.color.colorAccent);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.tab2, R.drawable.medical_ic, R.color.colorAccent);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.tab3, R.drawable.chat_ic, R.color.colorAccent);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.tab4, R.drawable.news_ic, R.color.colorAccent);
        AHBottomNavigationItem item5 = new AHBottomNavigationItem(R.string.tab5, R.drawable.user_ic, R.color.colorAccent);

        List<AHBottomNavigationItem> ahBottomNavigationItemList = new ArrayList<>();
        ahBottomNavigationItemList.add(item1);
        ahBottomNavigationItemList.add(item2);
        ahBottomNavigationItemList.add(item3);
        ahBottomNavigationItemList.add(item4);
        ahBottomNavigationItemList.add(item5);
        binding.bn.addItems(ahBottomNavigationItemList);
        binding.bn.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        binding.bn.setAccentColor(Color.parseColor("#D500F9"));
        binding.bn.setInactiveColor(Color.parseColor("#7B1FA2"));
        binding.bn.setNotification("1", 2);
        binding.bn.setCurrentItem(0, false);

        manager             = getChildFragmentManager();

        radarFragment       = new RadarFragment();
        periksaFragment     = new PeriksaFragment();
        pesanFragment       = new PesanFragment();
        infoFragment        = new InfoFragment();
        akunFragment        = new AkunFragment();

        fragmentList.add(radarFragment);
        fragmentList.add(periksaFragment);
        fragmentList.add(pesanFragment);
        fragmentList.add(infoFragment);
        fragmentList.add(akunFragment);

        transaction = manager.
                beginTransaction().
                replace(binding.fl.getId(), fragmentList.get(0)).
                setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        transaction.commit();
        binding.bn.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                transaction = manager.
                        beginTransaction().
                        replace(binding.fl.getId(), fragmentList.get(position)).
                        setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
                transaction.commit();

                binding.bn.setCurrentItem(position, false);
                return wasSelected;
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        if(isGooglePlayServicesAvailable(activity)){
            activity.myFirebase.getId().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (!task.isSuccessful()) {
                        return;
                    }
                    String token = task.getResult().getToken();
                    String msg = getString(R.string.msg_token_fmt, token);;
                    setToken(token);
                }
            });
        }
    }

    private void setToken(String token){
        DocumentReference reference         = activity.myFirebase.getFirestore().document("users/"+activity.myFirebase.getAuth().getCurrentUser().getUid());
        HashMap<String, Object> hashMap     = new HashMap<>();
        hashMap.put("token", token);
        reference.set(hashMap, SetOptions.merge());
    }

    private boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.makeGooglePlayServicesAvailable(getActivity());
                return true;
            }
            else{
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
                return false;
            }
        }
        else{
            return true;
        }
    }

}