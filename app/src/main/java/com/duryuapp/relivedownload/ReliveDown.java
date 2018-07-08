package com.duryuapp.relivedownload;

import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.StringTokenizer;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ReliveDown{
    private static final int BUFFER_SIZE = 1024;

    private CallbackEvent callbackEvent;
    boolean bCancel = false;

    ReliveDown(CallbackEvent event){
        callbackEvent = event;
        bCancel = false;
    }

    public void cancelDownload(){
        bCancel = true;
    }

    public void checkReliveInfo(String reliveUrl, String saveDir){
        Document doc;
        try{
            doc = Jsoup.connect(reliveUrl).get();
        }
        catch(IOException e){
            System.out.println("catch");
            callbackEvent.fileInfo("", "movie file not found!", 0);
            return;
        }
        //System.out.println(doc.title());
        callbackEvent.title(doc.title());

        Elements tags = doc.select(".video source");
        String mp4src = tags.attr("src");

        //mp4src = "abs " + mp4src;
        System.out.println(mp4src);
        if( !mp4src.startsWith("https://video.relive.cc") ){
            callbackEvent.fileInfo("", "movie file not found!", 0);
            return;
        }

        // StringTokenizer를 이용한 문자열 분리
        StringTokenizer tokens = new StringTokenizer(mp4src);
        String fileURL = tokens.nextToken("?");
        System.out.println(fileURL);

        //
        try {
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = httpConn.getHeaderField("Content-Disposition");
                String contentType = httpConn.getContentType();
                int contentLength = httpConn.getContentLength();

                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10,
                                disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                            fileURL.length());
                }

                //System.out.println("Content-Type = " + contentType);
                //System.out.println("Content-Disposition = " + disposition);
                //System.out.println("Content-Length = " + contentLength);
                //System.out.println("fileName = " + fileName);
                callbackEvent.fileInfo(fileURL, fileName, contentLength/1024);

            } else {
                System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            }
            httpConn.disconnect();
        }
        catch (IOException ex) {
            System.out.println("download fail") ;
            ex.printStackTrace();
        }
    }

    public void downloadRelive(String reliveUrl, String saveDir){
        bCancel = false;
        Document doc;
        try{
            doc = Jsoup.connect(reliveUrl).get();
        }
        catch(IOException e){
            Log.v("ywshin", "downloadRelive... connect fail");
            return;
        }
        //System.out.println(doc.title());
        callbackEvent.title(doc.title());

        Elements tags = doc.select(".video source");
        String mp4src = tags.attr("src"); 
        //System.out.println(mp4src);

        // StringTokenizer를 이용한 문자열 분리
        StringTokenizer tokens = new StringTokenizer(mp4src);
         
        String mp4url = tokens.nextToken("?");
        //System.out.println(mp4url);

        //
        try {
            downloadFile(mp4url, saveDir);
        }
        catch (IOException ex) {
            Log.v("ywshin", "downloadRelive... download fail");
            ex.printStackTrace();
            callbackEvent.downloadFailed();
        }
    }
 
    /**
     * Downloads a file from a URL
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @throws IOException
     */
    public void downloadFile(String fileURL, String saveDir) throws IOException {
        URL url = new URL(fileURL);
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        int responseCode = httpConn.getResponseCode();
 
        // always check HTTP response code first
        if (responseCode == HttpURLConnection.HTTP_OK) {
            String fileName = "";
            String disposition = httpConn.getHeaderField("Content-Disposition");
            String contentType = httpConn.getContentType();
            int contentLength = httpConn.getContentLength();
 
            if (disposition != null) {
                // extracts file name from header field
                int index = disposition.indexOf("filename=");
                if (index > 0) {
                    fileName = disposition.substring(index + 10,
                            disposition.length() - 1);
                }
            } else {
                // extracts file name from URL
                fileName = fileURL.substring(fileURL.lastIndexOf("/") + 1,
                        fileURL.length());
            }
 
            //System.out.println("Content-Type = " + contentType);
            //System.out.println("Content-Disposition = " + disposition);
            //System.out.println("Content-Length = " + contentLength);
            //System.out.println("fileName = " + fileName);
            callbackEvent.fileInfo(fileURL, fileName, contentLength/1024);
 
            // opens input stream from the HTTP connection
            InputStream inputStream = httpConn.getInputStream();
            String saveFilePath = saveDir + File.separator + fileName;
             
            // opens an output stream to save into file
            FileOutputStream outputStream = new FileOutputStream(saveFilePath);
            //System.out.println("file size = " + contentLength/1024 + "(kb)");

            int bytesRead = -1;
            double downloadByte = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int failCount = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                if(bCancel){
                    failCount++;
                    if(failCount < 10){
                        try {
                            Thread.sleep(500);
                            Log.v("ywshin", "sleep");
                        }
                        catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        continue;
                    }
                    outputStream.close();
                    inputStream.close();
                    callbackEvent.downloadCanceled();
                    httpConn.disconnect();
                    return;
                }
                outputStream.write(buffer, 0, bytesRead);
                downloadByte += bytesRead/1024.;
                //System.out.printf("downlaod : %d/%d (%d%%)\n", (int)downloadByte, contentLength/1024, (int)(downloadByte/(contentLength/1024)*100));
                callbackEvent.download((int)downloadByte, contentLength/1024);
            }
            
            outputStream.close();
            inputStream.close();
            callbackEvent.downloadCompleted();
        }
        else {
            System.out.println("No file to download. Server replied HTTP code: " + responseCode);
        }
        httpConn.disconnect();
    }
}
