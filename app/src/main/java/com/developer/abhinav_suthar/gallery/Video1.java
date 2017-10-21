package com.developer.abhinav_suthar.gallery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.developer.abhinav_suthar.gallery.extras.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class Video1 extends AppCompatActivity {

    private Context context;
    private String albumName;
    private RecyclerView recyclerView;
    private ArrayList<HashMap<String, String>> videoAlbumList = new ArrayList<>();
    private int totalSuccessfulCopy = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_1);


        albumName = getIntent().getStringExtra("key_albumName");

        context = this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.photo_1_toolBar);
        toolbar.setTitle(albumName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        loadVideoList();
        loadBannerAd();
    }

    private void loadBannerAd(){
        final AdView mAdView = (AdView) findViewById(R.id.adView11);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadVideoList(){
        LoadVideoAlbum videoAlbum = new LoadVideoAlbum();
        //videoAlbum.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
        videoAlbum.execute(albumName);
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onBackPressed();
        return true;
    }

    private class LoadVideoAlbum extends AsyncTask<String, Void, String> {

        String albumName;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            albumName = strings[0];
            String path ;
            String timestamp = null;
            String duration = null;
            String displayName;
            String size;

            Uri uriExternal = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_MODIFIED,
                    MediaStore.Video.VideoColumns.DURATION, MediaStore.Video.VideoColumns.DISPLAY_NAME, MediaStore.Video.VideoColumns.SIZE};
            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "bucket_display_name = \""+albumName+"\"", null, null);
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "bucket_display_name = \""+albumName+"\"", null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});

            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED));
                displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));
                size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
                duration  = cursor.getString(cursor.getColumnIndex("duration"));
                HashMap<String, String> temp = new HashMap<>();
                temp.put("key_path", path);
                temp.put("key_timestamp", timestamp);
                temp.put("key_duration", duration);
                temp.put("key_displayName", displayName);
                temp.put("key_size", size);

                videoAlbumList.add(temp);
            }

            if (cursorExternal != null) cursorExternal.close();
            if (cursorInternal != null) cursorInternal.close();
            cursor.close();
            sortImageList();
            return "Null";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            recyclerView = (RecyclerView) findViewById(R.id.recyclerVideoAlbum);
            VideoAlbumAdapter adapter = new VideoAlbumAdapter(videoAlbumList);

            LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);

            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);


        }
    }

    class VideoAlbumAdapter extends RecyclerView.Adapter<VideoAlbumAdapter.MyViewHolder>{

        private ArrayList<HashMap<String, String>> VideoList = new ArrayList<>();
        private boolean selectionMode = false;
        private Boolean[] itemSelectionMode;
        private int numOfSelectedItem = 0;

        public VideoAlbumAdapter(ArrayList<HashMap<String, String>> VideoList){
            this.VideoList = VideoList;
            itemSelectionMode = new Boolean[VideoList.size()];
            for (int i=0; i<VideoList.size();i++){
                itemSelectionMode[i] = false;
            }
        }

        @Override
        public VideoAlbumAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_0, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final VideoAlbumAdapter.MyViewHolder holder, int position) {

            holder.VdThumb.setPadding(0,0,0,0);
            holder.VdArrow.setVisibility(View.GONE);
            holder.VdCount.setVisibility(View.GONE);

            Resources r = getResources();
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, r.getDisplayMetrics());
            holder.VdTitle.setPadding(Math.round(px),0,0,0);
            holder.VdModified.setPadding(Math.round(px),0,0,0);

            File f = new File(VideoList.get(position).get("key_path"));

            Glide.with(context)
                    .load(f)
                    .into(holder.VdThumb);
            final String videoName = f.getName();
            holder.VdTitle.setText(videoName);

            long millis = Long.parseLong(VideoList.get(position).get("key_timestamp"))*1000L;
            Date d = new Date(millis);
            DateFormat df=new SimpleDateFormat("dd-MM-yyyy  HH:mm");
            df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
            holder.VdModified.setText(df.format(d));

            float fileSize = f.length()/1024F;
            String fileSizeSuffix = "KB";
            if (fileSize>1024){
                fileSize = fileSize/1024L;
                fileSizeSuffix = "MB";
            }if (fileSize>1024){
                fileSize = fileSize/1024L;
                fileSizeSuffix = "GB";}
            holder.VdFileSize.setText(String.format("%.2f", fileSize)+fileSizeSuffix);

            long duration = Long.parseLong(VideoList.get(position).get("key_duration"));
            d = new Date(duration);
            duration = duration/1000L;
            if (duration<3600) df = new SimpleDateFormat("mm:ss");
            else df = new SimpleDateFormat("HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            holder.VdVideoLength.setVisibility(View.VISIBLE);
            holder.VdVideoLength.setText(df.format(d));

            if (selectionMode){
                holder.VdShare.setVisibility(View.INVISIBLE);
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setChecked(itemSelectionMode[position]);
                holder.VdCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemSelectionMode[holder.getAdapterPosition()] = (!itemSelectionMode[holder.getAdapterPosition()]);
                        holder.checkBox.setChecked(itemSelectionMode[holder.getAdapterPosition()]);
                        if (itemSelectionMode[holder.getAdapterPosition()]) numOfSelectedItem++;
                        else numOfSelectedItem--;
                        getSupportActionBar().setTitle(""+numOfSelectedItem);
                        selectionModeFunctions();
                    }
                });
            }else {
                holder.VdShare.setVisibility(View.VISIBLE);
                holder.checkBox.setVisibility(View.GONE);
                holder.checkBox.setChecked(false);

                holder.VdCardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context,Video2.class);
                        intent.putExtra("key_list", VideoList);
                        intent.putExtra("key_pos", holder.getAdapterPosition());
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
                    }
                });

                holder.VdCardView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        selectionMode = true;
                        numOfSelectedItem++;
                        itemSelectionMode[holder.getAdapterPosition()] = (!itemSelectionMode[holder.getAdapterPosition()]);
                        notifyDataSetChanged();
                        menu.clear();
                        getMenuInflater().inflate(R.menu.selection_mode, menu);
                        getSupportActionBar().setTitle(""+numOfSelectedItem);
                        selectionModeFunctions();
                        return true;
                    }
                });
            }

            holder.VdShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Uri picUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName()
                      //      + ".com.developer.abhinav_suthar.gallery.provider", new File(VideoList.get(holder.getAdapterPosition()).get("key_path")));
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    //intent.setData(picUri);
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(VideoList.get(holder.getAdapterPosition()).get("key_path")));
                    intent.setType("video/*");
                    startActivity(Intent.createChooser(intent, "Share video via..."));
                }
            });

        }

        @Override
        public int getItemCount() {
            return VideoList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{

            private TextView VdTitle, VdCount, VdModified, VdFileSize, VdVideoLength;
            private ImageView VdThumb, VdArrow, VdShare;
            private CardView VdCardView;
            private CheckBox checkBox;
            public MyViewHolder(View itemView) {
                super(itemView);
                VdThumb = itemView.findViewById(R.id.imgVdFolderIcon);
                VdArrow = itemView.findViewById(R.id.imgVdArrow);
                VdShare = itemView.findViewById(R.id.imgVdShare);
                VdTitle = itemView.findViewById(R.id.txtVdFolderName);
                VdCount = itemView.findViewById(R.id.txtVdCount);
                VdModified = itemView.findViewById(R.id.txtVdModifiedDate);
                VdFileSize = itemView.findViewById(R.id.txtVdFileSize);
                VdVideoLength = itemView.findViewById(R.id.txtVdVideoLength);

                VdCardView = itemView.findViewById(R.id.VdCardView);
                checkBox  = itemView.findViewById(R.id.video_1_checkBox);
            }
        }
        private void selectionModeFunctions(){

            if (numOfSelectedItem<1) {
                closeSelectionMode();
                return;
            }

            final MenuItem selectAll = menu.findItem(R.id.selection_select_all);
            MenuItem share = menu.findItem(R.id.selection_share);
            MenuItem delete = menu.findItem(R.id.selection_delete);
            MenuItem close = menu.findItem(R.id.selection_close);
            MenuItem copy = menu.findItem(R.id.selection_copy);

            if (numOfSelectedItem!=VideoList.size())
                selectAll.setIcon(android.R.drawable.checkbox_on_background);
            else selectAll.setIcon(android.R.drawable.checkbox_off_background);

            selectAll.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (numOfSelectedItem==VideoList.size()){
                        for (int i=0; i<VideoList.size();i++) itemSelectionMode[i] = false;
                        numOfSelectedItem = 0;
                        getSupportActionBar().setTitle(""+numOfSelectedItem);
                        selectAll.setIcon(android.R.drawable.checkbox_on_background);
                        notifyDataSetChanged();
                    }else {
                        for (int i=0; i<VideoList.size();i++) itemSelectionMode[i] = true;
                        numOfSelectedItem = VideoList.size();
                        getSupportActionBar().setTitle(""+numOfSelectedItem);
                        selectAll.setIcon(android.R.drawable.checkbox_off_background);
                        notifyDataSetChanged();
                    }
                    return true;
                }
            });

            share.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (numOfSelectedItem<1) return false;
                    ArrayList<Uri> imgUri = new ArrayList<>();
                    for (int i=0;i<VideoList.size();i++){
                        if (itemSelectionMode[i]){
                            imgUri.add(Uri.parse(VideoList.get(i).get("key_path")));
                        }
                    }
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
                    intent.setType("video/*");
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imgUri);
                    startActivity(intent);
                    closeSelectionMode();
                    return true;
                }
            });

            delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    final BottomSheetDialog deleteDialog = new BottomSheetDialog(Video1.this);
                    deleteDialog.setContentView(R.layout.bottom_delete_dialog);
                    deleteDialog.show();
                    Button cancel = (Button) deleteDialog.findViewById(R.id.deleteDialogCancel);
                    Button delete = (Button) deleteDialog.findViewById(R.id.deleteDialogDelete);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {deleteDialog.dismiss();
                        }
                    });
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteDialog.dismiss();
                            final ProgressDialog progressDialog = new ProgressDialog(context);
                            progressDialog.setTitle("Processing...");
                            progressDialog.setMessage("Please wait...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();

                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    String[] imgPaths = new String[numOfSelectedItem];
                                    for (int i=0,j=0;i<VideoList.size();i++){
                                        if (itemSelectionMode[i]){
                                            imgPaths[j] = VideoList.get(i).get("key_path");
                                            j++;
                                        }
                                    }
                                    for (String imgPath : imgPaths) {
                                        totalSuccessfulCopy+=getContentResolver().delete(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                                MediaStore.Video.VideoColumns.DATA + "=?", new String[]{imgPath});

                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.setMessage("Please wait... ("+totalSuccessfulCopy+"/"+numOfSelectedItem+")");
                                            }
                                        });
                                    }
                                    progressDialog.dismiss();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //Load Album Again
                                            VideoList.clear();
                                            loadVideoList();
                                            setResult(RESULT_OK);
                                            closeSelectionMode();
                                            Toast.makeText(context, ""+totalSuccessfulCopy+" File deleted successfully !", Toast.LENGTH_SHORT).show();
                                            totalSuccessfulCopy = 0;
                                        }
                                    });
                                }
                            });
                        }
                    });
                    return true;
                }
            });

            copy.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {

                    final ArrayList<String>  AlbumNames = Utils.getVideoAlbumNames();
                    final ArrayList<String>  AlbumPaths = Utils.getVideoAlbumPath();

                    final BottomSheetDialog copyDialog = new BottomSheetDialog(Video1.this);
                    copyDialog.setContentView(R.layout.bottom_more_dialog);
                    copyDialog.show();
                    ListView listView = (ListView) copyDialog.findViewById(R.id.p2MoreListView);
                    ArrayAdapter<String> adapter=new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, AlbumNames);
                    listView.setAdapter(adapter);


                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                            //Dismiss the copy bottom dialog and initialize new progress dialog to
                            //show progress of copied items
                            copyDialog.dismiss();
                            final ProgressDialog progressDialog = new ProgressDialog(context);
                            progressDialog.setTitle("Processing...");
                            progressDialog.setMessage("Please wait... ("+totalSuccessfulCopy+"/"+numOfSelectedItem+")");
                            progressDialog.setCancelable(false);

                            //This is background task which copies files to given folder
                            final AsyncTask asyncTask = new AsyncTask() {
                                @Override
                                protected Object doInBackground(Object[] params) {
                                    for (int ii=0;ii<VideoList.size();ii++){
                                        if (itemSelectionMode[ii]){
                                            File f = new File(VideoList.get(ii).get("key_path"));
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
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressDialog.setMessage("Please wait... ("+totalSuccessfulCopy+"/"+numOfSelectedItem+")");
                                                    }
                                                });
                                            } catch (final IOException e) {
                                                e.printStackTrace();
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(context, "Failed :"+e.toString(), Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                                return null;
                                            }
                                        }
                                    }
                                    try {
                                        progressDialog.dismiss();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    setResult(RESULT_OK);
                                    if (i==(AlbumNames.size()-1)){
                                        AlbumPaths.remove((AlbumPaths.size()-1));
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            closeSelectionMode();
                                            Toast.makeText(getApplicationContext(), ""+totalSuccessfulCopy+" File copied successfully !", Toast.LENGTH_SHORT).show();
                                            totalSuccessfulCopy = 0;
                                        }
                                    });
                                    return null;
                                }
                            };

                            if (i==(AlbumNames.size()-1)){
                                //This block is called when user clicks make new folder
                                //Get new folder name here using alert dialog
                                final AlertDialog.Builder builder = new AlertDialog.Builder(Video1.this);
                                builder.setMessage("Enter folder name :");
                                final EditText input = new EditText(Video1.this);
                                builder.setView(input);
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String dirName = input.getText().toString();
                                        File folder = new File(Environment.getExternalStorageDirectory(), dirName);
                                        //If folder with current name does'nt exist then make a new folder
                                        // and copy everything in this newly created folder
                                        //If folder already exist then ask user to change the name
                                        if (!folder.exists()) {
                                            if (folder.mkdirs()){
                                                dialog.dismiss();
                                                AlbumPaths.add(folder.getAbsolutePath());
                                                progressDialog.show();
                                                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            }
                                        }else {
                                            //Toast.makeText(getApplicationContext(), "Folder already exist!\nTry different name !", Toast.LENGTH_SHORT).show();
                                            int iii = 1;
                                            while (folder.exists()){
                                                String dirName2 = dirName+" "+iii;
                                                folder = new File(Environment.getExternalStorageDirectory(), dirName2);
                                                iii++;
                                            }
                                            if (folder.mkdirs()){
                                                dialog.dismiss();
                                                AlbumPaths.add(folder.getAbsolutePath());
                                                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            }
                                        }
                                    }
                                });

                                builder.show();
                            }else{
                                progressDialog.show();
                                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                            }
                        }
                    });
                    return true;
                }
            });

            close.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    closeSelectionMode();
                    return true;
                }
            });

        }

        private void closeSelectionMode(){
            selectionMode = false;
            menu.clear();

            //Prepare Menu
            onCreateOptionMenu(menu);

            getSupportActionBar().setTitle(albumName);
            notifyDataSetChanged();
            for (int i=0; i<VideoList.size();i++){
                itemSelectionMode[i] = false;
            }
            numOfSelectedItem = 0;
        }

        private int copyFile(File sourceFile, File destFile, int TSC) throws IOException {
            if (!sourceFile.exists()) {
                Toast.makeText(context, "File already exist ! ! !", Toast.LENGTH_LONG).show();
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
            MediaScannerConnection.scanFile(context, new String[]{path}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {

                }

                @Override
                public void onScanCompleted(String s, Uri uri) {
                    setResult(RESULT_OK);
                }
            });
        }
    }

    private Menu menu;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        this.menu = menu;
        onCreateOptionMenu(menu);
        return true;
    }

    private void onCreateOptionMenu(Menu menu){

        getMenuInflater().inflate(R.menu.photo_1_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.photo_1_action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Search videos");
        searchView.setMaxWidth(2129960); // https://stackoverflow.com/questions/18063103/searchview-in-optionsmenu-not-full-width
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.toLowerCase();

                ArrayList<HashMap<String, String>> filteredList = new ArrayList<>();

                for (int i = 0; i < videoAlbumList.size(); i++) {
                    String text = videoAlbumList.get(i).get("key_displayName").toLowerCase();
                    if (text.contains(newText)) filteredList.add(videoAlbumList.get(i));
                }
                VideoAlbumAdapter adapter = new VideoAlbumAdapter(filteredList);
                recyclerView.swapAdapter(adapter, true);
                return false;
            }
        });

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoPlay  = sp.getBoolean("key_autoPlay", true);
        boolean backPlay  = sp.getBoolean("key_backPlay", false);
        boolean offScreen = sp.getBoolean("key_offScreen", false);
        menu.findItem(R.id.video_1_auto_play).setVisible(true).setChecked(autoPlay);
        menu.findItem(R.id.video_1_action_background_play).setVisible(true).setChecked(backPlay);
        menu.findItem(R.id.video_1_action_offscreen_play).setVisible(true).setChecked(offScreen);

        sortCase();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 1 stands dsc and 2 for asc
        SharedPreferences sp = getSharedPreferences(albumName, MODE_PRIVATE);
        SharedPreferences sp2 = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        SharedPreferences.Editor editor2 = sp2.edit();

        MenuItem title = menu.findItem(R.id.photo_1_action_title);
        MenuItem size = menu.findItem(R.id.photo_1_action_size);
        MenuItem asc = menu.findItem(R.id.photo_1_action_menu_sort_asc);
        MenuItem dsc = menu.findItem(R.id.photo_1_action_menu_sort_dsc);
        MenuItem autoPlay = menu.findItem(R.id.video_1_auto_play);
        MenuItem backPlay = menu.findItem(R.id.video_1_action_background_play);
        MenuItem offScreen= menu.findItem(R.id.video_1_action_offscreen_play);

        switch (item.getItemId()){
            case R.id.photo_1_action_title:
                if (asc.isChecked()) editor.putInt("key_sort", 51);
                else editor.putInt("key_sort", 52);
                editor.commit();
                reloadAdapter(0);
                break;
            case R.id.photo_1_action_size:
                if (asc.isChecked()) editor.putInt("key_sort", 53);
                else editor.putInt("key_sort", 54);
                editor.commit();
                reloadAdapter(0);
                break;
            case R.id.photo_1_action_date:
                if (asc.isChecked()) editor.putInt("key_sort", 55);
                else editor.putInt("key_sort", 56);
                editor.commit();
                reloadAdapter(0);
                break;
            case R.id.photo_1_action_menu_sort_asc:
                asc.setChecked(true);
                if (title.isChecked()) editor.putInt("key_sort", 51);
                else if (size.isChecked()) editor.putInt("key_sort", 53);
                else editor.putInt("key_sort", 55);
                editor.commit();
                reloadAdapter(0);
                break;
            case R.id.photo_1_action_menu_sort_dsc:
                dsc.setChecked(true);
                if (title.isChecked()) editor.putInt("key_sort", 52);
                else if (size.isChecked()) editor.putInt("key_sort", 54);
                else editor.putInt("key_sort", 56);
                editor.commit();
                reloadAdapter(0);
                break;
            case R.id.photo_1_action_about:
                startActivity(new Intent(context, About.class));
                break;
            case R.id.video_1_auto_play:
                editor2.putBoolean("key_autoPlay", !autoPlay.isChecked());
                autoPlay.setChecked(!autoPlay.isChecked());
                editor2.commit();
                break;
            case R.id.video_1_action_background_play:
                editor2.putBoolean("key_backPlay", !backPlay.isChecked());
                backPlay.setChecked(!backPlay.isChecked());
                editor2.commit();
                break;
            case R.id.video_1_action_offscreen_play:
                editor2.putBoolean("key_offScreen", !offScreen.isChecked());
                offScreen.setChecked(!offScreen.isChecked());
                editor2.commit();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private class MapComparator implements Comparator<Map<String, String>> {

        private final String key;
        private final int index;

        private MapComparator(String key, int index) {
            this.key = key;
            this.index = index;
        }

        public int compare(Map<String, String> first,
                           Map<String, String> second)
        {
            // TODO: Null checking, both for maps and values
            String firstValue = first.get(key);
            String secondValue = second.get(key);
            if (key.equals("key_size")){
                try {
                    Integer firstVal = Integer.parseInt(firstValue);
                    Integer secondVal = Integer.parseInt(secondValue);
                    if (index==2) return secondVal.compareTo(firstVal);
                    else return firstVal.compareTo(secondVal);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return 0;
                }
            }else {
                if (index==2) return secondValue.compareTo(firstValue);
                else return firstValue.compareTo(secondValue);
            }

        }
    }

    private void sortImageList(){
        SharedPreferences sp = getSharedPreferences(albumName, MODE_PRIVATE);//PreferenceManager.getDefaultSharedPreferences(context);
        int index = sp.getInt("key_sort", 56);
        // 1 stands asc and 2 for dsc
        switch (index){
            case 51:
                Collections.sort(videoAlbumList, new MapComparator("key_displayName", 1));
                break;
            case 52:
                Collections.sort(videoAlbumList, new MapComparator("key_displayName", 2));
                break;
            case 53:
                Collections.sort(videoAlbumList, new MapComparator("key_size", 1));
                break;
            case 54:
                Collections.sort(videoAlbumList, new MapComparator("key_size", 2));
                break;
            case 55:
                Collections.sort(videoAlbumList, new MapComparator("key_timestamp", 1));
                break;
            case 56:
                Collections.sort(videoAlbumList, new MapComparator("key_timestamp", 2));
                break;
        }
    }

    private void sortCase(){
        SharedPreferences sp = getSharedPreferences(albumName, MODE_PRIVATE);//PreferenceManager.getDefaultSharedPreferences(context);
        int index = sp.getInt("key_sort", 56);

        menu.findItem(R.id.photo_1_action_title).setChecked(false);
        menu.findItem(R.id.photo_1_action_size).setChecked(false);
        menu.findItem(R.id.photo_1_action_date).setChecked(false);
        menu.findItem(R.id.photo_1_action_menu_sort_asc).setChecked(false);
        menu.findItem(R.id.photo_1_action_menu_sort_dsc).setChecked(false);

        switch (index){
            case 51:
                menu.findItem(R.id.photo_1_action_title).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_asc).setChecked(true);
                break;
            case 52:
                menu.findItem(R.id.photo_1_action_title).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_dsc).setChecked(true);
                break;
            case 53:
                menu.findItem(R.id.photo_1_action_size).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_asc).setChecked(true);
                break;
            case 54:
                menu.findItem(R.id.photo_1_action_size).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_dsc).setChecked(true);
                break;
            case 55:
                menu.findItem(R.id.photo_1_action_date).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_asc).setChecked(true);
                break;
            case 56:
                menu.findItem(R.id.photo_1_action_date).setCheckable(true).setChecked(true);
                menu.findItem(R.id.photo_1_action_menu_sort_dsc).setChecked(true);
                break;
        }
    }

    private void reloadAdapter(int pos){
        sortCase();
        sortImageList();
        VideoAlbumAdapter adapter = new VideoAlbumAdapter(videoAlbumList);
        recyclerView.swapAdapter(adapter, true);
        recyclerView.scrollToPosition(pos);
    }

}
