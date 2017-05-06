package com.inuker.bluetooth.library;

import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;

import com.inuker.bluetooth.library.connect.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleIndicateResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleReadRssiResponse;
import com.inuker.bluetooth.library.connect.response.BleUnindicateResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;
import com.inuker.bluetooth.library.connect.response.BluetoothResponse;
import com.inuker.bluetooth.library.compat.BleGattProfile;

import java.util.UUID;

import static com.inuker.bluetooth.library.Constants.EXTRA_BYTE_VALUE;
import static com.inuker.bluetooth.library.Constants.EXTRA_GATT_PROFILE;
import static com.inuker.bluetooth.library.Constants.EXTRA_RSSI;

/**
 * Created by dingjikerbo on 2016/9/1.
 */
public class BluetoothClient extends BluetoothClientWrapper {

    public BluetoothClient(Context context) {
        super(context);
    }

    public void connect(String mac, BleConnectResponse response) {
        connect(mac, null, response);
    }

    public void connect(String mac, BleConnectOptions options, final BleConnectResponse response) {
        connect(mac, options, new BluetoothResponse() {

            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                response.onResponse(code, (BleGattProfile) data.getParcelable(EXTRA_GATT_PROFILE));
            }
        });
    }

    public void read(String mac, UUID service, UUID character, final BleReadResponse response) {
        read(mac, new ParcelUuid(service), new ParcelUuid(character), new BluetoothResponse() {

            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                response.onResponse(code, data.getByteArray(EXTRA_BYTE_VALUE));
            }
        });
    }

    public void write(String mac, UUID service, UUID character, byte[] value, final BleWriteResponse response) {
        write(mac, new ParcelUuid(service), new ParcelUuid(character), value, new BluetoothResponse() {

            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                response.onResponse(code);
            }
        });
    }

    public void readDescriptor(String mac, UUID service, UUID character, UUID descriptor, final BleReadResponse response) {
        readDescriptor(mac, new ParcelUuid(service), new ParcelUuid(character), new ParcelUuid(descriptor), new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                response.onResponse(code, data.getByteArray(EXTRA_BYTE_VALUE));
            }
        });
    }

    public void writeDescriptor(String mac, UUID service, UUID character, UUID descriptor, byte[] value, final BleWriteResponse response) {
        writeDescriptor(mac, new ParcelUuid(service), new ParcelUuid(character), new ParcelUuid(descriptor), value, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                response.onResponse(code);
            }
        });
    }

    public void notify(String mac, UUID service, UUID character, final BleNotifyResponse response) {
        notify(mac, new ParcelUuid(service), new ParcelUuid(character), new BluetoothResponse() {

            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                response.onResponse(code);
            }
        });
    }

    public void unnotify(String mac, UUID service, UUID character, final BleUnnotifyResponse response) {
        unnotify(mac, new ParcelUuid(service), new ParcelUuid(character), new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                if (response != null) {
                    response.onResponse(code);
                }
            }
        });
    }

    public void indicate(String mac, UUID service, UUID character, final BleIndicateResponse response) {
        indicate(mac, new ParcelUuid(service), new ParcelUuid(character), new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                response.onResponse(code);
            }
        });
    }

    public void unindicate(String mac, UUID service, UUID character, final BleUnindicateResponse response) {
        unnotify(mac, service, character, response);
    }

    public void readRssi(String mac, final BleReadRssiResponse response) {
        readRssi(mac, new BluetoothResponse() {
            @Override
            protected void onAsyncResponse(int code, Bundle data) {
                response.onResponse(code, data.getInt(EXTRA_RSSI, 0));
            }
        });
    }
}
