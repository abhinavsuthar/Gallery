package com.developer.abhinav_suthar.gallery.extras;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.developer.abhinav_suthar.gallery.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

public class ServiceCopyDelete extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String action = intent.getAction();
        if (action==null) action="None";
        if (action.equals("copy")){
            int albumPosition = intent.getIntExtra("albumPosition", 0);
            int photoOrVideo = intent.getIntExtra("photoOrVideo", 1);
            Copy copy = new Copy();
            copy.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,albumPosition, photoOrVideo);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private class Copy extends AsyncTask<Integer,Integer,String>{

        int totalSuccessfulCopy = 0;
        int totalSelectedItems = 0;

        Notification.Builder notificationBuilder;
        NotificationManager notificationManager;
        Notification notification;

        @Override
        protected void onPreExecute() {

            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationBuilder = new Notification.Builder(getApplicationContext());
            notificationBuilder
                    //.setOngoing(true)
                    .setSmallIcon(R.mipmap.launcher)
                    .setContentTitle("Copying files...")
                    .setContentText("Calculating..")
                    .setProgress(100, 0, false);

            //Send the notification:
            notification = notificationBuilder.build();
            notificationManager.notify(100, notification);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... s) {

            int i = s[0];
            ArrayList<String> AlbumPaths;
            ArrayList<String> AlbumNames;
            if (s[1]==0){
                AlbumPaths = Utils.getImageAlbumPath();
                AlbumNames = Utils.getImageAlbumNames();
            }else {
                AlbumPaths = Utils.getVideoAlbumPath();
                AlbumNames = Utils.getVideoAlbumNames();
            }
            ArrayList<HashMap<String, String>> imageList = Utils.getMediaList();
            Boolean[] itemSelectionMode = Utils.getSelectedMediaItems();
            totalSelectedItems = 0;
            for (int j=0;j<itemSelectionMode.length;j++){
                if (itemSelectionMode[j]) totalSelectedItems++;
            }

            for (int ii=0;ii<imageList.size();ii++){
                if (itemSelectionMode[ii]){
                    File f = new File(imageList.get(ii).get("key_path"));
                    String name = f.getName();
                    int dot = name.lastIndexOf('.');
                    String base = (dot == -1) ? name : name.substring(0, dot);
                    String extension = (dot == -1) ? "" : name.substring(dot+1);

                    String fileNewPath = AlbumPaths.get(i) + "/" + base + "_Copy" + "." + extension;

                    int alreadyExist = 0;
                    while ((new File(fileNewPath).exists())){
                        fileNewPath = AlbumPaths.get(i) + "/" + base + "_Copy(" + alreadyExist + ")." + extension;
                        alreadyExist++;
                    }
                    try {
                        totalSuccessfulCopy = copyFile(f, new File(fileNewPath), totalSuccessfulCopy);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }publishProgress(totalSuccessfulCopy);
                }
            }
            if (i==(AlbumNames.size()-1)){
                AlbumPaths.remove((AlbumPaths.size()-1));
            }
            return ""+totalSuccessfulCopy;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //Update notification information:
            notificationBuilder.setProgress(totalSelectedItems, values[0], false).setContentText(values[0]+"/"+totalSelectedItems);

            //Send the notification:
            notification = notificationBuilder.build();
            notificationManager.notify(100, notification);
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getApplicationContext(), s+" File copied successfully !", Toast.LENGTH_SHORT).show();
            notificationManager.cancel(100);
            super.onPostExecute(s);
            stopSelf();
        }

        private int copyFile(File sourceFile, File destFile, int TSC) throws IOException {
            if (!sourceFile.exists()) {
                Toast.makeText(getApplicationContext(), "File already exist ! ! !", Toast.LENGTH_LONG).show();
                return TSC;
            }

            FileChannel source = null;
            FileChannel destination = null;
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            if (destination != null && source != null) {
                destination.transferFrom(source, 0, source.size());
            }
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
            scanFile(destFile.getAbsolutePath());
            return ++TSC;
        }

        private void scanFile(String path){
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{path}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {

                }

                @Override
                public void onScanCompleted(String s, Uri uri) {

                }
            });
        }

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
