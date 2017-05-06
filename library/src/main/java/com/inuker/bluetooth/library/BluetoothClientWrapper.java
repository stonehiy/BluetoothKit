package com.inuker.bluetooth.library;

import android.content.Context;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.os.RemoteException;

import com.inuker.bluetooth.library.connect.BleConnectOptions;
import com.inuker.bluetooth.library.search.ISearchResponse;
import com.inuker.bluetooth.library.search.SearchRequest;

/**
 * Created by dingjikerbo on 17/5/6.
 */

public class BluetoothClientWrapper implements IBluetoothService {

    private IBluetoothService mService;

    BluetoothClientWrapper(Context context) {
        this.mService = BluetoothClientImpl.getInstance(context);
    }

    @Override
    public void connect(String mac, BleConnectOptions options, IResponse response) {
        try {
            mService.connect(mac, options, response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void disconnect(String mac) {
        try {
            mService.disconnect(mac);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void read(String mac, ParcelUuid service, ParcelUuid character, IResponse response) {
        try {
            mService.read(mac, service, character, response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(String mac, ParcelUuid service, ParcelUuid character, byte[] value, IResponse response) {
        try {
            mService.write(mac, service, character, value, response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readDescriptor(String mac, ParcelUuid service, ParcelUuid character, ParcelUuid descriptor, IResponse response) {
        try {
            mService.readDescriptor(mac, service, character, descriptor, response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void writeDescriptor(String mac, ParcelUuid service, ParcelUuid character, ParcelUuid descriptor, byte[] value, IResponse response) {
        try {
            mService.writeDescriptor(mac, service, character, descriptor, value, response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void notify(String mac, ParcelUuid service, ParcelUuid character, IResponse response) {
        try {
            mService.notify(mac, service, character, response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unnotify(String mac, ParcelUuid service, ParcelUuid character, IResponse response) {
        try {
            mService.unnotify(mac, service, character, response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void indicate(String mac, ParcelUuid service, ParcelUuid character, IResponse response) {
        try {
            mService.indicate(mac, service, character, response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void unindicate(String mac, ParcelUuid service, ParcelUuid character, IResponse response) {
        try {
            mService.unindicate(mac, service, character, response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void readRssi(String mac, IResponse response) {
        try {
            mService.readRssi(mac, response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void search(SearchRequest request, ISearchResponse response) {
        try {
            mService.search(request, response);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stopSearch() {
        try {
            mService.stopSearch();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder asBinder() {
        throw new UnsupportedOperationException();
    }
}
