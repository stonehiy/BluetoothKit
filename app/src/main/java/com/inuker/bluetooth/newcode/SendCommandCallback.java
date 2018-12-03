package com.inuker.bluetooth.newcode;

public interface SendCommandCallback {

    void onSendData(boolean isSuccess, byte[] data);

}
