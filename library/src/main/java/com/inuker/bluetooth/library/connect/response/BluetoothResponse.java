package com.inuker.bluetooth.library.connect.response;

import android.os.Bundle;

import com.inuker.bluetooth.library.BluetoothContext;
import com.inuker.bluetooth.library.IResponse;
import com.inuker.bluetooth.library.model.BleGattProfile;
import com.inuker.bluetooth.library.utils.BluetoothLog;

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
                if (data != null) {
                    data.setClassLoader(BleGattProfile.class.getClassLoader());
                }

                try {
                    onAsyncResponse(code, data);
                } catch (Throwable e) {
                    BluetoothLog.e(e);
                }
            }
        });
    }
}
