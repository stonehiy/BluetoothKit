package com.inuker.bluetooth.library;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.inuker.bluetooth.library.utils.BluetoothLog;
import com.inuker.bluetooth.library.utils.ReflectUtils;
import com.inuker.bluetooth.library.utils.proxy.ProxyBulk;

import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;

/**
 * Created by dingjikerbo on 16/4/8.
 */
public class BluetoothService extends Service {

    private static volatile IBluetoothService mService;
    private static volatile CountDownLatch mLatch;

    @Override
    public void onCreate() {
        super.onCreate();
        BluetoothContext.set(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return BluetoothServiceImpl.getInstance();
    }

    private static IBluetoothService getService() {
        while (mService == null) {
            bindServiceSync();
        }
        return mService;
    }

    private static void bindServiceSync() {
        if (BluetoothContext.bindService(BluetoothService.class, mConnection, Context.BIND_AUTO_CREATE)) {
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
        mLatch.countDown();
        mLatch = null;
    }

    private static void waitServiceReady() {
        if (mLatch == null) {
            mLatch = new CountDownLatch(1);
        }
        try {
            mLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void safeCallRemote(ProxyBulk bulk) {
        Method remote = ReflectUtils.getMethod(IBluetoothService.class, bulk.method);
        try {
            remote.invoke(getService(), bulk.args);
        } catch (Throwable e) {
            BluetoothLog.e(e);
        }
    }
}
