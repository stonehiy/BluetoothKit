package com.inuker.bluetooth.library;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;

import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.utils.proxy.ProxyBulk;
import com.inuker.bluetooth.library.utils.proxy.ProxyInterceptor;
import com.inuker.bluetooth.library.utils.proxy.ProxyUtils;

import java.lang.reflect.Method;


/**
 * Created by dingjikerbo on 16/4/8.
 */
public class BluetoothClientImpl implements IBluetoothService, ProxyInterceptor, Callback {

    private static final String TAG = BluetoothClientImpl.class.getSimpleName();

    private volatile static IBluetoothClient sInstance;

    private HandlerThread mWorkerThread;
    private Handler mWorkerHandler;

    private BluetoothClientImpl(Context context) {
        BluetoothContext.set(context.getApplicationContext());

        mWorkerThread = new HandlerThread(TAG);
        mWorkerThread.start();

        mWorkerHandler = new Handler(mWorkerThread.getLooper(), this);
    }

    public static IBluetoothClient getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BluetoothClientImpl.class) {
                if (sInstance == null) {
                    BluetoothClientImpl client = new BluetoothClientImpl(context);
                    sInstance = ProxyUtils.getProxy(client, IBluetoothClient.class, client);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void connect(String mac, BleConnectOptions options, IResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disconnect(String mac) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void read(String mac, ParcelUuid service, ParcelUuid character, IResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(String mac, ParcelUuid service, ParcelUuid character, byte[] value, IResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readDescriptor(String mac, ParcelUuid service, ParcelUuid character, ParcelUuid descriptor, IResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeDescriptor(String mac, ParcelUuid service, ParcelUuid character, ParcelUuid descriptor, byte[] value, IResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notify(String mac, ParcelUuid service, ParcelUuid character, IResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unnotify(String mac, ParcelUuid service, ParcelUuid character, IResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void indicate(String mac, ParcelUuid service, ParcelUuid character, IResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unindicate(String mac, ParcelUuid service, ParcelUuid character, IResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readRssi(String mac, IResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onIntercept(final Object object, final Method method, final Object[] args) {
        mWorkerHandler.obtainMessage(0, new ProxyBulk(object, method, args)).sendToTarget();
        return true;
    }

    @Override
    public boolean handleMessage(Message msg) {
        BluetoothService.safeCallRemote((ProxyBulk) msg.obj);
        return true;
    }

    @Override
    public IBinder asBinder() {
        throw new UnsupportedOperationException();
    }
}
