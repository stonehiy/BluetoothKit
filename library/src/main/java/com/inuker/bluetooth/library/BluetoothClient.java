package com.inuker.bluetooth.library;

import android.content.Context;
import android.os.ParcelUuid;

import com.inuker.bluetooth.library.connect.options.BleConnectOptions;
import com.inuker.bluetooth.library.connect.response.BleConnectResponse;
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleReadResponse;
import com.inuker.bluetooth.library.connect.response.BleReadRssiResponse;
import com.inuker.bluetooth.library.connect.response.BleResponse;
import com.inuker.bluetooth.library.connect.response.BleUnnotifyResponse;
import com.inuker.bluetooth.library.connect.response.BleWriteResponse;

import java.util.UUID;

/**
 * Created by dingjikerbo on 2016/9/1.
 */
public class BluetoothClient {

    private IBluetoothClient mClient;

    public BluetoothClient(Context context) {
        if (context == null) {
            throw new NullPointerException("Context null");
        }
        mClient = BluetoothClientImpl.getInstance(context);
    }

    public void connect(String mac, BleConnectOptions options, BleConnectResponse response) {
        mClient.connect(mac, options, new BleResponse(response));
    }

    public void disconnect(String mac) {
        mClient.disconnect(mac);
    }

    public void read(String mac, UUID service, UUID character, BleReadResponse response) {
        mClient.read(mac, new ParcelUuid(service), new ParcelUuid(character), new BleResponse(response));
    }

    public void write(String mac, UUID service, UUID character, byte[] value, BleWriteResponse response) {
        mClient.write(mac, new ParcelUuid(service), new ParcelUuid(character), value, new BleResponse(response));
    }

    public void readDescriptor(String mac, UUID service, UUID character, UUID descriptor, BleReadResponse response) {
        mClient.readDescriptor(mac, new ParcelUuid(service), new ParcelUuid(character), new ParcelUuid(descriptor), new BleResponse(response));
    }

    public void writeDescriptor(String mac, UUID service, UUID character, UUID descriptor, byte[] value, BleWriteResponse response) {
        mClient.writeDescriptor(mac, new ParcelUuid(service), new ParcelUuid(character), new ParcelUuid(descriptor), value, new BleResponse(response));
    }

    public void notify(String mac, UUID service, UUID character, BleNotifyResponse response) {
        mClient.notify(mac, new ParcelUuid(service), new ParcelUuid(character), new BleResponse(response));
    }

    public void unnotify(String mac, UUID service, UUID character, BleUnnotifyResponse response) {
        mClient.unnotify(mac, new ParcelUuid(service), new ParcelUuid(character), new BleResponse(response));
    }

    public void indicate(String mac, UUID service, UUID character, BleNotifyResponse response) {
        mClient.indicate(mac, new ParcelUuid(service), new ParcelUuid(character), new BleResponse(response));
    }

    public void unindicate(String mac, UUID service, UUID character, BleUnnotifyResponse response) {
        mClient.unindicate(mac, new ParcelUuid(service), new ParcelUuid(character), new BleResponse(response));
    }

    public void readRssi(String mac, BleReadRssiResponse response) {
        mClient.readRssi(mac, new BleResponse(response));
    }
}
