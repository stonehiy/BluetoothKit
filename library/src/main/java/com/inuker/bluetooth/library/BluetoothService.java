package com.inuker.bluetooth.library;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;

import com.inuker.bluetooth.library.connect.BleConnectOptions;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.ISearchResponse;
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
        return mServiceStub;
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
            mService = mServiceStub;
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

    private static final IBluetoothService.Stub mServiceStub = new IBluetoothService.Stub() {

        @Override
        public void connect(String mac, BleConnectOptions options, IResponse response) throws RemoteException {
            BluetoothServiceImpl.getInstance().connect(mac, options, response);
        }

        @Override
        public void disconnect(String mac) throws RemoteException {
            BluetoothServiceImpl.getInstance().disconnect(mac);
        }

        @Override
        public void read(String mac, ParcelUuid service, ParcelUuid character, IResponse response) throws RemoteException {
            BluetoothServiceImpl.getInstance().read(mac, service, character, response);
        }

        @Override
        public void write(String mac, ParcelUuid service, ParcelUuid character, byte[] value, IResponse response) throws RemoteException {
            BluetoothServiceImpl.getInstance().write(mac, service, character, value, response);
        }

        @Override
        public void readDescriptor(String mac, ParcelUuid service, ParcelUuid character, ParcelUuid descriptor, IResponse response) throws RemoteException {
            BluetoothServiceImpl.getInstance().read(mac, service, character, response);
        }

        @Override
        public void writeDescriptor(String mac, ParcelUuid service, ParcelUuid character, ParcelUuid descriptor, byte[] value, IResponse response) throws RemoteException {
            BluetoothServiceImpl.getInstance().writeDescriptor(mac, service, character, descriptor, value, response);
        }

        @Override
        public void notify(String mac, ParcelUuid service, ParcelUuid character, IResponse response) throws RemoteException {
            BluetoothServiceImpl.getInstance().notify(mac, service, character, response);
        }

        @Override
        public void unnotify(String mac, ParcelUuid service, ParcelUuid character, IResponse response) throws RemoteException {
            BluetoothServiceImpl.getInstance().unnotify(mac, service, character, response);
        }

        @Override
        public void indicate(String mac, ParcelUuid service, ParcelUuid character, IResponse response) throws RemoteException {
            BluetoothServiceImpl.getInstance().indicate(mac, service, character, response);
        }

        @Override
        public void unindicate(String mac, ParcelUuid service, ParcelUuid character, IResponse response) throws RemoteException {
            BluetoothServiceImpl.getInstance().unindicate(mac, service, character, response);
        }

        @Override
        public void readRssi(String mac, IResponse response) throws RemoteException {
            BluetoothServiceImpl.getInstance().readRssi(mac, response);
        }

        @Override
        public void search(SearchRequest request, ISearchResponse response) throws RemoteException {
            BluetoothServiceImpl.getInstance().search(request, response);
        }

        @Override
        public void stopSearch() throws RemoteException {
            BluetoothServiceImpl.getInstance().stopSearch();
        }
    };
}
