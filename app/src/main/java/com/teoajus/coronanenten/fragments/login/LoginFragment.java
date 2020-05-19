package com.teoajus.coronanenten.fragments.login;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;
import com.google.android.material.snackbar.Snackbar;
import com.teoajus.coronanenten.MainActivity;
import com.teoajus.coronanenten.R;
import com.teoajus.coronanenten.databinding.DialogTentangappliasiBinding;
import com.teoajus.coronanenten.databinding.FragmentLoginBinding;
import com.teoajus.coronanenten.statics.MyLog;

import static android.Manifest.permission.READ_CONTACTS;

public class LoginFragment extends Fragment implements View.OnClickListener {

    FragmentLoginBinding binding;
    FragmentManager fragmentManager;
    ReqotpFragment reqotpFragment;
    OtpFragment otpFragment;
    FragmentTransaction transaction;

    MainActivity activity;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding         = FragmentLoginBinding.inflate(inflater, container, false);

        activity        = (MainActivity) getActivity();

        fragmentManager = getChildFragmentManager();
        reqotpFragment  = new ReqotpFragment();
        otpFragment     = new OtpFragment();

        transaction     = fragmentManager.beginTransaction();
        transaction.replace(binding.fl.getId(), reqotpFragment).
                setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out).
                commit();

        activity.loginController.createTentangAplikasi();
        binding.tvtentangplikasi.setOnClickListener(this);

        return binding.getRoot();
    }

    public void show(){
        transaction     = fragmentManager.beginTransaction();
        transaction.replace(binding.fl.getId(), otpFragment);
        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        transaction.commit();
    }



    @Override
    public void onClick(View v) {
        if(v.getId() == binding.tvtentangplikasi.getId()){
            if(!activity.loginController.alertDialog.isShowing()){
                activity.loginController.alertDialog.show();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        activity.player.setPlayWhenReady(false);
    }
}