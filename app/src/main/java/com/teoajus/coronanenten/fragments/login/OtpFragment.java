package com.teoajus.coronanenten.fragments.login;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.teoajus.coronanenten.MainActivity;
import com.teoajus.coronanenten.databinding.FragmentOtpBinding;
import com.teoajus.coronanenten.statics.MyLog;

public class OtpFragment extends Fragment {

    FragmentOtpBinding binding;
    MainActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding     = FragmentOtpBinding.inflate(inflater, container, false);
        activity    = (MainActivity) getActivity();

        binding.txtPinEntry.setOnPinEnteredListener(str -> {
            activity.loginController.sendOtpCode(str.toString());
        });

        binding.btnsubmit.setOnClickListener(v -> {
            activity.loginController.sendOtpCode(binding.txtPinEntry.getText().toString());
        });
        return binding.getRoot();
    }
}
