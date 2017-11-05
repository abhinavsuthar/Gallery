package com.developer.abhinav_suthar.gallery.extras;

import android.app.Service;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Abhinav_Suthar on 11/5/2017.
 */

public class ServiceCopyDelete extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action==null) action="None";
        if (action.equals("copy")){
            int albumPosition = intent.getIntExtra("albumPosition", 0);
            Copy copy = new Copy();
            copy.execute(albumPosition);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private class Copy extends AsyncTask<Integer,Void,String>{

        int totalSuccessfulCopy = 0;
        int totalSelectedItems = 0;

        @Override
        protected String doInBackground(Integer... s) {

            int i = s[0];
            //int numOfSelectedItem = Integer.parseInt(s[1]);
            ArrayList<HashMap<String, String>> imageList = Utils.getMediaList();
            ArrayList<String> AlbumPaths = Utils.getImageAlbumPath();
            ArrayList<String> AlbumNames = Utils.getImageAlbumNames();
            Boolean[] itemSelectionMode = Utils.getSelectedMediaItems();
            totalSelectedItems = itemSelectionMode.length;

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
                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setMessage("Please wait... ("+totalSuccessfulCopy+"/"+numOfSelectedItem+")");
                            }
                        });*/
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();
                        return null;
                    }
                }
            }
            if (i==(AlbumNames.size()-1)){
                AlbumPaths.remove((AlbumPaths.size()-1));
            }
            return ""+totalSuccessfulCopy;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(getApplicationContext(), s+" File copied successfully !", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
