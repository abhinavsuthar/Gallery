package com.developer.abhinav_suthar.gallery;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Color;
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
import com.developer.abhinav_suthar.gallery.extras.ServiceCopyDelete;
import com.developer.abhinav_suthar.gallery.extras.Utils;

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
import java.util.Random;
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
    }

    private void loadVideoList(){
        LoadVideoAlbum videoAlbum = new LoadVideoAlbum();
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

            if(videoAlbumList.size()==0) finish();
            Utils.setMediaList(videoAlbumList);
            recyclerView = findViewById(R.id.recyclerVideoAlbum);
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
        private String[] mColors = {
                "FFEBEE", "FFCDD2", "EF9A9A", "E57373", "EF5350", "F44336", "E53935",        //reds
                "D32F2F", "C62828", "B71C1C", "FF8A80", "FF5252", "FF1744", "D50000",
                "FCE4EC", "F8BBD0", "F48FB1", "F06292", "EC407A", "E91E63", "D81B60",        //pinks
                "C2185B", "AD1457", "880E4F", "FF80AB", "FF4081", "F50057", "C51162",
                "F3E5F5", "E1BEE7", "CE93D8", "BA68C8", "AB47BC", "9C27B0", "8E24AA",        //purples
                "7B1FA2", "6A1B9A", "4A148C", "EA80FC", "E040FB", "D500F9", "AA00FF",
                "EDE7F6", "D1C4E9", "B39DDB", "9575CD", "7E57C2", "673AB7", "5E35B1",        //deep purples
                "512DA8", "4527A0", "311B92", "B388FF", "7C4DFF", "651FFF", "6200EA",
                "E8EAF6", "C5CAE9", "9FA8DA", "7986CB", "5C6BC0", "3F51B5", "3949AB",        //indigo
                "303F9F", "283593", "1A237E", "8C9EFF", "536DFE", "3D5AFE", "304FFE",
                "E3F2FD", "BBDEFB", "90CAF9", "64B5F6", "42A5F5", "2196F3", "1E88E5",        //blue
                "1976D2", "1565C0", "0D47A1", "82B1FF", "448AFF", "2979FF", "2962FF",
                "E1F5FE", "B3E5FC", "81D4fA", "4fC3F7", "29B6FC", "03A9F4", "039BE5",        //light blue
                "0288D1", "0277BD", "01579B", "80D8FF", "40C4FF", "00B0FF", "0091EA",
                "E0F7FA", "B2EBF2", "80DEEA", "4DD0E1", "26C6DA", "00BCD4", "00ACC1",        //cyan
                "0097A7", "00838F", "006064", "84FFFF", "18FFFF", "00E5FF", "00B8D4",
                "E0F2F1", "B2DFDB", "80CBC4", "4DB6AC", "26A69A", "009688", "00897B",        //teal
                "00796B", "00695C", "004D40", "A7FFEB", "64FFDA", "1DE9B6", "00BFA5",
                "E8F5E9", "C8E6C9", "A5D6A7", "81C784", "66BB6A", "4CAF50", "43A047",        //green
                "388E3C", "2E7D32", "1B5E20", "B9F6CA", "69F0AE", "00E676", "00C853",
                "F1F8E9", "DCEDC8", "C5E1A5", "AED581", "9CCC65", "8BC34A", "7CB342",        //light green
                "689F38", "558B2F", "33691E", "CCFF90", "B2FF59", "76FF03", "64DD17",
                "F9FBE7", "F0F4C3", "E6EE9C", "DCE775", "D4E157", "CDDC39", "C0CA33",        //lime
                "A4B42B", "9E9D24", "827717", "F4FF81", "EEFF41", "C6FF00", "AEEA00",
                "FFFDE7", "FFF9C4", "FFF590", "FFF176", "FFEE58", "FFEB3B", "FDD835",        //yellow
                "FBC02D", "F9A825", "F57F17", "FFFF82", "FFFF00", "FFEA00", "FFD600",
                "FFF8E1", "FFECB3", "FFE082", "FFD54F", "FFCA28", "FFC107", "FFB300",        //amber
                "FFA000", "FF8F00", "FF6F00", "FFE57F", "FFD740", "FFC400", "FFAB00",
                "FFF3E0", "FFE0B2", "FFCC80", "FFB74D", "FFA726", "FF9800", "FB8C00",        //orange
                "F57C00", "EF6C00", "E65100", "FFD180", "FFAB40", "FF9100", "FF6D00",
                "FBE9A7", "FFCCBC", "FFAB91", "FF8A65", "FF7043", "FF5722", "F4511E",        //deep orange
                "E64A19", "D84315", "BF360C", "FF9E80", "FF6E40", "FF3D00", "DD2600",
                "EFEBE9", "D7CCC8", "BCAAA4", "A1887F", "8D6E63", "795548", "6D4C41",        //brown
                "5D4037", "4E342E", "3E2723",
                "FAFAFA", "F5F5F5", "EEEEEE", "E0E0E0", "BDBDBD", "9E9E9E", "757575",        //grey
                "616161", "424242", "212121",
                "ECEFF1", "CFD8DC", "B0BBC5", "90A4AE", "78909C", "607D8B", "546E7A",        //blue grey
                "455A64", "37474F", "263238"
        };

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

            //itemView.setBackgroundColor(Color.parseColor ("#"+mColors[new Random().nextInt(254)]));

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
                        intent.putExtra("key_pos", holder.getAdapterPosition());
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_up_in, android.R.anim.fade_out);
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

            //holder.VdCardView.setCardBackgroundColor(Color.parseColor ("#"+mColors[new Random().nextInt(254)]));
            //holder.VdCardView.setBackgroundColor(Color.parseColor ("#"+mColors[new Random().nextInt(254)]));
            //holder.VdCardView.setDrawingCacheBackgroundColor(Color.parseColor ("#"+mColors[new Random().nextInt(254)]));
            //holder.VdShare.setBackgroundColor(Color.parseColor ("#"+mColors[new Random().nextInt(254)]));
            //holder.VdCardView.setCardBackgroundColor(Color.RED);

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
                selectAll.setIcon(R.drawable.select_all_icon);
            else selectAll.setIcon(R.drawable.select_none_icon);

            selectAll.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (numOfSelectedItem==VideoList.size()){
                        for (int i=0; i<VideoList.size();i++) itemSelectionMode[i] = false;
                        numOfSelectedItem = 0;
                        getSupportActionBar().setTitle(""+numOfSelectedItem);
                        selectAll.setIcon(R.drawable.select_all_icon);
                        notifyDataSetChanged();
                    }else {
                        for (int i=0; i<VideoList.size();i++) itemSelectionMode[i] = true;
                        numOfSelectedItem = VideoList.size();
                        getSupportActionBar().setTitle(""+numOfSelectedItem);
                        selectAll.setIcon(R.drawable.select_none_icon);
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
                    ListView listView = copyDialog.findViewById(R.id.p2MoreListView);
                    ArrayAdapter<String> adapter=new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, AlbumNames);
                    listView.setAdapter(adapter);


                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                            //Dismiss the copy bottom dialog and initialize new progress dialog to
                            copyDialog.dismiss();
                            Utils.setSelectedMediaItems(itemSelectionMode);


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
                                                Intent copyService = new Intent(Video1.this, ServiceCopyDelete.class);
                                                copyService.setAction("copy");
                                                copyService.putExtra("albumPosition", i);
                                                startService(copyService);
                                                closeSelectionMode();
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
                                                Intent copyService = new Intent(Video1.this, ServiceCopyDelete.class);
                                                copyService.setAction("copy");
                                                copyService.putExtra("albumPosition", i);
                                                startService(copyService);
                                                closeSelectionMode();
                                            }
                                        }
                                    }
                                });

                                builder.show();
                            }else{
                                Intent copyService = new Intent(Video1.this, ServiceCopyDelete.class);
                                copyService.setAction("copy");
                                copyService.putExtra("albumPosition", i);
                                startService(copyService);
                                closeSelectionMode();
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
