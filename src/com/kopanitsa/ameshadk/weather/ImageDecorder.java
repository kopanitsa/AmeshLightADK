package com.kopanitsa.ameshadk.weather;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

public class ImageDecorder {
    private static final String TAG = "ImageDecorder";
    
    static enum Weather {
        SUNNY,
        RAIN
    };

    // location data
    // TODO need more accurate image latitude&longtitude!!!
    // ref:Shinbamba(righter/lower than konan)
    //      -> left:1610 top:1138 right:1670 bottom:1178
    private static final int ORIGINAL_IMAGE_WIDTH = 3080;
    private static final int ORIGINAL_IMAGE_HEIGHT = 1920;
    private static final double IMAGE_LEFT_LONGTITUDE  = 138.43597412109375;
    private static final double IMAGE_RIGHT_LONGTITUDE = 140.5975341796875;
    private static final double IMAGE_UP_LATITUDE      = 36.202174411834484;
    private static final double IMAGE_BOTTOM_LATITUDE  = 35.1041810882765;
    
    private static final int DEFAULT_LEFT   = 1887; // Shinagawa Konan Area
    private static final int DEFAULT_UP     = 1004;
    private static final int DEFAULT_RIGHT  = 1947;
    private static final int DEFAULT_BOTTOM = 1046;

    // amesh view color data(r,g,b)
    private static final int[] MAP_COLOR_LEVEL1 = {204,255,255};
    private static final int[] MAP_COLOR_LEVEL2 = {102,153,255};
    private static final int[] MAP_COLOR_LEVEL3 = { 51, 51,255};
    private static final int[] MAP_COLOR_LEVEL4 = {  0,255,0};
    private static final int[] MAP_COLOR_LEVEL5 = {255,255,0};
    private static final int[] MAP_COLOR_LEVEL6 = {255,153,0};
    private static final int[] MAP_COLOR_LEVEL7 = {255,0  ,255};
    private static final int[] MAP_COLOR_LEVEL8 = {255,0  ,0};

    private static final int PRECIPITATION_1 = 3;  // Žã‚¢‰J - 3mm–¢–ž
    private static final int PRECIPITATION_2 = 10; // •À‚Ì‰J
    private static final int PRECIPITATION_3 = 20; // ‚â‚â‹­‚¢‰J - 10mmˆÈã20mm–¢–ž
    private static final int PRECIPITATION_4 = 30; // ‹­‚¢‰J - 20mmˆÈã30mm–¢–ž
    private static final int PRECIPITATION_5 = 40; // ‚â‚âŒƒ‚µ‚¢‰J
    private static final int PRECIPITATION_6 = 50; // Œƒ‚µ‚¢‰J - 30mmˆÈã50mm–¢–ž
    private static final int PRECIPITATION_7 = 80; // ”ñí‚ÉŒƒ‚µ‚¢‰J - 50mmˆÈã80mm–¢–ž
    private static final int PRECIPITATION_8 = 100;// –Ò—ó‚È‰J - 80mmˆÈã

    private static final int THRESHOLD = 30;
    
    private int[] mDecodeArea = {DEFAULT_LEFT,
            DEFAULT_UP,
            DEFAULT_RIGHT,
            DEFAULT_BOTTOM};
    
    private class AmeshWeatherColor {
        public final int color;
        public final int preciputation;
        public AmeshWeatherColor(int[] c, int p){
            color = Color.rgb(c[0],c[1],c[2]);
            preciputation = p;
        }
    }
    private AmeshWeatherColor[] mAmeshColor = new AmeshWeatherColor[8];
    
    public ImageDecorder(){
        initAmeshColor();
    }
    
    public void initAmeshColor(){
        mAmeshColor[0] = new AmeshWeatherColor(MAP_COLOR_LEVEL1, PRECIPITATION_1);
        mAmeshColor[1] = new AmeshWeatherColor(MAP_COLOR_LEVEL2, PRECIPITATION_2);
        mAmeshColor[2] = new AmeshWeatherColor(MAP_COLOR_LEVEL3, PRECIPITATION_3);
        mAmeshColor[3] = new AmeshWeatherColor(MAP_COLOR_LEVEL4, PRECIPITATION_4);
        mAmeshColor[4] = new AmeshWeatherColor(MAP_COLOR_LEVEL5, PRECIPITATION_5);
        mAmeshColor[5] = new AmeshWeatherColor(MAP_COLOR_LEVEL6, PRECIPITATION_6);
        mAmeshColor[6] = new AmeshWeatherColor(MAP_COLOR_LEVEL7, PRECIPITATION_7);
        mAmeshColor[7] = new AmeshWeatherColor(MAP_COLOR_LEVEL8, PRECIPITATION_8);
    }

    public void setLocation(double latitude, double longtitude) {
        double centerX = (double)ORIGINAL_IMAGE_WIDTH *
                (latitude  - IMAGE_UP_LATITUDE)/ (IMAGE_BOTTOM_LATITUDE - IMAGE_UP_LATITUDE);
        double centerY = (double)ORIGINAL_IMAGE_HEIGHT *
                (longtitude - IMAGE_LEFT_LONGTITUDE) / (IMAGE_RIGHT_LONGTITUDE - IMAGE_LEFT_LONGTITUDE);

        mDecodeArea[0] = (int)centerX - 30;
        mDecodeArea[1] = (int)centerY - 20;
        mDecodeArea[2] = (int)centerX + 30;
        mDecodeArea[3] = (int)centerY + 20;

        Log.e(TAG,"DecodeArea -> left:"+mDecodeArea[0]+" top:"+mDecodeArea[1]+
                " right:"+mDecodeArea[2]+" bottom:"+mDecodeArea[3]);
    }
    
    public Weather decode(Bitmap bmp) {
        if (bmp.getWidth() != ORIGINAL_IMAGE_WIDTH ||
                bmp.getHeight() != ORIGINAL_IMAGE_HEIGHT){
            throw new RuntimeException("input image size is invalid. "+
                    "w:"+bmp.getWidth()+" h:"+bmp.getHeight());
        }
        Bitmap trimmed = trim(bmp);
        int accummedRain = getWeather(trimmed);
        if (accummedRain > THRESHOLD){
            return Weather.RAIN;
        }
        return Weather.SUNNY;
    }
    
    private Bitmap trim(Bitmap bmp){
        int width  = mDecodeArea[2] - mDecodeArea[0];
        int height = mDecodeArea[3] - mDecodeArea[1];
        Bitmap result=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(result);
        canvas.drawBitmap(bmp,-mDecodeArea[0],-mDecodeArea[1],null);
        return result;
    }
    
    private int getWeather(Bitmap bmp){
        int accummedRain = 0;
        for (int x=0; x<bmp.getWidth();x++){
            for (int y=0; y<bmp.getHeight();y++){
                for (int level = 0; level < 8; level++){
                    // ‚·‚×‚Ä‚ÌƒsƒNƒZƒ‹‚Ì~…—Ê‚ðÏŽZ‚·‚é
                    if (bmp.getPixel(x,y) == mAmeshColor[level].color){
                        accummedRain += mAmeshColor[level].preciputation;
                    }
                }
            }
        }
        return accummedRain;
    }

}
