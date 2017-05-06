// IBluetoothManager.aidl
package com.inuker.bluetooth.library;

// Declare any non-default types here with import statements

import com.inuker.bluetooth.library.IResponse;
import com.inuker.bluetooth.library.connect.BleConnectOptions;

interface IBluetoothService {
    void callBluetoothApi(int code, inout Bundle args, IResponse response);

    void connect(String mac, in BleConnectOptions options, IResponse response);
}
