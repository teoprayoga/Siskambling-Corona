package com.teoajus.coronanenten.controllers;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.OAuthCredential;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.teoajus.coronanenten.MainActivity;
import com.teoajus.coronanenten.R;
import com.teoajus.coronanenten.databinding.DialogTentangappliasiBinding;
import com.teoajus.coronanenten.models.Reqotp;
import com.teoajus.coronanenten.models.User;
import com.teoajus.coronanenten.statics.MyLog;
import com.teoajus.coronanenten.utils.VolleySingleton;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class LoginController extends PhoneAuthProvider.OnVerificationStateChangedCallbacks {

    MainActivity activity;

    public LoginController(Context context){
        this.activity               = (MainActivity) context;
    }

    private PhoneAuthProvider.ForceResendingToken resendingToken;
    private String verificationId;

    @Override
    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
        Log.e(MyLog.TAG, phoneAuthCredential.getSmsCode());
        sendOtpCode(phoneAuthCredential);
    }

    @Override
    public void onVerificationFailed(@NonNull FirebaseException e) {
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            Log.e(MyLog.TAG,e.toString());
        } else if (e instanceof FirebaseTooManyRequestsException) {
            Log.e(MyLog.TAG, e.toString());
        }
    }

    @Override
    public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
        super.onCodeSent(verificationId, forceResendingToken);
        Log.e(MyLog.TAG, "verificationId: "+verificationId);
        this.verificationId = verificationId;
        resendingToken      = forceResendingToken;
    }

    @Override
    public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
        super.onCodeAutoRetrievalTimeOut(s);
        Log.e(MyLog.TAG, s);
    }

    public void requestOtp(Reqotp reqotp){
        String formattedNumber;
        if(Build.VERSION.SDK_INT >= 21) {
            formattedNumber = PhoneNumberUtils.formatNumberToE164(reqotp.getTelp(), reqotp.getKode());
        } else {
            formattedNumber = PhoneNumberUtils.formatNumber(reqotp.getTelp(), reqotp.getKode());
        }
        if(formattedNumber!=null){
            activity.myFirebase.getProvider().verifyPhoneNumber(
                    formattedNumber, 120, TimeUnit.SECONDS, activity, this
            );
            activity.getLoginFragment().show();
        }
        else{
            Toast.makeText(activity, "Nomor telepon salah", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendOtpCode(String code){
        if(verificationId!=null){
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(this.verificationId, code);
            activity.myFirebase.getAuth().signInWithCredential(credential).
                    addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    FirebaseUser firebaseUser   = authResult.getUser();
                    DocumentReference reference = activity.myFirebase.getFirestore().document("users/"+firebaseUser.getUid());
                    reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if(documentSnapshot==null){
                                return;
                            }
                            if(!documentSnapshot.exists()){
                                String spos = activity.preferencesLocation.getString("pos", null);
                                GeoPoint pos = null;
                                if(spos!=null){
                                    pos = new Gson().fromJson(spos, GeoPoint.class);
                                }
                                else{
                                    pos = new GeoPoint(-10.1718, 95.31644);
                                }
                                String token = activity.preferencesToken.getString("token", null);
                                if(token == null){
                                    token = "";
                                }
                                Date date            = new Date(System.currentTimeMillis());
                                User user = new User(
                                        firebaseUser.getUid(),
                                        firebaseUser.getPhoneNumber(),
                                        pos,
                                        token,
                                        new Timestamp(date),
                                        0
                                );
                                reference.set(user, SetOptions.merge());
                            }
                        }
                    });
                }
            }).
                    addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(activity,"Kode Verifikasi Salah", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            Toast.makeText(activity, "Kuota kode OTP habis", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendOtpCode(PhoneAuthCredential credential){
        activity.myFirebase.getAuth().signInWithCredential(credential).
                addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser firebaseUser   = authResult.getUser();
                        DocumentReference reference = activity.myFirebase.getFirestore().document("users/"+firebaseUser.getUid());
                        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if(documentSnapshot==null){
                                    return;
                                }
                                if(!documentSnapshot.exists()){
                                    String spos = activity.preferencesLocation.getString("pos", null);
                                    GeoPoint pos = null;
                                    if(spos!=null){
                                        pos = new Gson().fromJson(spos, GeoPoint.class);
                                    }
                                    else{
                                        pos = new GeoPoint(-10.1718, 95.31644);
                                    }
                                    String token = activity.preferencesToken.getString("token", null);
                                    if(token == null){
                                        token = "";
                                    }
                                    Date date            = new Date(System.currentTimeMillis());
                                    User user = new User(
                                            firebaseUser.getUid(),
                                            firebaseUser.getPhoneNumber(),
                                            pos,
                                            token,
                                            new Timestamp(date),
                                            0
                                    );
                                    reference.set(user, SetOptions.merge());
                                }
                            }
                        });
                    }
                }).
                addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(activity,"Kode Verifikasi Salah", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void resendCodeVerify(String phone){
        if(resendingToken != null){
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phone, 120, TimeUnit.SECONDS, activity, this, resendingToken
            );
        }
        Toast.makeText(activity,"Kode dikirim ulang", Toast.LENGTH_SHORT).show();
    }


    public AlertDialog alertDialog;
    public void createTentangAplikasi(){
        AlertDialog.Builder builder             = new AlertDialog.Builder(activity);
        DialogTentangappliasiBinding binding    = DialogTentangappliasiBinding.inflate(activity.getLayoutInflater());

        binding.pv.setPlayer(activity.player);
        DataSource.Factory factory              = new DefaultDataSourceFactory(activity, Util.getUserAgent(activity, activity.getString(R.string.app_name)));
        MediaSource mediaSource                 = new ProgressiveMediaSource.Factory(factory).createMediaSource(Uri.parse(activity.getString(R.string.videokumparan)));
        activity.player.prepare(mediaSource);
        activity.player.addListener(playerListener);
        builder.setView(binding.getRoot());

        alertDialog = builder.create();
        alertDialog.setOnDismissListener(dialog -> {
            activity.player.setPlayWhenReady(false);
        });
        alertDialog.setOnShowListener(dialog -> {
            activity.player.setPlayWhenReady(true);
        });;
    }
    private Player.EventListener playerListener = new Player.EventListener() {
        String status = "";
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {

        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            com.google.android.exoplayer2.util.Log.i(MyLog.TAG, reason+"");
        }

        @Override
        public void onSeekProcessed() {
            com.google.android.exoplayer2.util.Log.i(MyLog.TAG, "onprocessed");
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    status = "Video buffering";
                    break;
                case Player.STATE_ENDED:
                    status = "Video stopped";
                    break;
                case Player.STATE_IDLE:
                    status = "Video idle";
                    break;
                case Player.STATE_READY:
                    status = "Video ready";
                    break;
                default:
                    status = "Default";
                    break;
            }
        }
    };
}
