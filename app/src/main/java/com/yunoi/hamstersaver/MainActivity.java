package com.yunoi.hamstersaver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "::::: TEST :::::";
    private static final int PERMISSION_REQUEST_CODE = 1;
    static final String FOLDER_PATH = "/hamsterSaver";
    private File filePath;  // path included file name
    private File directoryPath;
    private Bitmap downloadedImg;
    Button btnSave;
    EditText editText;
    ImageView imageView;
    String action = "";
    String type = "";
    Intent intent;

    private void requestNecessaryPermissions(String[] permissions){
        // Runtime Permission request over API23
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE);
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSave = findViewById(R.id.btnSave);
        editText = findViewById(R.id.editText);
        imageView = findViewById(R.id.imageView);
        Log.v(TAG, "########## START onCreate ##########");
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "########## START onResume ##########");
        intent = getIntent();
        if(intent != null){
            Log.v(TAG, "[onResume] ######### intent is not null ##########");

            action = intent.getAction();
            type = intent.getType();

            Log.v(TAG, "[onResume] intent.getAction()" + " : " + action);
            Log.v(TAG, "[onResume] intent.getType()" + " : " + type);
        } else {
            Log.v(TAG, "[onResume] ########## intent IS NULL ##########");
        }

        if(Intent.ACTION_SEND.equals(action) && type != null) {
            if(type.startsWith("image/")){
                Toast.makeText(getApplicationContext(), "before saveSendImage function", Toast.LENGTH_SHORT).show();
                saveSendImage(intent);
            } else if ("text/plain".equals(type)){
                saveSendText(intent);
            } else if (type.startsWith("video/")){
                saveSendVideo(intent);
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if(type.startsWith("image/")){
                Toast.makeText(getApplicationContext(), "before saveSendMultipleImages function", Toast.LENGTH_SHORT).show();
                saveSendMultipleImages(intent);
            }
        }
    }

    public void saveSendText(Intent intent){
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        Toast.makeText(getApplicationContext(), sharedText, Toast.LENGTH_LONG).show();
        new LoadImage().execute(sharedText);
    }
    public void saveSendImage(Intent intent){
        Uri imgUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

        if(imgUri != null) {
            Toast.makeText(getApplicationContext(), "imgUri", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveSendMultipleImages(Intent intent){
        ArrayList<Uri> imgUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);

        if(imgUris != null) {
            Toast.makeText(getApplicationContext(), "saveSendMultipleImages", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveSendVideo(Intent intent){
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        Toast.makeText(getApplicationContext(), sharedText, Toast.LENGTH_LONG).show();
        new SaveVideo().execute(sharedText);
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        ProgressDialog progressBar;
        String fileName;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = new ProgressDialog(MainActivity.this);
            progressBar.setMessage("downloading...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.setIndeterminate(true);
            progressBar.setCancelable(true);
            progressBar.show();
        }

        @Override
        protected Bitmap doInBackground(String... strings) {

            String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+FOLDER_PATH;
            File dir = new File(savePath);

            if(!dir.exists()){
                dir.mkdirs();
            }

            Date day = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
            fileName = String.valueOf(sdf.format(day));
            String localPath = savePath + "/"+fileName+".jpg";
            File imgFile = new File(localPath);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            FileOutputStream fos = null;
            File tempFile = imgFile.getParentFile();
            boolean tempFile2 = imgFile.getParentFile().mkdirs();
            try {
                if(imgFile.getParentFile() != null && !imgFile.getParentFile().mkdirs()) {
                    //handle permission problems here
                    Log.d(TAG, "No permission");
                }
                if (imgFile.createNewFile() || imgFile.isFile()){
                    //ready to write
                    Log.d(TAG, "ready to write");
                } else {
                    Log.d(TAG, "directory problem");
                }

                //URL url = new URL(strings[0]);
                URL url = new URL("https://upload.wikimedia.org/wikipedia/commons/thumb/d/de/Pearl_Winter_White_Russian_Dwarf_Hamster_-_Front.jpg/220px-Pearl_Winter_White_Russian_Dwarf_Hamster_-_Front.jpg");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                int s = conn.getInputStream().read();
                URL expandedUrl = conn.getURL();
                Log.d(TAG, expandedUrl.toString());
                URL imgUrl = null;
                String query = expandedUrl.getQuery();
                String[] pairs = query.split("&");
                Map<String, String> queryPairs = new HashMap<>();
                for(String pair : pairs){
                    int idx = pair.indexOf("=");
                    queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
                imgUrl = new URL(queryPairs.get("imgurl"));
                conn = (HttpsURLConnection) imgUrl.openConnection();
                //conn.setRequestMethod("GET");
                //conn.setDoInput(true);
                //conn.setInstanceFollowRedirects(true);
                //HttpsURLConnection.setFollowRedirects(true);
                Log.d(TAG, imgUrl.toString());
                Log.d(TAG, String.valueOf(conn.getResponseCode()));
                //Log.d(TAG, conn.getHeaderField("Location"));

                InputStream in = new BufferedInputStream(conn.getInputStream());
                downloadedImg = BitmapFactory.decodeStream(in);
                downloadedImg.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bitmapData = bos.toByteArray();
                fos = new FileOutputStream(imgFile);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return downloadedImg;
        }

        @Override
        protected void onPostExecute(Bitmap image){
            if (image == null) {
                progressBar.dismiss();
                Toast.makeText(getApplicationContext(), "empty", Toast.LENGTH_LONG).show();
            } else {
                progressBar.dismiss();
                imageView.setImageBitmap(image);
            }

        }
    }

    private class SaveVideo extends AsyncTask<String, Integer, String> {
        private Context context;
        private PowerManager.WakeLock wakeLock;
        private ProgressDialog progressBar;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar = new ProgressDialog(MainActivity.this);
            progressBar.setMessage("downloading...");
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressBar.setIndeterminate(true);
            progressBar.setCancelable(true);
            progressBar.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+FOLDER_PATH;
            File dir = new File(savePath);
            String fileName = "";
            if(!dir.exists()){
                dir.mkdirs();
            }

            Date day = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
            fileName = String.valueOf(sdf.format(day));
            String localPath = savePath + "/"+fileName+".jpg";
            File imgFile = new File(localPath);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            FileOutputStream fos = null;
            File tempFile = imgFile.getParentFile();
            boolean tempFile2 = imgFile.getParentFile().mkdirs();
            try {
                if(imgFile.getParentFile() != null && !imgFile.getParentFile().mkdirs()) {
                    //handle permission problems here
                    Log.d(TAG, "No permission");
                }
                if (imgFile.createNewFile() || imgFile.isFile()){
                    //ready to write
                    Log.d(TAG, "ready to write");
                } else {
                    Log.d(TAG, "directory problem");
                }

                URL url = new URL(strings[0]);
                //URL url = new URL("https://upload.wikimedia.org/wikipedia/commons/thumb/d/de/Pearl_Winter_White_Russian_Dwarf_Hamster_-_Front.jpg/220px-Pearl_Winter_White_Russian_Dwarf_Hamster_-_Front.jpg");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                int s = conn.getInputStream().read();
                URL expandedUrl = conn.getURL();
                Log.d(TAG, expandedUrl.toString());
                URL imgUrl = null;
                String query = expandedUrl.getQuery();
                String[] pairs = query.split("&");
                Map<String, String> queryPairs = new HashMap<>();
                for(String pair : pairs){
                    int idx = pair.indexOf("=");
                    queryPairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                }
                imgUrl = new URL(queryPairs.get("imgurl"));
                conn = (HttpsURLConnection) imgUrl.openConnection();
                //conn.setRequestMethod("GET");
                //conn.setDoInput(true);
                //conn.setInstanceFollowRedirects(true);
                //HttpsURLConnection.setFollowRedirects(true);
                Log.d(TAG, imgUrl.toString());
                Log.d(TAG, String.valueOf(conn.getResponseCode()));
                //Log.d(TAG, conn.getHeaderField("Location"));

                InputStream in = new BufferedInputStream(conn.getInputStream());
                downloadedImg = BitmapFactory.decodeStream(in);
                downloadedImg.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] bitmapData = bos.toByteArray();
                fos = new FileOutputStream(imgFile);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result){


        }
    }

}