package com.kopanitsa.ameshadk;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

public class AccessoryController {
    private static final String TAG = "AccesoryController";
    
    /** Allows you to enumerate and communicate with connected USB accessories. */
    private UsbManager mUsbManager;
    /** Represents a USB accessory and contains methods to access its identifying information.*/
    private UsbAccessory mAccessory;
    private ParcelFileDescriptor mFileDescriptor;
    //private FileInputStream mInputStream;
    private FileOutputStream mOutputStream;

    AccessoryController(UsbManager usbManager){
        mUsbManager = usbManager;
    }
    
    /**
     * accessoryをUsbManagerから取得する。
     * accessoryとコミュニケートするスレッドをスタートさせる。
     * */
    public void openAccessory(UsbAccessory accessory) {
        Log.e(TAG, "openAccessory");
        mAccessory = accessory;
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            //mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            Log.e(TAG, "accessory opened");
        } else {
            Log.e(TAG, "accessory open fail");
        }
    }

    public void closeAccessory() {
        Log.d(TAG,"closeAccesory");
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }

    public UsbAccessory getAccessory() {
        return mAccessory;
    }
    
    public void sendCommand(byte command, byte target, int value) {
        byte[] buffer = new byte[3];
        if (value > 255)
            value = 255;

        buffer[0] = command;
        buffer[1] = target;
        buffer[2] = (byte) value;
        if (mOutputStream != null && buffer[1] != -1) {
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
    }
}
