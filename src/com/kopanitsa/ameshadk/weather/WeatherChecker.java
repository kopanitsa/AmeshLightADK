package com.kopanitsa.ameshadk.weather;

import java.util.Calendar;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Handler;
import android.util.Log;

import com.kopanitsa.ameshadk.AccessoryController;
import com.kopanitsa.ameshadk.AmeshLightActivity;
import com.kopanitsa.ameshadk.weather.ImageDecorder.Weather;
import com.kopanitsa.ameshadk.weather.LocationChecker.LocationCheckerListener;

public class WeatherChecker {
    private static final String TAG = "WeatherChecker";
    //private static final int REPEAT_INTERVAL = 1000 * 60 * 5;
    private static final int REPEAT_INTERVAL = 1000 * 10;
    private Handler mHandler = new Handler();
    private Runnable mWeatherCheckRunnable;
    private AccessoryController mAccesoryController;
    private AmeshAccessor mAmeshAccessor;
    private ImageDecorder mImageDecorder;
    private LocationChecker mLocationChecker;
    
    public WeatherChecker(AmeshLightActivity ameshLightActivity,
            AccessoryController accesoryController) {
        mAccesoryController = accesoryController;
        mAmeshAccessor = new AmeshAccessor(ameshLightActivity);
        mImageDecorder = new ImageDecorder();
        mWeatherCheckRunnable = new Runnable() {
            public void run() {
                checkWeather();
                mHandler.postDelayed(mWeatherCheckRunnable, REPEAT_INTERVAL);
            }
        };
        mLocationChecker = new LocationChecker(ameshLightActivity);
        mLocationChecker.checkLocation(new LocationCheckerListener() {
            @Override
            public void onLocationDetected(Location location) {
                double latitude = location.getLatitude();
                double longtitude = location.getLongitude();
                Log.e(TAG,"***** la:"+latitude + " lo:"+longtitude);
                mImageDecorder.setLocation(latitude, longtitude);
            }
        });
    }
    
    public void startMonitoring(){
        mHandler.post(mWeatherCheckRunnable);
    }
    
    public void stopMonitoring(){
        mHandler.removeCallbacks(mWeatherCheckRunnable);
    }
    
    /**
     * 1. access to AmeshView
     * (1'. check current location)
     * 2. decode image
     * 3. send command to ADK
     * */
    private void checkWeather(){
        Bitmap bmp = mAmeshAccessor.getImage();
        ImageDecorder.Weather value = mImageDecorder.decode(bmp);

        if (value == Weather.SUNNY) {
            Log.i(TAG,"SUNNY at " + getCurrentTime());
            mAccesoryController.sendCommand(AmeshAdkCommand.COMMAND_WEATHER, (byte)0,
                    AmeshAdkCommand.EVENT_WEATHER_SUNNY);
        } else if (value == Weather.RAIN) {
            Log.i(TAG,"RAIN at " + getCurrentTime());
            mAccesoryController.sendCommand(AmeshAdkCommand.COMMAND_WEATHER, (byte)0,
                    AmeshAdkCommand.EVENT_WEATHER_RAIN);
        }
    }

    private String getCurrentTime(){
        final Calendar calendar = Calendar.getInstance();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int hour = calendar.get(Calendar.HOUR_OF_DAY);
        final int minute = calendar.get(Calendar.MINUTE);
        final int second = calendar.get(Calendar.SECOND);

        return year + "/" + (month + 1) + "/" + day + "/" + " " +
            hour + ":" + minute + ":" + second;
    }
    
}
