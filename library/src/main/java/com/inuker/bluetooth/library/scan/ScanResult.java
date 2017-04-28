package com.inuker.bluetooth.library.scan;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by dingjikerbo on 2016/8/28.
 */
public class ScanResult implements Parcelable {

    public BluetoothDevice device;

    public int rssi;

    public byte[] scanRecord;

    public ScanResult(BluetoothDevice device) {
        this(device, 0, null);
    }

    public ScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {
        this.device = device;
        this.rssi = rssi;
        this.scanRecord = scanRecord;
    }

    public String getName() {
        String name = device.getName();
        return TextUtils.isEmpty(name) ? "NULL" : name;
    }

    public String getAddress() {
        return device != null ? device.getAddress() : "";
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder();
        sb.append(", mac = " + device.getAddress());
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.device, 0);
        dest.writeInt(this.rssi);
        dest.writeByteArray(this.scanRecord);
    }

    public ScanResult(Parcel in) {
        this.device = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.rssi = in.readInt();
        this.scanRecord = in.createByteArray();
    }

    public static final Creator<ScanResult> CREATOR = new Creator<ScanResult>() {
        public ScanResult createFromParcel(Parcel source) {
            return new ScanResult(source);
        }

        public ScanResult[] newArray(int size) {
            return new ScanResult[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScanResult that = (ScanResult) o;

        return device.equals(that.device);

    }

    @Override
    public int hashCode() {
        return device.hashCode();
    }
}
