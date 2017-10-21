package com.developer.abhinav_suthar.gallery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.developer.abhinav_suthar.gallery.extras.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;


public class Photo2 extends AppCompatActivity {

    private Context context;
    private Activity activity;
    private ViewPager mViewPager;
    private ArrayList<HashMap<String, String>> imageList;
    private File outPutFile = null;
    private final int cropImage = 0, cropWallpaper = 1, setAs = 2, editImage = 3;
    private Timer t = new Timer(),timerSlideShow;
    private int reloadAlbum = 8;
    private int currentPhotoPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_2);

        context = this;
        activity = (Photo2) context;

        imageList = (ArrayList<HashMap<String, String>>) getIntent().getSerializableExtra("key_list");
        Integer position = getIntent().getIntExtra("key_position",0);

        loadPhoto(position);
        photoControls();

    }

    private void loadPhoto(int position){
        //Adapter
        FullScreenImageAdapter adapter = new FullScreenImageAdapter(activity);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(position);
        mViewPager.setPageMargin(dp2Pixels(context, 10));
        mViewPager.setOffscreenPageLimit(4);

        showImageDateTime(position);
        currentPhotoPosition = position;

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                showImageDateTime(position);
                currentPhotoPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void showImageDateTime(int position){
        TextView date = (TextView) findViewById(R.id.txtImgDate);
        TextView time = (TextView) findViewById(R.id.txtImgTime);
        long millis = Long.parseLong(imageList.get(position).get("key_timestamp"))*1000L;
        Date d = new Date(millis);
        DateFormat df=new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        date.setText(df.format(d));
        df = new SimpleDateFormat("HH:mm");
        time.setText(df.format(d));
    }

    private void photoControls(){

        ImageView back   = (ImageView) findViewById(R.id.imgBackButton);
        ImageView share  = (ImageView) findViewById(R.id.p2ImgShare);
        ImageView delete = (ImageView) findViewById(R.id.p2ImgDelete);
        ImageView more   = (ImageView) findViewById(R.id.p2ImgMore);

        //Back
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String result = ""+reloadAlbum+currentPhotoPosition;
                try {
                    setResult(Integer.parseInt(result));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                }
                Photo2.super.onBackPressed();
                overridePendingTransition(R.anim.slide_down_out, R.anim.slide_down_in);
            }
        });
        //Share
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareImage(imageList.get(mViewPager.getCurrentItem()).get("key_path"));
            }
        });
        //Delete
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final BottomSheetDialog deleteDialog = new BottomSheetDialog(Photo2.this);
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
                        reloadAlbum = 9;
                        int currentItem = mViewPager.getCurrentItem();
                        getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                MediaStore.Images.ImageColumns.DATA + "=?" ,
                                new String[]{ imageList.get(mViewPager.getCurrentItem()).get("key_path") });
                        imageList.remove(currentItem);
                        if (currentItem!=0) currentItem -=1;
                        if (imageList.size()!=0) loadPhoto(currentItem);
                        else {setResult(9); finish();}
                    }
                });
            }
        });
        //More
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] more = new String[7];
                more[0] = "Crop";
                more[1] = "Copy";
                more[2] = "Edit";
                more[3] = "Set as...";
                more[4] = "Details";
                more[5] = "Set as wallpaper";
                more[6] = "Slide Show";

                final BottomSheetDialog moreDialog = new BottomSheetDialog(Photo2.this);
                moreDialog.setContentView(R.layout.bottom_more_dialog);
                moreDialog.show();
                ListView listView = (ListView) moreDialog.findViewById(R.id.p2MoreListView);
                ArrayAdapter<String> adapter=new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, more);
                listView.setAdapter(adapter);

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if (i==0){
                            //Image crop
                            moreDialog.dismiss();
                            Uri picUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".com.developer.abhinav_suthar.gallery.provider", new File(imageList.get(mViewPager.getCurrentItem()).get("key_path")));
                            File file = new File(imageList.get(mViewPager.getCurrentItem()).get("key_path"));
                            String name = file.getName();

                            int dot = name.lastIndexOf('.');
                            String base = (dot == -1) ? name : name.substring(0, dot);
                            String extension = (dot == -1) ? "" : name.substring(dot+1);
                            String newFileName = file.getParent() + "/" + base + "_Crop" + "." + extension;

                            int alreadyExist = 0;
                            while ((new File(newFileName).exists())){
                                newFileName = file.getParent() + "/" + base + "_Crop(" + alreadyExist + ")." + extension;
                                alreadyExist++;
                            }
                            outPutFile = new File(newFileName);
                            performCrop(picUri, cropImage);
                        }else if (i==1){
                            //Copy/cut
                            final ArrayList<String>  AlbumNames = Utils.getImageAlbumNames();
                            final ArrayList<String>  AlbumPaths = Utils.getImageAlbumPath();

                            moreDialog.dismiss();
                            final BottomSheetDialog copyDialog = new BottomSheetDialog(Photo2.this);
                            copyDialog.setContentView(R.layout.bottom_more_dialog);
                            copyDialog.show();
                            ListView listView = (ListView) copyDialog.findViewById(R.id.p2MoreListView);
                            ArrayAdapter<String> adapter=new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, AlbumNames);
                            listView.setAdapter(adapter);


                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                                    copyDialog.dismiss();
                                    final int curPos = mViewPager.getCurrentItem();
                                    final AsyncTask asyncTask = new AsyncTask() {
                                        @Override
                                        protected Object doInBackground(Object[] params) {
                                            File f = new File(imageList.get(curPos).get("key_path"));
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
                                                copyFile(f, new File(fileNewPath));
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                                                return null;
                                            }
                                            setResult(RESULT_OK);
                                            if (i==(AlbumNames.size()-1)){
                                                AlbumPaths.remove((AlbumPaths.size()-1));
                                            }
                                            return null;
                                        }
                                    };

                                    if (i==(AlbumNames.size()-1)){
                                        //This block is called when user clicks make new folder
                                        //Get new folder name here using alert dialog
                                        final AlertDialog.Builder builder = new AlertDialog.Builder(Photo2.this);
                                        builder.setMessage("Enter folder name :");
                                        final EditText input = new EditText(Photo2.this);
                                        builder.setView(input);
                                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                String dirName = input.getText().toString();
                                                File folder = new File(Environment.getExternalStorageDirectory(), dirName);
                                                if (!folder.exists()) {
                                                    if (folder.mkdirs()){
                                                        AlbumPaths.add(folder.getAbsolutePath());
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
                                                        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                                    }
                                                }
                                            }
                                        });
                                        builder.show();
                                    }else {
                                        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                                    }
                                }
                            });
                        }else if (i==2){
                            moreDialog.dismiss();
                            Uri picUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() +
                                    ".com.developer.abhinav_suthar.gallery.provider", new File(imageList.get(mViewPager.getCurrentItem()).get("key_path")));
                            File file = new File(imageList.get(mViewPager.getCurrentItem()).get("key_path"));

                            String name = file.getName();
                            int dot = name.lastIndexOf('.');
                            String base = (dot == -1) ? name : name.substring(0, dot);
                            String extension = (dot == -1) ? "" : name.substring(dot+1);
                            String newFileName = file.getParent() + "/" + base + "_Edit" + "." + extension;

                            int alreadyExist = 0;
                            while ((new File(newFileName).exists())){
                                newFileName = file.getParent() + "/" + base + "_Edit(" + alreadyExist + ")." + extension;
                                alreadyExist++;
                            }
                            outPutFile = new File(newFileName);

                            Intent editIntent = new Intent(Intent.ACTION_EDIT);
                            editIntent.setDataAndType(picUri, "image/*");
                            editIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outPutFile));
                            editIntent.putExtra("finishActivityOnSaveCompleted", true);
                            editIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivityForResult(editIntent, editImage);

                        }else if (i==3){
                            //Set as
                            moreDialog.dismiss();
                            File f = new File(imageList.get(mViewPager.getCurrentItem()).get("key_path"));
                            //Convert to bitmap
                            Bitmap bmp = decodeFile(f);

                            try {
                                Intent myIntent = new Intent();
                                myIntent.setAction(Intent.ACTION_ATTACH_DATA);
                                Uri imageUri = getImageUri(context, bmp);
                                myIntent.setDataAndType(imageUri, "image/jpg");
                                myIntent.putExtra("file_path", imageUri);
                                startActivityForResult(myIntent, setAs);

                            } catch (ActivityNotFoundException anfe) {
                                Toast.makeText(context, "Error :Firing Intent to set image as contact failed.", Toast.LENGTH_SHORT).show();
                            }
                        }else if (i==4){
                            //Image Details
                            moreDialog.dismiss();
                            String timeDate     = imageList.get(mViewPager.getCurrentItem()).get("key_timestamp");
                            String size         = imageList.get(mViewPager.getCurrentItem()).get("key_size");
                            String height       = imageList.get(mViewPager.getCurrentItem()).get("key_height");
                            String width        = imageList.get(mViewPager.getCurrentItem()).get("key_width");
                            String displayName  = imageList.get(mViewPager.getCurrentItem()).get("key_displayName");
                            String path         = imageList.get(mViewPager.getCurrentItem()).get("key_path");

                            /*ExifInterface exif = null;
                            try {
                                exif = new ExifInterface(path);
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                                    String s = exif.getAttribute(ExifInterface.TAG_SHUTTER_SPEED_VALUE);
                                    Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/

                            BottomSheetDialog dialog1 = new BottomSheetDialog(Photo2.this);
                            dialog1.setContentView(R.layout.bottom_image_detail_dialog);
                            dialog1.show();

                            long millis = Long.parseLong(timeDate)*1000L;
                            Date d = new Date(millis);
                            DateFormat df=new SimpleDateFormat("dd-MM-yyyy  HH:mm");
                            df.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

                            float fileSize = Float.parseFloat(size)/1024F;
                            String fileSizeSuffix = "KB";
                            if (fileSize>1024){
                                fileSize = fileSize/1024L;
                                fileSizeSuffix = "MB";
                            }if (fileSize>1024){
                                fileSize = fileSize/1024L;
                                fileSizeSuffix = "GB";}

                            ((TextView) dialog1.findViewById(R.id.txtImageFileName)).setText(displayName);
                            ((TextView) dialog1.findViewById(R.id.txtImageDate)).setText(df.format(d));
                            ((TextView) dialog1.findViewById(R.id.txtImageSize)).setText(String.format("%.2f", fileSize)+fileSizeSuffix);
                            ((TextView) dialog1.findViewById(R.id.txtImageResolution)).setText(width+"X"+height);
                            ((TextView) dialog1.findViewById(R.id.txtImagePath)).setText(path);

                        }else if (i==5) {
                            //Set as wallpaper
                            moreDialog.dismiss();
                            outPutFile = new File(android.os.Environment.getExternalStorageDirectory(), "Wallpaper.jpg");
                            Uri picUri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".com.developer.abhinav_suthar.gallery.provider", new File(imageList.get(mViewPager.getCurrentItem()).get("key_path")));
                            performCrop(picUri, cropWallpaper);
                        }else if(i==6){
                            //Slide Show
                            moreDialog.dismiss();
                            findViewById(R.id.imgStopSlideShow).setVisibility(View.VISIBLE);
                            timerSlideShow = new Timer();
                            timerSlideShow.scheduleAtFixedRate(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (currentPhotoPosition==imageList.size()-1) {
                                                timerSlideShow.cancel();
                                                Toast.makeText(context, "End of the page \nSlideshow stopped !", Toast.LENGTH_SHORT).show();
                                                findViewById(R.id.imgStopSlideShow).setVisibility(View.GONE);
                                                return;
                                            }
                                            mViewPager.setCurrentItem(currentPhotoPosition+1, true);
                                        }
                                    });
                                }
                            },1500,1500);
                            findViewById(R.id.imgStopSlideShow).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    timerSlideShow.cancel();
                                    Toast.makeText(context, "Slideshow stopped ! ! !", Toast.LENGTH_SHORT).show();
                                    findViewById(R.id.imgStopSlideShow).setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public void shareImage(String imgPath) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        File f = new File(imgPath);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+f.getAbsolutePath()));
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(intent, "Share image via..."));
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            return;
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "File copied successfully !", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void scanFile(String path){
        MediaScannerConnection.scanFile(context, new String[]{path}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {

            }

            @Override
            public void onScanCompleted(String s, Uri uri) {
                reloadAlbum = 9;
                setResult(RESULT_OK);
            }
        });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);

        Utils.setTempImagePath(Uri.parse(path));
        return Uri.parse(path);
    }

    public static int dp2Pixels(Context context, int dip) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.getResources().getDisplayMetrics());
    }

    @Override
    public void onBackPressed() {
        String result = ""+reloadAlbum+currentPhotoPosition;
        try {
            setResult(Integer.parseInt(result));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_down_out, R.anim.slide_down_in);
    }

    /**
     * ViewPager Adapter
     */
    private class FullScreenImageAdapter extends PagerAdapter {

        private Activity _activity;
        private LayoutInflater inflater;

        // constructor
        public FullScreenImageAdapter(Activity activity) {

            this._activity = activity;
        }

        @Override
        public int getCount() {
            return imageList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == (object);
        }

        @Override
        public Object instantiateItem(final ViewGroup container, int position) {

            inflater = (LayoutInflater) _activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.tab_image_preview, container, false);

            SubsamplingScaleImageView imgDisplay = viewLayout.findViewById(R.id.imagePreview);

            imgDisplay.setImage(ImageSource.uri((imageList.get(position).get("key_path"))));
            imgDisplay.setMinimumTileDpi(100);
            imgDisplay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleMenu();
                }
            });

            (container).addView(viewLayout);

            return viewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            (container).removeView((RelativeLayout) object);

        }
    }

    public void toggleMenu(){
        final RelativeLayout top    = (RelativeLayout) findViewById(R.id.p2TopLayout);
        final RelativeLayout bottom = (RelativeLayout) findViewById(R.id.p2BottomLayout);


        if (top.getVisibility()==View.VISIBLE){

            top.setAlpha(1.0f);
            top.animate()
                    .translationY(-top.getHeight())
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            top.setVisibility(View.GONE);
                        }
                    });
            bottom.setAlpha(1.0f);
            bottom.animate()
                    .translationY(bottom.getHeight())
                    .alpha(0.0f)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            bottom.setVisibility(View.GONE);
                        }
                    });

            t.cancel();
            t = new Timer();
        }else {
            top.setVisibility(View.VISIBLE);
            bottom.setVisibility(View.VISIBLE);

            top.setAlpha(0.0f);
            top.animate()
                    .translationYBy(top.getHeight())
                    .alpha(1.0f)
                    .setListener(null);
            bottom.setAlpha(0.0f);
            bottom.animate()
                    .translationYBy(-bottom.getHeight())
                    .alpha(1.0f)
                    .setListener(null);

            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleMenu();
                        }
                    });
                }
            }, 10000);
        }
    }

    private void performCrop(Uri picUri, int type) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(picUri, "image/*");
            cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outPutFile));
            cropIntent.putExtra("scaleUpIfNeeded", true);
            cropIntent.putExtra("scale", true);
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (type==cropWallpaper){
                cropIntent.putExtra("aspectX", 9);
                cropIntent.putExtra("aspectY", 16);
                startActivityForResult(cropIntent, cropWallpaper);
            }else if (type==cropImage){
                startActivityForResult(cropIntent, cropImage);
            }

        }
        // respond to users whose devices do not support the crop action
        catch (ActivityNotFoundException anfe) {
            // display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();

            //if device does't have any crop app then just make it wallpaper as it is
            if (type==cropWallpaper){
                WallpaperManager manager = WallpaperManager.getInstance(context);
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), picUri);
                    manager.setBitmap(bitmap);
                    Toast.makeText(context, "Wallpaper changed", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(context, ""+e, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == cropImage) {
            if (resultCode==RESULT_OK && data != null && outPutFile.exists()){
                Toast.makeText(getApplicationContext(), "Cropped and saved", Toast.LENGTH_SHORT).show();
                scanFile(outPutFile.getAbsolutePath());
            }else Toast.makeText(getApplicationContext(), "Error while save image", Toast.LENGTH_SHORT).show();

        }else if (requestCode == cropWallpaper){
            if (resultCode==RESULT_OK){
                if (data != null){
                    WallpaperManager manager = WallpaperManager.getInstance(context);
                    //Get Cropped Photo
                    Bitmap bitmap;
                    if(outPutFile.exists()){
                        bitmap = decodeFile(outPutFile);
                    } else {
                        Toast.makeText(getApplicationContext(), "Error while cropping image", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        manager.setBitmap(bitmap);
                        Toast.makeText(context, "Wallpaper changed", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error :"+e, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }else if (requestCode==setAs){

            if (resultCode==RESULT_OK) Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show();
            Uri uri = Utils.getTempImagePath();
            getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    MediaStore.Images.ImageColumns.DATA + "=?" , new String[]{ getRealPathFromURI(context, uri) });

        }else if (requestCode==editImage){
            if (resultCode==RESULT_OK){
                scanFile(outPutFile.getAbsolutePath());
                if (outPutFile.exists()) Toast.makeText(Photo2.this, "Saved ! ! !", Toast.LENGTH_SHORT).show();
                else Toast.makeText(Photo2.this, "Saved to DCIM", Toast.LENGTH_SHORT).show();
            }else Toast.makeText(Photo2.this, "Error while saving ! ! !", Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private Bitmap decodeFile(File f) {
        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 512;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }
}
