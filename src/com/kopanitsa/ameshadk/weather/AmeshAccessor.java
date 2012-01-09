package com.kopanitsa.ameshadk.weather;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class AmeshAccessor {
    private static final String TAG = "AmeshAccessor";
    private static final String mUrlHeader = "http://tokyo-ame.jwa.or.jp/mesh/100/";
    private static final String mUrlFooter = ".gif";
    private Context mContext;

    public AmeshAccessor(Context context){
        mContext = context;
    }
    
    public Bitmap getImage() {
        Drawable drawable = null;
        Bitmap bmp = null;
        URL url;
        try {
            HttpURLConnection http;
            url = new URL(createUrl());
            http = (HttpURLConnection)url.openConnection();
            http.setRequestMethod("GET");
            http.connect();
            InputStream in = http.getInputStream();
            drawable = Drawable.createFromStream(in, "a");
            in.close();
        } catch (MalformedURLException e) {
            Log.e(TAG,"MalformedURLException");
            Log.e(TAG, "message", new Throwable());
        } catch (IOException e) {
            Log.e(TAG,"IOException");
            Log.e(TAG, "message", new Throwable());
        }
        if (drawable != null) {
            bmp = ((BitmapDrawable) drawable).getBitmap();
            //addImageForDebug(bmp);
        }
        return bmp;
    }

    private String createUrl(){
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
        Date date = new Date();
        String time = df.format(date);

        char d = time.charAt(11);
        int singleDigit = Character.digit(d, 10);
        if (singleDigit>5){
            singleDigit = 5;
        } else {
            singleDigit = 0;
        }
        time = time.substring(0,11);
        
        String url = mUrlHeader + time + singleDigit + mUrlFooter;
        Log.i(TAG, "amesh URL is "+url);

        return url;
    }

    private Uri addImageForDebug(Bitmap bitmap) {
        String name = "test_amesh.jpg";
        ContentResolver cr = mContext.getContentResolver();
        String uriStr = MediaStore.Images.Media.insertImage(cr, bitmap, name,  
                null);  
        Log.e(TAG, "[debug] image saved" + uriStr);
        return Uri.parse(uriStr);  
    }  
  
}
