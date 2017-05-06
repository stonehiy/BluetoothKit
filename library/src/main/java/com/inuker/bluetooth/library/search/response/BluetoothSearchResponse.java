package com.inuker.bluetooth.library.search.response;

import android.os.RemoteException;

import com.inuker.bluetooth.library.search.ISearchResponse;
import com.inuker.bluetooth.library.search.SearchResult;

public class BluetoothSearchResponse extends ISearchResponse.Stub {

    private ISearchResponse response;

    public BluetoothSearchResponse() {

    }

    public BluetoothSearchResponse(ISearchResponse response) {
        this.response = response;
    }

    @Override
    public void onSearchStarted() {
        if (response != null) {
            try {
                response.onSearchStarted();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDeviceFounded(SearchResult device) {
        if (response != null) {
            try {
                response.onDeviceFounded(device);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSearchStopped() {
        if (response != null) {
            try {
                response.onSearchStopped();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onSearchCanceled() {
        if (response != null) {
            try {
                response.onSearchCanceled();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
