package com.teoajus.coronanenten.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class VolleySingleton {

    private static final String TAG = VolleySingleton.class.getSimpleName();
    private static VolleySingleton singletonInstance = null;
    private RequestQueue requestQueue;
    private ImageLoader imageLoader;

    private VolleySingleton(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        imageLoader = new ImageLoader(this.requestQueue, new ImageLoader.ImageCache() {

            private final LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(30);
            public void putBitmap(String url, Bitmap bitmap) {
                lruCache.put(url, bitmap);
            }

            public Bitmap getBitmap(String url) {
                return lruCache.get(url);
            }
        });
    }

    public static VolleySingleton getInstance(Context context) {
        if (singletonInstance == null) {
            singletonInstance = new VolleySingleton(context);
        }
        return singletonInstance;
    }

    public RequestQueue getRequestQueue() {
        return this.requestQueue;
    }

    public ImageLoader getImageLoader() {
        return this.imageLoader;
    }
}
