package com.duryuapp.relivedownload;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static android.webkit.ConsoleMessage.MessageLevel.LOG;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {
    private AdView mAdView = null;

    EditText textReliveUrl;
    TextView textFileUrl;
    TextView textFileName;
    TextView textFileSize;
    CircularProgressBar circularProgressBar;
    VideoView videoView;
    String reliveUrl = "";
    String downloadDir = "";
    String downloadFileName = "";
    int mFileSize = 0;

    public void MessageBox(String msg, int showlength){
        Toast.makeText(getApplicationContext(), msg, showlength).show();
    }

    private void enbleEditText(EditText editText, boolean enable) {
        //editText.setFocusable(enable);
        editText.setEnabled(enable);
        //editText.setCursorVisible(enable);
    }

    public void setStateChecking(){
        textReliveUrl.setBackgroundResource( R.drawable.edit_border);
        enbleEditText(textReliveUrl,true);
        circularProgressBar.setVisibility(View.GONE);

        textFileSize.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        //
        textFileName.setVisibility(View.GONE);

        findViewById(R.id.button_download).setVisibility(View.GONE);
        findViewById(R.id.button_cancel).setVisibility(View.GONE);
        findViewById(R.id.button_export).setVisibility(View.GONE);
        findViewById(R.id.button_reset).setVisibility(View.GONE);
    }

    public void setStateReady(){
        textReliveUrl.setBackgroundResource( R.drawable.edit_border);
        enbleEditText(textReliveUrl,true);
        circularProgressBar.setVisibility(View.VISIBLE);


        textFileSize.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);

        //
        textFileName.setVisibility(View.VISIBLE);

        findViewById(R.id.button_download).setVisibility(View.VISIBLE);
        findViewById(R.id.button_cancel).setVisibility(View.GONE);
        findViewById(R.id.button_export).setVisibility(View.GONE);
        findViewById(R.id.button_reset).setVisibility(View.GONE);

        setAdMob();
    }

    public void setStateDownloading(){
        textReliveUrl.setBackgroundResource( R.drawable.edit_border_disable);
        enbleEditText(textReliveUrl,false);

        circularProgressBar.setVisibility(View.VISIBLE);

        textFileSize.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);

        //
        textFileName.setVisibility(View.VISIBLE);

        findViewById(R.id.button_download).setVisibility(View.GONE);
        findViewById(R.id.button_cancel).setVisibility(View.VISIBLE);
        findViewById(R.id.button_export).setVisibility(View.GONE);
        findViewById(R.id.button_reset).setVisibility(View.GONE);
    }

    public void setStateCompleted(){
        textReliveUrl.setBackgroundResource( R.drawable.edit_border_disable);
        enbleEditText(textReliveUrl,false);
        circularProgressBar.setVisibility(View.GONE);

        videoView.setVisibility(View.VISIBLE);
        textFileSize.setVisibility(View.VISIBLE);

        //
        textFileName.setVisibility(View.VISIBLE);

        findViewById(R.id.button_download).setVisibility(View.GONE);
        findViewById(R.id.button_cancel).setVisibility(View.GONE);
        findViewById(R.id.button_export).setVisibility(View.VISIBLE);
        findViewById(R.id.button_reset).setVisibility(View.VISIBLE);

        //
        final MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);


        videoView.setVideoPath(downloadDir + "/" + downloadFileName);
        videoView.start();

        videoView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaController.show(2000);
                videoView.start();
            }
        }, 100);
        MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener(){
            public void onCompletion(MediaPlayer mp){
                videoView.start();
            }
        };

        videoView.setOnCompletionListener(completionListener);
    }

    public void checkUrl(){
        reliveUrl = textReliveUrl.getText().toString();
        downloadDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();

        if(!reliveUrl.startsWith("https://www.relive.cc/view/")){
            Log.v("ywshin", "not relive url");
            return;
        }

        textFileSize.setText("Calculating the file size...");
        setStateChecking();

        JsoupAsyncTask jsoupAsyncTask = new JsoupAsyncTask(0);
        jsoupAsyncTask.execute();
    }

    JsoupAsyncTask jsoup = null;
    public void run(){
        jsoup = new JsoupAsyncTask(1);
        jsoup.execute();
    }
    public void cancel(){
        jsoup.cancelDownload();
    }

    public void setAdMob(){
        if(mAdView != null){
            return;
        }
        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this,"ca-app-pub-7038722453555715~8532516786");
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
            }
        });

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textReliveUrl = (EditText)findViewById(R.id.textview_reliveurl);
        textReliveUrl.setBackgroundResource( R.drawable.edit_border);

        textFileName = (TextView)findViewById(R.id.textview_filename);
        textFileSize = (TextView)findViewById(R.id.textview_filesize);
        circularProgressBar = (CircularProgressBar)findViewById(R.id.circularProgress);
        circularProgressBar.setProgressColor(Color.argb(255, 255, 197, 0));
        circularProgressBar.setProgressBackColor(Color.argb(50,255, 197, 0));
        circularProgressBar.setProgressWidth(50);
        videoView = (VideoView)findViewById(R.id.videoView);
        reliveUrl = "";
        downloadDir = "";

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        // 인텐트 정보가 있는 경우 실행
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);    // 가져온 인텐트의 텍스트 정보
                new AlertDialog.Builder(this)
                        .setTitle("ReliveDownload")
                        .setMessage(sharedText )
                        .setPositiveButton(android.R.string.ok,null)
                        .setCancelable(false)
                        .create()
                        .show();

                Log.v("ywshin", sharedText);
                if(sharedText.contains("http")){
                    int index = sharedText.indexOf("http");
                    textReliveUrl.setText(sharedText.substring(index));

                    String title = sharedText.substring(0, index-1).replace("Check out ","");
                    getSupportActionBar().setTitle(title);
                }
            }
        }

        checkUrl();

        {
            PermissionRequester.Builder request = new PermissionRequester.Builder(this);
            request.create().request(Manifest.permission.INTERNET, 10000, new PermissionRequester.OnClickDenyButtonListener() {
                @Override
                public void onClick(Activity activity) {
                    Toast.makeText(activity, "인터넷 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
            });
        }

        {
            PermissionRequester.Builder request = new PermissionRequester.Builder(this);
            request.create().request(Manifest.permission.READ_EXTERNAL_STORAGE, 10000, new PermissionRequester.OnClickDenyButtonListener() {
                @Override
                public void onClick(Activity activity) {
                    Toast.makeText(activity, "파일읽기 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
            });
        }

        {
            PermissionRequester.Builder request = new PermissionRequester.Builder(this);
            request.create().request(Manifest.permission.WRITE_EXTERNAL_STORAGE, 10000, new PermissionRequester.OnClickDenyButtonListener() {
                @Override
                public void onClick(Activity activity) {
                    Toast.makeText(activity, "파일쓰기 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    activity.finish();
                }
            });
        }

        //final JsoupAsyncTask jsoup = new JsoupAsyncTask(1);



        findViewById(R.id.button_download).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        reliveUrl = textReliveUrl.getText().toString();
                        downloadDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                        Toast.makeText(getApplicationContext(), downloadDir, Toast.LENGTH_LONG).show();

                        //ReliveDown rd = new ReliveDown(callbackEvent);
                        //rd.downloadRelive(reliveUrl, downloadDir);
                        //jsoup = new JsoupAsyncTask(1);
                        //jsoup.execute();
                        run();
                    }
                }
        );

        findViewById(R.id.button_cancel).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        //jsoup.cancelDownload();
                        cancel();
                    }
                }
        );

        findViewById(R.id.button_reset).setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        circularProgressBar.setProgress(0);
                        textFileSize.setText(String.format("%.2f(MB)", mFileSize/1024.));
                        setStateReady();
                    }
                }
        );

        findViewById(R.id.button_export).setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        String filepath = downloadDir + "/" + downloadFileName;
                        Log.v("ywshin", "export button : " + filepath);
                        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                        File fileWithinMyDir = new File(filepath);

                        if(fileWithinMyDir.exists()) {
                            intentShareFile.setType("video/mpeg");
                            //intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+filepath));
                            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse(filepath));

                            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,"Sharing File...");
                            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...");

                            startActivity(Intent.createChooser(intentShareFile, "Share File"));
                        }
                    }
                }
        );

        textReliveUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkUrl();
            }
        });
    }

    private class JsoupAsyncTask extends AsyncTask<Void, Void, Void> {

        private int mode;
        ReliveDown rd = null;

        JsoupAsyncTask(int _mode){
            mode = _mode;
            rd = new ReliveDown(callbackEvent);
        }

        public void cancelDownload(){
            if(rd == null){
                return;
            }
            rd.cancelDownload();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //rd.downloadRelive(reliveUrl, downloadDir);
                if( mode == 0 ) {
                    rd.checkReliveInfo(reliveUrl, downloadDir);
                }
                else{
                    rd.downloadRelive(reliveUrl, downloadDir);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {

        }

        CallbackEvent callbackEvent = new CallbackEvent(){
            @Override
            public void title(final String title){
                //callbackUI.title(title);
                //Log.v("ywshin", String.format("title : %s\n", title));
                //Toast.makeText(getApplicationContext(), title, Toast.LENGTH_LONG).show();

                runOnUiThread(new Runnable() {
                    public void run() {
                        getSupportActionBar().setTitle(title);

                    }
                });
            }

            @Override
            public void fileInfo(final String fileurl, final String filename, final int filesize){
                mFileSize = filesize;
                Log.v("ywshin", String.format("fileurl : %s\n", fileurl));
                Log.v("ywshin", String.format("filename : %s\n", filename));
                Log.v("ywshin", String.format("filesize : %.2f(mb)\n", filesize/1024.));

                runOnUiThread(new Runnable() {
                    public void run() {

                        if(filesize > 0) {
                            downloadFileName = filename;
                            textFileName.setText(filename);
                            textFileSize.setText(String.format("%.2f(MB)", filesize/1024.));

                            if(mode == 0){
                                setStateReady();
                            }
                            else if(mode == 1){
                                setStateDownloading();
                            }
                        }
                        else{
                            textFileSize.setText("Unable to determine file information.");
                            setStateChecking();
                        }
                    }
                });
            }

            @Override
            public void download( final int cur, final int total) {
                // TODO Auto-generated method stub
                final int progress = (int)(100.0*cur/total);
                //callbackUI.download(cur, total);
                Log.v("ywshin", String.format("downlaod : %d/%d (%d%%)", cur, total, progress));
                //progressBar.setProgress(progress);

                runOnUiThread(new Runnable() {
                    public void run() {
                        circularProgressBar.setProgress(progress);
                        textFileSize.setText(String.format("%.2f/%.2f(MB)", cur/1024.f, total/1024.f));
                        setStateDownloading();
                    }
                });

            }

            @Override
            public void downloadCompleted() {
                // TODO Auto-generated method stub
                //callbackUI.downloadCompleted();
                //Log.v("ywshin", "\ndownlaod completed\n");
                //Toast.makeText(getApplicationContext(), "downlaod completed", Toast.LENGTH_LONG).show();

                runOnUiThread(new Runnable() {
                    public void run() {
                        setStateCompleted();
                    }
                });
            }

            @Override
            public void downloadCanceled() {
                // TODO Auto-generated method stub
                runOnUiThread(new Runnable() {
                    public void run() {
                        setStateReady();
                    }
                });
            }

            @Override
            public void downloadFailed() {
                // TODO Auto-generated method stub
                runOnUiThread(new Runnable() {
                    public void run() {
                        setStateReady();
                        MessageBox("Download failed. Try again.", Toast.LENGTH_LONG);
                    }
                });
            }
        };


    }
}
