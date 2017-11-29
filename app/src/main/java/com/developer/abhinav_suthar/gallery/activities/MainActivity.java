package com.developer.abhinav_suthar.gallery.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Toast;

import com.developer.abhinav_suthar.gallery.R;
import com.developer.abhinav_suthar.gallery.adapters.Main_AudioAdapter;
import com.developer.abhinav_suthar.gallery.adapters.Main_PhotoAdapter;
import com.developer.abhinav_suthar.gallery.adapters.Main_VideoAdapter;
import com.developer.abhinav_suthar.gallery.extras.Utils;
import com.developer.abhinav_suthar.gallery.fragments.Music;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public Context context;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));

        setContentView(R.layout.activity_main);
        context = this;





        //Setup view pager and tab layout
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        loadAlbum();
        registerContentObserver();

    }

    private void loadAlbum(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED){
            new LoadPhotoAlbumList().execute();
            new LoadVideoAlbumList().execute();
            //new LoadAudioList().execute();
        }else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }



    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position==2) return new Music();
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Photos";
                case 1:
                    return "Videos";
                case 2:
                    return "Music";
            }
            return null;
        }
    }
    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

            if (sectionNumber==1) {
                return inflater.inflate(R.layout.tab1_photos, container, false);
            }else if (sectionNumber==2){
                return inflater.inflate(R.layout.tab2_videos, container, false);
            }else if (sectionNumber==3) {
                return inflater.inflate(R.layout.tab3_audio, container, false);
            }return null;
        }

    }



    private class LoadPhotoAlbumList extends AsyncTask<String, Void, String> {

        private ArrayList<HashMap<String, String>> AlbumList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            String path, album, timestamp, countPhoto;

            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};
            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name", null, null);
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name", null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});


            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                countPhoto = getContentResolver().query(uriExternal, projection, "bucket_display_name = \""+album+"\"", null, null).getCount()+" Photos";
                if (countPhoto.equals("0 Photos")) countPhoto = getContentResolver().query(uriInternal, projection, "bucket_display_name = \""+album+"\"", null, null).getCount()+" Photos";
                HashMap<String, String> temp = new HashMap<>();
                temp.put("key_path", path);
                temp.put("key_album", album);
                temp.put("key_timestamp", timestamp);
                temp.put("key_countPhoto", countPhoto);

                AlbumList.add(temp);
            }

            if (cursorExternal != null) cursorExternal.close();
            if (cursorInternal != null) cursorInternal.close();
            cursor.close();
            return "null";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            int time_stamp[] = new int[AlbumList.size()];

            final RecyclerView recyclerView = findViewById(R.id.viewPhotos);
            Main_PhotoAdapter main_photoAdapter = new Main_PhotoAdapter(context, AlbumList);
            //PhotosAdapter adapter = new PhotosAdapter(AlbumList);

            GridLayoutManager layoutManager;
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                layoutManager = new GridLayoutManager(context, 2);
            } else {
                layoutManager = new GridLayoutManager(context, 3);
            }

            if(recyclerView!=null){
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(main_photoAdapter);
            }

            Utils.ImageAlbumDetails(AlbumList);
        }
    }
    private class LoadVideoAlbumList extends AsyncTask<String, Void, String> {

        private ArrayList<HashMap<String, String>> VideoList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            String path, album, timestamp, countPhoto;

            Uri uriExternal = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Video.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED};
            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name", null, null);
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name", null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});

            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                countPhoto = getContentResolver().query(uriExternal, projection, "bucket_display_name = \""+album+"\"", null, null).getCount()+"";
                if (countPhoto.equals("0")) countPhoto = getContentResolver().query(uriInternal, projection, "bucket_display_name = \""+album+"\"", null, null).getCount()+"";
                HashMap<String, String> temp = new HashMap<>();
                temp.put("key_path", path);
                temp.put("key_album", album);
                temp.put("key_timestamp", timestamp);
                temp.put("key_countPhoto", countPhoto);

                VideoList.add(temp);
            }
            if (cursorExternal != null) cursorExternal.close();
            if (cursorInternal != null) cursorInternal.close();
            cursor.close();
            //Collections.sort(imageList, imageList.get().get("key_timestamp"));
            //Collections.sort(albumList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return "Abhi";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            RecyclerView recyclerView = findViewById(R.id.viewVideos);
            //VideoAdapter adapter = new VideoAdapter(VideoList);
            Main_VideoAdapter main_videoAdapter = new Main_VideoAdapter(context, VideoList);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            if(recyclerView!=null){
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(main_videoAdapter);
            }
            Utils.VideoAlbumDetails(VideoList);
        }
    }
    private class LoadAudioList extends AsyncTask<String, Void, String> {

        ArrayList<HashMap<String,String>> music_list = new ArrayList<>();

        @Override
        protected String doInBackground(String... strings) {

            Uri uriExternal = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE
                    , MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DURATION
                    , MediaStore.Audio.Media._ID, MediaStore.Images.Media.DATA};
            Cursor cur = getContentResolver().query(uriExternal, projection, null, null, null);


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

            RecyclerView recyclerView = findViewById(R.id.viewAudio);
            Main_AudioAdapter adapter = new Main_AudioAdapter(context, music_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
            if(recyclerView!=null){
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(adapter);
            }

        }
    }




    {
        observer_1 = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                new LoadPhotoAlbumList().execute();
            }
        };

        observer_2 = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                new LoadVideoAlbumList().execute();
            }
        };
    }
    ContentObserver observer_1, observer_2;
    private void registerContentObserver() {
        unregisterContentObserver();

        getContentResolver().registerContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, true, observer_1);
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, observer_1);
        getContentResolver().registerContentObserver(MediaStore.Video.Media.INTERNAL_CONTENT_URI, true, observer_2);
        getContentResolver().registerContentObserver(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, true, observer_2);

    }
    private void unregisterContentObserver() {
        try {
            getContentResolver().unregisterContentObserver(observer_1);
            getContentResolver().unregisterContentObserver(observer_2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 101:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {loadAlbum();break;}
                else Toast.makeText(MainActivity.this, "You must give access to storage.", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterContentObserver();
    }
    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem()!=0) mViewPager.setCurrentItem(0);
        else super.onBackPressed();
    }
}

