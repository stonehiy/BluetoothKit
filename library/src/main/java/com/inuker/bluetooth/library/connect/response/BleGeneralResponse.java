package com.inuker.bluetooth.library.connect.response;

import android.os.Bundle;
import android.os.RemoteException;

import com.inuker.bluetooth.library.IResponse;
import com.inuker.bluetooth.library.utils.BluetoothLog;

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
		if (response != null) {
			try {
				response.onResponse(code, data);
			} catch (Throwable e) {
				BluetoothLog.e(e);
			}
		}
	}
}
