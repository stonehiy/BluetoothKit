package com.inuker.bluetooth.library.connect.response;

import android.os.Bundle;
import android.os.RemoteException;

import com.inuker.bluetooth.library.BluetoothContext;
import com.inuker.bluetooth.library.IResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

/**
 * Created by dingjikerbo on 2016/8/28.
 */
public class BleResponse extends IResponse.Stub {

    private BleTResponse response;

    public BleResponse(BleTResponse response) {
        this.response = response;
    }

    @Override
    public void onResponse(final int code, final Bundle data) throws RemoteException {
        BluetoothContext.post(new Runnable() {
            @Override
            public void run() {
                if (response != null) {
                    try {
                        response.onResponse(code, data);
                    } catch (Throwable e) {
                        BluetoothLog.e(e);
                    }
                }
            }
        });
    }
}
