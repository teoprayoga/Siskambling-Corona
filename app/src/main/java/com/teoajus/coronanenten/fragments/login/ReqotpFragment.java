package com.teoajus.coronanenten.fragments.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.teoajus.coronanenten.MainActivity;
import com.teoajus.coronanenten.databinding.FragmentReqotpBinding;
import com.teoajus.coronanenten.models.Reqotp;

public class ReqotpFragment extends Fragment {

    FragmentReqotpBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding     = FragmentReqotpBinding.inflate(inflater, container, false);

        binding.btnsubmit.setOnClickListener(v -> {
            String kode         = binding.ccp.getSelectedCountryNameCode();
            String kodeplus     = binding.ccp.getSelectedCountryCodeWithPlus();
            String telp = binding.ettelepon.getText().toString();
            Reqotp reqotp = new Reqotp(kode, kodeplus, telp);

            MainActivity activity = (MainActivity) getActivity();
            activity.loginController.requestOtp(reqotp);
        });
        return binding.getRoot();
    }
}
