package com.inuker.bluetooth.library;


import android.os.ParcelUuid;

import com.inuker.bluetooth.library.connect.options.BleConnectOptions;

/**
 * Created by dingjikerbo on 2016/8/25.
 */
public interface IBluetoothClient {

	void connect(String mac, BleConnectOptions options, IResponse response);

	void disconnect(String mac);

	void read(String mac, ParcelUuid service, ParcelUuid character, IResponse response);

	void write(String mac, ParcelUuid service, ParcelUuid character, byte[] value, IResponse response);

	void readDescriptor(String mac, ParcelUuid service, ParcelUuid character, ParcelUuid descriptor, IResponse response);

	void writeDescriptor(String mac, ParcelUuid service, ParcelUuid character, ParcelUuid descriptor, byte[] value, IResponse response);

	void notify(String mac, ParcelUuid service, ParcelUuid character, IResponse response);

	void unnotify(String mac, ParcelUuid service, ParcelUuid character, IResponse response);

	void indicate(String mac, ParcelUuid service, ParcelUuid character, IResponse response);

	void unindicate(String mac, ParcelUuid service, ParcelUuid character, IResponse response);

	void readRssi(String mac, IResponse response);
}
