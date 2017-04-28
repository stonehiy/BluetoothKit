package com.inuker.bluetooth.library.scan;

public interface BluetoothSearchResponse {
    void onSearchStarted();

    void onDeviceFounded(ScanResult device);

    void onSearchStopped();

    void onSearchCanceled();
}
