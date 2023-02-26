package com.example.cms_netflixpp;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class VolleySingleton {
    private VolleySingleton mInstance;
    private RequestQueue mRequestQueue;
    private final Context mCtx;

    public VolleySingleton(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();

        new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final Map<String, Bitmap> cache = new Map<String, Bitmap>() {
                        @Override
                        public int size() {
                            return 0;
                        }

                        @Override
                        public boolean isEmpty() {
                            return false;
                        }

                        @Override
                        public boolean containsKey(@Nullable Object o) {
                            return false;
                        }

                        @Override
                        public boolean containsValue(@Nullable Object o) {
                            return false;
                        }

                        @Nullable
                        @Override
                        public Bitmap get(@Nullable Object o) {
                            return null;
                        }

                        @Nullable
                        @Override
                        public Bitmap put(String s, Bitmap bitmap) {
                            return null;
                        }

                        @Nullable
                        @Override
                        public Bitmap remove(@Nullable Object o) {
                            return null;
                        }

                        @Override
                        public void putAll(@NonNull Map<? extends String, ? extends Bitmap> map) {

                        }

                        @Override
                        public void clear() {

                        }

                        @NonNull
                        @Override
                        public Set<String> keySet() {
                            return null;
                        }

                        @NonNull
                        @Override
                        public Collection<Bitmap> values() {
                            return null;
                        }

                        @NonNull
                        @Override
                        public Set<Entry<String, Bitmap>> entrySet() {
                            return null;
                        }
                    };

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public synchronized VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

}