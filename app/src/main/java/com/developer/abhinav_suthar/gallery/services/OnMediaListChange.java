package com.developer.abhinav_suthar.gallery.services;

import android.app.Service;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

public class OnMediaListChange extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        registerContentObserver();

        return super.onStartCommand(intent, flags, startId);
    }


    ContentObserver observer_1, observer_2, observer_3, observer_4;

    private void registerContentObserver()
    {
        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
                observer_1 = new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        Toast.makeText(getApplicationContext(), "MediaChange1", Toast.LENGTH_SHORT).show();
                        super.onChange(selfChange);
                    }
                }
        );
        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, true,
                observer_2 = new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        Toast.makeText(getApplicationContext(), "MediaChange2", Toast.LENGTH_SHORT).show();
                        super.onChange(selfChange);
                    }
                }
        );
        getContentResolver().registerContentObserver(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true,
                observer_3 = new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        Toast.makeText(getApplicationContext(), "MediaChange1", Toast.LENGTH_SHORT).show();
                        super.onChange(selfChange);
                    }
                }
        );
        getContentResolver().registerContentObserver(android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI, true,
                observer_4 = new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        Toast.makeText(getApplicationContext(), "MediaChange2", Toast.LENGTH_SHORT).show();
                        super.onChange(selfChange);
                    }
                }
        );

    }

    private void unregisterContentObserver()
    {
        getContentResolver().unregisterContentObserver(observer_1);
        getContentResolver().unregisterContentObserver(observer_2);
        getContentResolver().unregisterContentObserver(observer_3);
        getContentResolver().unregisterContentObserver(observer_4);
    }

    @Override
    public void onDestroy() {
        unregisterContentObserver();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
