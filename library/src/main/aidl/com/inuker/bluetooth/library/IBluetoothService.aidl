// IBluetoothManager.aidl
package com.inuker.bluetooth.library;

// Declare any non-default types here with import statements

import com.inuker.bluetooth.library.IResponse;
import com.inuker.bluetooth.library.connect.BleConnectOptions;
import com.inuker.bluetooth.library.search.SearchRequest;
import com.inuker.bluetooth.library.search.ISearchResponse;

interface IBluetoothService {

    void connect(String mac, in BleConnectOptions options, IResponse response);

    void disconnect(String mac);

    void read(String mac, in ParcelUuid service, in ParcelUuid character, IResponse response);

    void write(String mac, in ParcelUuid service, in ParcelUuid character, in byte[] value, IResponse response);

    void readDescriptor(String mac, in ParcelUuid service, in ParcelUuid character, in ParcelUuid descriptor, IResponse response);

    void writeDescriptor(String mac, in ParcelUuid service, in ParcelUuid character, in ParcelUuid descriptor, in byte[] value, IResponse response);

    void notify(String mac, in ParcelUuid service, in ParcelUuid character, IResponse response);

    void unnotify(String mac, in ParcelUuid service, in ParcelUuid character, IResponse response);

    void indicate(String mac, in ParcelUuid service, in ParcelUuid character, IResponse response);

    void unindicate(String mac, in ParcelUuid service, in ParcelUuid character, IResponse response);

    void readRssi(String mac, IResponse response);

    void search(in SearchRequest request, ISearchResponse response);

    void stopSearch();
}
