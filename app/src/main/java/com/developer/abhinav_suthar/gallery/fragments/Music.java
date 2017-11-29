package com.developer.abhinav_suthar.gallery.fragments;


import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.developer.abhinav_suthar.gallery.R;
import com.developer.abhinav_suthar.gallery.adapters.Main_AudioAdapter;
import com.developer.abhinav_suthar.gallery.extras.Utils;
import com.developer.abhinav_suthar.gallery.services.BackgroundVideoPlay;

import java.util.ArrayList;
import java.util.HashMap;

public class Music extends Fragment {

    RecyclerView recyclerView;
    MediaPlayer musicPlayer;

    public Music() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_music, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.tab_3_music);
        new LoadAudioList().execute();
        playControls(view);
    }

    private class LoadAudioList extends AsyncTask<String, Void, String> {

        ArrayList<HashMap<String,String>> music_list = new ArrayList<>();

        @Override
        protected String doInBackground(String... strings) {

            Uri uriExternal = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE
                    , MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION
                    , MediaStore.Audio.Media._ID, MediaStore.Images.Media.DATA};
            Cursor cur = getContext().getContentResolver().query(uriExternal, projection, null, null, null);


            if (cur != null) {
                while (cur.moveToNext()) {
                    int artistColumn    = cur.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                    int titleColumn     = cur.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int albumColumn     = cur.getColumnIndex(MediaStore.Audio.Media.ALBUM);
                    int durationColumn  = cur.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                    int idColumn        = cur.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    int filePathIndex   = cur.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                    HashMap<String, String> song_detail = new HashMap<>();
                    song_detail.put("key_artist",   cur.getString(artistColumn));
                    song_detail.put("key_title",    cur.getString(titleColumn));
                    song_detail.put("key_album",    cur.getString(albumColumn));
                    song_detail.put("key_duration", cur.getString(durationColumn));
                    song_detail.put("key_id",       cur.getString(idColumn));
                    song_detail.put("key_path",     cur.getString(filePathIndex));
                    music_list.add(song_detail);

                }
                cur.close();
            }

            return "Suthar";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (music_list.isEmpty()) return;
            Utils.setMusicList(music_list);


            Main_AudioAdapter adapter = new Main_AudioAdapter(getContext(), music_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
            if(recyclerView!=null){
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
            }

        }
    }


    private void playControls(View view){

        view.findViewById(R.id.tab_3_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPlayer = BackgroundVideoPlay.getMediaPlayer();
                if (musicPlayer==null) return;
                if (musicPlayer.isPlaying()) musicPlayer.pause();
                else musicPlayer.start();
            }
        });
    }
}
