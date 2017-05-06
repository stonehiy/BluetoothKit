package com.inuker.bluetooth.library;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.RemoteException;

import com.inuker.bluetooth.library.connect.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BluetoothResponse;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.utils.proxy.ProxyBulk;
import com.inuker.bluetooth.library.utils.proxy.ProxyInterceptor;
import com.inuker.bluetooth.library.utils.proxy.ProxyUtils;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by dingjikerbo on 16/4/8.
 */
public class BluetoothClientImpl implements IBluetoothService, ProxyInterceptor, Callback {

    private static final String TAG = BluetoothClientImpl.class.getSimpleName();

    private volatile static BluetoothClientImpl sInstance;

    private HandlerThread mWorkerThread;
    private Handler mWorkerHandler;

    private BluetoothClientImpl(Context context) {
        BluetoothContext.set(context);

        mWorkerThread = new HandlerThread(TAG);
        mWorkerThread.start();

        mWorkerHandler = new Handler(mWorkerThread.getLooper(), this);
    }

    public static BluetoothClientImpl getInstance(Context context) {
        if (sInstance == null) {
            synchronized (BluetoothClientImpl.class) {
                if (sInstance == null) {
                    BluetoothClientImpl client = new BluetoothClientImpl(context);
                    sInstance = ProxyUtils.getProxy(client, IBluetoothService.class, client);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void connect(String mac, BleConnectOptions options, final BluetoothResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void disconnect(String mac) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void read(String mac, ParcelUuid service, ParcelUuid character, IResponse response) throws RemoteException {

    }

    @Override
    public void write(String mac, ParcelUuid service, ParcelUuid character, byte[] value, IResponse response) throws RemoteException {

    }

    @Override
    public void read(String mac, UUID service, UUID character, final BluetoothResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(String mac, UUID service, UUID character, byte[] value, final BluetoothResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readDescriptor(String mac, UUID service, UUID character, UUID descriptor, final BluetoothResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeDescriptor(String mac, UUID service, UUID character, UUID descriptor, byte[] value, final BluetoothResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notify(final String mac, final UUID service, final UUID character, final BluetoothResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unnotify(final String mac, final UUID service, final UUID character, final BluetoothResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void indicate(final String mac, final UUID service, final UUID character, final BluetoothResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void unindicate(String mac, UUID service, UUID character, BluetoothResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readRssi(String mac, final BluetoothResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void search(SearchRequest request, final BluetoothResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void stopSearch() {
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
    public void connect(String mac, BleConnectOptions options, IResponse response) throws RemoteException {

    }

    @Override
    public IBinder asBinder() {
        return null;
    }
}
