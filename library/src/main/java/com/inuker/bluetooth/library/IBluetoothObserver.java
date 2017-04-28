package com.inuker.bluetooth.library;

import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener;
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener;
import com.inuker.bluetooth.library.receiver.listener.BluetoothBondListener;

/**
 * Created by dingjikerbo on 17/4/25.
 */

public interface IBluetoothObserver {

	void registerConnectStatusListener(String mac, BleConnectStatusListener listener);

	void unregisterConnectStatusListener(String mac, BleConnectStatusListener listener);

	void registerBluetoothStateListener(BluetoothStateListener listener);

	void unregisterBluetoothStateListener(BluetoothStateListener listener);

	void registerBluetoothBondListener(BluetoothBondListener listener);

	void unregisterBluetoothBondListener(BluetoothBondListener listener);
}
