package com.kopanitsa.ameshadk;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import com.kopanitsa.ameshadk.weather.WeatherChecker;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

public class AmeshLightActivity extends Activity {
    private static final String TAG = "AmeshLightActivity";
    
    private static final String ACTION_USB_PERMISSION = "com.kopanitsa.blackphone.action.USB_PERMISSION";

    /** UsbManager�ɓn��Permission���炤�p��intent */
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;

    /** Allows you to enumerate and communicate with connected USB accessories. */
    private UsbManager mUsbManager;
    private AccessoryController mAccesoryController;

    /** AmeshLight specific module */
    private WeatherChecker mWeatherChecker;
    
    /**
     * USB���h���������Ƃ����m���邽�߂�Broadcast Receiver
     * action : com.kopanitsa.movingfan.action.USB_PERMISSION
     * action : ACTION_USB_ACCESSORY_DETACHED
     * */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // Attach���ꂽ�Ƃ�
            if (ACTION_USB_PERMISSION.equals(action)) {
                Log.e(TAG, "onReceive ACTION_USB_PERMISSION");
                synchronized (this) {
                    // intent��UsbAccesory���܂܂�Ă���
                    UsbAccessory accessory = UsbManager.getAccessory(intent);
                    // USB���ڑ����ꂽ�Ƃ��ɕ\�������_�C�A���O��
                    // ���[�U���I�������l(��2�����̓f�t�H���g�l)
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        mAccesoryController.openAccessory(accessory);
                    } else {
                        Log.d(TAG, "permission denied for accessory "
                                + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            // Detach���ꂽ�Ƃ�
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = UsbManager.getAccessory(intent);
                Log.e(TAG, "onReceive ACTION_USB_ACCESSORY_DETACHED");
                if (accessory != null && accessory.equals(mAccesoryController.getAccessory())) {
                    mAccesoryController.closeAccessory();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.e(TAG, "onCreate");
        setupUsbManager();
    }

    private void setupUsbManager(){
        mUsbManager = UsbManager.getInstance(this);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, 
                new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);
        
        // �I�����ɕۑ�����mAccesory������Ύg�p����B
        if (getLastNonConfigurationInstance() != null && 
                mAccesoryController != null) {
            UsbAccessory accessory = (UsbAccessory) getLastNonConfigurationInstance();
            mAccesoryController.openAccessory(accessory);
        }
        
        mAccesoryController = new AccessoryController(mUsbManager);
    }

    public void startUsbCommunication(){
        // UsbManager����A�N�Z�T���̃C���X�^���X���擾����B
        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        Log.e(TAG, "startUsbCommunication");
        if (accessory != null) {
            // ���ł�permission�������Ă�����A�ڑ�����B
            // �����Ă��Ȃ����requestPermission����
            if (mUsbManager.hasPermission(accessory)){
                mAccesoryController.openAccessory(accessory);
            } else {
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending){
                        Log.e(TAG, "requestPermission");
                        mUsbManager.requestPermission(accessory,
                               mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.e(TAG, "mAccessory is null");
        }
    }

    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        mAccesoryController.closeAccessory(); // at first, close accessory and,
        startUsbCommunication(); // open accessory
        
        mWeatherChecker = new WeatherChecker(this, mAccesoryController);
        mWeatherChecker.startMonitoring();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "onPause");
        mWeatherChecker.stopMonitoring();
        mAccesoryController.closeAccessory();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        mAccesoryController.closeAccessory();
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }
    
}