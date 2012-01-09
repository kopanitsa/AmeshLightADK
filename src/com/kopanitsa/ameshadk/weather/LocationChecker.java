package com.kopanitsa.ameshadk.weather;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


// copied from http://d.hatena.ne.jp/orangesignal/20101223/1293079002
public class LocationChecker {
    private static final String TAG = "LocationListener";
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Timer mLocationTimer;
    private long mTime;
    private Context mContext;
    private LocationCheckerListener mListener;

    public LocationChecker(Context context){
        mContext = context;
    }
    
    public void checkLocation(LocationCheckerListener listener) {
        mListener = listener;
        mLocationManager = (LocationManager) mContext.getSystemService
        (Context.LOCATION_SERVICE);
        if (mLocationManager == null) {
            return;
        }

        final Criteria criteria = new Criteria();
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setAltitudeRequired(false);

        final String provider = mLocationManager.getBestProvider(criteria, true);

        if (provider == null) {
            new AlertDialog.Builder(mContext)
            .setTitle("���ݒn�@�\�����P")
            .setMessage("���݁A�ʒu���͈ꕔ�L���ł͂Ȃ����̂�����܂��B���̂悤�ɐݒ肷��ƁA�����Ƃ����΂₭���m�Ɍ��ݒn�����o�ł���悤�ɂȂ�܂�:\n\n�� �ʒu���̐ݒ��GPS�ƃ��C�����X�l�b�g���[�N���I���ɂ���\n\n�� Wi-Fi���I���ɂ���")
            .setPositiveButton("�ݒ�", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                    try {
                        mContext.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                    } catch (final ActivityNotFoundException e) {
                    }
                }
            })
            .setNegativeButton("�X�L�b�v", new DialogInterface.OnClickListener() {
                @Override 
                public void onClick(final DialogInterface dialog, final int which) {
                    // do nothing;
                }
            })
            .create()
            .show();

            stopLocationService();
            return;
        }

        final Location lastKnownLocation = mLocationManager.getLastKnownLocation(provider);
        if (lastKnownLocation != null && (new Date().getTime() - lastKnownLocation.getTime()) <= (5 * 60 * 1000L)) {
            setLocation(lastKnownLocation);
            return;
        }

        // Toast �̕\���� LocationListener �̐������Ԃ����肷��^�C�}�[���N�����܂��B
        mLocationTimer = new Timer(true);
        mTime = 0L;
        final Handler handler = new Handler();
        mLocationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mTime == 1000L) {
                            Toast.makeText(mContext, "���ݒn����肵�Ă��܂��B", Toast.LENGTH_LONG).show();
                        } else if (mTime >= (30 * 1000L)) {
                            Toast.makeText(mContext, "���ݒn�����ł��܂���ł����B", Toast.LENGTH_LONG).show();
                            stopLocationService();
                        }
                        mTime = mTime + 1000L;
                    }
                });
            }
        }, 0L, 1000L);

        // �ʒu���̎擾���J�n���܂��B
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                setLocation(location);
            }
            @Override public void onProviderDisabled(final String provider) {}
            @Override public void onProviderEnabled(final String provider) {}
            @Override public void onStatusChanged(final String provider, final int status, final Bundle extras) {}
        };
        Log.e(TAG,"start to check location");
        mLocationManager.requestLocationUpdates(provider, 60000, 0, mLocationListener);
    }

    void stopLocationService() {
        if (mLocationTimer != null) {
            mLocationTimer.cancel();
            mLocationTimer.purge();
            mLocationTimer = null;
        }
        if (mLocationManager != null) {
            if (mLocationListener != null) {
                mLocationManager.removeUpdates(mLocationListener);
                mLocationListener = null;
            }
            mLocationManager = null;
        }
    }

    void setLocation(final Location location) {
        stopLocationService();
        mListener.onLocationDetected(location);
    }
    
    public interface LocationCheckerListener {
        void onLocationDetected(Location location);
    }

}
