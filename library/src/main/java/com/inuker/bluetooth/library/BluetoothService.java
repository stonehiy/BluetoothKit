package com.inuker.bluetooth.library;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;

import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.proxy.ProxyBulk;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by dingjikerbo on 16/4/8.
 */
public class BluetoothService extends Service {

    private static Context mContext;
    private static volatile IBluetoothService mService;
    private static volatile CountDownLatch mCountDownLatch;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        BluetoothContext.set(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        BluetoothLog.v(String.format("BluetoothService onBind"));
        return BluetoothServiceImpl.getInstance().asBinder();
    }

    private static IBluetoothService getService() {
        while (mService == null) {
            bindServiceSync();
        }
        return mService;
    }

    private static void bindServiceSync() {
        Intent intent = new Intent();
        intent.setClass(mContext, BluetoothService.class);

        if (mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
            waitServiceReady();
        } else {
            mService = BluetoothServiceImpl.getInstance();
        }
    }

    private static final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IBluetoothService.Stub.asInterface(service);
            notifyServiceReady();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    private static void notifyServiceReady() {
        if (mCountDownLatch == null) {
            throw new IllegalStateException();
        }
        mCountDownLatch.countDown();
        mCountDownLatch = null;
    }

    private static void waitServiceReady() {
        if (mCountDownLatch == null) {
            mCountDownLatch = new CountDownLatch(1);
        }
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void safeCallRemote(ProxyBulk bulk) {
        try {
            Method method = bulk.method;
            Method remoteMethod = IBluetoothService.class.getDeclaredMethod(method.getName(), method.getParameterTypes());
            remoteMethod.setAccessible(true);
            remoteMethod.invoke(getService(), bulk.args);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
