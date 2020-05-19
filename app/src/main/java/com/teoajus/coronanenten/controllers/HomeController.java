package com.teoajus.coronanenten.controllers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.maps.android.SphericalUtil;
import com.teoajus.coronanenten.MainActivity;
import com.teoajus.coronanenten.R;
import com.teoajus.coronanenten.models.Riwayat;
import com.teoajus.coronanenten.models.RsRujukan;
import com.teoajus.coronanenten.models.User;
import com.teoajus.coronanenten.services.LocationService;
import com.teoajus.coronanenten.statics.MyLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeController {

    MainActivity activity;

    public HomeController(Context context){
        activity        = (MainActivity) context;

        Intent intent   = new Intent(activity, LocationService.class);
        activity.startService(intent);

        colors.add(activity.getColor(R.color.blue_A40030));//BLUE
        colors.add(activity.getColor(R.color.yellow_A40030));//YELLOW PDP
        colors.add(activity.getColor(R.color.red_A40030));//RED ODP
        colors.add(activity.getColor(R.color.green_A40030));//RED ODP

        bitmaps.add(getBitmap(R.drawable.ic_markerblue));
        bitmaps.add(getBitmap(R.drawable.ic_markeryellow));
        bitmaps.add(getBitmap(R.drawable.ic_markerred));

        stats.add("Normal");
        stats.add("Suhu tubuh tinggi");
        stats.add("Terinfeksi");
    }

    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = activity.getResources().getDrawable(drawableRes, null);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    List<Circle> circles    = new ArrayList<>();
    List<Marker> markers    = new ArrayList<>();

    List<Integer> colors    = new ArrayList<>();
    List<User> users        = new ArrayList<>();
    List<Bitmap> bitmaps    = new ArrayList<>();
    List<String> stats      = new ArrayList<>();

    public List<Marker> getMarkers() {
        return markers;
    }

    public List<String> getStats() {
        return stats;
    }

    public List<Integer> getColors() {
        return colors;
    }

    List<RsRujukan> getRsRujukan(){
        String rs               = activity.preferencesRujukan.getString("rujukan", null);
        JsonObject jsonObject   = new Gson().fromJson(rs, JsonObject.class);
        String rujukan          = new Gson().toJson(jsonObject.get("rs").getAsJsonArray());
        RsRujukan[] rsRujukans1 = new Gson().fromJson(rujukan, RsRujukan[].class);
        return Arrays.asList(rsRujukans1);
    }

    public void setMap(GoogleMap googleMap, List<User> userList){
        for(Circle c : circles){
            c.remove();
        }
        circles.clear();
        for(Marker m : markers){
            m.remove();
        }
        users.clear();
        for (User user : userList) {
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(new LatLng(user.getPos().getLatitude(), user.getPos().getLongitude()));
            circleOptions.radius(1000);
            circleOptions.fillColor(colors.get(user.getStatus()));
            circleOptions.strokeColor(colors.get(user.getStatus()));
            Circle circle = googleMap.addCircle(circleOptions);
            circles.add(circle);

            activity.iconGenerator.setColor(colors.get(user.getStatus()));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmaps.get(user.getStatus()))).position(new LatLng(user.getPos().getLatitude(), user.getPos().getLongitude()));
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(user);
            marker.setSnippet("user"+marker.getId());
            markers.add(marker);

            users.add(user);
        }

        for(RsRujukan rsRujukan : getRsRujukan()){
            CircleOptions circleOptions = new CircleOptions();
            circleOptions.center(new LatLng(rsRujukan.getPos().getLatitude(), rsRujukan.getPos().getLongitude())).
                    fillColor(getColors().get(3)).
                    strokeColor(getColors().get(3)).
                    radius(1000);
            Circle circle = googleMap.addCircle(circleOptions);
            circles.add(circle);

            MarkerOptions markerOptions = new MarkerOptions();
            Bitmap bitmap               = getBitmap(R.drawable.ic_hospital2);
            markerOptions.
                    icon(BitmapDescriptorFactory.fromBitmap(bitmap)).
                    position(new LatLng(rsRujukan.getPos().getLatitude(), rsRujukan.getPos().getLongitude()));
            Marker marker = googleMap.addMarker(markerOptions);
            marker.setTag(rsRujukan);
            marker.setSnippet("rs"+marker.getId());
            markers.add(marker);
        }
    }

    public void setListUser(){
        List<User> list = new ArrayList<>();
        CollectionReference reference = activity.myFirebase.getFirestore().collection("users");
        reference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots==null){
                    return;
                }

                User me                     = null;
                FirebaseUser firebaseUser   = activity.myFirebase.getAuth().getCurrentUser();
                list.clear();

                for(DocumentSnapshot snapshot : queryDocumentSnapshots){
                    Map<String, Object> map = snapshot.getData();
                    String string           = new Gson().toJson(map);
                    User user               = new Gson().fromJson(string, User.class);
                    list.add(user);
                }

                for(User user : list){
                    if(firebaseUser == null){
                        return;
                    }
                    if(user.getId().equals(firebaseUser.getUid())){
                        me = user;
                        break;
                    }
                }

                if(me != null || firebaseUser != null){
                    for(User user : list){
                        if(!user.getId().equals(me.getId())){
                            LatLng myLatLng     = new LatLng(me.getPos().getLatitude(), me.getPos().getLongitude());
                            LatLng latLnguser   = new LatLng(user.getPos().getLatitude(), user.getPos().getLongitude());
                            int statusUser      = user.getStatus();
                            double distance     = SphericalUtil.computeDistanceBetween(myLatLng, latLnguser);

                            if(distance<=1000){
                                CollectionReference collectionReference = activity.myFirebase.getFirestore().collection("riwayats");
                                DocumentReference documentReference     = collectionReference.document(me.getId()+user.getId());
                                Riwayat riwayat = new Riwayat(
                                        documentReference.getId(),
                                        me.getPos(),
                                        me.getId(),
                                        user.getId()
                                );
                                documentReference.set(riwayat, SetOptions.merge());
                                if(statusUser == 2){
                                    me.setStatus(2);
                                }
                            }
                        }
                    }

                    for(RsRujukan rs : getRsRujukan()){
                        LatLng latLngMe     = new LatLng(me.getPos().getLatitude(), me.getPos().getLongitude());
                        LatLng latLngUser   = new LatLng(rs.getPos().getLatitude(), rs.getPos().getLongitude());
                        double distance     = SphericalUtil.computeDistanceBetween(latLngMe, latLngUser);
                        if(distance<=1000){
                            me.setStatus(0);
                        }
                    }

                    DocumentReference reference1 = activity.myFirebase.getFirestore().document("users/"+firebaseUser.getUid());
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", me.getStatus());
                    reference1.set(map, SetOptions.merge());
                }
                else{
                    Log.e(MyLog.TAG, "Anjeng");
                }

            }
        });
    }

    public List<User> getRiwayat(List<Riwayat> riwayatList, List<User> userList){
        List<User> list = new ArrayList<>();
        for(User u : userList){
            for(Riwayat r : riwayatList){
                if(u.getId().equals(r.getId2())){
                    list.add(u);
                }
            }
        }
        return list;
    }
}