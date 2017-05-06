package com.inuker.bluetooth.library.connect.response;

import android.os.Bundle;

import com.inuker.bluetooth.library.IResponse;

/**
 * Created by dingjikerbo on 2016/10/11.
 */
public class BleGeneralResponse implements BleTResponse<Bundle> {

    private IResponse response;

    public BleGeneralResponse(IResponse response) {
        this.response = response;
    }

    @Override
    public void onResponse(int code, Bundle data) {
        try {
            response.onResponse(code, data);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
