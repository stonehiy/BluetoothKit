package com.inuker.bluetooth.library.connect.response;

/**
 * 经典蓝牙
 *
 * @param <T>
 */
public interface ClassicResponse<T> {
    void onResponse(int code, T data);
}
