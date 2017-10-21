package com.developer.abhinav_suthar.gallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
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

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.developer.abhinav_suthar.gallery.extras.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public Context context;
    private int pageNumber = 0;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        setContentView(R.layout.activity_main);

        loadAlbum();

        context = MainActivity.this;

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position==1) pageNumber = 1;
                else pageNumber = 0;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loadBannerAd();
                    }
                });
            }
        }, 1000);
    }

    private void loadAlbum(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            LoadAlbumList loadPhotos = new LoadAlbumList();
            loadPhotos.execute();
            LoadVideoList loadVideoList = new LoadVideoList();
            loadVideoList.execute();
        }else ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
    }

    private void loadBannerAd(){
        final AdView mAdView = (AdView) findViewById(R.id.adView00);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case 501:{
                if (resultCode==RESULT_OK) loadAlbum();
            }case 502:{
                if (resultCode==RESULT_OK) loadAlbum();
            }
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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Photos";
                case 1:
                    return "Videos";
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

            if (sectionNumber==1) {
                return inflater.inflate(R.layout.tab1_photos, container, false);

            }else if (sectionNumber==2){
                return inflater.inflate(R.layout.tab2_videos, container, false);
            }
            return null;
        }

    }

    /**
     * LoadAlbumList
     */
    private class LoadAlbumList extends AsyncTask<String, Void, String> {

        private ArrayList<HashMap<String, String>> AlbumList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            String path = null;
            String album = null;
            String timestamp = null;
            String countPhoto = null;

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
            //Collections.sort(imageList, imageList.get().get("key_timestamp"));
            //Collections.sort(albumList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return "Abhi";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.viewPhotos);
            PhotosAdapter adapter = new PhotosAdapter(AlbumList);

            GridLayoutManager layoutManager;
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                layoutManager = new GridLayoutManager(context, 3);
            } else {
                layoutManager = new GridLayoutManager(context, 5);
            }

            try {
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setHasFixedSize(true);
                recyclerView.setAdapter(adapter);
            } catch (Exception e) {
                e.printStackTrace();
                recreate();
            }

            Utils.ImageAlbumDetails(AlbumList);
        }
    }

    /**
     * PhotosAdapter
     */
    class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.MyViewHolder>{

        private ArrayList<HashMap<String, String>> albumList = new ArrayList<>();

        public PhotosAdapter(ArrayList<HashMap<String, String>> albumList){
            this.albumList = albumList;

        }

        @Override
        public PhotosAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.photo_0, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final PhotosAdapter.MyViewHolder holder, int position) {
            final HashMap<String, String> photoAlbumDetail = albumList.get(position);

            Glide.with(context)
                    .load(new File(photoAlbumDetail.get("key_path")))
                    .override(200, 200)
                    .centerCrop()
                    .into(holder.albumImage);
            holder.albumTitle.setText(photoAlbumDetail.get("key_album"));
            holder.albumCount.setText(photoAlbumDetail.get("key_countPhoto"));

            holder.albumImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, Photo1.class);
                    intent.putExtra("key_album", photoAlbumDetail.get("key_album"));
                    startActivityForResult(intent, 501);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.fade_out);


                }
            });
        }

        @Override
        public int getItemCount() {
            return albumList.size();
        }
        public class MyViewHolder extends RecyclerView.ViewHolder{
            private ImageView albumImage;
            private TextView albumTitle, albumCount;
            private MyViewHolder(View itemView) {
                super(itemView);
                albumImage = itemView.findViewById(R.id.albumImage);
                albumTitle = itemView.findViewById(R.id.albumTitle);
                albumCount = itemView.findViewById(R.id.albumCount);
            }
        }
    }

    /**
     * LoadAlbumList
     */
    private class LoadVideoList extends AsyncTask<String, Void, String> {

        private ArrayList<HashMap<String, String>> VideoList = new ArrayList<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            String path = null;
            String album = null;
            String timestamp = null;
            String countPhoto = null;

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

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.viewVideos);
            VideoAdapter adapter = new VideoAdapter(VideoList);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            try {
                recyclerView.setHasFixedSize(true);
            } catch (Exception e) {
                e.printStackTrace();
                recreate();
            }
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            Utils.VideoAlbumDetails(VideoList);
        }
    }

    /**
     * VideoAdapter
     */
    class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder>{

        private ArrayList<HashMap<String, String>> VideoList = new ArrayList<>();

        public VideoAdapter(ArrayList<HashMap<String, String>> VideoList){
            this.VideoList = VideoList;
        }

        @Override
        public VideoAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.video_0, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final VideoAdapter.MyViewHolder holder, int position) {
            HashMap<String, String> photoAlbumDetail = VideoList.get(position);

            holder.albumArt.setImageResource(R.drawable.folder);
            holder.albumTitle.setText(photoAlbumDetail.get("key_album"));
            holder.albumCount.setText(photoAlbumDetail.get("key_countPhoto"));

            long millis = Long.parseLong(photoAlbumDetail.get("key_timestamp"))*1000L;
            Date d = new Date(millis);
            DateFormat df=new SimpleDateFormat("dd-MM-yyyy  HH:mm");
            df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            holder.albumModified.setText(df.format(d));

            holder.VdCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, Video1.class);
                    intent.putExtra("key_albumName", VideoList.get(holder.getAdapterPosition()).get("key_album"));
                    startActivityForResult(intent, 502);
                }
            });
        }

        @Override
        public int getItemCount() {
            return VideoList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder{

            private TextView albumTitle, albumCount, albumModified;
            private CardView VdCardView;
            private ImageView albumArt;
            public MyViewHolder(View itemView) {
                super(itemView);
                albumTitle = itemView.findViewById(R.id.txtVdFolderName);
                albumCount = itemView.findViewById(R.id.txtVdCount);
                albumModified = itemView.findViewById(R.id.txtVdModifiedDate);
                albumArt = itemView.findViewById(R.id.imgVdFolderIcon);

                VdCardView = itemView.findViewById(R.id.VdCardView);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (pageNumber==1) mViewPager.setCurrentItem(0);
        else super.onBackPressed();
    }

}

