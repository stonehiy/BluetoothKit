package com.inuker.bluetooth.library.scan;

/**
 * Created by dingjikerbo on 2016/9/1.
 */
public interface ScanResponse {

    void onSearchStarted();

    void onDeviceFounded(ScanResult device);

    void onSearchStopped();

    void onSearchCanceled();
}
