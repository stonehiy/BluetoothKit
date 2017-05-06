package com.inuker.bluetooth.library.connect.response;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import com.inuker.bluetooth.library.BluetoothContext;
import com.inuker.bluetooth.library.IResponse;

/**
 * Created by dingjikerbo on 2015/12/31.
 */
public abstract class BluetoothResponse extends IResponse.Stub {

    protected abstract void onAsyncResponse(int code, Bundle data);

    @Override
    public void onResponse(final int code, final Bundle data) {
        BluetoothContext.post(new Runnable() {
            @Override
            public void run() {
                onAsyncResponse(code, data);
            }
        });
    }
}
