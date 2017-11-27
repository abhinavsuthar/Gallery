package com.developer.abhinav_suthar.gallery.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.support.v7.widget.SearchView;
import android.widget.ListView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.developer.abhinav_suthar.gallery.R;
import com.developer.abhinav_suthar.gallery.services.ServiceCopyDelete;
import com.developer.abhinav_suthar.gallery.extras.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Photo1 extends AppCompatActivity {

    public Context context;
    private String albumName;
    private int photoPosition = 0;
    private RecyclerView recyclerView;
    private ArrayList<HashMap<String, String>> imageList = new ArrayList<>();
    private Menu menu;
    private int totalSuccessfulCopy = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_photo_1);

        recyclerView = findViewById(R.id.photo_1_recyclerView);

        albumName = getIntent().getExtras().getString("key_album");

        context = this;
        Toolbar toolbar = findViewById(R.id.photo_1_toolBar);
        toolbar.setTitle(albumName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        LoadAlbum loadAlbum = new LoadAlbum();
        loadAlbum.execute(albumName);
        registerContentObserver();
    }


    private class LoadAlbum extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageList.clear();
        }

        @Override
        protected String doInBackground(String... strings) {

            String albumName = strings[0];
            String path, mime, size, height, width, displayName, timestamp;

            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DATE_MODIFIED, MediaStore.MediaColumns.SIZE,
                    MediaStore.MediaColumns.HEIGHT, MediaStore.MediaColumns.WIDTH, MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.MIME_TYPE};
            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, "bucket_display_name = \"" + albumName + "\"", null, null);
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, "bucket_display_name = \"" + albumName + "\"", null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal, cursorInternal});

            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                size = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE));
                height = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT));
                width = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH));
                displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
                mime = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.MIME_TYPE));
                HashMap<String, String> temp = new HashMap<>();
                temp.put("key_path", path);
                temp.put("key_mime", mime);
                temp.put("key_timestamp", timestamp);
                temp.put("key_size", size);
                temp.put("key_height", height);
                temp.put("key_width", width);
                temp.put("key_displayName", displayName);

                imageList.add(temp);
            }

            if (cursorExternal != null) cursorExternal.close();
            if (cursorInternal != null) cursorInternal.close();
            cursor.close();
            sortImageList();
            return "Abhi";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (imageList.size() == 0) finish();
            Utils.setMediaList(imageList);

            recyclerView = findViewById(R.id.photo_1_recyclerView);
            gridView = findViewById(R.id.photo_1_gridView);
            int orientation = context.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                gridView.setNumColumns(4);
            } else {
                gridView.setNumColumns(7);
            }
            PhotoAdaptor2 adaptor2 = new PhotoAdaptor2();
            gridView.setAdapter(adaptor2);

        }
    }
    GridView gridView;
    private boolean showCheckBox;
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.MyViewHolder>{

        private ArrayList<HashMap<String, String>> albumList = new ArrayList<>();
        private Boolean[] itemSelectionMode;
        private boolean showCheckBox;
        private int numOfSelectedItem = 0;

        public PhotoAdapter(ArrayList<HashMap<String, String>> albumList){
            this.albumList = albumList;
            itemSelectionMode = new Boolean[albumList.size()];
            for (int i=0; i<albumList.size();i++){
                itemSelectionMode[i] = false;
            }

        }

        @Override
        public PhotoAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.photo_1, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {

            Glide.with(context)
                    .load(new File(albumList.get(position).get("key_path")))
                    .into(holder.albumImage);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.albumImage.setTransitionName("sharedElementTransition"+position);
            }

            if (showCheckBox) {
                holder.checkBox.setVisibility(View.VISIBLE);
                holder.checkBox.setChecked(itemSelectionMode[position]);
                holder.albumImage.setOnClickListener(new View.OnClickListener() {
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
            }else{
                holder.checkBox.setVisibility(View.GONE);

                holder.albumImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(context, Photo2.class);
                        intent.putExtra("key_position", holder.getAdapterPosition());

                        startActivityForResult(intent,501);
                        overridePendingTransition(R.anim.slide_up_in, R.anim.slide_up_out);
                    }
                });

                holder.albumImage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        showCheckBox = true;
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
        }

        @Override
        public int getItemCount() {
            return albumList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            private ImageView albumImage;
            private CheckBox checkBox;
            private MyViewHolder(View itemView) {
                super(itemView);
                albumImage = itemView.findViewById(R.id.albumImage2);
                checkBox   = itemView.findViewById(R.id.photo_1_checkBox);
            }
        }

        private void selectionModeFunctions(){
            final MenuItem selectAll = menu.findItem(R.id.selection_select_all);
            MenuItem share = menu.findItem(R.id.selection_share);
            MenuItem delete = menu.findItem(R.id.selection_delete);
            MenuItem copy = menu.findItem(R.id.selection_copy);
            MenuItem close = menu.findItem(R.id.selection_close);
            if (numOfSelectedItem!=imageList.size())
                selectAll.setIcon(R.drawable.select_all_icon);
            else selectAll.setIcon(R.drawable.select_none_icon);

            selectAll.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (numOfSelectedItem==imageList.size()){
                        for (int i=0; i<imageList.size();i++) itemSelectionMode[i] = false;
                        numOfSelectedItem = 0;
                        getSupportActionBar().setTitle(""+numOfSelectedItem);
                        selectAll.setIcon(R.drawable.select_all_icon);
                        notifyDataSetChanged();
                    }else {
                        for (int i=0; i<imageList.size();i++) itemSelectionMode[i] = true;
                        numOfSelectedItem = imageList.size();
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
                    for (int i=0;i<imageList.size();i++){
                        if (itemSelectionMode[i]){
                            imgUri.add(Uri.parse(imageList.get(i).get("key_path")));
                        }
                    }
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
                    intent.setType("image/jpeg");
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imgUri);
                    startActivity(intent);
                    closeSelectionMode();
                    return true;
                }
            });

            delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (numOfSelectedItem<1) return false;
                    final BottomSheetDialog deleteDialog = new BottomSheetDialog(Photo1.this);
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
                                    for (int i=0,j=0;i<imageList.size();i++){
                                        if (itemSelectionMode[i]){
                                            imgPaths[j] = imageList.get(i).get("key_path");
                                            j++;
                                        }
                                    }
                                    for (String imgPath : imgPaths) {
                                        totalSuccessfulCopy+=getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                MediaStore.Images.ImageColumns.DATA + "=?", new String[]{imgPath});

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
                                            imageList.clear();
                                            LoadAlbum loadAlbum = new LoadAlbum();
                                            loadAlbum.execute(albumName);
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

                    final ArrayList<String>  AlbumNames = Utils.getImageAlbumNames();
                    final ArrayList<String>  AlbumPaths = Utils.getImageAlbumPath();

                    final BottomSheetDialog copyDialog = new BottomSheetDialog(Photo1.this);
                    copyDialog.setContentView(R.layout.bottom_more_dialog);
                    copyDialog.show();
                    ListView listView = (ListView) copyDialog.findViewById(R.id.p2MoreListView);
                    ArrayAdapter<String> adapter=new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, AlbumNames);
                    listView.setAdapter(adapter);


                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                            copyDialog.dismiss();
                            final ProgressDialog progressDialog = new ProgressDialog(context);
                            progressDialog.setTitle("Processing");
                            progressDialog.setMessage("Please wait...");
                            progressDialog.setCancelable(false);

                            final AsyncTask asyncTask = new AsyncTask() {
                                @Override
                                protected Object doInBackground(Object[] params) {
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
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressDialog.setMessage("Please wait... ("+totalSuccessfulCopy+"/"+numOfSelectedItem+")");
                                                    }
                                                });
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                                                return null;
                                            }
                                        }
                                    }
                                    progressDialog.dismiss();
                                    setResult(RESULT_OK);
                                    if (i==(AlbumNames.size()-1)){
                                        AlbumPaths.remove((AlbumPaths.size()-1));
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            closeSelectionMode();
                                            Toast.makeText(context, ""+totalSuccessfulCopy+" File copied successfully !", Toast.LENGTH_SHORT).show();
                                            totalSuccessfulCopy = 0;
                                        }
                                    });
                                    return null;
                                }
                            };

                            if (i==(AlbumNames.size()-1)){
                                //This block is called when user clicks make new folder
                                //Get new folder name here using alert dialog
                                final AlertDialog.Builder builder = new AlertDialog.Builder(Photo1.this);
                                builder.setMessage("Enter folder name :");
                                final EditText input = new EditText(Photo1.this);
                                builder.setView(input);
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String dirName = input.getText().toString();
                                        File folder = new File(Environment.getExternalStorageDirectory(), dirName);
                                        if (!folder.exists()) {
                                            if (folder.mkdirs()){
                                                AlbumPaths.add(folder.getAbsolutePath());
                                                progressDialog.show();
                                                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            }
                                        }else {int iii = 1;
                                            while (folder.exists()){
                                                String temp = dirName;
                                                dirName = temp+" "+iii;
                                                folder = new File(Environment.getExternalStorageDirectory(), dirName);
                                                iii++;
                                            }
                                            if (folder.mkdirs()){
                                                AlbumPaths.add(folder.getAbsolutePath());
                                                progressDialog.show();
                                                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                            }
                                        }
                                    }
                                });
                                builder.show();
                            }else {
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
            showCheckBox = false;
            menu.clear();
            getMenuInflater().inflate(R.menu.basic_menu, menu);
            getSupportActionBar().setTitle(albumName);
            notifyDataSetChanged();
            for (int i=0; i<imageList.size();i++){
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

    private class PhotoAdaptor2 extends BaseAdapter{

        private Boolean[] itemSelectionMode;
        private int numOfSelectedItem = 0;

        public PhotoAdaptor2(){
            itemSelectionMode = new Boolean[imageList.size()];
            for (int i=0; i<imageList.size();i++){
                itemSelectionMode[i] = false;
            }
        }
        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater layoutInflater = LayoutInflater.from(Photo1.this);
            if (convertView == null)convertView = layoutInflater.inflate(R.layout.photo_1, null);

            final ImageView albumImage = convertView.findViewById(R.id.albumImage2);
            final CheckBox checkBox   = convertView.findViewById(R.id.photo_1_checkBox);

            Glide.with(context)
                    .load(new File(imageList.get(position).get("key_path")))
                    .into(albumImage);

            final int pos = position;
            if (showCheckBox) {
                checkBox.setVisibility(View.VISIBLE);
                checkBox.setChecked(itemSelectionMode[position]);
                albumImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        itemSelectionMode[pos] = (!itemSelectionMode[pos]);
                        checkBox.setChecked(itemSelectionMode[pos]);
                        if (itemSelectionMode[pos]) numOfSelectedItem++;
                        else numOfSelectedItem--;
                        getSupportActionBar().setTitle(""+numOfSelectedItem);
                        selectionModeFunctions();
                    }
                });
            }else{
                checkBox.setVisibility(View.GONE);

                albumImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Intent intent = new Intent(context, Photo2.class);
                        intent.putExtra("key_position", pos);

                        startActivityForResult(intent, 501);
                        overridePendingTransition(R.anim.slide_up_in, android.R.anim.fade_out);
                    }
                });

                albumImage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        showCheckBox = true;
                        numOfSelectedItem++;
                        itemSelectionMode[pos] = (!itemSelectionMode[pos]);
                        notifyDataSetChanged();
                        menu.clear();
                        getMenuInflater().inflate(R.menu.selection_mode, menu);
                        getSupportActionBar().setTitle(""+numOfSelectedItem);
                        selectionModeFunctions();
                        return true;
                    }
                });
            }

            return convertView;
        }

        private void temp(){
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width2  = displayMetrics.widthPixels;
            int height2 = displayMetrics.heightPixels;
            float screenProportion = (float) width2/(float) height2;
        }

        private void selectionModeFunctions(){
            final MenuItem selectAll = menu.findItem(R.id.selection_select_all);
            MenuItem share = menu.findItem(R.id.selection_share);
            MenuItem delete = menu.findItem(R.id.selection_delete);
            MenuItem copy = menu.findItem(R.id.selection_copy);
            MenuItem close = menu.findItem(R.id.selection_close);
            if (numOfSelectedItem!=imageList.size())
                selectAll.setIcon(R.drawable.select_all_icon);
            else selectAll.setIcon(R.drawable.select_none_icon);

            selectAll.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (numOfSelectedItem==imageList.size()){
                        for (int i=0; i<imageList.size();i++) itemSelectionMode[i] = false;
                        numOfSelectedItem = 0;
                        getSupportActionBar().setTitle(""+numOfSelectedItem);
                        selectAll.setIcon(R.drawable.select_all_icon);
                        notifyDataSetChanged();
                    }else {
                        for (int i=0; i<imageList.size();i++) itemSelectionMode[i] = true;
                        numOfSelectedItem = imageList.size();
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
                    for (int i=0;i<imageList.size();i++){
                        if (itemSelectionMode[i]){
                            imgUri.add(Uri.parse(imageList.get(i).get("key_path")));
                        }
                    }
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND_MULTIPLE);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Here are some files.");
                    intent.setType("image/jpeg");
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imgUri);
                    startActivity(intent);
                    closeSelectionMode();
                    return true;
                }
            });

            delete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    if (numOfSelectedItem<1) return false;
                    final BottomSheetDialog deleteDialog = new BottomSheetDialog(Photo1.this);
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
                            /*final ProgressDialog progressDialog = new ProgressDialog(context);
                            progressDialog.setTitle("Processing...");
                            progressDialog.setMessage("Please wait...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();*/

                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    String[] imgPaths = new String[numOfSelectedItem];
                                    for (int i=0,j=0;i<imageList.size();i++){
                                        if (itemSelectionMode[i]){
                                            imgPaths[j] = imageList.get(i).get("key_path");
                                            j++;
                                        }
                                    }
                                    for (String imgPath : imgPaths) {
                                        totalSuccessfulCopy+=getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                                MediaStore.Images.ImageColumns.DATA + "=?", new String[]{imgPath});

                                        /*runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                progressDialog.setMessage("Please wait... ("+totalSuccessfulCopy+"/"+numOfSelectedItem+")");
                                            }
                                        });*/
                                    }
                                    //progressDialog.dismiss();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //Load Album Again
                                            imageList.clear();
                                            LoadAlbum loadAlbum = new LoadAlbum();
                                            loadAlbum.execute(albumName);
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

                    final ArrayList<String>  AlbumNames = Utils.getImageAlbumNames();
                    final ArrayList<String>  AlbumPaths = Utils.getImageAlbumPath();

                    final BottomSheetDialog copyDialog = new BottomSheetDialog(Photo1.this);
                    copyDialog.setContentView(R.layout.bottom_more_dialog);
                    copyDialog.show();
                    ListView listView = copyDialog.findViewById(R.id.p2MoreListView);
                    ArrayAdapter<String> adapter=new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, AlbumNames);
                    listView.setAdapter(adapter);


                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {

                            copyDialog.dismiss();
                            Utils.setSelectedMediaItems(itemSelectionMode);

                            if (i==(AlbumNames.size()-1)){
                                //This block is called when user clicks make new folder
                                //Get new folder name here using alert dialog
                                final AlertDialog.Builder builder = new AlertDialog.Builder(Photo1.this);
                                builder.setMessage("Enter folder name :");
                                final EditText input = new EditText(Photo1.this);
                                builder.setView(input);
                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String dirName = input.getText().toString();
                                        File folder = new File(Environment.getExternalStorageDirectory(), dirName);
                                        if (!folder.exists()) {
                                            if (folder.mkdirs()){
                                                AlbumPaths.add(folder.getAbsolutePath());
                                                Intent copyService = new Intent(Photo1.this, ServiceCopyDelete.class);
                                                copyService.setAction("copy");
                                                copyService.putExtra("albumPosition", i);
                                                copyService.putExtra("photoOrVideo", 0);
                                                startService(copyService);
                                                closeSelectionMode();
                                            }
                                        }else {int iii = 1;
                                            while (folder.exists()){
                                                String temp = dirName;
                                                dirName = temp+" "+iii;
                                                folder = new File(Environment.getExternalStorageDirectory(), dirName);
                                                iii++;
                                            }
                                            if (folder.mkdirs()){
                                                AlbumPaths.add(folder.getAbsolutePath());
                                                Intent copyService = new Intent(Photo1.this, ServiceCopyDelete.class);
                                                copyService.setAction("copy");
                                                copyService.putExtra("albumPosition", i);
                                                copyService.putExtra("photoOrVideo", 0);
                                                startService(copyService);
                                                closeSelectionMode();
                                            }
                                        }
                                    }
                                });
                                builder.show();
                            }else {
                                Intent copyService = new Intent(Photo1.this, ServiceCopyDelete.class);
                                copyService.setAction("copy");
                                copyService.putExtra("albumPosition", i);
                                copyService.putExtra("photoOrVideo", 0);
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

        public void closeSelectionMode(){
            showCheckBox = false;
            menu.clear();
            getMenuInflater().inflate(R.menu.basic_menu, menu);
            getSupportActionBar().setTitle(albumName);
            notifyDataSetChanged();
            for (int i=0; i<imageList.size();i++){
                itemSelectionMode[i] = false;
            }
            numOfSelectedItem = 0;
        }
    }

    /**
     * Sort values
     */
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
                Collections.sort(imageList, new MapComparator("key_displayName", 1));
                break;
            case 52:
                Collections.sort(imageList, new MapComparator("key_displayName", 2));
                break;
            case 53:
                Collections.sort(imageList, new MapComparator("key_size", 1));
                break;
            case 54:
                Collections.sort(imageList, new MapComparator("key_size", 2));
                break;
            case 55:
                Collections.sort(imageList, new MapComparator("key_timestamp", 1));
                break;
            case 56:
                Collections.sort(imageList, new MapComparator("key_timestamp", 2));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.basic_menu, menu);
        this.menu = menu;
        MenuItem searchItem = menu.findItem(R.id.photo_1_action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Search images");
        searchView.setMaxWidth(2129960);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                newText = newText.toLowerCase();

                ArrayList<HashMap<String, String>> filteredList = new ArrayList<>();

                for (int i = 0; i < imageList.size(); i++) {
                    String text = imageList.get(i).get("key_displayName").toLowerCase();
                    if (text.contains(newText)) filteredList.add(imageList.get(i));
                }
                PhotoAdapter adapter = new PhotoAdapter(filteredList);
                recyclerView.swapAdapter(adapter, true);
                return true;
            }
        });
        Utils.sortCase(menu, context, albumName);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 1 stands dsc and 2 for asc
        SharedPreferences sp = getSharedPreferences(albumName, MODE_PRIVATE);//PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();

        MenuItem title = menu.findItem(R.id.photo_1_action_title);
        MenuItem size = menu.findItem(R.id.photo_1_action_size);
        MenuItem date = menu.findItem(R.id.photo_1_action_date);
        MenuItem asc = menu.findItem(R.id.photo_1_action_menu_sort_asc);
        MenuItem dsc = menu.findItem(R.id.photo_1_action_menu_sort_dsc);
        switch (item.getItemId()){
            case R.id.photo_1_action_title:
                if (asc.isChecked()) editor.putInt("key_sort", 51);
                else editor.putInt("key_sort", 52);
                editor.commit();
                reloadAdapter();
                break;
            case R.id.photo_1_action_size:
                if (asc.isChecked()) editor.putInt("key_sort", 53);
                else editor.putInt("key_sort", 54);
                editor.commit();
                reloadAdapter();
                break;
            case R.id.photo_1_action_date:
                if (asc.isChecked()) editor.putInt("key_sort", 55);
                else editor.putInt("key_sort", 56);
                editor.commit();
                reloadAdapter();
                break;
            case R.id.photo_1_action_menu_sort_asc:
                asc.setChecked(true);
                if (title.isChecked()) editor.putInt("key_sort", 51);
                else if (size.isChecked()) editor.putInt("key_sort", 53);
                else editor.putInt("key_sort", 55);
                editor.commit();
                reloadAdapter();
                break;
            case R.id.photo_1_action_menu_sort_dsc:
                dsc.setChecked(true);
                if (title.isChecked()) editor.putInt("key_sort", 52);
                else if (size.isChecked()) editor.putInt("key_sort", 54);
                else editor.putInt("key_sort", 56);
                editor.commit();
                reloadAdapter();
                break;
            case R.id.photo_1_action_about:
                startActivity(new Intent(context, z_About.class));
                break;
            case R.id.photo_1_action_settings:
                startActivity(new Intent(context, z_Settings.class));
            default:
                return super.onOptionsItemSelected(item);
        }

        return super.onOptionsItemSelected(item);
    }

    private void reloadAdapter(){
        Utils.sortCase(menu, context, albumName);
        sortImageList();
        PhotoAdapter adapter = new PhotoAdapter(imageList);
        recyclerView.swapAdapter(adapter, true);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==501){
            try {
                int reloadAlbum = Integer.parseInt((Integer.toString(resultCode)).substring(0, 1));
                int photoPosition = 0;
                if (Integer.toString(resultCode).length()>1) photoPosition = Integer.parseInt(Integer.toString(resultCode).substring(1));
                if (reloadAlbum==9){
                    imageList.clear();
                    LoadAlbum loadAlbum = new LoadAlbum();
                    loadAlbum.execute(albumName);
                    setResult(RESULT_OK);
                    this.photoPosition = photoPosition;
                }
                try {
                    gridView.smoothScrollToPosition(photoPosition);
                    //recyclerView.scrollToPosition(photoPosition);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (NumberFormatException | Resources.NotFoundException e) {
                e.printStackTrace();
                Toast.makeText(context, "1"+e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }


    ContentObserver observer_1, observer_2;

    private void registerContentObserver()
    {
        unregisterContentObserver();

        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true,
                observer_1 = new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        LoadAlbum loadAlbum = new LoadAlbum();
                        loadAlbum.execute(albumName);
                    }
                }
        );
        getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI, true,
                observer_2 = new ContentObserver(new Handler()) {
                    @Override
                    public void onChange(boolean selfChange) {
                        LoadAlbum loadAlbum = new LoadAlbum();
                        loadAlbum.execute(albumName);
                    }
                }
        );

    }

    private void unregisterContentObserver()
    {
        try {
            getContentResolver().unregisterContentObserver(observer_1);
            getContentResolver().unregisterContentObserver(observer_2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterContentObserver();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_out_left);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
